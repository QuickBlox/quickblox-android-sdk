package com.quickblox.chat_v2.apis;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import com.quickblox.chat_v2.core.ChatApplication;
import com.quickblox.chat_v2.gcm.GCMSender;
import com.quickblox.chat_v2.interfaces.OnDialogCreateComplete;
import com.quickblox.chat_v2.interfaces.OnDialogListRefresh;
import com.quickblox.chat_v2.interfaces.OnMessageListDownloaded;
import com.quickblox.chat_v2.interfaces.OnNewMessageIncome;
import com.quickblox.chat_v2.interfaces.OnNewRoomMessageIncome;
import com.quickblox.chat_v2.interfaces.OnPictureDownloadComplete;
import com.quickblox.chat_v2.interfaces.OnRoomListDownloaded;
import com.quickblox.chat_v2.interfaces.OnUserProfileDownloaded;
import com.quickblox.chat_v2.utils.ContextForDownloadUser;
import com.quickblox.chat_v2.utils.GlobalConsts;
import com.quickblox.chat_v2.utils.OfflineMessageSeparatorQuery;
import com.quickblox.core.QBCallbackImpl;
import com.quickblox.core.result.Result;
import com.quickblox.internal.module.custom.request.QBCustomObjectRequestBuilder;
import com.quickblox.module.chat.QBChat;
import com.quickblox.module.custom.QBCustomObjects;
import com.quickblox.module.custom.model.QBCustomObject;
import com.quickblox.module.custom.result.QBCustomObjectLimitedResult;
import com.quickblox.module.custom.result.QBCustomObjectResult;
import com.quickblox.module.users.model.QBUser;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Timer;
import java.util.TimerTask;

public class MessageManager implements MessageListener, OnPictureDownloadComplete, OnUserProfileDownloaded, PacketListener {

    private Context context;
    private ChatApplication app;

    private int opponentId;
    private int openChatOpponentId;
    private QBCustomObject customDialog;
    private int authorMessageId;
    private ArrayList<String> messageQuery;
    private String lastHoldMessage;
    private QBCustomObject localResult;

    private OnMessageListDownloaded listDownloadedListener;
    private OnDialogCreateComplete dialogCreateListener;
    private OnNewMessageIncome newMessageListener;
    private OnDialogListRefresh dialogRefreshListener;
    private OnRoomListDownloaded roomListDownloadListener;
    private OnNewRoomMessageIncome mNewRoomMessageIncome;

    private OfflineMessageSeparatorQuery omsq;
    private HashSet<Integer> mDialogCreateQueryCache;

    public MessageManager(Context context) {
        this.context = context;
        app = ChatApplication.getInstance();
        omsq = new OfflineMessageSeparatorQuery();
        messageQuery = new ArrayList<String>();
        mDialogCreateQueryCache = new HashSet<Integer>();
    }

