package com.quickblox.snippets.modules;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;
import com.quickblox.core.QBSettings;
import com.quickblox.internal.core.exception.BaseServiceException;
import com.quickblox.internal.core.helper.ToStringHelper;
import com.quickblox.module.auth.QBAuth;
import com.quickblox.module.chat.QBChatService;
import com.quickblox.module.chat.listeners.ChatMessageListener;
import com.quickblox.module.chat.listeners.RoomListener;
import com.quickblox.module.chat.listeners.RoomReceivingListener;
import com.quickblox.module.chat.listeners.SessionListener;
import com.quickblox.module.chat.model.QBChatRoster;
import com.quickblox.module.chat.smack.SmackAndroid;
import com.quickblox.module.chat.utils.QBChatUtils;
import com.quickblox.module.chat.QBChatRoom;
import com.quickblox.module.chat.xmpp.QBPrivateChat;
import com.quickblox.module.users.model.QBUser;
import com.quickblox.module.videochat.model.objects.MessageExtension;
import com.quickblox.snippets.Consts;
import com.quickblox.snippets.Snippet;
import com.quickblox.snippets.Snippets;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;

import java.util.*;

/**
 * User: Oleg Soroka
 * Date: 11.10.12
 * Time: 17:01
 */
public class SnippetsChat extends Snippets {

    private static final String TAG = SnippetsChat.class.getSimpleName();
    private Handler handler = new Handler(Looper.getMainLooper());

    // test Chat user credentials
    public static final int USER_ID = 999;
    public static final int SUBSCRIBE_USER_ID = 13163;
    public static final String TEST_PASSWORD = "AndroidGirl";
    public static final String TEST_PASSWORD_2 = "Gerrit";
    private final QBUser qbUser;

    // 1-1 Chat properties
    private QBPrivateChat qbPrivateChat;

    // Group Chat properties
    private RoomListener roomReceivingListener;
    private QBChatRoom currentQBChatRoom;
    public static final String ROOM_NAME = "temp_room_for_snippet";

    // Common properties
    private ChatMessageListener chatMessageListener;
    private PacketListener packetListener;
    private QBChatRoster qbChatRoster;

    public SnippetsChat(final Context context) {
        super(context);
        SmackAndroid.init(context);

        // init test user
        qbUser = new QBUser();
        qbUser.setId(USER_ID);
        qbUser.setPassword(TEST_PASSWORD);
        initRoomListener();
        initChatMessageListener();
        initNotMsgListener();
        snippets.add(loginInChat);
        snippets.add(isLoggedIn);
        snippets.add(logoutFromChat);
        snippets.add(createChat);
        //
        snippets.add(sendPresence);
        snippets.add(addSubscription);
        snippets.add(sendCustomPresence);
        snippets.add(sendPresenceWithStatus);
        snippets.add(startAutoSendPresence);
        snippets.add(stopAutoSendPresence);
        //
        snippets.add(sendMessageWithText);
        snippets.add(sendMessageWithMessage);
        snippets.add(sendMessageWithSaving);
        //
        snippets.add(createRoom);
        snippets.add(joinRoom);
        snippets.add(addUsersToRoom);
        snippets.add(removeUsersFromRoom);
        snippets.add(sendMessageToRoom);
        snippets.add(getOnlineRoomUsers);
        snippets.add(getRooms);
        snippets.add(leaveRoom);

//        // Add Chat message listener
        initChatMessageListener();
    }


    //
    ///////////////////////////////////////////// Login/Logout /////////////////////////////////////////////
    //


