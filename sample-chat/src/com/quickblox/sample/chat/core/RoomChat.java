package com.quickblox.sample.chat.core;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.quickblox.module.chat.QBChat;
import com.quickblox.module.chat.listeners.RoomListener;
import com.quickblox.module.chat.model.QBChatRoom;
import com.quickblox.module.chat.utils.QBChatUtils;
import com.quickblox.sample.chat.QuickbloxSampleChat;
import com.quickblox.sample.chat.model.ChatMessage;
import com.quickblox.sample.chat.ui.activities.ChatActivity;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;

import java.util.Calendar;
import java.util.Date;

public class RoomChat implements Chat {

    public static final String ROOM_NAME = "name";
    public static final String ROOM_ACTION = "action";
    private static final String TAG = RoomChat.class.getSimpleName();
    private Handler handler = new Handler(Looper.getMainLooper());
    private ChatActivity chatActivity;
    private QBChatRoom chatRoom;
    private RoomMessageListener roomMessageListener;
    private CustomRoomListener roomListener;

    public RoomChat(ChatActivity activity) {
        chatActivity = activity;
        roomListener = new CustomRoomListener();
        roomMessageListener = new RoomMessageListener();

        String chatRoomName = chatActivity.getIntent().getStringExtra(ROOM_NAME);
        RoomAction action = (RoomAction) chatActivity.getIntent().getSerializableExtra(ROOM_ACTION);

        switch (action) {
            case CREATE:
                create(chatRoomName);
                break;
            case JOIN:
                join(chatRoomName);
                break;
        }
    }

    @Override
    public void sendMessage(String message) {
        try {
            if (chatRoom != null) {
                chatRoom.sendMessage(message);
            } else {
                Toast.makeText(chatActivity, "Join unsuccessfull", Toast.LENGTH_LONG).show();
            }
        } catch (XMPPException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void release() {
        if (chatRoom != null) {
            chatRoom.leave();
        }
    }

    public void create(String roomName) {
        QBChat.getInstance().createRoom(roomName, false, true, roomListener);
    }

    public void join(String roomName) {
        QBChat.getInstance().joinRoom(roomName, roomListener);
    }

    public enum RoomAction {CREATE, JOIN}

    private class CustomRoomListener implements RoomListener {

        @Override
        public void onCreatedRoom(QBChatRoom room) {
            Log.d(TAG, "room was created");
            chatRoom = room;
            chatRoom.addMessageListener(roomMessageListener);
        }

        @Override
        public void onJoinedRoom(QBChatRoom room) {
            Log.d(TAG, "joined to room");
            chatRoom = room;
            chatRoom.addMessageListener(roomMessageListener);
        }

        @Override
        public void onError(String msg) {
            Log.d(TAG, "error joining to room");
        }
    }

    private class RoomMessageListener implements PacketListener {

        @Override
        public void processPacket(final Packet packet) {
            Log.d(TAG, "got message in room");
            final Message message = (Message) packet;
            Date time = QBChatUtils.parseTime(packet);
            if (time == null) {
                time = Calendar.getInstance().getTime();
            }
            // Show message
            final Date finalTime = time;
            handler.post(new Runnable() {
                @Override
                public void run() {
                    String from = packet.getFrom();
                    if (QuickbloxSampleChat.getInstance().getQbUser().getId() == QBChatUtils.parseQBRoomOccupant(from)) {
                        chatActivity.showMessage(new ChatMessage(message.getBody(), finalTime, true));
                    } else {
                        chatActivity.showMessage(new ChatMessage(message.getBody(), finalTime, false));
                    }
                }
            });
        }
    }
}
