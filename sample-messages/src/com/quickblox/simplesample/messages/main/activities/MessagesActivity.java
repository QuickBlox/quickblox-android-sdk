package com.quickblox.simplesample.messages.main.activities;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.core.helper.StringifyArrayList;
import com.quickblox.messages.QBMessages;
import com.quickblox.messages.model.QBEnvironment;
import com.quickblox.messages.model.QBEvent;
import com.quickblox.messages.model.QBNotificationType;
import com.quickblox.simplesample.messages.R;
import com.quickblox.simplesample.messages.main.Consts;
import com.quickblox.simplesample.messages.main.helper.PlayServicesHelper;
import com.quickblox.simplesample.messages.main.utils.DialogUtils;

import java.util.List;

public class MessagesActivity extends Activity {

    private static final String TAG = MessagesActivity.class.getSimpleName();

    private EditText messageOutEditText;
    private EditText messageInEditText;
    private ProgressBar progressBar;
    private Button sendMessageButton;

    private PlayServicesHelper playServicesHelper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.messages_layout);

        playServicesHelper = new PlayServicesHelper(this);

        initUI();
        addMessageToList();

        // Register to receive push notifications events
        //
        LocalBroadcastManager.getInstance(this).registerReceiver(mPushReceiver,
                new IntentFilter(Consts.NEW_PUSH_EVENT));
    }

    @Override
    protected void onDestroy() {

        // Unregister since the activity is about to be closed.
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mPushReceiver);

        super.onDestroy();
    }

    private void initUI() {
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        messageOutEditText = (EditText) findViewById(R.id.message_out_edittext);
        messageInEditText = (EditText) findViewById(R.id.messages_in_edittext);
        sendMessageButton = (Button) findViewById(R.id.send_message_button);

        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessageOnClick(view);
            }
        });
    }

    private void addMessageToList() {
        String message = getIntent().getStringExtra(Consts.EXTRA_MESSAGE);
        if (message != null) {
            retrieveMessage(message);
        }
    }

    public void retrieveMessage(final String message) {
        String text = message + "\n\n" + messageInEditText.getText().toString();
        messageInEditText.setText(text);
        progressBar.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        playServicesHelper.checkPlayServices();
    }

    public void sendMessageOnClick(View view) {
        // Send Push: create QuickBlox Push Notification Event
        QBEvent qbEvent = new QBEvent();
        qbEvent.setNotificationType(QBNotificationType.PUSH);
        qbEvent.setEnvironment(QBEnvironment.DEVELOPMENT);

        // generic push - will be delivered to all platforms (Android, iOS, WP, Blackberry..)
        qbEvent.setMessage(messageOutEditText.getText().toString());

        StringifyArrayList<Integer> userIds = new StringifyArrayList<Integer>();
        userIds.add(1243440);
        qbEvent.setUserIds(userIds);


        QBMessages.createEvent(qbEvent, new QBEntityCallbackImpl<QBEvent>() {
            @Override
            public void onSuccess(QBEvent qbEvent, Bundle bundle) {
                progressBar.setVisibility(View.INVISIBLE);

                // hide keyboard
                InputMethodManager imm = (InputMethodManager) getSystemService(
                        Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(messageOutEditText.getWindowToken(), 0);
            }

            @Override
            public void onError(List<String> strings) {
                // errors
                DialogUtils.showLong(MessagesActivity.this, strings.toString());

                progressBar.setVisibility(View.INVISIBLE);

                // hide keyboard
                InputMethodManager imm = (InputMethodManager) getSystemService(
                        Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(messageOutEditText.getWindowToken(), 0);
            }
        });

        progressBar.setVisibility(View.VISIBLE);
    }

    // Our handler for received Intents.
    //
    private BroadcastReceiver mPushReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            // Get extra data included in the Intent
            String message = intent.getStringExtra(Consts.EXTRA_MESSAGE);

            Log.i(TAG, "Receiving event " + Consts.NEW_PUSH_EVENT + " with data: " + message);

            retrieveMessage(message);
        }
    };
}