    @Override
    public void processMessage(Chat chat, final Message message) {


        if (message.getBody() == null) {
            return;
        }

        // cut the full-name message author
        String[] partsIdto = message.getTo().split("-");
        String[] partsIdfrom = message.getFrom().split("-");
        authorMessageId = Integer.parseInt(partsIdfrom[0]);

        //notify open chat if this message foe him
        if (newMessageListener != null && authorMessageId == openChatOpponentId) {
            newMessageListener.incomeNewMessage(message.getBody());
        }
        omsq.addNewQueryElement(authorMessageId, message.getBody(), authorMessageId);

        // look in created dialogs, for dialog with this user and if current message is attach
        localResult = dialogReview(authorMessageId);
        ((Activity) context).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (localResult != null) {


                    if (message.getBody().length() > 13 && message.getBody().substring(0, 13).equals(GlobalConsts.ATTACH_INDICATOR)) {
                        updateDialogLastMessage(GlobalConsts.ATTACH_TEXT_FOR_DIALOGS, localResult.getCustomObjectId());
                    } else {
                        updateDialogLastMessage(message.getBody(), localResult.getCustomObjectId());
                    }
                } else {
                    if (!mDialogCreateQueryCache.contains(authorMessageId)) {
                        mDialogCreateQueryCache.add(authorMessageId);
                        startDialogCreate(authorMessageId);
                    }
                }
                // separate attach
                if (message.getBody().length() > 13 && message.getBody().substring(0, 13).equals(GlobalConsts.ATTACH_INDICATOR)) {
                    String[] parts = message.getBody().split("#");

                    QBUser tmpUser = new QBUser();
                    tmpUser.setFileId(Integer.parseInt(parts[1]));
                    app.getQbm().downloadQBFile(tmpUser);
                }
                configureAndPlaySoundNotification();
            }
        });
    }

    private void startDialogCreate(final Integer pAuthorMessageId) {


        ((Activity) context).runOnUiThread(new Runnable() {

            @Override
            public void run() {
                app.getQbm().addUserProfileListener(MessageManager.this);
                app.getQbm().getSingleUserInfo(pAuthorMessageId, ContextForDownloadUser.DOWNLOAD_FOR_MESSAGE_MANAGER);

            }
        });
    }

    // send messages into xmpp & customobject
    public void sendSingleMessage(Integer userId, String messageBody, String dialogId) {

        if (messageBody == null && dialogId == null && userId == null) {
            return;
        }
        HashMap<Integer, String> tmpMap = app.getUserNetStatusMap();
        if ((tmpMap.get(userId) == null || !tmpMap.get(userId).equals(GlobalConsts.PRESENCE_TYPE_AVAIABLE)) && app.getContactsMap().containsKey(String.valueOf(userId))) {

            pushSender(userId, app.getContactsMap().get(String.valueOf(userId)), messageBody);

            app.getUserNetStatusMap().put(userId, GlobalConsts.PRESENCE_TYPE_UNAVAIABLE);
        }
        QBChat.getInstance().sendMessage(userId, messageBody);


        if (messageBody.length() > 12 && messageBody.substring(0, 13).equals(GlobalConsts.ATTACH_INDICATOR)) {
            updateDialogLastMessage(GlobalConsts.ATTACH_TEXT_FOR_DIALOGS, dialogId);
        } else {

            omsq.addNewQueryElement(userId, messageBody, app.getQbUser().getId());
            updateDialogLastMessage(messageBody, dialogId);
        }


    }

    private void pushSender(int userId, QBUser pQBUser, String pMessage) {
        GCMSender gs = new GCMSender();
        gs.sendPushNotifications(userId, buldHybridMessageBody(pQBUser, pMessage));
    }

    public synchronized void createDialog(final QBUser qbuser, final boolean isNeedExtraReview) {
        //TODO: убрать костыль.
        if (qbuser == null) {
            return;
        }
        opponentId = qbuser.getId();

        if (isNeedExtraReview) {
            QBCustomObject oldDialog = dialogReview(opponentId);
            if (oldDialog != null) {
                dialogCreateListener.dialogCreate(opponentId, oldDialog.getCustomObjectId());
                return;
            }
        }

        ((Activity) context).runOnUiThread(new Runnable() {

            @Override
            public void run() {

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
                            if (dialogCreateListener != null) {
                                dialogCreateListener.dialogCreate(opponentId, ((QBCustomObjectResult) result).getCustomObject().getCustomObjectId());
                            }

                            mDialogCreateQueryCache.remove(opponentId);
                        }
                    }
                });

            }
        });
    }

    public void downloadDialogList(final boolean isNeedDownloadUsers) {

        if (Thread.currentThread().equals(Looper.getMainLooper().getThread())) {
            getUsersDialogsInUiTread(isNeedDownloadUsers);
        } else {

            Handler handler = new Handler(Looper.getMainLooper());
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
                            for (QBCustomObject co : ((QBCustomObjectLimitedResult) result).getCustomObjects()) {
                                app.getUserIdDialogIdMap().put(Integer.parseInt(co.getFields().get(GlobalConsts.RECEPIENT_ID_FIELD).toString()), co);
                                app.getDialogMap().put(co.getCustomObjectId(), co);
                            }
                            if (isNeedDownloadUsers) {
                                ArrayList userIds = new ArrayList<String>();
                                for (QBCustomObject co : ((QBCustomObjectLimitedResult) result).getCustomObjects()) {
                                    if (!app.getDialogsUsersMap().containsKey(co.getFields().get(GlobalConsts.RECEPIENT_ID_FIELD).toString())) {
                                        userIds.add(co.getFields().get(GlobalConsts.RECEPIENT_ID_FIELD).toString());
                                    }
                                }

                                app.getQbm().getQbUsersFromCollection(userIds, ContextForDownloadUser.DOWNLOAD_FOR_DIALOG);

                            }

                        }

                        dialogRefreshListener.reSetList();
                    }
                }
        );
    }

    public void getDialogMessages(int userId) {
        QBCustomObjectRequestBuilder requestBuilder = new QBCustomObjectRequestBuilder();
        requestBuilder.eq(GlobalConsts.USER_ID_FIELD, app.getQbUser().getId());
        requestBuilder.eq(GlobalConsts.OPPONENT_ID, userId);
        requestBuilder.sortAsc("created_at");

        QBCustomObjects.getObjects(GlobalConsts.MESSAGES, requestBuilder, new QBCallbackImpl() {
            @Override
            public void onComplete(Result result) {
                if (result.isSuccess()) {
                    listDownloadedListener.messageListDownloaded(((QBCustomObjectLimitedResult) result).getCustomObjects());
                }
            }
        });
    }

    public void updateDialogLastMessage(final String lastMsg, final String dialogId) {


        final QBCustomObject co = new QBCustomObject();
        co.setClassName(GlobalConsts.DIALOGS);
        HashMap<String, Object> fields = new HashMap<String, Object>();
        fields.put(GlobalConsts.LAST_MSG, lastMsg);
        co.setFields(fields);
        co.setCustomObjectId(dialogId);
        ((Activity) context).runOnUiThread(new Runnable() {
            @Override
            public void run() {

                QBCustomObjects.updateObject(co, new QBCallbackImpl() {
                    @Override
                    public void onComplete(Result result) {
                        if (dialogRefreshListener != null) {
                            dialogRefreshListener.reFreshList();
                            if (app.getDialogMap().containsKey(dialogId)) {
                                app.getDialogMap().get(dialogId).getFields().put(GlobalConsts.LAST_MSG, lastMsg);
                            }
                        }
                    }
                });

            }
        });
    }

    public QBCustomObject dialogReview(int opponentId) {
        for (QBCustomObject dialog : new ArrayList<QBCustomObject>(app.getDialogMap().values())) {
            HashMap<String, Object> test = dialog.getFields();
            if (Integer.parseInt((String) test.get(GlobalConsts.RECEPIENT_ID_FIELD)) == opponentId) {
                return dialog;
            }
        }
        return null;
    }

    //download custom object (private room) section
    public void downloadPersistentRoom() {

        QBCustomObjectRequestBuilder requestBuilder = new QBCustomObjectRequestBuilder();
        requestBuilder.in(GlobalConsts.ROOM_LIST_USERS_POOL, app.getQbUser().getId());
        QBCustomObjects.getObjects(GlobalConsts.ROOM_LIST_CLASS, requestBuilder, new QBCallbackImpl() {
            @Override
            public void onComplete(Result result) {
                if (result.isSuccess()) {

                    app.setUserPresentRoomList(((QBCustomObjectLimitedResult) result).getCustomObjects());
                    if (roomListDownloadListener != null) {
                        roomListDownloadListener.roomListDownloaded();
                    }
                }
            }
        });
    }

    public void createRoom(String roomName, String roomJid, ArrayList<String> invaiteList) {

        customDialog = new QBCustomObject();
        HashMap<String, Object> fields = new HashMap<String, Object>();
        fields.put(GlobalConsts.ROOM_LIST_NAME, roomName);
        fields.put(GlobalConsts.ROOM_LIST_JID, roomJid);
        fields.put(GlobalConsts.ROOM_LIST_USERS_POOL, invaiteList);
        customDialog.setFields(fields);
        customDialog.setClassName(GlobalConsts.ROOM_LIST_CLASS);

        ((Activity) context).runOnUiThread(new Runnable() {
            @Override
            public void run() {

                QBCustomObjects.createObject(customDialog, new QBCallbackImpl() {
                    @Override
                    public void onComplete(Result result) {
                        if (result.isSuccess()) {

                            QBCustomObject co = ((QBCustomObjectResult) result).getCustomObject();

                            app.getUserPresentRoomList().add(co);
                        }
                    }
                });

            }
        });
    }

    @Override
    public void downloadComlete(QBUser friend, ContextForDownloadUser pContextForDownloadUser) {
        if (pContextForDownloadUser == ContextForDownloadUser.DOWNLOAD_FOR_MESSAGE_MANAGER) {
            createDialog(friend, false);
        }

    }

    public void setListDownloadedListener(OnMessageListDownloaded listDownloadedListener) {
        this.listDownloadedListener = listDownloadedListener;
    }

    @Override
    public void downloadComlete(Bitmap bitmap, File file) {

    }

    @Override
    public void processPacket(Packet packet) {

        Message roomMessage = (Message) packet;
        StringBuilder builder = new StringBuilder();
        String[] parts = roomMessage.getFrom().split("/");
        builder.append(parts[1]).append(" : ").append(roomMessage.getBody());
        messageQuery.add(builder.toString());
        Timer time = new Timer();
        time.schedule(new TimerTask() {
            @Override
            public void run() {
                if (messageQuery.size() > 0) {
                    mNewRoomMessageIncome.incomeMessagePool(new ArrayList<String>(messageQuery));
                    messageQuery.clear();
                    messageQuery.trimToSize();
                }
            }
        }, 3000L, 2000L);

    }

    private void configureAndPlaySoundNotification() {

        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Ringtone r = RingtoneManager.getRingtone(context, notification);
        //TODO: снять блокировку перед билдом.
        //r.play();
    }


    private String buldHybridMessageBody(QBUser pQbuser, String pMessage) {
        StringBuilder sb = new StringBuilder();
        sb.append(pQbuser.getFullName() != null ? pQbuser.getFullName() : pQbuser.getLogin()).append(" : ").append(pMessage);
        return sb.toString();
    }


    public void setDialogCreateListener(OnDialogCreateComplete dialogCreateListener) {
        this.dialogCreateListener = dialogCreateListener;
    }

    public void setDialogRefreshListener(OnDialogListRefresh dialogRefreshListener) {
        this.dialogRefreshListener = dialogRefreshListener;
    }

    public void setNewMessageListener(OnNewMessageIncome newMessageListener, int openChatOpponentId) {
        this.newMessageListener = newMessageListener;
        this.openChatOpponentId = openChatOpponentId;
    }

    public void setRoomListDownloadListener(OnRoomListDownloaded roomListDownloadListener) {
        this.roomListDownloadListener = roomListDownloadListener;
    }

    public void setmNewRoomMessageIncome(OnNewRoomMessageIncome mNewRoomMessageIncome) {
        this.mNewRoomMessageIncome = mNewRoomMessageIncome;
    }

    public String getLastHoldMessage() {
        return lastHoldMessage;
    }

    public void setLastHoldMessage(String lastHoldMessage) {
        this.lastHoldMessage = lastHoldMessage;
    }
}