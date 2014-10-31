package com.quickblox.sample.chat.core;

import android.util.Log;
import android.widget.Toast;

import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBGroupChat;
import com.quickblox.chat.QBGroupChatManager;
import com.quickblox.chat.exception.QBChatException;
import com.quickblox.chat.listeners.QBMessageListenerImpl;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.chat.model.QBDialog;
import com.quickblox.sample.chat.ui.activities.ChatActivity;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.muc.DiscussionHistory;

import java.util.Arrays;
import java.util.List;

public class GroupChatManagerImpl extends QBMessageListenerImpl<QBGroupChat> implements ChatManager {
    private static final String TAG = "GroupChatManagerImpl";

    private ChatActivity chatActivity;

    private QBGroupChatManager groupChatManager;
    private QBGroupChat groupChat;

    public GroupChatManagerImpl(ChatActivity chatActivity) {
        this.chatActivity = chatActivity;

        groupChatManager = QBChatService.getInstance().getGroupChatManager();
    }

    public void joinGroupChat(QBDialog dialog, QBEntityCallback callback){
        groupChat = groupChatManager.createGroupChat(dialog.getRoomJid());
        join(groupChat, callback);
    }

    private void join(final QBGroupChat groupChat, final QBEntityCallback callback) {
        DiscussionHistory history = new DiscussionHistory();
        history.setMaxStanzas(0);

        groupChat.join(history, new QBEntityCallbackImpl() {
            @Override
            public void onSuccess() {

                groupChat.addMessageListener(GroupChatManagerImpl.this);

                chatActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        callback.onSuccess();

                        Toast.makeText(chatActivity, "Join successful", Toast.LENGTH_LONG).show();
                    }
                });
                Log.w("Chat", "Join successful");
            }

            @Override
            public void onError(final List list) {
                chatActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        callback.onError(list);
                    }
                });


                Log.w("Could not join chat, errors:", Arrays.toString(list.toArray()));
            }
        });
    }

    @Override
    public void release() throws XMPPException {
        if (groupChat != null) {
            try {
                groupChat.leave();
            } catch (SmackException.NotConnectedException nce){
                nce.printStackTrace();
            }

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
            } catch (IllegalStateException e){
                e.printStackTrace();

                Toast.makeText(chatActivity, "You are still joining a group chat, please white a bit", Toast.LENGTH_LONG).show();
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
}
