package com.quickblox.sample.chat.utils.chat;

import android.util.Log;

import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBGroupChat;
import com.quickblox.chat.QBGroupChatManager;
import com.quickblox.chat.model.QBDialog;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.sample.chat.ui.activity.ChatActivity;
import com.quickblox.sample.chat.utils.ErrorUtils;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.muc.DiscussionHistory;

import java.util.List;

public class GroupChatImpl extends BaseChatImpl<QBGroupChat> {
    private static final String TAG = GroupChatImpl.class.getSimpleName();

    private QBGroupChatManager qbGroupChatManager;

    public GroupChatImpl(ChatActivity chatActivity) {
        super(chatActivity);
    }

    @Override
    protected void initManagerIfNeed() {
        if (qbGroupChatManager == null) {
            qbGroupChatManager = QBChatService.getInstance().getGroupChatManager();
        }
    }

    public void joinGroupChat(QBDialog dialog, QBEntityCallback callback) {
        if (qbChat == null) {
            qbChat = qbGroupChatManager.createGroupChat(dialog.getRoomJid());
        }
        join(callback);
    }

    private void join(final QBEntityCallback callback) {
        DiscussionHistory history = new DiscussionHistory();
        history.setMaxStanzas(0);

        qbChat.join(history, new QBEntityCallbackImpl<String>() {
            @Override
            public void onSuccess() {
                qbChat.addMessageListener(GroupChatImpl.this);

                chatActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        callback.onSuccess();
                    }
                });
                Log.i(TAG, "Join successful");
            }

            @Override
            public void onError(final List<String> list) {
                chatActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        callback.onError(list);
                        ErrorUtils.showErrorDialog(chatActivity, "Could not join chat, errors:", list);
                    }
                });
            }
        });
    }

    public void leave() {
        try {
            qbChat.leave();
        } catch (SmackException.NotConnectedException | XMPPException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void release() throws XMPPException {
        if (qbChat != null) {
            leave();
            qbChat.removeMessageListener(this);
        }
    }
}
