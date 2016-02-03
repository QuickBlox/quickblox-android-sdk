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
import android.widget.TextView;

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

import java.util.ArrayList;
import java.util.List;

public class MessagesActivity extends CoreBaseActivity {

    private final String TAG = getClass().getSimpleName();

    private EditText messageOutText;
    private TextView messageInText;
    private ListView messageInList;
    private ProgressBar progressBar;
    private List<String> receivedPushes;
    private ArrayAdapter<String> adapter;

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
        receivedPushes = new ArrayList<>();

        googlePlayServicesHelper = new GooglePlayServicesHelper();
        googlePlayServicesHelper.registerForGcmIfPossible(this, Consts.GCM_SENDER_ID);

        initUI();
//        TODO Is it need?
//        addMessageToList();
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
        messageOutText = _findViewById(R.id.message_out_edittext);
        messageInText = _findViewById(R.id.messages_in_text);
        messageInList = _findViewById(R.id.list_messages);
        adapter = new ArrayAdapter<>(this, R.layout.list_item_message, R.id.item_message, receivedPushes);

        messageInList.setAdapter(adapter);
    }

    private void addMessageToList() {
        String message = getIntent().getStringExtra(Consts.EXTRA_GCM_MESSAGE);
        if (message != null) {
            Log.d(TAG, "message != null, message = " + message);
            retrieveMessage(message);
        }
    }

    public void retrieveMessage(String message) {
        receivedPushes.add(message);
        messageInText.setVisibility(View.GONE);
        adapter.notifyDataSetChanged();
        progressBar.setVisibility(View.INVISIBLE);
    }

    public void sendPushNotification() {
        String outMessage = messageOutText.getText().toString();
        if (!isValidData(outMessage)) {
            return;
        }
        // Send Push: create QuickBlox Push Notification Event
        QBEvent qbEvent = new QBEvent();
        qbEvent.setNotificationType(QBNotificationType.PUSH);
        qbEvent.setEnvironment(QBEnvironment.DEVELOPMENT);

        // generic push - will be delivered to all platforms (Android, iOS, WP, Blackberry..)
        qbEvent.setMessage(outMessage);

        StringifyArrayList<Integer> userIds = new StringifyArrayList<Integer>();
        userIds.add(Consts.USER_ID);
        qbEvent.setUserIds(userIds);

        QBMessages.createEvent(qbEvent, new QBEntityCallbackImpl<QBEvent>() {
            @Override
            public void onSuccess(QBEvent qbEvent, Bundle bundle) {
                progressBar.setVisibility(View.INVISIBLE);
                KeyboardUtils.hideKeyboard(messageOutText);
                messageOutText.setText(null);
            }

            @Override
            public void onError(List<String> errors) {
                Toaster.longToast(errors.toString());

                progressBar.setVisibility(View.INVISIBLE);
                KeyboardUtils.hideKeyboard(messageOutText);
            }
        });

        progressBar.setVisibility(View.VISIBLE);
    }

    private boolean isValidData(String outcome) {
        String space = Character.toString((char) 0x20);

        if (outcome.startsWith(space) || TextUtils.isEmpty(outcome)) {
            Toaster.longToast(R.string.error_fields_is_empty);
            return false;
        }
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
                sendPushNotification();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}