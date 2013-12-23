package com.quickblox.snippets.modules;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;
import com.quickblox.core.QBCallbackImpl;
import com.quickblox.core.result.Result;
import com.quickblox.internal.core.exception.BaseServiceException;
import com.quickblox.internal.core.helper.ToStringHelper;
import com.quickblox.module.auth.QBAuth;
import com.quickblox.module.chat.QBChat;
import com.quickblox.module.chat.listeners.ChatMessageListener;
import com.quickblox.module.chat.listeners.RoomListener;
import com.quickblox.module.chat.listeners.RoomReceivingListener;
import com.quickblox.module.chat.listeners.SessionListener;
import com.quickblox.module.chat.model.QBChatRoom;
import com.quickblox.module.chat.smack.SmackAndroid;
import com.quickblox.module.chat.utils.QBChatUtils;
import com.quickblox.module.custom.QBCustomObjects;
import com.quickblox.module.custom.model.QBCustomObject;
import com.quickblox.module.users.model.QBUser;
import com.quickblox.module.videochat.model.objects.MessageExtension;
import com.quickblox.snippets.Consts;
import com.quickblox.snippets.Snippet;
import com.quickblox.snippets.Snippets;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    public static final String TEST_PASSWORD = "AndroidGirl";
    private final QBUser qbUser;

    // 1-1 Chat properties
    private ChatMessageListener chatMessageListener;

    // Group Chat properties
    private RoomListener roomReceivingListener;
    private QBChatRoom currentQBChatRoom;
    private PacketListener pMessageListener;
    public static final String ROOM_NAME = "quickblox";

    public SnippetsChat(final Context context) {
        super(context);
        SmackAndroid.init(context);
        initChatMessageListener();
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
        snippets.add(sendMessageToRoom);
        snippets.add(getOnlineRoomUsers);
        snippets.add(getRooms);
        snippets.add(leaveRoom);
    }


    //
    ///////////////////////////////////////////// Login/Logout /////////////////////////////////////////////
    //


    Snippet loginInChat = new Snippet("login in Chat") {
        @Override
        public void execute() {

                    QBChat.getInstance().loginWithUser(qbUser, new SessionListener() {
                        @Override
                        public void onLoginSuccess() {
                            System.out.println("success when login");

                            // Add Chat message listener
                            initChatMessageListener();

                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(context, "Success when login", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }

                        @Override
                        public void onLoginError() {
                            System.out.println("error when login");

                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(context, "Error when login", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }

                        @Override
                        public void onDisconnect() {
                            System.out.println("You have been disconnected");

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


    //
    ///////////////////////////////////////////// Presence /////////////////////////////////////////////
    //
    Snippet sendPresence = new Snippet("send presence") {
        @Override
        public void execute() {
            QBChat.getInstance().sendPresence();
        }
    };


    Snippet sendPresenceWithStatus = new Snippet("send presence") {
        String status = "";
        @Override
        public void execute() {
            QBChat.getInstance().sendPresence(status);
        }
    };

    Snippet startAutoSendPresence = new Snippet("start auto send presence") {
        @Override
        public void execute() {
            int intervalInSecondsBetweenSending = 10;
            QBChat.getInstance().startAutoSendPresence(intervalInSecondsBetweenSending);
        }
    };

    Snippet stopAutoSendPresence = new Snippet("stop auto send presence") {
        @Override
        public void execute() {
            QBChat.getInstance().stopAutoSendPresence();
        }
    };



    //
    ///////////////////////////////////////////// 1-1 Chat /////////////////////////////////////////////
    //
    Snippet sendMessageWithText = new Snippet("send message with text") {
        @Override
        public void execute() {
            QBChat.getInstance().sendMessage(USER_ID, "Hey man!");
        }
    };

    Snippet sendMessageWithMessage = new Snippet("send message with Message") {
        @Override
        public void execute() {
            Message message = new Message(QBChatUtils.getChatLoginFull(USER_ID), Message.Type.chat);
            message.setBody("Hey QuickBlox!");
            QBChat.getInstance().sendMessage(USER_ID, message);
        }
    };

    Snippet sendMessageWithSaving = new Snippet("send message with saving in history") {
        @Override
        public void execute() {
            Map<String, Object> addinfoParams = new HashMap<String, Object>();
            addinfoParams.put(Consts.AGE, 22);
            addinfoParams.put(Consts.TYPE, "actor");
            final String BODY = "Hey QuickBlox!";
            Message message = createMsgWithAdditionalInfo(USER_ID, BODY, addinfoParams);
            QBChat.getInstance().sendMessage(USER_ID, message);
            registerMsgOnServer(USER_ID, BODY, addinfoParams);
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

    private void registerMsgOnServer(int userId, String msg,  Map<String, Object> addParams){
        QBCustomObject custobj = new QBCustomObject();

        custobj.setClassName(Consts.MESSAGES);

        HashMap<String, Object> fields = new HashMap<String, Object>();

        fields.put(Consts.AUTHOR_ID, qbUser.getId());
        fields.put(Consts.OPPONENT_ID, userId);
        fields.put(Consts.MESSAGE, msg);
        fields.putAll(addParams);

        custobj.setFields(fields);
        QBCustomObjects.createObject(custobj, new QBCallbackImpl(){
            @Override
            public void onComplete(Result result) {
                if(result.isSuccess()){
                    Log.i(TAG, "Message stored in history");
                }
            }
        });
    }

    private void initChatMessageListener() {
        // Set 1-1 Chat message listener
        chatMessageListener = new ChatMessageListener() {
            @Override
            public void processMessage(Message message) {

                // get message params
                int userId = QBChatUtils.parseQBUser(message.getFrom());
                String messageBody = message.getBody();
                //
                final String messageText = String.format("Received message from user %s:'%s'", userId, messageBody);

                // Show message
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context, messageText, Toast.LENGTH_SHORT).show();
                    }
                });

                System.out.println("processMessage >>> " + message.toString());
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
        //
        QBChat.getInstance().initChat(chatMessageListener);
    }


    //
    ///////////////////////////////////////////// Group Chat /////////////////////////////////////////////
    //
    Snippet createRoom = new Snippet("create Room") {
        @Override
        public void execute() {
            QBChat.getInstance().createRoom(ROOM_NAME, false, true, roomReceivingListener);
        }
    };

    Snippet joinRoom = new Snippet("join Room") {
        @Override
        public void execute() {
            QBChat.getInstance().joinRoom(ROOM_NAME,  roomReceivingListener);
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

    Snippet getRooms = new Snippet("get list of rooms") {
        @Override
        public void execute() {
            QBChat.getInstance().getRooms(new RoomReceivingListener() {
                @Override
                public void onReceiveRooms(List<QBChatRoom> list) {
                    for (QBChatRoom room : list) {
                        System.out.println("room: " + room.getJid());
                    }
                }
            });
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

            @Override
            public void onError(String msg) {
                System.out.println("on join Room error listener");
            }
        };
    }

    private void initRoomMessageListener() {
        pMessageListener = new PacketListener() {
            @Override
            public void processPacket(Packet packet) {
                final Message message = (Message) packet;

                final String messageText = String.format("Received message in room, %s:'%s'", message.getFrom(), message.getBody());

                // Show message
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context, messageText, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        };
    }
}