    Snippet loginInChat = new Snippet("login in Chat") {
        @Override
        public void execute() {

                    QBChatService.getInstance().loginWithUser(qbUser, new SessionListener() {
                        @Override
                        public void onLoginSuccess() {
                            Log.i(TAG, "success when login");

                            // Add Chat message listener
                            initChat();
                            initRoster();

                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(context, "Success when login", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }

                        @Override
                        public void onLoginError() {
                            Log.i(TAG, "error when login");

                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(context, "Error when login", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }

                        @Override
                        public void onDisconnect() {
                            Log.i(TAG, "You have been disconnected");

                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(context, "You have been disconnected", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }

                        @Override
                        public void onDisconnectOnError(Exception exc) {

                        }
                    });
        }

    };

    private void initRoster() {
        qbChatRoster = QBChatService.getInstance().registerRoster(new QBChatRoster.QBRosterListener() {
            @Override
            public void entriesDeleted(Collection<String> users) {

            }

            @Override
            public void entriesAdded(Collection<String> users) {
                //List<Integer> usersId = qbChatRoster.getUsersId();
                for (String s : users) {
                    Log.i(TAG, "roster added="+s);
                }
            }

            @Override
            public void entriesUpdated(Collection<String> users) {
                for (String s : users) {
                    Log.i(TAG, "roster updated="+s);
                }
            }

            @Override
            public void presenceChanged(Presence presence) {
                Log.i(TAG, "presence changed="+presence.getFrom() + " "+presence.getType());
            }
        });
    }

    private void initChat() {
        qbPrivateChat = QBChatService.getInstance().createChat();
        initChatMessageListener(qbPrivateChat);
        QBChatService.getInstance().addNotMessageListener(packetListener);
    }

    Snippet createChat = new Snippet("create 1 to 1 chat") {
        @Override
        public void execute() {
           initChat();
        }
    };

    Snippet isLoggedIn = new Snippet("Is logged In") {
        @Override
        public void execute() {
            boolean isLoggedIn = QBChatService.getInstance().isLoggedIn();
           Log.i(TAG, "isLoggedIn: " + isLoggedIn);
        }
    };

    Snippet logoutFromChat = new Snippet("Logout from Chat") {
        @Override
        public void execute() {
            QBChatService.getInstance().logout();
        }
    };


    //
    ///////////////////////////////////////////// Presence /////////////////////////////////////////////
    //
    Snippet sendPresence = new Snippet("send presence") {
        @Override
        public void execute() {
            QBChatService.getInstance().sendPresence();
        }
    };

    Snippet sendCustomPresence = new Snippet("send custom presence") {
        @Override
        public void execute() {
            Presence presence = new Presence(Presence.Type.subscribed);
            QBChatService.getInstance().sendPresenceToUser(presence, SUBSCRIBE_USER_ID);
        }
    };


    Snippet sendPresenceWithStatus = new Snippet("send presence with custom status") {
        String status = "Away";
        @Override
        public void execute() {
            QBChatService.getInstance().sendPresence(status);
        }
    };

    Snippet startAutoSendPresence = new Snippet("start auto sending presence") {
        @Override
        public void execute() {
            int intervalInSecondsBetweenSending = 10;
            QBChatService.getInstance().startAutoSendPresence(intervalInSecondsBetweenSending);
        }
    };

    Snippet stopAutoSendPresence = new Snippet("stop auto sending presence") {
        @Override
        public void execute() {
            QBChatService.getInstance().stopAutoSendPresence();
        }
    };

    Snippet addSubscription = new Snippet("add subscription") {
        @Override
        public void execute() {
            try {
                qbChatRoster.createEntry(SUBSCRIBE_USER_ID, "friend");
            } catch (XMPPException e) {
                e.printStackTrace();
            }
        }
    };



    //
    ///////////////////////////////////////////// 1-1 Chat /////////////////////////////////////////////
    //
    Snippet sendMessageWithText = new Snippet("send message with text") {
        @Override
        public void execute() {
            try {
                qbPrivateChat.sendMessage(USER_ID, "Hey man!");
            } catch (XMPPException e) {
                e.printStackTrace();
            }
        }
    };

    Snippet sendMessageWithMessage = new Snippet("send message with Message") {
        @Override
        public void execute() {
            Message message = new Message(QBChatUtils.getChatLoginFull(USER_ID), Message.Type.chat);
            message.setBody("Hey QuickBlox!");
            try {
                qbPrivateChat.sendMessage(USER_ID, message);
            } catch (XMPPException e) {
                e.printStackTrace();
            }
        }

    };


    // to use this feature you should login with user in QBApp
    Snippet sendMessageWithSaving = new Snippet("send message with saving in history") {
        @Override
        public void execute() {
            Map<String, Object> addinfoParams = new HashMap<String, Object>();
            addinfoParams.put(Consts.AGE, 22);
            addinfoParams.put(Consts.TYPE, "actor");
            final String BODY = "Hey QuickBlox!";
            Message message = createMsgWithAdditionalInfo(USER_ID, BODY, addinfoParams);
            Log.i(TAG, "message: " + message.toXML());
            try {
                qbPrivateChat.sendMessage(USER_ID, message);
            } catch (XMPPException e) {
                e.printStackTrace();
            }
        }
    };

    private Message createMsgWithAdditionalInfo(int userId, String body,  Map<?, ?> addinfoParams){
        Message message = new Message(QBChatUtils.getChatLoginFull(userId), Message.Type.chat);
        String addInfo = ToStringHelper.toString(addinfoParams, "", Consts.ESCAPED_AMPERSAND);
        MessageExtension messageExtension = new MessageExtension(Consts.QB_INFO, "");
        try {
            messageExtension.setValue(Consts.QBTOKEN, QBAuth.getBaseService().getToken());
            //messageExtension.setValue(Consts.CLASS_NAME, Consts.MESSAGES);
            messageExtension.setValue(Consts.ADDITIONAL_INFO, addInfo);
        } catch (BaseServiceException e) {
            e.printStackTrace();
        }
        message.addExtension(messageExtension);
        message.setBody(body);
        return message;
    }

    private void initChatMessageListener(QBPrivateChat qbPrivateChat) {
        qbPrivateChat.addChatMessageListener(chatMessageListener);
    }


    //
    ///////////////////////////////////////////// Group Chat /////////////////////////////////////////////
    //
    Snippet createRoom = new Snippet("create Room") {
        @Override
        public void execute() {
            QBChatService.getInstance().createRoom(ROOM_NAME, false, true, roomReceivingListener);
        }
    };

    Snippet joinRoom = new Snippet("join Room") {
        @Override
        public void execute() {
            QBChatService.getInstance().joinRoom(ROOM_NAME,  roomReceivingListener);
        }
    };


    Snippet sendMessageToRoom = new Snippet("send message to room") {
        @Override
        public void execute() {
            try {
                currentQBChatRoom.sendMessage("message to room");
            } catch (final XMPPException e) {
                e.printStackTrace();
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    };

    Snippet addUsersToRoom =  new Snippet("add users to Room") {
        @Override
        public void execute() {
            List<Integer> users = new ArrayList<Integer>();
            users.add(958); // user ced
            try {
                currentQBChatRoom.addRoomUsers(users);
            } catch (XMPPException e) {
                Log.i(TAG, e.getLocalizedMessage());
            }
        }
    };

    Snippet removeUsersFromRoom =  new Snippet("remove users from Room") {
        @Override
        public void execute() {
            List<Integer> users = new ArrayList<Integer>();
            users.add(958); // user ced
            try {
                currentQBChatRoom.removeRoomUsers(users);
            } catch (XMPPException e) {
                Log.i(TAG, e.getLocalizedMessage());
            }
        }
    };

    Snippet getOnlineRoomUsers = new Snippet("get Online Room users") {
        @Override
        public void execute() {
            Collection<String> onlineRoomUsers = null;
            try {
                onlineRoomUsers = currentQBChatRoom.getOnlineRoomUsers();
            } catch (XMPPException e) {
               Log.i(TAG, e.getLocalizedMessage());
            }
            for (String nickname : onlineRoomUsers) {
               Log.i(TAG, "nickname: " + nickname);
            }
        }
    };

    Snippet getRooms = new Snippet("get list of rooms") {
        @Override
        public void execute() {
            QBChatService.getInstance().getRooms(new RoomReceivingListener() {
                @Override
                public void onReceiveRooms(List<QBChatRoom> list) {
                    for (QBChatRoom room : list) {
                       Log.i(TAG, "room: " + room.getJid());
                    }
                }
            });
        }
    };

    Snippet leaveRoom = new Snippet("leave Room") {
        @Override
        public void execute() {
            try {
                QBChatService.getInstance().leaveRoom(currentQBChatRoom);
            } catch (final XMPPException e) {
                e.printStackTrace();
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    };

    private void initRoomListener() {
        roomReceivingListener = new RoomListener() {
            @Override
            public void onCreatedRoom(QBChatRoom qbChatRoom) {
               Log.i(TAG, "on Created Room listener");
                currentQBChatRoom = qbChatRoom;
                currentQBChatRoom.addMessageListener(chatMessageListener);
            }

            @Override
            public void onJoinedRoom(QBChatRoom qbChatRoom) {
               Log.i(TAG, "on Joined Room listener");
                currentQBChatRoom = qbChatRoom;
                currentQBChatRoom.addMessageListener(chatMessageListener);
            }

            @Override
            public void onError(String msg) {
               Log.i(TAG, "on join Room error listener");
            }
        };
    }

    private void initChatMessageListener() {
        // listener for room msg and chat 1to1 msg
        // You can define which msg will be accept in accept method
        chatMessageListener = new ChatMessageListener() {
            @Override
            public void processMessage(Message message) {
                String from = message.getFrom();
                String messageBody = message.getBody();
                String messageText;
                if (message.getType() == Message.Type.groupchat) {
                    String room = QBChatUtils.parseRoomName(from, QBSettings.getInstance().getApplicationId());
                    messageText = String.format("Received message from room %s:'%s'", room, messageBody);
                } else {
                    String userId = QBChatUtils.parseQBUser(from);
                    messageText = String.format("Received message from user %s:'%s'", userId, messageBody);
                }

                // Show message
                Toast.makeText(context, messageText, Toast.LENGTH_SHORT).show();

                Log.i(TAG, "processMessage >>> " + message.toString());
            }

            @Override
            public boolean accept(Message.Type type) {
                switch (type) {
                    case normal:
                    case chat:
                    case groupchat:
                        return true;
                    default:
                        return false;
                }
            }
        };
    }

    private void initNotMsgListener(){
        packetListener = new PacketListener(){

            @Override
            public void processPacket(Packet packet) {
                Log.i(TAG, "processPacket >>> " + packet.getFrom());
                if(packet instanceof Presence){
                    Presence presence = (Presence) packet;
                    Log.i(TAG, "processPresence >>> " +presence.getType());
                }
            }
        };
    }
}