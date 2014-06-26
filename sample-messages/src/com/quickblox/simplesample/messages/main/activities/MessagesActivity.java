package com.quickblox.simplesample.messages.main.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.quickblox.core.QBCallback;
import com.quickblox.core.result.Result;
import com.quickblox.internal.core.helper.StringifyArrayList;
import com.quickblox.internal.core.request.QBPagedRequestBuilder;
import com.quickblox.module.messages.QBMessages;
import com.quickblox.module.messages.model.QBEnvironment;
import com.quickblox.module.messages.model.QBEvent;
import com.quickblox.module.messages.model.QBNotificationType;
import com.quickblox.module.users.QBUsers;
import com.quickblox.module.users.model.QBUser;
import com.quickblox.module.users.result.QBUserPagedResult;
import com.quickblox.simplesample.messages.R;
import com.quickblox.simplesample.messages.main.definitions.Consts;
import com.quickblox.simplesample.messages.main.playservices.PlayServicesHelper;
import com.quickblox.simplesample.messages.main.utils.DialogUtils;

import java.util.ArrayList;
import java.util.List;

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
            }

            @Override
            public void onComplete(Result result, Object o) {
            }
        });
        progressBar.setVisibility(View.VISIBLE);
    }
}