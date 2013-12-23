package com.quickblox.sample.chat.core;

import android.os.Handler;
import android.os.Looper;

import com.quickblox.module.chat.QBChat;
import com.quickblox.module.chat.listeners.ChatMessageListener;
import com.quickblox.sample.chat.ui.activities.ChatActivity;
import com.quickblox.sample.chat.model.ChatMessage;

import org.jivesoftware.smack.packet.Message;

import java.util.Calendar;

public class SingleChat implements Chat {

    public static final String USER_ID = "user_id";

    private Handler handler = new Handler(Looper.getMainLooper());
    private ChatActivity chat;
    private int userId;
    private ChatMessageListener chatMessageListener;

    public SingleChat(ChatActivity сhat) {
        this.chat = сhat;
        userId = chat.getIntent().getIntExtra(USER_ID, 0);

        // Set 1-1 Chat message listener
        chatMessageListener = new ChatMessageListener() {

            @Override
            public void processMessage(Message message) {
                final String messageBody = message.getBody();
                // Show message
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        chat.showMessage(new ChatMessage(messageBody, Calendar.getInstance().getTime(), false));
                    }
                });
            }

            @Override
            public boolean accept(Message.Type type) {
                switch (type) {
                    case normal:
                    case chat:
                    case groupchat:
                        return true;
                    default:
                        return false;
                }
            }
        };
        QBChat.getInstance().initChat(chatMessageListener);
    }

    @Override
    public void sendMessage(String message) {
        QBChat.getInstance().sendMessage(userId, message);
    }

    @Override
    public void release() {

    }
}
