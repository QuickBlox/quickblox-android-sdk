package com.quickblox.sample.chat.ui.activities;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.chat.model.QBDialog;
import com.quickblox.sample.chat.core.ChatService;
import com.quickblox.sample.chat.pushnotifications.Consts;
import com.quickblox.sample.chat.pushnotifications.PlayServicesHelper;
import com.quickblox.sample.chat.R;
import com.quickblox.sample.chat.ui.adapters.DialogsAdapter;

import java.util.ArrayList;
import java.util.List;

public class DialogsActivity extends BaseActivity {

    private static final String TAG = DialogsActivity.class.getSimpleName();

    private ListView dialogsListView;
    private ProgressBar progressBar;

    private PlayServicesHelper playServicesHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialogs_activity);

        playServicesHelper = new PlayServicesHelper(this);

        dialogsListView = (ListView) findViewById(R.id.roomsList);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);


        // Register to receive push notifications events
        //
        LocalBroadcastManager.getInstance(this).registerReceiver(mPushReceiver,
                new IntentFilter(Consts.NEW_PUSH_EVENT));

        // Get dialogs if the session is active
        //
        if(isSessionActive()){
            getDialogs();
        }
    }

    private void getDialogs(){
        progressBar.setVisibility(View.VISIBLE);

        // Get dialogs
        //
        ChatService.getInstance().getDialogs(new QBEntityCallbackImpl() {
            @Override
            public void onSuccess(Object object, Bundle bundle) {
                progressBar.setVisibility(View.GONE);

                final ArrayList<QBDialog> dialogs = (ArrayList<QBDialog>)object;

                // build list view
                //
                buildListView(dialogs);
            }

            @Override
            public void onError(List errors) {
                progressBar.setVisibility(View.GONE);

                AlertDialog.Builder dialog = new AlertDialog.Builder(DialogsActivity.this);
                dialog.setMessage("get dialogs errors: " + errors).create().show();
            }
        });
    }


    void buildListView(List<QBDialog> dialogs){
        final DialogsAdapter adapter = new DialogsAdapter(dialogs, DialogsActivity.this);
        dialogsListView.setAdapter(adapter);

        // choose dialog
        //
        dialogsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                QBDialog selectedDialog = (QBDialog) adapter.getItem(position);

                Bundle bundle = new Bundle();
                bundle.putSerializable(ChatActivity.EXTRA_DIALOG, selectedDialog);

                // Open chat activity
                //
                ChatActivity.start(DialogsActivity.this, bundle);

                finish();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        playServicesHelper.checkPlayServices();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.rooms, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_add) {

            // go to New Dialog activity
            //
            Intent intent = new Intent(DialogsActivity.this, NewDialogActivity.class);
            startActivity(intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Our handler for received Intents.
    //
    private BroadcastReceiver mPushReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            // Get extra data included in the Intent
            String message = intent.getStringExtra(Consts.EXTRA_MESSAGE);

            Log.i(TAG, "Receiving event " + Consts.NEW_PUSH_EVENT + " with data: " + message);
        }
    };


    //
    // ApplicationSessionStateCallback
    //

    @Override
    public void onStartSessionRecreation() {

    }

    @Override
    public void onFinishSessionRecreation(final boolean success) {
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
