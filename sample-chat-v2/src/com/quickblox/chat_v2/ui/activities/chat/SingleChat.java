package com.quickblox.chat_v2.ui.activities.chat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.net.Uri;
import android.view.View;
import android.widget.Toast;

import com.quickblox.chat_v2.R;
import com.quickblox.chat_v2.apis.MessageFacade;
import com.quickblox.chat_v2.core.ChatApplication;
import com.quickblox.chat_v2.interfaces.OnFileUploadComplete;
import com.quickblox.chat_v2.interfaces.OnMessageListDownloaded;
import com.quickblox.chat_v2.utils.GlobalConsts;
import com.quickblox.chat_v2.widget.TopBar;
import com.quickblox.module.custom.model.QBCustomObject;
import com.quickblox.module.users.model.QBUser;

import java.io.FileNotFoundException;
import java.util.List;

/**
 * Created by andrey on 05.07.13.
 */
public class SingleChat implements Chat, OnFileUploadComplete {

    private final ChatApplication app;

    private UIChat mChat;

    private String userId;

    private QBUser opponentUser;

    private String dialogFreezingStatus;

    private MessageFacade msgManager;

    private String dialogId;

    private String lastMsg;


    private BroadcastReceiver mainReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context pContext, Intent pIntent) {
            String action = pIntent.getAction();
            if (action.equalsIgnoreCase(GlobalConsts.DIALOG_CREATED_ACTION)) {
                dialogCreated(pIntent.getIntExtra(GlobalConsts.OPPONENT_ID, 0));
            } else if (action.equalsIgnoreCase(GlobalConsts.INCOMING_MESSAGE_ACTION)) {
                String messageBody = pIntent.getStringExtra(GlobalConsts.EXTRA_MESSAGE);
                lastMsg = messageBody;
                int messageAuthorId = pIntent.getIntExtra(GlobalConsts.OPPONENT_ID, 0);
                if (messageAuthorId == opponentUser.getId()) {
                    mChat.showMessage(messageBody, false);
                }
            }
        }
    };

    private OnMessageListDownloaded dialogMessagesListDownloadedListener = new OnMessageListDownloaded() {
        @Override
        public void messageListDownloaded(List<QBCustomObject> downloadedList) {
            for (QBCustomObject customObject : downloadedList) {
                int userId = Integer.parseInt(customObject.getFields().get("author_id").toString());
                String message = customObject.getFields().get(GlobalConsts.MSG_TEXT).toString();
                mChat.showMessage(message, userId == app.getQbUser().getId());
            }
        }
    };

    public SingleChat(UIChat pChat, boolean inContacts) {
        mChat = pChat;

        app = ChatApplication.getInstance();

        msgManager = app.getMsgManager();

        userId = mChat.getIntent().getStringExtra(GlobalConsts.USER_ID);

        if (inContacts) {

            opponentUser = app.getContactsMap().get(userId);

            if (app.getUserIdDialogIdMap().get(opponentUser.getId()) != null) {
                dialogId = app.getUserIdDialogIdMap().get(opponentUser.getId()).getCustomObjectId();
            }
        } else {
            dialogId = mChat.getIntent().getStringExtra(GlobalConsts.DIALOG_ID);
            opponentUser = app.getDialogsUsersMap().get(userId);
        }

        mChat.setTopBarFriendParams(opponentUser, app.getContactsMap().containsKey(userId));
        mChat.setTopBarParams(TopBar.CHAT_ACTIVITY, View.VISIBLE, true);

        int intParseUserId = Integer.parseInt(userId);


        msgManager.getDialogMessages(intParseUserId, dialogMessagesListDownloadedListener);

        mChat.setBarTitle(opponentUser.getFullName() != null ? opponentUser.getFullName() : opponentUser.getLogin());

    }

    @Override
    public void sendMessage(String message) {
        lastMsg = message;
        //mChat.showMessage(lastMsg, true);
        if (dialogId == null && dialogFreezingStatus == null) {
            msgManager.createDialog(opponentUser, false);
            dialogFreezingStatus = "processed";
        }

        msgManager.sendSingleMessage(opponentUser.getId(), message, dialogId);

    }

    @Override
    public void release() {
        msgManager.updateDialogLastMessage(lastMsg, dialogId);
    }


    @Override
    public void uploadComplete(int uploafFileId, String picUrl) {
        String serviceMessage = "<Attach file>#" + picUrl;
        msgManager.sendSingleMessage(opponentUser.getId(), serviceMessage, dialogId);
        mChat.showMessage(serviceMessage, true);
        mChat.changeUploadState(false);
    }

    public void dialogCreated(int userId) {
        dialogId = String.valueOf(userId);
        dialogFreezingStatus = null;
        if (lastMsg != null) {
            mChat.showMessage(lastMsg, true);
        }
    }


    public void uploadAttach(Uri data) {
        try {

            Toast.makeText(mChat.getContext(), R.string.chat_activity_attach_info, Toast.LENGTH_LONG).show();

            mChat.changeUploadState(true);

            app.getQbm().setUploadListener(this);
            Bitmap yourSelectedImage = app.getPicManager().decodeUri(data);
            app.getQbm().uploadPic(app.getPicManager().convertBitmapToFile(yourSelectedImage), true);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void registerListeners() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(GlobalConsts.DIALOG_CREATED_ACTION);
        intentFilter.addAction(GlobalConsts.INCOMING_MESSAGE_ACTION);
        mChat.getContext().registerReceiver(mainReceiver, intentFilter);
    }

    @Override
    public void unRegisterListeners() {
        mChat.getContext().unregisterReceiver(mainReceiver);
    }
}
