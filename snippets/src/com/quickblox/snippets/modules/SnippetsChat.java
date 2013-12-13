package com.quickblox.snippets.modules;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;
import com.quickblox.core.QBCallbackImpl;
import com.quickblox.module.auth.QBAuth;
import com.quickblox.module.chat.QBChat;
import com.quickblox.module.chat.listeners.ChatMessageListener;
import com.quickblox.module.chat.listeners.LoginListener;
import com.quickblox.module.chat.listeners.RoomListener;
import com.quickblox.module.chat.model.QBChatRoom;
import com.quickblox.module.chat.smack.SmackAndroid;
import com.quickblox.module.chat.utils.QBChatUtils;
import com.quickblox.module.users.model.QBUser;
import com.quickblox.snippets.Snippet;
import com.quickblox.snippets.Snippets;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;

import java.util.Collection;

/**
 * User: Oleg Soroka
 * Date: 11.10.12
 * Time: 17:01
 */
public class SnippetsChat extends Snippets {

    public static final String ROOM_NAME = "temp_room_for_snippet_test";
    private Handler handler = new Handler(Looper.getMainLooper());

    //    private static final String TAG = SnippetsChat.class.getSimpleName();
    public static final int USER_ID = 999;
    public static final String TEST_PASSWORD = "AndroidGirl";

    private ChatMessageListener chatMessageListener;
    private final QBUser qbUser;
    private RoomListener roomReceivingListener;
    private QBChatRoom currentQBChatRoom;
    private PacketListener pMessageListener;

    public SnippetsChat(Context context) {
        super(context);
        SmackAndroid.init(context);

        // init test user
        qbUser = new QBUser();
        qbUser.setId(USER_ID);
        qbUser.setPassword(TEST_PASSWORD);

        initRoomListener();
        initRoomMessageListener();

        snippets.add(loginInChat);
        snippets.add(isLoggedIn);
        snippets.add(logoutFromChat);
        //
        snippets.add(sendPresence);
        snippets.add(startAutoSendPresence);
        snippets.add(addChatMessageListener);
        snippets.add(sendMessageWithStringParameter);
        snippets.add(sendMessageWithMessageParameter);
        snippets.add(createRoom);
        snippets.add(joinRoom);
        snippets.add(sendMessageToRoom);
        snippets.add(getOnlineRoomUsers);
        snippets.add(leaveRoom);
    }

    //
    ///////////////////////////////////////////// Login/Logout /////////////////////////////////////////////
    //
    Snippet loginInChat = new Snippet("login in Chat") {
        @Override
        public void execute() {
            QBChat.getInstance().loginWithUser(qbUser, new LoginListener() {
                @Override
                public void onLoginSuccess() {
                    System.out.println("success when login");
                    QBChat.getInstance().createRoom(ROOM_NAME, qbUser, false, true, roomReceivingListener);
                }

                @Override
                public void onLoginError() {
                    System.out.println("error when login");
                }
            });
        }
    };

    Snippet isLoggedIn = new Snippet("Is logged In") {
        @Override
        public void execute() {
            boolean isLoggedIn = QBChat.getInstance().isLoggedIn();
            System.out.println("isLoggedIn: " + isLoggedIn);
        }
    };

    Snippet logoutFromChat = new Snippet("Logout from Chat") {
        @Override
        public void execute() {
            QBChat.getInstance().logout();
        }
    };


    Snippet sendPresence = new Snippet("send presence") {
        @Override
        public void execute() {
            QBChat.getInstance().sendPresence();
        }
    };

    Snippet startAutoSendPresence = new Snippet("start auto send presence") {
        @Override
        public void execute() {
            int intervalInSecondsBetweenSending = 10;
            QBChat.getInstance().startAutoSendPresence(intervalInSecondsBetweenSending);
        }
    };

    Snippet addChatMessageListener = new Snippet("add chat message listener") {
        @Override
        public void execute() {
            chatMessageListener = new ChatMessageListener() {
                @Override
                public void processMessage(Message message) {
                    int userId = QBChatUtils.parseQBUser(message.getFrom());
                    String messageBody = message.getBody();
                    Message.Type type = message.getType();
                    String userChatLogin = QBChat.getInstance().getChatLoginFull(new QBUser(userId));
                    final String messageText = String.format("Received message from QuickBlox %s from user (full login: %s) with id %s:'%s'", type, userChatLogin, userId, messageBody);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(context, messageText, Toast.LENGTH_SHORT).show();
                        }
                    });
                    System.out.println(">>> " + messageText);
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
            QBChat.getInstance().addChatMessageListener(chatMessageListener);
        }
    };

    Snippet sendMessageWithStringParameter = new Snippet("send message with String parameter") {
        @Override
        public void execute() {
            QBChat.getInstance().sendMessage(USER_ID, "some message as String parameter");
        }
    };

    Snippet sendMessageWithMessageParameter = new Snippet("send message with Message parameter") {
        @Override
        public void execute() {
            Message message = new Message(QBChatUtils.getChatLoginFull(USER_ID), Message.Type.chat);
            String messageText = "some message as Message parameter";
            message.setBody(messageText);
            QBChat.getInstance().sendMessage(USER_ID, message);
        }
    };

    Snippet createRoom = new Snippet("create Room") {
        @Override
        public void execute() {
            QBChat.getInstance().createRoom(ROOM_NAME, qbUser, false, true, roomReceivingListener);
        }
    };

    Snippet joinRoom = new Snippet("join Room") {
        @Override
        public void execute() {
            QBChat.getInstance().joinRoom(ROOM_NAME, qbUser, roomReceivingListener);
        }
    };


    Snippet sendMessageToRoom = new Snippet("send message to room") {
        @Override
        public void execute() {
            try {
                currentQBChatRoom.sendMessage("message to room");
            } catch (XMPPException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
    };

    Snippet getOnlineRoomUsers = new Snippet("get Online Room users") {
        @Override
        public void execute() {
            Collection<Integer> onlineRoomUsers = null;
            try {
                onlineRoomUsers = currentQBChatRoom.getOnlineRoomUsers();
            } catch (XMPPException e) {
                System.out.println(e);
            }
            for (Integer id : onlineRoomUsers) {
                System.out.println("id: " + id);
            }
        }
    };

    Snippet leaveRoom = new Snippet("leave Room") {
        @Override
        public void execute() {
            QBChat.getInstance().leaveRoom(ROOM_NAME);
        }
    };

    private void initRoomListener() {
        roomReceivingListener = new RoomListener() {
            @Override
            public void onCreatedRoom(QBChatRoom qbChatRoom) {
                System.out.println("on Created Room listener");
                currentQBChatRoom = qbChatRoom;
                currentQBChatRoom.addMessageListener(pMessageListener);
            }

            @Override
            public void onJoinedRoom(QBChatRoom qbChatRoom) {
                System.out.println("on Joined Room listener");
                currentQBChatRoom = qbChatRoom;
                currentQBChatRoom.addMessageListener(pMessageListener);
            }
        };
    }

    private void initRoomMessageListener() {
        pMessageListener = new PacketListener() {
            @Override
            public void processPacket(Packet packet) {
                Message message = (Message) packet;
                System.out.println(">>>received message from room: " + message.getBody() + " " + message.getFrom());
            }
        };
    }
}