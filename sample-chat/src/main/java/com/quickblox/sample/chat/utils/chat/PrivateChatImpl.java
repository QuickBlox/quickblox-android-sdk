package com.quickblox.sample.chat.utils.chat;

import android.util.Log;

import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBPrivateChat;
import com.quickblox.chat.QBPrivateChatManager;
import com.quickblox.chat.listeners.QBMessageSentListener;
import com.quickblox.chat.listeners.QBPrivateChatManagerListener;
import com.quickblox.chat.model.QBChatMessage;

public class PrivateChatImpl extends BaseChatImpl<QBPrivateChat>
        implements QBPrivateChatManagerListener, QBMessageSentListener<QBPrivateChat> {
    private static final String TAG = PrivateChatImpl.class.getSimpleName();

    private QBPrivateChatManager qbPrivateChatManager;

    public PrivateChatImpl(QBChatMessageListener chatMessageListener, Integer opponentId) {
        super(chatMessageListener);

        qbChat = qbPrivateChatManager.getChat(opponentId);
        if (qbChat == null) {
            qbChat = qbPrivateChatManager.createChat(opponentId, this);
        } else {
            qbChat.addMessageListener(this);
        }
        qbChat.addMessageSentListener(this);
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
        initManagerIfNeed();

        qbChat.removeMessageSentListener(this);
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

    @Override
    public void processMessageSent(QBPrivateChat qbPrivateChat, QBChatMessage qbChatMessage) {
        Log.i(TAG, "processMessageSent: " + qbChatMessage.getBody());
    }

    @Override
    public void processMessageFailed(QBPrivateChat qbPrivateChat, QBChatMessage qbChatMessage) {

    }
}