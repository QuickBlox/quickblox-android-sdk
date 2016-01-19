package com.quickblox.sample.chat.ui.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.quickblox.chat.model.QBDialog;
import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.sample.chat.R;
import com.quickblox.sample.chat.gcm.GooglePlayServicesHelper;
import com.quickblox.sample.chat.ui.adapter.DialogsAdapter;
import com.quickblox.sample.chat.utils.Consts;
import com.quickblox.sample.chat.utils.chat.ChatHelper;
import com.quickblox.sample.core.utils.ErrorUtils;
import com.quickblox.users.model.QBUser;

import java.util.ArrayList;
import java.util.List;

public class DialogsActivity extends BaseActivity {
    private static final String TAG = DialogsActivity.class.getSimpleName();
    private static final int REQUEST_SELECT_PEOPLE = 174;

    private ListView dialogsListView;
    private LinearLayout emptyHintLayout;
    private ProgressBar progressBar;

    private BroadcastReceiver pushBroadcastReceiver;
    private GooglePlayServicesHelper googlePlayServicesHelper;

    public static void start(Context context) {
        Intent intent = new Intent(context, DialogsActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialogs);

        googlePlayServicesHelper = new GooglePlayServicesHelper();
        googlePlayServicesHelper.registerForGcmIfPossible(this, Consts.GCM_SENDER_ID);

        pushBroadcastReceiver = new PushBroadcastReceiver();

        initUi();
    }

    @Override
    protected void onResume() {
        super.onResume();

        googlePlayServicesHelper.checkGooglePlayServices(this);

        LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(this);
        broadcastManager.registerReceiver(pushBroadcastReceiver, new IntentFilter(Consts.ACTION_NEW_GCM_EVENT));
    }

    @Override
    protected void onPause() {
        super.onPause();

        LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(this);
        broadcastManager.unregisterReceiver(pushBroadcastReceiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_dialogs, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.menu_dialogs_action_logout:
            ChatHelper.getInstance().logout();
            LoginActivity.start(this);
            finish();
            return true;

        default:
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onSessionCreated(boolean success) {
        if (success) {
            QBUser currentUser = ChatHelper.getCurrentUser();
            if (currentUser != null) {
                actionBar.setTitle(getString(R.string.dialogs_logged_in_as, currentUser.getFullName()));
            }

            loadDialogsFromQb();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_SELECT_PEOPLE) {
                ArrayList<QBUser> selectedUsers = (ArrayList<QBUser>) data.getSerializableExtra(SelectUsersActivity.EXTRA_QB_USERS);

                ChatHelper.getInstance().createDialogWithSelectedUsers(selectedUsers,
                        new QBEntityCallbackImpl<QBDialog>() {
                            @Override
                            public void onSuccess(QBDialog dialog, Bundle args) {
                                ChatActivity.start(DialogsActivity.this, dialog);
                            }

                            @Override
                            public void onError(List<String> errors) {
                                ErrorUtils.showErrorDialog(DialogsActivity.this, R.string.dialogs_creation_error, errors);
                            }
                        }
                );
            }
        }
    }

    public void onStartNewChatClick(View view) {
        SelectUsersActivity.startForResult(this, REQUEST_SELECT_PEOPLE);
    }

    private void initUi() {
        emptyHintLayout = _findViewById(R.id.layout_chat_empty);
        dialogsListView = _findViewById(R.id.list_dialogs_chats);
        progressBar = _findViewById(R.id.progress_dialogs);

        TextView listHeader = (TextView) LayoutInflater.from(this).inflate(R.layout.include_list_hint_header, dialogsListView, false);
        listHeader.setText(R.string.dialogs_list_hint);
        dialogsListView.addHeaderView(listHeader, null, false);
        dialogsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                QBDialog selectedDialog = (QBDialog) parent.getItemAtPosition(position);
                ChatActivity.start(DialogsActivity.this, selectedDialog);
            }
        });
        dialogsListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                // TODO Temporary, need to use ActionMode for selection
                QBDialog selectedDialog = (QBDialog) parent.getItemAtPosition(position);
                ChatHelper.getInstance().deleteDialog(selectedDialog, new QBEntityCallbackImpl<Void>() {
                    @Override
                    public void onSuccess() {
                        loadDialogsFromQb();
                    }

                    @Override
                    public void onError(List<String> errors) {
                        ErrorUtils.showErrorDialog(DialogsActivity.this, R.string.dialogs_deletion_error, errors);
                    }
                });
                return true;
            }
        });
    }

    private void loadDialogsFromQb() {
        progressBar.setVisibility(View.VISIBLE);

        ChatHelper.getInstance().getDialogs(new QBEntityCallbackImpl<ArrayList<QBDialog>>() {
            @Override
            public void onSuccess(ArrayList<QBDialog> dialogs, Bundle bundle) {
                progressBar.setVisibility(View.GONE);
                fillListView(dialogs);
            }

            @Override
            public void onError(List<String> errors) {
                progressBar.setVisibility(View.GONE);
                ErrorUtils.showErrorDialog(DialogsActivity.this, R.string.dialogs_get_error, errors);
            }
        });
    }

    private void fillListView(List<QBDialog> dialogs) {
        DialogsAdapter adapter = new DialogsAdapter(this, dialogs);
        dialogsListView.setEmptyView(emptyHintLayout);
        dialogsListView.setAdapter(adapter);
    }

    private static class PushBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            String message = intent.getStringExtra(Consts.EXTRA_GCM_MESSAGE);
            Log.i(TAG, "Received broadcast " + intent.getAction() + " with data: " + message);
        }
    }
}
