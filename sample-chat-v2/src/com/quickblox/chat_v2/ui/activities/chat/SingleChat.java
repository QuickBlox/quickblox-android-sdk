package com.quickblox.chat_v2.ui.activities.chat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;
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
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Created by andrey on 05.07.13.
 */
public class SingleChat implements Chat, OnFileUploadComplete {

    private final ChatApplication app;

    private UIChat mChat;

    private int userId;

    private QBUser opponentUser;

    private boolean dialogFreezingStatus;

    private MessageFacade msgManager;

    private String dialogId;

    private String lastMsg;

    private Queue<String> messages = new LinkedList<String>();

    private BroadcastReceiver mainReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context pContext, Intent pIntent) {
            String action = pIntent.getAction();
            if (action.equalsIgnoreCase(GlobalConsts.DIALOG_CREATED_ACTION)) {

                String dialogId = pIntent.getStringExtra(GlobalConsts.DIALOG_ID);
                dialogCreated(dialogId);

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
                int userId = Integer.parseInt(customObject.getFields().get(GlobalConsts.AUTHOR_ID).toString());
                String message = customObject.getFields().get(GlobalConsts.MESSAGE).toString();
                mChat.showMessage(message, userId == app.getQbUser().getId());
            }
        }
    };

    public SingleChat(UIChat pChat, boolean inContacts) {
        mChat = pChat;

        app = ChatApplication.getInstance();

        msgManager = app.getMsgManager();

        userId = mChat.getIntent().getIntExtra(GlobalConsts.USER_ID, 0);

        if (inContacts) {

            opponentUser = app.getContactsMap().get(userId);
            QBCustomObject co = app.getUserIdDialogIdMap().get(userId);
            if (co != null) {
                dialogId = co.getCustomObjectId();
            }
        } else {
            dialogId = mChat.getIntent().getStringExtra(GlobalConsts.DIALOG_ID);
            opponentUser = app.getDialogsUsersMap().get(userId);
        }

        mChat.setTopBarFriendParams(opponentUser, app.getContactsMap().containsKey(userId));
        mChat.setTopBarParams(TopBar.CHAT_ACTIVITY, View.VISIBLE, true);

        msgManager.getDialogMessages(userId, dialogMessagesListDownloadedListener);

        if(opponentUser != null){
             Log.e("opponentUser", "opponentUser nil");
            mChat.setBarTitle(opponentUser.getFullName() != null ? opponentUser.getFullName() : opponentUser.getLogin());
        }


    }

    @Override
    public void sendMessage(String message) {
        lastMsg = message;
        //mChat.showMessage(lastMsg, true);
        if (dialogId == null && !dialogFreezingStatus) {
            msgManager.createDialog(opponentUser, true);
            dialogFreezingStatus = true;
            messages.add(message);
            return;
        }

        msgManager.sendSingleMessage(opponentUser.getId(), message, dialogId);
    }

    @Override
    public void release() {
    }


    @Override
    public void uploadComplete(int uploafFileId, String picUrl) {
        String serviceMessage = "<Attach file>#" + picUrl;
        msgManager.sendSingleMessage(opponentUser.getId(), serviceMessage, dialogId);
        mChat.showMessage(serviceMessage, true);
        mChat.changeUploadState(false);
    }

    public void dialogCreated(String pDialogId) {

        dialogId = pDialogId;

        dialogFreezingStatus = false;
        while (!messages.isEmpty()) {
            msgManager.sendSingleMessage(opponentUser.getId(), messages.poll(), dialogId);
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
