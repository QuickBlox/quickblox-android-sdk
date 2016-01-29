package com.quickblox.sample.chat.core;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBGroupChat;
import com.quickblox.chat.QBGroupChatManager;
import com.quickblox.chat.exception.QBChatException;
import com.quickblox.chat.listeners.QBMessageListenerImpl;
import com.quickblox.chat.listeners.QBMessageSentListener;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.chat.model.QBDialog;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.sample.chat.ui.activities.ChatActivity;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.muc.DiscussionHistory;

public class GroupChatImpl extends QBMessageListenerImpl<QBGroupChat> implements Chat, QBMessageSentListener<QBGroupChat> {
    private static final String TAG = GroupChatImpl.class.getSimpleName();

    private ChatActivity chatActivity;

    private QBGroupChatManager groupChatManager;
    private QBGroupChat groupChat;

    public GroupChatImpl(ChatActivity chatActivity) {
        this.chatActivity = chatActivity;
    }

    public void joinGroupChat(QBDialog dialog, QBEntityCallback callback){
        initManagerIfNeed();

        if(groupChat == null) {
            groupChat = groupChatManager.createGroupChat(dialog.getRoomJid());
        }
        join(groupChat, callback);
    }

    private void initManagerIfNeed(){
        if(groupChatManager == null){
            groupChatManager = QBChatService.getInstance().getGroupChatManager();
        }
    }

    private void join(final QBGroupChat groupChat, final QBEntityCallback callback) {
        DiscussionHistory history = new DiscussionHistory();
        history.setMaxStanzas(0);

        Toast.makeText(chatActivity, "Joining room...", Toast.LENGTH_LONG).show();

        groupChat.join(history, new QBEntityCallback<Void>() {
            @Override
            public void onSuccess(final Void result, final Bundle bundle) {

                groupChat.addMessageListener(GroupChatImpl.this);
                groupChat.addMessageSentListener(GroupChatImpl.this);

                chatActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        callback.onSuccess(result, bundle);

                        Toast.makeText(chatActivity, "Join successful", Toast.LENGTH_LONG).show();
                    }
                });
                Log.w("Chat", "Join successful");
            }

            @Override
            public void onError(final QBResponseException error) {
                chatActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        callback.onError(error);
                    }
                });


                Log.w("Could not join chat:", error.getErrors().toString());
            }
        });
    }

    public void leave(){
        try {
            groupChat.leave();
        } catch (SmackException.NotConnectedException nce) {
            nce.printStackTrace();
        } catch (XMPPException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void release() throws XMPPException {
        if (groupChat != null) {
            leave();

            groupChat.removeMessageListener(this);
        }
    }

    @Override
    public void sendMessage(QBChatMessage message) throws XMPPException, SmackException.NotConnectedException {
        if (groupChat != null) {
            try {
                groupChat.sendMessage(message);
            } catch (SmackException.NotConnectedException nce){
                nce.printStackTrace();
                Toast.makeText(chatActivity, "Can't send a message, You are not connected to chat", Toast.LENGTH_SHORT).show();
            } catch (IllegalStateException e){
                e.printStackTrace();
                Toast.makeText(chatActivity, "You are still joining a group chat, please wait a bit", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(chatActivity, "Join unsuccessful", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void processMessage(QBGroupChat groupChat, QBChatMessage chatMessage) {
        // Show message
        Log.w(TAG, "new incoming message: " + chatMessage);
        chatActivity.showMessage(chatMessage);
    }

    @Override
    public void processError(QBGroupChat groupChat, QBChatException error, QBChatMessage originMessage){

    }

    @Override
    public void processMessageSent(QBGroupChat qbChat, QBChatMessage qbChatMessage) {

    }

    @Override
    public void processMessageFailed(QBGroupChat qbChat, QBChatMessage qbChatMessage) {

    }
}
