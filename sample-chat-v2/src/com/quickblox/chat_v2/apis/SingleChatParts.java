package com.quickblox.chat_v2.apis;

import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import android.util.Log;
import com.quickblox.chat_v2.core.ChatApplication;
import com.quickblox.chat_v2.gcm.GCMSender;
import com.quickblox.chat_v2.interfaces.OnMessageListDownloaded;
import com.quickblox.chat_v2.interfaces.OnUserProfileDownloaded;
import com.quickblox.chat_v2.utils.ContextForDownloadUser;
import com.quickblox.chat_v2.utils.GlobalConsts;
import com.quickblox.chat_v2.utils.OfflineMessageSeparatorQuery;
import com.quickblox.core.QBCallbackImpl;
import com.quickblox.core.result.Result;
import com.quickblox.internal.module.custom.request.QBCustomObjectRequestBuilder;
import com.quickblox.module.chat.QBChat;
import com.quickblox.module.chat.listeners.ChatMessageListener;
import com.quickblox.module.chat.utils.QBChatUtils;
import com.quickblox.module.custom.QBCustomObjects;
import com.quickblox.module.custom.model.QBCustomObject;
import com.quickblox.module.custom.result.QBCustomObjectLimitedResult;
import com.quickblox.module.custom.result.QBCustomObjectResult;
import com.quickblox.module.users.model.QBUser;

import org.jivesoftware.smack.packet.Message;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by andrey on 05.07.13.
 */
public class SingleChatParts {

    private OfflineMessageSeparatorQuery omsq;

    private ChatApplication app;

    private Handler handler = new Handler(Looper.getMainLooper());

    private ConcurrentMap<Integer, String> mMessageStack = new ConcurrentHashMap<Integer, String>();

    private MediaPlayer mediaPlayer = new MediaPlayer();

