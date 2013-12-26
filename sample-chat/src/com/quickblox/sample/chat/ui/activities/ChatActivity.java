package com.quickblox.sample.chat.ui.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.quickblox.sample.chat.R;
import com.quickblox.sample.chat.core.Chat;
import com.quickblox.sample.chat.core.RoomChat;
import com.quickblox.sample.chat.core.SingleChat;
import com.quickblox.sample.chat.model.ChatMessage;
import com.quickblox.sample.chat.ui.adapters.ChatAdapter;

import java.util.ArrayList;
import java.util.Calendar;

public class ChatActivity extends Activity {

    public static final String EXTRA_MODE = "mode";
    private EditText messageEditText;
    private Mode mode = Mode.SINGLE;
    private Chat chat;
    private ChatAdapter adapter;
    private ListView messagesContainer;

    public static void start(Context context, Bundle bundle) {
        Intent intent = new Intent(context, ChatActivity.class);
        intent.putExtras(bundle);
        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        initViews();
    }

    @Override
    public void onBackPressed() {
        chat.release();
        super.onBackPressed();
    }

    private void initViews() {
        messagesContainer = (ListView) findViewById(R.id.messagesContainer);
        messageEditText = (EditText) findViewById(R.id.messageEdit);
        Button sendButton = (Button) findViewById(R.id.chatSendButton);

        adapter = new ChatAdapter(this, new ArrayList<ChatMessage>());
        messagesContainer.setAdapter(adapter);

        Intent intent = getIntent();
        mode = (Mode) intent.getSerializableExtra(EXTRA_MODE);
        switch (mode) {
            case GROUP:
                chat = new RoomChat(this);
                break;
            case SINGLE:
                chat = new SingleChat(this);
                break;
        }

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String lastMsg = messageEditText.getText().toString();
                if (TextUtils.isEmpty(lastMsg)) {
                    return;
                }

                messageEditText.setText("");
                chat.sendMessage(lastMsg);

                if (mode == Mode.SINGLE) {
                    showMessage(new ChatMessage(lastMsg, Calendar.getInstance().getTime(), false));
                }
            }
        });
    }

    public void showMessage(ChatMessage message) {
        adapter.add(message);
        adapter.notifyDataSetChanged();
        scrollDown();
    }

    private void scrollDown() {
        messagesContainer.setSelection(messagesContainer.getCount() - 1);
    }

    public static enum Mode {SINGLE, GROUP}
}
