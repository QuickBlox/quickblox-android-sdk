package com.quickblox.chat_v2.apis;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;

import com.quickblox.chat_v2.core.ChatApplication;
import com.quickblox.chat_v2.interfaces.OnMessageListDownloaded;
import com.quickblox.chat_v2.interfaces.OnNewRoomMessageIncome;
import com.quickblox.chat_v2.interfaces.OnPictureDownloadComplete;
import com.quickblox.chat_v2.interfaces.OnRoomListDownloaded;
import com.quickblox.chat_v2.utils.GlobalConsts;
import com.quickblox.core.QBCallbackImpl;
import com.quickblox.core.result.Result;
import com.quickblox.internal.module.custom.request.QBCustomObjectRequestBuilder;
import com.quickblox.module.custom.QBCustomObjects;
import com.quickblox.module.custom.model.QBCustomObject;
import com.quickblox.module.custom.result.QBCustomObjectLimitedResult;
import com.quickblox.module.custom.result.QBCustomObjectResult;
import com.quickblox.module.users.model.QBUser;

import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MessageFacade implements OnPictureDownloadComplete, PacketListener {

    private Context context;
    private ChatApplication app;

    private QBCustomObject customDialog;
    private ArrayList<String> messageQuery;
    private OnRoomListDownloaded roomListDownloadListener;
    private OnNewRoomMessageIncome mNewRoomMessageIncome;


    private SingleChatParts mParts;

    public MessageFacade(Context context) {
        this.context = context;
        app = ChatApplication.getInstance();

        messageQuery = new ArrayList<String>();
        mParts = new SingleChatParts();
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

    public void createRoom(String roomName, String roomJid, List<Integer> invaiteList) {

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

    public void setRoomListDownloadListener(OnRoomListDownloaded roomListDownloadListener) {
        this.roomListDownloadListener = roomListDownloadListener;
    }

    public void setmNewRoomMessageIncome(OnNewRoomMessageIncome mNewRoomMessageIncome) {
        this.mNewRoomMessageIncome = mNewRoomMessageIncome;
    }

    public void createDialog(QBUser pUser, boolean b) {
        mParts.createDialog(pUser, b);
    }

    public MessageListener getGlobalMessagesListener() {
        return mParts.getMessageListener();
    }

    public void sendSingleMessage(int pId, String pMessage, String pDialogId) {
        mParts.sendMessage(pId, pMessage, pDialogId);
    }

    public void updateDialogLastMessage(String pLastMsg, String pDialogId) {
        mParts.updateDialogLastMessage(pLastMsg, pDialogId);
    }

    public void getDialogMessages(int pIntParseUserId, OnMessageListDownloaded pDialogMessagesListDownloadedListener) {
        mParts.getDialogMessages(app.getQbUser().getId(), pIntParseUserId, pDialogMessagesListDownloadedListener);
    }

    public void getDialogs(boolean isNeed) {
        mParts.downloadDialogList(isNeed);
    }
}