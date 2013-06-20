package com.quickblox.chat_v2.apis;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

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
import com.quickblox.chat_v2.utils.GlobalConsts;
import com.quickblox.chat_v2.utils.OfflineMessageSeparatorQuery;
import com.quickblox.chat_v2.utils.SingleChatDialogTable;
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
import java.util.Timer;
import java.util.TimerTask;

public class MessageManager implements MessageListener, OnPictureDownloadComplete, OnUserProfileDownloaded, PacketListener {

    private Context context;
    private ChatApplication app;

    private String backgroundMessage;
    private int opponentId;
    private QBUser tQbuser;
    private boolean isNeedreview;
    private int openChatOpponentId;
    private QBCustomObject customDialog;
    private boolean isNeedDownloadUser;
    private int authorMessageId;
    private ArrayList<String> messageQuery;
    private String lastHoldMessage;

    private OnMessageListDownloaded listDownloadedListener;
    private OnDialogCreateComplete dialogCreateListener;
    private OnNewMessageIncome newMessageListener;
    private OnDialogListRefresh dialogRefreshListener;
    private OnRoomListDownloaded roomListDownloadListener;
    private OnNewRoomMessageIncome mNewRoomMessageIncome;

    private SingleChatDialogTable coupleTable;
    private OfflineMessageSeparatorQuery omsq;

    public MessageManager(Context context) {
        this.context = context;
        app = ChatApplication.getInstance();
        omsq = new OfflineMessageSeparatorQuery();
        messageQuery = new ArrayList<String>();
    }

    @Override
    public void processMessage(Chat chat, Message message) {

        if (message.getBody() == null) {
            return;
        }

        String[] partsIdto = message.getTo().split("-");
        String[] partsIdfrom = message.getFrom().split("-");
        authorMessageId = Integer.parseInt(partsIdfrom[0]);

        if (newMessageListener != null && authorMessageId == openChatOpponentId) {
            newMessageListener.incomeNewMessage(message.getBody());
        }
        omsq.addNewQueryElement(authorMessageId, message.getBody(), authorMessageId);

        QBCustomObject localResult = dialogReview(authorMessageId);
        if (localResult != null) {
            updateDialogLastMessage(message.getBody(), localResult.getCustomObjectId());
        } else {

            if (coupleTable == null) {
                coupleTable = new SingleChatDialogTable();
                coupleTable.setCoupleDate(authorMessageId, Integer.parseInt(partsIdto[0]));
                startDialogCreate(message);
            } else {

                if (!coupleTable.reviewCoupleIsExist(authorMessageId, Integer.parseInt(partsIdto[0]))) {
                    startDialogCreate(message);
                }
            }
        }
        // separate attach
        if (message.getBody().substring(0, 13).equals(GlobalConsts.ATTACH_INDICATOR)) {
            String[] parts = message.getBody().split("#");

            QBUser tmpUser = new QBUser();
            tmpUser.setFileId(Integer.parseInt(parts[1]));
            app.getQbm().downloadQBFile(tmpUser);
        }

    }

    private void startDialogCreate(Message message) {

        backgroundMessage = message.getBody();
        ((Activity) context).runOnUiThread(new Runnable() {

            @Override
            public void run() {
                app.getQbm().setUserProfileListener(MessageManager.this);
                app.getQbm().getSingleUserInfo(opponentId);

            }
        });
    }

    // send messages into xmpp & customobject
    public void sendSingleMessage(Integer userId, String messageBody, String dialogId) {

        if (messageBody == null && dialogId == null && userId == null) {
            return;
        }

        if (app.getUserNetStatusMap().get(userId) == null || !app.getUserNetStatusMap().get(userId).equals(GlobalConsts.PRESENCE_TYPE_AVAIABLE)) {
            pushSender(userId);

            app.getUserNetStatusMap().put(userId, GlobalConsts.PRESENCE_TYPE_UNAVAIABLE);
        }
        QBChat.getInstance().sendMessage(userId, messageBody);

        omsq.addNewQueryElement(userId, messageBody, app.getQbUser().getId());
        updateDialogLastMessage(messageBody, dialogId);

    }

