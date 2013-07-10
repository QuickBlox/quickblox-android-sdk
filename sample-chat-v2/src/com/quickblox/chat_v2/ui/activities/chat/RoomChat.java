package com.quickblox.chat_v2.ui.activities.chat;

import android.view.View;
import android.widget.Toast;

import com.quickblox.chat_v2.R;
import com.quickblox.chat_v2.core.ChatApplication;
import com.quickblox.chat_v2.interfaces.OnNewRoomMessageIncome;
import com.quickblox.chat_v2.utils.GlobalConsts;
import com.quickblox.chat_v2.widget.TopBar;
import com.quickblox.module.chat.QBChat;
import com.quickblox.module.chat.listeners.RoomListener;
import com.quickblox.module.chat.model.QBChatRoom;

import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smackx.muc.InvitationListener;

import java.util.ArrayList;

/**
 * Created by andrey on 05.07.13.
 */
public class RoomChat implements OnNewRoomMessageIncome, Chat {

    private final ChatApplication app;
    private UIChat mChat;

    private QBChatRoom chatRoom;

    public RoomChat(UIChat pChat) {
        mChat = pChat;
        app = ChatApplication.getInstance();

        QBChat.getInstance().startWatchRoom(pInvitationListener);

        String chatRoomName = mChat.getIntent().getStringExtra(GlobalConsts.ROOM_NAME);

        join(chatRoomName);

        app.getMsgManager().setmNewRoomMessageIncome(this);


        mChat.setBarTitle(chatRoomName);

        mChat.setTopBarParams(TopBar.ROOM_ACTIVITY, View.INVISIBLE, false);
    }


    private InvitationListener pInvitationListener = new InvitationListener() {
        @Override
        public void invitationReceived(Connection connection, String s, String s2, String s3, String s4, Message message) {
            if (s != null) {

            }
        }
    };

    private PacketListener pParticipantListener = new PacketListener() {
        @Override
        public void processPacket(Packet packet) {

        }
    };


    public void sendMessage(String pMessage) {
        try {
            if (chatRoom != null) {
                chatRoom.sendMessage(pMessage);
            } else {
                Toast.makeText(mChat.getContext(), R.string.room_join_fall, Toast.LENGTH_LONG).show();
            }
        } catch (XMPPException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(String pMessage, boolean pIsDelivered, boolean pIsComposed) {
        try {
            if (chatRoom != null) {
                chatRoom.sendMessage(pMessage, pIsDelivered, pIsComposed);
            } else {
                Toast.makeText(mChat.getContext(), R.string.room_join_fall, Toast.LENGTH_LONG).show();
            }
        } catch (XMPPException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void release() {
        app.getMsgManager().downloadPersistentRoom();
        if (chatRoom != null) {
            chatRoom.removeMessageListener(app.getMsgManager());
            chatRoom.leave();
            app.setJoinedRoom(null);
        }
    }

    @Override
    public void registerListeners() {

    }

    @Override
    public void unRegisterListeners() {

    }

    @Override
    public void incomeMessagePool(final ArrayList<String> messagesPool) {
        mChat.showMessages(messagesPool);
    }


    public void join(String roomName) {
        QBChat.getInstance().joinRoom(roomName, app.getQbUser(), mRoomListener);
    }

    private RoomListener mRoomListener = new RoomListener() {
        @Override
        public void onCreatedRoom(QBChatRoom pRoom) {
            pRoom.setmRoomReceivingListener(null);
        }

        @Override
        public void onJoinedRoom(QBChatRoom pRoom) {
            chatRoom = pRoom;
            chatRoom.addMessageListener(app.getMsgManager());
            chatRoom.addParticipantListener(pParticipantListener);
            app.setJoinedRoom(chatRoom);
            pRoom.setmRoomReceivingListener(null);
        }
    };
}
