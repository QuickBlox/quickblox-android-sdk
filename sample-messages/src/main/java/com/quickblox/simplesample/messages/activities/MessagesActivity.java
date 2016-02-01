package com.quickblox.simplesample.messages.activities;

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
import android.widget.EditText;
import android.widget.ProgressBar;

import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.core.helper.StringifyArrayList;
import com.quickblox.messages.QBMessages;
import com.quickblox.messages.model.QBEnvironment;
import com.quickblox.messages.model.QBEvent;
import com.quickblox.messages.model.QBNotificationType;
import com.quickblox.sample.core.ui.activity.CoreBaseActivity;
import com.quickblox.sample.core.utils.KeyboardUtils;
import com.quickblox.sample.core.utils.Toaster;
import com.quickblox.simplesample.messages.Consts;
import com.quickblox.simplesample.messages.R;
import com.quickblox.simplesample.messages.gcm.GooglePlayServicesHelper;

import java.util.List;

public class MessagesActivity extends CoreBaseActivity {

    private final String TAG = getClass().getSimpleName();
    public static final String CRLF = "\n\n";

    private EditText messageOutEditText;
    private EditText messageInEditText;
    private ProgressBar progressBar;

    private GooglePlayServicesHelper googlePlayServicesHelper;

    private BroadcastReceiver pushBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String message = intent.getStringExtra(Consts.EXTRA_GCM_MESSAGE);
            Log.i(TAG, "Receiving event " + Consts.ACTION_NEW_GCM_EVENT + " with data: " + message);
            retrieveMessage(message);
        }
    };

    public static void start(Context context) {
        Intent intent = new Intent(context, MessagesActivity.class);
        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);

        googlePlayServicesHelper = new GooglePlayServicesHelper();
        googlePlayServicesHelper.registerForGcmIfPossible(this, Consts.GCM_SENDER_ID);

        initUI();
//        TODO Is it need?
        addMessageToList();
    }

    @Override
    protected void onResume() {
        super.onResume();
        googlePlayServicesHelper.checkPlayServices(this);

        LocalBroadcastManager.getInstance(this).registerReceiver(pushBroadcastReceiver,
                new IntentFilter(Consts.ACTION_NEW_GCM_EVENT));
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(pushBroadcastReceiver);
    }

    private void initUI() {
        progressBar = _findViewById(R.id.progress_bar);
        messageOutEditText = _findViewById(R.id.message_out_edittext);
        messageInEditText = _findViewById(R.id.messages_in_edittext);
    }

    private void addMessageToList() {
        String message = getIntent().getStringExtra(Consts.EXTRA_GCM_MESSAGE);
        if (message != null) {
            Log.d(TAG, "message != null, message = " + message);
            retrieveMessage(message);
        }
    }

    public void retrieveMessage(String message) {
        String text = message + CRLF + messageInEditText.getText().toString();
        messageInEditText.setText(text);
        progressBar.setVisibility(View.INVISIBLE);
    }

    public void sendPushNotification() {
        // Send Push: create QuickBlox Push Notification Event
        QBEvent qbEvent = new QBEvent();
        qbEvent.setNotificationType(QBNotificationType.PUSH);
        qbEvent.setEnvironment(QBEnvironment.DEVELOPMENT);

        // generic push - will be delivered to all platforms (Android, iOS, WP, Blackberry..)
        qbEvent.setMessage(messageOutEditText.getText().toString());

        StringifyArrayList<Integer> userIds = new StringifyArrayList<Integer>();
        userIds.add(Consts.USER_ID);
        qbEvent.setUserIds(userIds);

        QBMessages.createEvent(qbEvent, new QBEntityCallbackImpl<QBEvent>() {
            @Override
            public void onSuccess(QBEvent qbEvent, Bundle bundle) {
                progressBar.setVisibility(View.INVISIBLE);

                KeyboardUtils.hideKeyboard(messageOutEditText);
            }

            @Override
            public void onError(List<String> errors) {
                Toaster.longToast(errors.toString());
                progressBar.setVisibility(View.INVISIBLE);

                KeyboardUtils.hideKeyboard(messageOutEditText);
            }
        });

        progressBar.setVisibility(View.VISIBLE);
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
                sendPushNotification();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}