    private void pushSender(final int userId) {
        GCMSender gs = new GCMSender();
        gs.sendPushNotifications(userId);
    }

    public synchronized void createDialog(QBUser qbuser, boolean isNeedExtraReview) {

        opponentId = qbuser.getId();
        isNeedreview = isNeedExtraReview;
        tQbuser = qbuser;

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
                fields.put(GlobalConsts.RECEPIENT_ID_FIELD, tQbuser.getId());
                fields.put(GlobalConsts.NAME_FIELD, tQbuser.getFullName());
                co.setFields(fields);
                co.setClassName(GlobalConsts.DIALOGS_CLASS);
                QBCustomObjects.createObject(co, new QBCallbackImpl() {
                    @Override
                    public void onComplete(Result result) {
                        if (result.isSuccess()) {

                            if (isNeedreview) {
                                dialogCreateListener.dialogCreate(opponentId, ((QBCustomObjectResult) result).getCustomObject().getCustomObjectId());
                            } else {
                                updateDialogLastMessage(backgroundMessage, ((QBCustomObjectResult) result).getCustomObject().getCustomObjectId());
                                dialogRefreshListener.refreshList();
                            }
                            if (lastHoldMessage != null) {
                                updateDialogLastMessage(lastHoldMessage, ((QBCustomObjectResult) result).getCustomObject().getCustomObjectId());
                            }
                        }
                    }
                });

            }
        });
    }

    public void downloadDialogList(boolean isNeedDownloadUsers) {
        isNeedDownloadUser = isNeedDownloadUsers;
        QBCustomObjectRequestBuilder requestBuilder = new QBCustomObjectRequestBuilder();

        requestBuilder.eq(GlobalConsts.USER_ID_FIELD, app.getQbUser().getId());
        QBCustomObjects.getObjects(GlobalConsts.DIALOGS, requestBuilder, new QBCallbackImpl() {
            @Override
            public void onComplete(Result result) {
                if (result.isSuccess()) {
                    app.setDialogList(((QBCustomObjectLimitedResult) result).getCustomObjects());
                    dialogRefreshListener.refreshList();

                    for (QBCustomObject co : app.getDialogList()) {
                        app.getUserIdDialogIdMap().put(Integer.parseInt(co.getFields().get(GlobalConsts.RECEPIENT_ID_FIELD).toString()), co);
                    }
                    if (isNeedDownloadUser) {
                        ArrayList<String> userIds = new ArrayList<String>();
                        for (QBCustomObject co : app.getDialogList()) {
                            if (!app.getDialogsUsersMap().containsKey(co.getFields().get(GlobalConsts.RECEPIENT_ID_FIELD).toString())) {

                                userIds.add(co.getFields().get(GlobalConsts.RECEPIENT_ID_FIELD).toString());
                            }
                        }
                        app.getQbm().getQbUsersFromCollection(userIds, GlobalConsts.DOWNLOAD_LIST_FOR_DIALOG);
                    }
                }
            }
        });
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

        ((Activity) context).runOnUiThread(new Runnable() {
            @Override
            public void run() {

                QBCustomObject co = new QBCustomObject();
                co.setClassName(GlobalConsts.DIALOGS);
                HashMap<String, Object> fields = new HashMap<String, Object>();
                fields.put(GlobalConsts.LAST_MSG, lastMsg);
                co.setFields(fields);
                co.setCustomObjectId(dialogId);
                QBCustomObjects.updateObject(co, new QBCallbackImpl() {
                    @Override
                    public void onComplete(Result result) {
                        dialogRefreshListener.refreshList();
                    }
                });

            }
        });
    }

    public QBCustomObject dialogReview(int opponentId) {
        for (QBCustomObject dialog : app.getDialogList()) {
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
    public void downloadComlete(QBUser friend) {
        createDialog(friend, false);
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
        Log.e("MM", "message = " + roomMessage.getBody());

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