package com.quickblox.sample.chat.core;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.quickblox.module.chat.QBChatService;
import com.quickblox.module.chat.listeners.ChatMessageListener;
import com.quickblox.module.chat.xmpp.QBPrivateChat;
import com.quickblox.sample.chat.model.ChatMessage;
import com.quickblox.sample.chat.ui.activities.ChatActivity;

import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;

import java.util.Calendar;

public class SingleChat implements Chat, ChatMessageListener {

    public static final String USER_ID = "user_id";
    private static final String TAG = SingleChat.class.getSimpleName();

    private Handler handler = new Handler(Looper.getMainLooper());
    private ChatActivity chatActivity;
    private QBPrivateChat chat;
    private int companionId;

    public SingleChat(ChatActivity chatActivity) {
        this.chatActivity = chatActivity;
        companionId = chatActivity.getIntent().getIntExtra(USER_ID, 0);
        chat = QBChatService.getInstance().createChat();
        chat.addChatMessageListener(this);
    }

    @Override
    public void sendMessage(String message) {
        try {
            chat.sendMessage(companionId, message);
        } catch (XMPPException e) {
            Log.e(TAG, "failed to send a message", e);
        }
    }

    @Override
    public void release() {

    }

    @Override
    public void processMessage(Message message) {
        final String messageBody = message.getBody();
        // Show message
        handler.post(new Runnable() {
            @Override
            public void run() {
                chatActivity.showMessage(new ChatMessage(messageBody, Calendar.getInstance().getTime(), false));
            }
        });
    }

    @Override
    public boolean accept(Message.Type messageType) {
        switch (messageType) {
            case normal:
            case chat:
            case groupchat:
                return true;
            default:
                return false;
        }
    }
}
