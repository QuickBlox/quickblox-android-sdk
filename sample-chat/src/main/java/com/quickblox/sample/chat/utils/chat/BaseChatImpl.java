package com.quickblox.sample.chat.utils.chat;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.quickblox.chat.QBChat;
import com.quickblox.chat.exception.QBChatException;
import com.quickblox.chat.listeners.QBMessageListener;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.sample.core.utils.Toaster;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;

public abstract class BaseChatImpl<T extends QBChat> implements Chat, QBMessageListener<T> {
    private static final String TAG = BaseChatImpl.class.getSimpleName();

    protected Handler mainThreadHandler = new Handler(Looper.getMainLooper());
    protected QBChatMessageListener chatMessageListener;
    protected T qbChat;

    public BaseChatImpl(QBChatMessageListener chatMessageListener) {
        // It's not a good practice to pass Activity to other classes as it may lead to memory leak
        // We're doing this only for chat sample simplicity, don't do this in your projects
        this.chatMessageListener = chatMessageListener;
        initManagerIfNeed();
    }

    protected abstract void initManagerIfNeed();

    @Override
    public void sendMessage(QBChatMessage message) throws XMPPException, SmackException.NotConnectedException {
        if (qbChat != null) {
            try {
                qbChat.sendMessage(message);
            } catch (SmackException.NotConnectedException e) {
                Log.w(TAG, e);
                Toaster.shortToast("Can't send a message, You are not connected to chat");
            } catch (IllegalStateException e) {
                Log.w(TAG, e);
                Toaster.shortToast("You're still joining a group chat, please wait a bit");
            }
        } else {
            Toaster.longToast("Join unsuccessful");
        }
    }

    @Override
    public void processMessage(final T qbChat, final QBChatMessage chatMessage) {
        // Show message in activity
        Log.i(TAG, "New incoming message: " + chatMessage);
        mainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                chatMessageListener.onQBChatMessageReceived(qbChat, chatMessage);
            }
        });
    }

    @Override
    public void processError(T qbChat, QBChatException e, QBChatMessage qbChatMessage) {
        Log.w(TAG, "Error processing message", e);
    }
}
