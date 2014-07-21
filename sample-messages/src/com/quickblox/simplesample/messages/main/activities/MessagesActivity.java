package com.quickblox.simplesample.messages.main.activities;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.quickblox.core.QBCallback;
import com.quickblox.core.result.Result;
import com.quickblox.internal.core.helper.StringifyArrayList;
import com.quickblox.module.messages.QBMessages;
import com.quickblox.module.messages.model.QBEnvironment;
import com.quickblox.module.messages.model.QBEvent;
import com.quickblox.module.messages.model.QBNotificationType;
import com.quickblox.simplesample.messages.R;
import com.quickblox.simplesample.messages.main.definitions.Consts;
import com.quickblox.simplesample.messages.main.playservices.PlayServicesHelper;

public class MessagesActivity extends Activity {

    private EditText messageOutEditText;
    private EditText messageInEditText;
    private ProgressBar progressBar;
    private Button sendMessageButton;


    private static MessagesActivity instance;
    private PlayServicesHelper playServicesHelper;

    public static MessagesActivity getInstance() {
        return instance;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.messages_layout);

        instance = this;
        playServicesHelper = new PlayServicesHelper(this);

        initUI();
        addMessageToList();
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

        QBMessages.createEvent(qbEvent, new QBCallback() {
            @Override
            public void onComplete(Result result) {
                progressBar.setVisibility(View.INVISIBLE);

                // hide keyboard
                InputMethodManager imm = (InputMethodManager) getSystemService(
                        Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(messageOutEditText.getWindowToken(), 0);
            }

            @Override
            public void onComplete(Result result, Object o) {
            }
        });
        progressBar.setVisibility(View.VISIBLE);
    }
}