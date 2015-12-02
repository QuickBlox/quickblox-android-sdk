package com.quickblox.sample.chat.ui.activity;

import android.app.AlertDialog;
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
import com.quickblox.sample.chat.utils.chat.ChatHelper;

import java.util.ArrayList;
import java.util.List;

public class DialogsActivity extends BaseActivity {
    private static final String TAG = DialogsActivity.class.getSimpleName();

    private ListView dialogsListView;
    private ProgressBar progressBar;

    private GooglePlayServicesHelper googlePlayServicesHelper;

    public static void start(Context context) {
        Intent intent = new Intent(context, DialogsActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialogs_activity);

        googlePlayServicesHelper = new GooglePlayServicesHelper();
        googlePlayServicesHelper.registerForGcmIfPossible(this);

        dialogsListView = (ListView) findViewById(R.id.list_dialogs_chats);
        progressBar = (ProgressBar) findViewById(R.id.progress_chat_messages);

        LocalBroadcastManager.getInstance(this)
                .registerReceiver(gcmBroadcastReceiver, new IntentFilter(Consts.ACTION_NEW_GCM_EVENT));

        if (isSessionActive()) {
            getDialogs();
        }
    }

    private void getDialogs() {
        progressBar.setVisibility(View.VISIBLE);

        ChatHelper.getInstance().getDialogs(new QBEntityCallbackImpl<ArrayList<QBDialog>>() {
            @Override
            public void onSuccess(ArrayList<QBDialog> dialogs, Bundle bundle) {
                progressBar.setVisibility(View.GONE);
                buildListView(dialogs);
            }

            @Override
            public void onError(List<String> errors) {
                progressBar.setVisibility(View.GONE);

                AlertDialog.Builder dialog = new AlertDialog.Builder(DialogsActivity.this);
                dialog.setMessage("get dialogs errors: " + errors).create().show();
            }
        });
    }

    public void buildListView(List<QBDialog> dialogs) {
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
    protected void onResume() {
        super.onResume();
        googlePlayServicesHelper.checkGooglePlayServices(this);
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

    private BroadcastReceiver gcmBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            String message = intent.getStringExtra(Consts.EXTRA_GCM_MESSAGE);

            Log.i(TAG, "Received event " + intent.getAction() + " with data: " + message);
        }
    };

    //
    // ApplicationSessionStateCallback
    //

    @Override
    public void onSessionRecreationFinish(final boolean success) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (success) {
                    getDialogs();
                }
            }
        });
    }
}
