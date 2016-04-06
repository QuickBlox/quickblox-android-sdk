package com.quickblox.sample.chat.utils.chat;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBGroupChat;
import com.quickblox.chat.QBGroupChatManager;
import com.quickblox.chat.listeners.QBMessageSentListener;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.chat.model.QBDialog;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.muc.DiscussionHistory;

public class GroupChatImpl extends BaseChatImpl<QBGroupChat> implements QBMessageSentListener<QBGroupChat> {
    private static final String TAG = GroupChatImpl.class.getSimpleName();

    private QBGroupChatManager qbGroupChatManager;

    public GroupChatImpl(QBChatMessageListener chatMessageListener) {
        super(chatMessageListener);
    }

    @Override
    protected void initManagerIfNeed() {
        if (qbGroupChatManager == null) {
            qbGroupChatManager = QBChatService.getInstance().getGroupChatManager();
        }
    }

    public void joinGroupChat(QBDialog dialog, QBEntityCallback<Void> callback) {
        initManagerIfNeed();
        if (qbChat == null) {
            qbChat = qbGroupChatManager.createGroupChat(dialog.getRoomJid());
        }
        join(callback);
    }

    private void join(final QBEntityCallback<Void> callback) {
        DiscussionHistory history = new DiscussionHistory();
        history.setMaxStanzas(0);

        QBEntityCallback<Void> qbEntityCallback = new QBEntityCallback<Void>() {

            @Override
            public void onSuccess(final Void result, final Bundle bundle) {
                qbChat.addMessageListener(GroupChatImpl.this);
                qbChat.addMessageSentListener(GroupChatImpl.this);

                mainThreadHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onSuccess(result, bundle);
                    }
                });
                Log.i(TAG, "Join successful");
            }

            @Override
            public void onError(final QBResponseException e) {
                mainThreadHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onError(e);
                    }
                });
            }
        };

        new JoinRoomTask(qbEntityCallback).execute(history);
    }

    public void leaveChatRoom() {
        try {
            qbChat.leave();
        } catch (SmackException.NotConnectedException | XMPPException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void release() throws XMPPException {
        if (qbChat != null) {
            leaveChatRoom();
            qbChat.removeMessageListener(this);
        }
    }

    @Override
    public void processMessageSent(QBGroupChat qbGroupChat, QBChatMessage qbChatMessage) {

    }

    @Override
    public void processMessageFailed(QBGroupChat qbGroupChat, QBChatMessage qbChatMessage) {

    }

    class JoinRoomTask extends AsyncTask {
        private QBEntityCallback qbEntityCallback;

        public JoinRoomTask(QBEntityCallback qbEntityCallback) {
            this.qbEntityCallback = qbEntityCallback;
        }

        @Override
        protected Object doInBackground(Object[] params) {
            DiscussionHistory history = (DiscussionHistory) params[0];
            try {
                qbChat.join(history);
                qbEntityCallback.onSuccess(null, Bundle.EMPTY);
            } catch (XMPPException | SmackException e) {
                qbEntityCallback.onError(new QBResponseException(e.getMessage()));
            }
            return qbEntityCallback;
        }
    }
}
