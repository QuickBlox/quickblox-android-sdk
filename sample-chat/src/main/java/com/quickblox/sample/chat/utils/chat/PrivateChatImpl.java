package com.quickblox.sample.chat.utils.chat;

import android.util.Log;

import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBPrivateChat;
import com.quickblox.chat.QBPrivateChatManager;
import com.quickblox.chat.listeners.QBPrivateChatManagerListener;
import com.quickblox.sample.chat.ui.activity.ChatActivity;

public class PrivateChatImpl extends BaseChatImpl<QBPrivateChat> implements QBPrivateChatManagerListener {
    private static final String TAG = PrivateChatImpl.class.getSimpleName();

    private QBPrivateChatManager qbPrivateChatManager;

    public PrivateChatImpl(ChatActivity chatActivity, Integer opponentId) {
        super(chatActivity);

        qbChat = qbPrivateChatManager.getChat(opponentId);
        if (qbChat == null) {
            qbChat = qbPrivateChatManager.createChat(opponentId, this);
        } else {
            qbChat.addMessageListener(this);
        }
    }

    @Override
    protected void initManagerIfNeed() {
        if (qbPrivateChatManager == null) {
            qbPrivateChatManager = QBChatService.getInstance().getPrivateChatManager();
            qbPrivateChatManager.addPrivateChatManagerListener(this);
        }
    }

    @Override
    public void release() {
        Log.i(TAG, "Release private chat");

        qbChat.removeMessageListener(this);
        qbPrivateChatManager.removePrivateChatManagerListener(this);
    }

    @Override
    public void chatCreated(QBPrivateChat incomingPrivateChat, boolean createdLocally) {
        Log.i(TAG, "Private chat created: " + incomingPrivateChat.getParticipant() + ", createdLocally:" + createdLocally);

        if (!createdLocally) {
            qbChat = incomingPrivateChat;
            qbChat.addMessageListener(this);
        }

    }
}
