package com.quickblox.simplesample.messages.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.helper.StringifyArrayList;
import com.quickblox.messages.QBPushNotifications;
import com.quickblox.messages.model.QBEnvironment;
import com.quickblox.messages.model.QBEvent;
import com.quickblox.messages.model.QBNotificationType;
import com.quickblox.sample.core.gcm.GooglePlayServicesHelper;
import com.quickblox.sample.core.ui.activity.CoreBaseActivity;
import com.quickblox.sample.core.utils.KeyboardUtils;
import com.quickblox.sample.core.utils.Toaster;
import com.quickblox.sample.core.utils.constant.GcmConsts;
import com.quickblox.simplesample.messages.App;
import com.quickblox.simplesample.messages.Consts;
import com.quickblox.simplesample.messages.R;

import java.util.ArrayList;
import java.util.List;

public class MessagesActivity extends CoreBaseActivity {

    private final String TAG = getClass().getSimpleName();

    private EditText outgoingMessageEditText;
    private ProgressBar progressBar;
    private ArrayAdapter<String> adapter;

    private List<String> receivedPushes;
    private GooglePlayServicesHelper googlePlayServicesHelper;

    private BroadcastReceiver pushBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra(GcmConsts.EXTRA_GCM_MESSAGE);
            Log.i(TAG, "Receiving event " + GcmConsts.ACTION_NEW_GCM_EVENT + " with data: " + message);
            retrieveMessage(message);
        }
    };

    public static void start(Context context, String message) {
        Intent intent = new Intent(context, MessagesActivity.class);
        intent.putExtra(GcmConsts.EXTRA_GCM_MESSAGE, message);
        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);

        receivedPushes = new ArrayList<>();

        googlePlayServicesHelper = new GooglePlayServicesHelper();
        if (googlePlayServicesHelper.checkPlayServicesAvailable(this)) {
            googlePlayServicesHelper.registerForGcm(Consts.GCM_SENDER_ID);
        }
        initUI();

        String message = getIntent().getStringExtra(GcmConsts.EXTRA_GCM_MESSAGE);

        if (message != null) {
            retrieveMessage(message);
        }
        initUI();
    }

    @Override
    protected void onResume() {
        super.onResume();
        App.activityResumed();
        googlePlayServicesHelper.checkPlayServicesAvailable(this);

        LocalBroadcastManager.getInstance(this).registerReceiver(pushBroadcastReceiver,
                new IntentFilter(GcmConsts.ACTION_NEW_GCM_EVENT));
    }

    @Override
    protected void onPause() {
        super.onPause();
        App.activityPaused();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(pushBroadcastReceiver);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.getItem(0).setEnabled(true);
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.activity_messages, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_send_message:
                item.setEnabled(false);
                sendPushMessage();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void initUI() {
        progressBar = _findViewById(R.id.progress_bar);
        outgoingMessageEditText = _findViewById(R.id.edit_message_out);

        ListView incomingMessagesListView = _findViewById(R.id.list_messages);
        adapter = new ArrayAdapter<>(this, R.layout.list_item_message, R.id.item_message, receivedPushes);
        incomingMessagesListView.setAdapter(adapter);
        incomingMessagesListView.setEmptyView(_findViewById(R.id.text_empty_messages));
    }

    private void retrieveMessage(String message) {
        receivedPushes.add(0, message);
        adapter.notifyDataSetChanged();

        progressBar.setVisibility(View.INVISIBLE);
    }

    private void sendPushMessage() {
        String outMessage = outgoingMessageEditText.getText().toString().trim();
        if (!isValidData(outMessage)) {
            Toaster.longToast(R.string.error_fields_is_empty);
            invalidateOptionsMenu();
            return;
        }

        // Send Push: create QuickBlox Push Notification Event
        QBEvent qbEvent = new QBEvent();
        qbEvent.setNotificationType(QBNotificationType.PUSH);
        qbEvent.setEnvironment(QBEnvironment.DEVELOPMENT);
        // Generic push - will be delivered to all platforms (Android, iOS, WP, Blackberry..)
        qbEvent.setMessage(outMessage);

        StringifyArrayList<Integer> userIds = new StringifyArrayList<>();
        userIds.add(App.getInstance().getCurrentUserId());
        qbEvent.setUserIds(userIds);

        QBPushNotifications.createEvent(qbEvent, new QBEntityCallback<QBEvent>() {
            @Override
            public void onSuccess(QBEvent qbEvent, Bundle bundle) {
                progressBar.setVisibility(View.INVISIBLE);
                KeyboardUtils.hideKeyboard(outgoingMessageEditText);
                outgoingMessageEditText.setText(null);
                invalidateOptionsMenu();
            }

            @Override
            public void onError(QBResponseException e) {
                View rootView = findViewById(R.id.activity_messages);
                showSnackbarError(rootView, R.string.connection_error, e, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        sendPushMessage();
                    }
                });
                progressBar.setVisibility(View.INVISIBLE);
                KeyboardUtils.hideKeyboard(outgoingMessageEditText);
                invalidateOptionsMenu();
            }
        });

        progressBar.setVisibility(View.VISIBLE);
    }

    private boolean isValidData(String message) {
        return !TextUtils.isEmpty(message);
    }
}