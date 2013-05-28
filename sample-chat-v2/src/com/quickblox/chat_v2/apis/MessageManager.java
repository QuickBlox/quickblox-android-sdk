package com.quickblox.chat_v2.apis;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import com.quickblox.chat_v2.core.ChatApplication;
import com.quickblox.chat_v2.interfaces.*;
import com.quickblox.chat_v2.utils.GlobalConsts;
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
import org.jivesoftware.smack.packet.Message;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageManager implements MessageListener, OnPictureDownloadComplete, OnUserProfileDownloaded {

    private Context context;
    private ChatApplication app;

    private String message;
    private String backgroundMessage;
    private String tmpIdforReview;
    private int authorId;
    private int opponentId;
    private QBUser tQbuser;
    private boolean isNeedreview;
    private int openChatOpponentId;
    private QBCustomObject customDialog;
    private boolean isNeedDownloadUser;

    private OnMessageListDownloaded listDownloadedListener;
    private OnDialogCreateComplete dialogCreateListener;
    private OnNewMessageIncome newMessageListener;
    private OnDialogListRefresh dialogRefreshListener;
    private OnRoomListDownloaded roomListDownloadListener;

    private Pattern idFromJidPattern;
    private Matcher matcher;

    public MessageManager(Context context) {
        this.context = context;
        app = ChatApplication.getInstance();
        idFromJidPattern = Pattern.compile(GlobalConsts.REGEX_MESSAGE_AUTHOR_ID);
    }

    // Глобальный слушатель
    @Override
    public void processMessage(Chat chat, Message message) {
        if (message.getBody() == null) {
            return;
        }

        matcher = idFromJidPattern.matcher(message.getFrom());
        sendToQB(Integer.parseInt(matcher.group(0)), message.getBody(), Integer.parseInt(matcher.group(0)));

        if (newMessageListener != null && Integer.parseInt(matcher.group(0)) == openChatOpponentId) {
            newMessageListener.incomeNewMessage(message.getBody());
        }

        QBCustomObject localResult = dialogReview(Integer.parseInt(matcher.group(0)));

        if (localResult != null) {
            updateDialogLastMessage(message.getBody(), localResult.getCustomObjectId());
        } else {
            if (tmpIdforReview == null && !tmpIdforReview.equals(matcher.group(0))) {
                tmpIdforReview = matcher.group(0);
                backgroundMessage = message.getBody();
                ((Activity) context).runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        app.getQbm().setUserProfileListener(MessageManager.this);
                        app.getQbm().getSingleUserInfo(opponentId);

                    }
                });
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

    // send messages into xmpp & customobject
    public void sendSingleMessage(Integer userId, String messageBody, String dialogId) {

        if (messageBody == null && dialogId == null && userId == null) {
            return;
        }

        QBChat.sendMessage(userId, messageBody);

        sendToQB(userId, messageBody, app.getQbUser().getId());
        updateDialogLastMessage(messageBody, dialogId);

    }

    private void sendToQB(Integer opponentID, String messageBody, Integer authorID) {
        message = messageBody;
        authorId = authorID;
        opponentId = opponentID;

        ((Activity) context).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                QBCustomObject custobj = new QBCustomObject();

                custobj.setClassName(GlobalConsts.MESSAGES);

                HashMap<String, Object> fields = new HashMap<String, Object>();

                fields.put(GlobalConsts.AUTHOR_ID, authorId);
                fields.put(GlobalConsts.OPPONENT_ID, opponentId);
                fields.put(GlobalConsts.MSG_TEXT, message);

                custobj.setFields(fields);

                QBCustomObjects.createObject(custobj, new QBCallbackImpl() {
                    @Override
                    public void onComplete(Result result) {
                        System.out.println("Сообщение отправлено на QB");
                    }
                });
            }

        });

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

                    if (isNeedDownloadUser) {
                        ArrayList<String> userIds = new ArrayList<String>();
                        for (QBCustomObject co : app.getDialogList()) {
                            if (!app.getDialogsUsers().containsKey(co.getFields().get(GlobalConsts.RECEPIENT_ID_FIELD).toString())) {

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

    public void updateDialogLastMessage(String lastMsg, String dialogId) {
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
}