package com.quickblox.simplesample.messages.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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
import com.quickblox.simplesample.messages.helper.PlayServicesHelper;

import java.util.List;

public class MessagesActivity extends CoreBaseActivity {

    private static final String TAG = MessagesActivity.class.getSimpleName();

    private EditText messageOutEditText;
    private EditText messageInEditText;
    private ProgressBar progressBar;

    private PlayServicesHelper playServicesHelper;

    public static void start(Context context) {
        Intent intent = new Intent(context, MessagesActivity.class);
        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);

        playServicesHelper = new PlayServicesHelper(this);

        initUI();
        addMessageToList();
    }


    @Override
    protected void onResume() {
        super.onResume();
        playServicesHelper.checkPlayServices();

        // Register to receive push notifications events
        LocalBroadcastManager.getInstance(this).registerReceiver(pushBroadcastReceiver,
                new IntentFilter(Consts.NEW_PUSH_EVENT));
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(pushBroadcastReceiver);
    }

    private void initUI() {
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        messageOutEditText = (EditText) findViewById(R.id.message_out_edittext);
        messageInEditText = (EditText) findViewById(R.id.messages_in_edittext);

        Button sendMessageButton = (Button) findViewById(R.id.send_message_button);
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

    public void retrieveMessage(String message) {
        String text = message + "\n\n" + messageInEditText.getText().toString();
        messageInEditText.setText(text);
        progressBar.setVisibility(View.INVISIBLE);
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

    // Our handler for receiving Intents
    private BroadcastReceiver pushBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            String message = intent.getStringExtra(Consts.EXTRA_MESSAGE);
            Log.i(TAG, "Receiving event " + Consts.NEW_PUSH_EVENT + " with data: " + message);
            retrieveMessage(message);
        }
    };
}