    private ChatMessageListener mMessageListener = new ChatMessageListener() {

        @Override
        public void processMessage(final Message message) {
            Log.w("NEW MSG", message.toString());

            if (message.getBody() == null) {
                return;
            }

            final int userFrom = QBChatUtils.parseQBUser(message.getFrom());


            app.sendBroadcast(new Intent(GlobalConsts.INCOMING_MESSAGE_ACTION)
                    .putExtra(GlobalConsts.EXTRA_MESSAGE, message.getBody())
                    .putExtra(GlobalConsts.OPPONENT_ID, userFrom));

            omsq.addNewQueryElement(userFrom, message.getBody(), userFrom);

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    String localResult = app.getDialogIdByUser(userFrom);

                    if (localResult != null) {
                          updateDialogLastMessage(message.getBody(), localResult);
                    } else {
                        if (!mMessageStack.containsKey(userFrom)) {
                            startDialogCreate(userFrom);
                        }
                        mMessageStack.put(userFrom, message.getBody());
                    }
                    configureAndPlaySoundNotification();
                }
            }, 2000);
        }

        @Override
        public boolean accept(Message.Type type) {
            return true;
        }
    };

    private OnUserProfileDownloaded mOnUserProfileDownloaded = new OnUserProfileDownloaded() {
        @Override
        public void downloadComplete(QBUser friend, ContextForDownloadUser pContextForDownloadUser) {
            if (pContextForDownloadUser == ContextForDownloadUser.DOWNLOAD_FOR_MESSAGE_MANAGER) {
                createDialog(friend);
            }
        }
    };

    public SingleChatParts() {
        app = ChatApplication.getInstance();
        omsq = new OfflineMessageSeparatorQuery();
    }

    private void startDialogCreate(int pAuthorMessageId) {
        app.getQbm().addUserProfileListener(mOnUserProfileDownloaded);
        app.getQbm().getSingleUserInfo(pAuthorMessageId, ContextForDownloadUser.DOWNLOAD_FOR_MESSAGE_MANAGER);


    }


    public void sendMessage(int userId, String messageBody, String dialogId) {

        if (messageBody == null && dialogId == null) {
            return;
        }
        HashMap<Integer, String> tmpMap = app.getUserNetStatusMap();

        if ((tmpMap.get(userId) == null || !tmpMap.get(userId).equals(GlobalConsts.PRESENCE_TYPE_AVAIABLE)) && app.getContactsMap().containsKey(userId)) {

            sendPush(userId, app.getContactsMap().get(userId), messageBody);

            app.getUserNetStatusMap().put(userId, GlobalConsts.PRESENCE_TYPE_UNAVAIABLE);
        }
        QBChat.getInstance().sendMessage(userId, messageBody);

        updateDialogLastMessage(messageBody, dialogId);

        omsq.addNewQueryElement(userId, messageBody, app.getQbUser().getId());

    }

    private void sendPush(int userId, QBUser pQBUser, String pMessage) {
        GCMSender gs = new GCMSender();
        String name = pQBUser.getFullName() != null ? pQBUser.getFullName() : pQBUser.getLogin();

        StringBuilder sb = new StringBuilder();
        sb.append(name).append(" : ").append(pMessage);

        gs.sendPushNotifications(userId, sb.toString());
    }

    public void createDialog(final QBUser qbuser) {
        final int opponentId = qbuser.getId();

        String localDialog = app.getDialogIdByUser(opponentId);
        if (localDialog != null) {

            app.sendBroadcast(new Intent(GlobalConsts.DIALOG_CREATED_ACTION)
                    .putExtra(GlobalConsts.OPPONENT_ID, opponentId)
                    .putExtra(GlobalConsts.DIALOG_ID, localDialog));
            return;
        }

        QBCustomObject co = new QBCustomObject();
        HashMap<String, Object> fields = new HashMap<String, Object>();
        fields.put(GlobalConsts.RECEPIENT_ID_FIELD, opponentId);
        fields.put(GlobalConsts.NAME_FIELD, qbuser.getFullName());
        co.setFields(fields);
        co.setClassName(GlobalConsts.DIALOGS_CLASS);
        QBCustomObjects.createObject(co, new QBCallbackImpl() {
            @Override
            public void onComplete(Result result) {
                if (result.isSuccess()) {
                    QBCustomObjectResult customObjectResult = (QBCustomObjectResult) result;
                    QBCustomObject customObject = customObjectResult.getCustomObject();

                    String lastMessage = mMessageStack.get(opponentId);
                    if (lastMessage != null) {
                        customObject.getFields().put(GlobalConsts.LAST_MSG, lastMessage);
                        mMessageStack.remove(opponentId);
                    }
                    ;
                    String dialogId = customObject.getCustomObjectId();
                    app.getDialogMap().put(dialogId, customObject);
                    app.getDialogsUsersMap().put(opponentId, qbuser);
                    app.getUserIdDialogIdMap().put(opponentId, customObject);

                    app.sendBroadcast(new Intent(GlobalConsts.DIALOG_CREATED_ACTION)
                            .putExtra(GlobalConsts.OPPONENT_ID, opponentId)
                            .putExtra(GlobalConsts.DIALOG_ID, dialogId));
                }
            }
        });

    }

    public void downloadDialogList(final boolean isNeedDownloadUsers) {

        if (Thread.currentThread().equals(Looper.getMainLooper().getThread())) {
            getUsersDialogsInUiTread(isNeedDownloadUsers);
        } else {
            handler.post(new Runnable() {

                @Override
                public void run() {
                    getUsersDialogsInUiTread(isNeedDownloadUsers);
                }
            });
        }
    }

    private void getUsersDialogsInUiTread(final boolean isNeedDownloadUsers) {

        final QBCustomObjectRequestBuilder requestBuilder = new QBCustomObjectRequestBuilder();
        requestBuilder.eq(GlobalConsts.USER_ID_FIELD, app.getQbUser().getId());

        QBCustomObjects.getObjects(GlobalConsts.DIALOGS, requestBuilder, new

                QBCallbackImpl() {
                    @Override
                    public void onComplete(Result result) {
                        if (result.isSuccess()) {
                            QBCustomObjectLimitedResult limitedResult = (QBCustomObjectLimitedResult) result;

                            List<Integer> userIds = new ArrayList<Integer>();

                            for (QBCustomObject customObject : limitedResult.getCustomObjects()) {

                                Integer recepientId = Integer.parseInt(customObject.getFields().get(GlobalConsts.RECEPIENT_ID_FIELD).toString());

                                app.getUserIdDialogIdMap().put(recepientId, customObject);
                                app.getDialogMap().put(customObject.getCustomObjectId(), customObject);
                                if (isNeedDownloadUsers) {
                                    if (!app.getDialogsUsersMap().containsKey(recepientId)) {
                                        userIds.add(recepientId);
                                    }
                                }

                            }
                            if (!userIds.isEmpty()) {
                                app.getQbm().getQbUsersFromCollection(userIds, ContextForDownloadUser.DOWNLOAD_FOR_DIALOG);
                            }
                        }

                        notifyRefreshDialogs();
                    }
                }
        );
    }

    public void getDialogMessages(int userId, int opponentId, final OnMessageListDownloaded pDialogMessagesListDownloadedListener) {
        QBCustomObjectRequestBuilder requestBuilder = new QBCustomObjectRequestBuilder();
        requestBuilder.eq(GlobalConsts.USER_ID_FIELD, userId);
        requestBuilder.eq(GlobalConsts.OPPONENT_ID, opponentId);
        requestBuilder.sortAsc("created_at");

        QBCustomObjects.getObjects(GlobalConsts.MESSAGES, requestBuilder, new QBCallbackImpl() {
            @Override
            public void onComplete(Result result) {
                if (result.isSuccess()) {
                    if (pDialogMessagesListDownloadedListener != null) {
                        pDialogMessagesListDownloadedListener.messageListDownloaded(((QBCustomObjectLimitedResult) result).getCustomObjects());
                    }
                }
            }
        });
    }

    public void updateDialogLastMessage(String lastMsg, final String dialogId) {

        Log.e("Update dialog message", "update ="+lastMsg);

        if (lastMsg.length() > 12 && lastMsg.substring(0, 13).equals(GlobalConsts.ATTACH_INDICATOR)) {
            lastMsg = GlobalConsts.ATTACH_TEXT_FOR_DIALOGS;
        }
        final String lastMessage = lastMsg;
        final QBCustomObject co = new QBCustomObject();
        co.setClassName(GlobalConsts.DIALOGS);
        HashMap<String, Object> fields = new HashMap<String, Object>();
        fields.put(GlobalConsts.LAST_MSG, lastMsg);
        co.setFields(fields);
        co.setCustomObjectId(dialogId);
        QBCustomObjects.updateObject(co, new QBCallbackImpl() {
            @Override
            public void onComplete(Result result) {
                notifyRefreshDialogs();
                if (app.getDialogMap().containsKey(dialogId)) {
                    app.getDialogMap().get(dialogId).getFields().put(GlobalConsts.LAST_MSG, lastMessage);
                }
            }
        });
    }


    private void notifyRefreshDialogs() {
        app.sendBroadcast(new Intent(GlobalConsts.DIALOG_REFRESHED_ACTION));
    }

    private void configureAndPlaySoundNotification() {

        if (mediaPlayer.isPlaying()) {
            return;
        }

        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_NOTIFICATION);

        try {
            mediaPlayer.setDataSource(app, notification);
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public ChatMessageListener getMessageListener() {
        return mMessageListener;
    }
}
