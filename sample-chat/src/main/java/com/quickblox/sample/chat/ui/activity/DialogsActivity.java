package com.quickblox.sample.chat.ui.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.quickblox.chat.model.QBDialog;
import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.sample.chat.R;
import com.quickblox.sample.chat.gcm.GooglePlayServicesHelper;
import com.quickblox.sample.chat.ui.adapter.DialogsAdapter;
import com.quickblox.sample.chat.utils.Consts;
import com.quickblox.sample.chat.utils.ErrorUtils;
import com.quickblox.sample.chat.utils.chat.ChatHelper;

import java.util.ArrayList;
import java.util.List;

public class DialogsActivity extends BaseActivity {
    private static final String TAG = DialogsActivity.class.getSimpleName();

    private ListView dialogsListView;
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
        googlePlayServicesHelper.registerForGcmIfPossible(this);

        pushBroadcastReceiver = new PushBroadcastReceiver();

        dialogsListView = (ListView) findViewById(R.id.list_dialogs_chats);
        progressBar = (ProgressBar) findViewById(R.id.progress_chat);

        if (isSessionActive()) {
            loadDialogsFromQb();
        }
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
        int id = item.getItemId();
        switch (id) {
        case R.id.menu_action_add:
            NewDialogActivity.start(this);
            return true;

        default:
            return super.onOptionsItemSelected(item);
        }
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
                ErrorUtils.showErrorDialog(DialogsActivity.this, getString(R.string.dialogs_get_error), errors);
            }
        });
    }

    private void fillListView(List<QBDialog> dialogs) {
        final DialogsAdapter adapter = new DialogsAdapter(this, dialogs);
        dialogsListView.setAdapter(adapter);
        dialogsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                QBDialog selectedDialog = (QBDialog) adapter.getItem(position);
                ChatActivity.start(DialogsActivity.this, selectedDialog);
            }
        });
    }

    @Override
    public void onSessionCreated(boolean success) {
        if (success) {
            loadDialogsFromQb();
        }
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
