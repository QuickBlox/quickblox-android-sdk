package com.sdk.snippets.modules;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBMessageStatusesManager;
import com.quickblox.chat.QBPingManager;
import com.quickblox.chat.QBPrivacyListsManager;
import com.quickblox.chat.QBRestChatService;
import com.quickblox.chat.QBRoster;
import com.quickblox.chat.QBSystemMessagesManager;
import com.quickblox.chat.exception.QBChatException;
import com.quickblox.chat.listeners.QBChatDialogMessageListener;
import com.quickblox.chat.listeners.QBChatDialogMessageSentListener;
import com.quickblox.chat.listeners.QBChatDialogParticipantListener;
import com.quickblox.chat.listeners.QBChatDialogTypingListener;
import com.quickblox.chat.listeners.QBMessageStatusListener;
import com.quickblox.chat.listeners.QBPrivacyListListener;
import com.quickblox.chat.listeners.QBRosterListener;
import com.quickblox.chat.listeners.QBSubscriptionListener;
import com.quickblox.chat.listeners.QBSystemMessageListener;
import com.quickblox.chat.model.QBAttachment;
import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.chat.model.QBDialogCustomData;
import com.quickblox.chat.model.QBDialogType;
import com.quickblox.chat.model.QBPresence;
import com.quickblox.chat.model.QBPrivacyList;
import com.quickblox.chat.model.QBPrivacyListItem;
import com.quickblox.chat.model.QBRosterEntry;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.helper.StringifyArrayList;
import com.quickblox.core.request.QBRequestGetBuilder;
import com.quickblox.core.request.QBRequestUpdateBuilder;
import com.quickblox.core.request.QueryRule;
import com.quickblox.users.model.QBUser;
import com.sdk.snippets.core.ApplicationConfig;
import com.sdk.snippets.core.Snippet;
import com.sdk.snippets.core.SnippetAsync;
import com.sdk.snippets.core.Snippets;

import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.muc.DiscussionHistory;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * User: Igor Khomenko
 * Date: 1.07.14
 */
public class SnippetsChat extends Snippets {

    private static final String TAG = SnippetsChat.class.getSimpleName();

    // Chat service
    //
    private QBChatService chatService;


    //Chat API 3.0
    private QBChatDialog privateChatDialog;
    private QBChatDialog groupChatDialog;

    private QBChatDialogMessageListener privateChatDialogMessageListener;
    private QBChatDialogMessageListener groupChatDialogMessageListener;

    private QBChatDialogMessageSentListener privateChatDialogMessageSentListener;
    private QBChatDialogMessageSentListener groupChatDialogMessageSentListener;

    private QBChatDialogTypingListener privateChatDialogTypingListener;
    private QBChatDialogTypingListener groupChatDialogTypingListener;

    private QBChatDialogMessageListener globalChatDialogMessageListener;

    // Message statuses
    //
    private QBMessageStatusesManager messageStatusesManager;
    private QBMessageStatusListener messageStatusListener;


    // System messages
    //
    private QBSystemMessagesManager systemMessagesManager;
    private QBSystemMessageListener systemMessageListener;


    private QBChatDialogParticipantListener participantListener;


    // Roster
    //
    private QBRoster сhatRoster;
    private QBRosterListener rosterListener;
    private QBSubscriptionListener subscriptionListener;


    // Privacy lists
    //
    private QBPrivacyListsManager privacyListsManager;
    private QBPrivacyListListener privacyListListener;


    public SnippetsChat(final Context context) {
        super(context);

        // Init Chat service
        initChatService();

        // Init 1-1 listeners
        initPrivateChatMessageListener();
        initIsTypingListener();

        initMessageSentListener();

        // Init Group listeners
        initGroupChatMessageListener();
        initParticipantListener();

        // Init Roster and its listeners
        initRosterListener();
        initSubscriptionListener();

        //
        snippets.add(loginInChat);
        snippets.add(loginInChatSynchronous);
        //
        snippets.add(isLoggedIn);
        //
        snippets.add(logoutFromChat);
        snippets.add(logoutFromChatSynchronous);
        //
        //
        snippets.add(enableCarbons);
        snippets.add(disableCarbons);
        snippets.add(getCarbonsEnabled);
        //
        //
        snippets.add(sendPrivateMessageExtended);
        //
        //
        snippets.add(sendIsTypingInPrivateChat);
        snippets.add(sendStopTypingInPrivateChat);
        snippets.add(sendIsTypingInGroupChat);
        snippets.add(sendStopTypingInGroupChat);
        //
        //
        snippets.add(readMessagePrivateChat);
        snippets.add(deliverMessagePrivateChat);
        snippets.add(readMessageGroupChat);
        snippets.add(deliverMessageGroupChat);
        //
        //
        snippets.add(joinRoom);
        snippets.add(joinRoomSynchronous);
        snippets.add(sendMessageToRoomExtended);
        snippets.add(sendMessageToRoomWithoutJoin);
        snippets.add(getOnlineRoomUsersSynchronous);
        snippets.add(leaveRoom);
        //
        //
        snippets.add(getDialogs);
        snippets.add(getDialogsSynchronous);
        snippets.add(getDialogsCount);
        snippets.add(getDialogsCountSynchronous);
        snippets.add(createGroupDialog);
        snippets.add(createGroupDialogSynchronous);

        snippets.add(createPrivateDialog);
        snippets.add(createPrivateDialogSynchronous);

        snippets.add(updateDialog);
        snippets.add(updateDialogSynchronous);
        snippets.add(deleteDialog);
        snippets.add(deleteDialogSynchronous);
        //
        snippets.add(getMessages);
        snippets.add(getMessagesSynchronous);
        snippets.add(markMessagesAsRead);
        snippets.add(markMessagesAsReadSynchronous);
        snippets.add(deleteMessages);
        snippets.add(deleteMessagesSynchronous);
        snippets.add(createMessage);
        snippets.add(createMessageSynchronous);
        //
        //
        snippets.add(sendPresence);
        snippets.add(getRosterUsers);
        snippets.add(getUserPresence);
        snippets.add(addUserToRoster);
        snippets.add(removeUserFromRoster);
        snippets.add(confirmAddRequest);
        snippets.add(rejectAddRequest);
        //
        //
        snippets.add(getPrivacyLists);
        snippets.add(getPrivacyList);
        snippets.add(setPrivacyList);
        snippets.add(deletePrivacyList);
        snippets.add(setDefaultPrivacyList);
        //
        //
        snippets.add(enterInactiveState);
        snippets.add(enterActiveState);
        //
        //
        snippets.add(pingServer);
        snippets.add(pingServerSynchronous);
        //
        //
        snippets.add(sendSystemMessage);
    }

    private void initChatService() {
        QBChatService.ConfigurationBuilder configurationBuilder = new QBChatService.ConfigurationBuilder();
        configurationBuilder.setKeepAlive(true)
                .setSocketTimeout(0);
        QBChatService.setConfigurationBuilder(configurationBuilder);
        QBChatService.setDebugEnabled(true);
        QBChatService.setDefaultPacketReplyTimeout(10000);

        chatService = QBChatService.getInstance();
        chatService.addConnectionListener(chatConnectionListener);

        // stream management
        chatService.setUseStreamManagement(true);
//        chatService.setUseStreamManagementResumption(true);
    }


    private void initMessageSentListener() {
        privateChatDialogMessageSentListener = new QBChatDialogMessageSentListener() {
            @Override
            public void processMessageSent(String dialogId, QBChatMessage qbChatMessage) {
                log("message " + qbChatMessage.getId() + " sent to dialog " + dialogId);
            }

            @Override
            public void processMessageFailed(String dialogId, QBChatMessage qbChatMessage) {
                log("send message " + qbChatMessage.getId() + " has failed to dialog " + dialogId);
            }
        };

        groupChatDialogMessageSentListener = new QBChatDialogMessageSentListener() {
            @Override
            public void processMessageSent(String dialogId, QBChatMessage qbChatMessage) {
                log("message " + qbChatMessage.getId() + " sent to dialog " + dialogId);
            }

            @Override
            public void processMessageFailed(String dialogId, QBChatMessage qbChatMessage) {
                log("send message " + qbChatMessage.getId() + " has failed to dialog " + dialogId);
            }
        };
    }

    private void initMessageStatusManagerAndListener() {
        messageStatusesManager = chatService.getMessageStatusesManager();

        messageStatusListener = new QBMessageStatusListener() {
            @Override
            public void processMessageDelivered(String messageId, String dialogId, Integer userId) {
                log("message delivered: " + messageId + " to user " + userId
                        + ". DialogId: " + dialogId);
            }

            @Override
            public void processMessageRead(String messageId, String dialogId, Integer userId) {
                log("message read: " + messageId + " to user " + userId
                        + ". DialogId: " + dialogId);
            }
        };

        messageStatusesManager.addMessageStatusListener(messageStatusListener);
    }

    private void initGlobalMessageListener() {
        globalChatDialogMessageListener = new QBChatDialogMessageListener() {
            @Override
            public void processMessage(String dialogId, QBChatMessage qbChatMessage, Integer senderId) {
                QBRestChatService.getChatDialogById(dialogId).performAsync(new QBEntityCallback<QBChatDialog>() {
                    @Override
                    public void onSuccess(QBChatDialog chatDialog, Bundle bundle) {
                        log(chatDialog.getType().toString() +  " chat with id " + chatDialog.getDialogId() + " downloaded");
                        if (QBDialogType.PRIVATE.equals(chatDialog.getType())){
                            privateChatDialog = chatDialog;
                            privateChatDialog.addMessageListener(privateChatDialogMessageListener);
                            privateChatDialog.addMessageSentListener(privateChatDialogMessageSentListener);
                            privateChatDialog.addIsTypingListener(privateChatDialogTypingListener);
                        } else {
                            groupChatDialog = chatDialog;
                            groupChatDialog.addMessageListener(groupChatDialogMessageListener);
                            groupChatDialog.addMessageSentListener(groupChatDialogMessageSentListener);
                            groupChatDialog.addIsTypingListener(groupChatDialogTypingListener);
                        }
                    }

                    @Override
                    public void onError(QBResponseException e) {
                        log("Error loading dialog from global message listener: " + e);
                    }
                });
            }

            @Override
            public void processError(String dialogId, QBChatException e, QBChatMessage qbChatMessage, Integer senderId) {
                log("Received error message to message listener");
            }
        };

        QBChatService.getInstance().getIncomingMessagesManager().addDialogMessageListener(globalChatDialogMessageListener);
    }

    //
    ///////////////////////////////////////////// Login/Logout /////////////////////////////////////////////
    //


    Snippet loginInChat = new Snippet("login to Chat") {
        @Override
        public void execute() {

            // Init Chat service
//            initChatService();

            // init test user
            final QBUser qbUser = new QBUser();
            qbUser.setId(ApplicationConfig.getInstance().getTestUserId1());
            qbUser.setPassword(ApplicationConfig.getInstance().getTestUserPassword1());

            chatService.login(qbUser, new QBEntityCallback<Void>() {
                @Override
                public void onSuccess(Void result, Bundle bundle) {

                    log("success when login");

                    initGlobalMessageListener();

//                    // Add Chat message listener
                    initRoster();

                    initPrivacyListsManager();
                    initPrivacyListsListener();

                    initMessageStatusManagerAndListener();
                    initSystemMessagesManagerAndListener();
                }

                @Override
                public void onError(QBResponseException errors) {
                    log("error when login: " + errors);
                }
            });
        }
    };

    Snippet loginInChatSynchronous = new SnippetAsync("login to Chat (synchronous)", context) {
        @Override
        public void executeAsync() {

            // Init Chat service
//            initChatService();

            // init test user
            QBUser qbUser = new QBUser();
            qbUser.setId(ApplicationConfig.getInstance().getTestUserId1());
            qbUser.setPassword(ApplicationConfig.getInstance().getTestUserPassword1());

            log("login with user: " + qbUser.getId());

            if (!chatService.isLoggedIn()) {

                try {
                    chatService.login(qbUser);

                } catch (SmackException | IOException | XMPPException e) {
                    setException(e);
                }
            }
        }

        @Override
        protected void postExecute() {
            super.postExecute();
            final Exception exc = getException();

            if (exc == null) {
                initGlobalMessageListener();

                log("success when login");

                initRoster();

                initPrivacyListsManager();
                initPrivacyListsListener();

                initMessageStatusManagerAndListener();
                initSystemMessagesManagerAndListener();
            } else {
                log("error when login: " + exc.getClass().getSimpleName());
            }
        }
    };

    Snippet isLoggedIn = new Snippet("Is logged In") {
        @Override
        public void execute() {
            boolean isLoggedIn = chatService.isLoggedIn();

            log("isLoggedIn:" + isLoggedIn);
        }
    };

    Snippet logoutFromChat = new Snippet("Logout from Chat") {
        @Override
        public void execute() {
            chatService.logout(new QBEntityCallback<Void>() {

                @Override
                public void onSuccess(Void result, Bundle bundle) {
                    log("Logout success");

//                    chatService.destroy();
                }

                @Override
                public void onError(final QBResponseException list) {
                    log("Logout error:" + list);
                }
            });
        }
    };

    Snippet logoutFromChatSynchronous = new SnippetAsync("Logout from Chat (synchronous)", context) {
        @Override
        public void executeAsync() {
            try {
                chatService.logout();
                //
//                chatService.destroy();
            } catch (SmackException.NotConnectedException e) {
                setException(e);
            }
        }

        @Override
        protected void postExecute() {
            super.postExecute();
            final Exception exc = getException();

            if (exc == null) {
                log("Logout success");
            } else {
                log("Logout error: " + exc.getClass().getSimpleName());
            }
        }
    };

    ConnectionListener chatConnectionListener = new ConnectionListener() {
        @Override
        public void connected(XMPPConnection connection) {
            log("connected");
        }

        @Override
        public void authenticated(XMPPConnection connection, boolean authenticated) {
            log("authenticated");
        }

        @Override
        public void connectionClosed() {
            log("connectionClosed");
        }

        @Override
        public void connectionClosedOnError(final Exception e) {
            log("connectionClosedOnError: " + e.getLocalizedMessage());
            log("isLoggedIn: " + chatService.isLoggedIn());

        }

        @Override
        public void reconnectingIn(final int seconds) {
            if (seconds % 5 == 0) {
                log("reconnectingIn: " + seconds);
            }
        }

        @Override
        public void reconnectionSuccessful() {
            log("reconnectionSuccessful");
        }

        @Override
        public void reconnectionFailed(final Exception error) {
            log("reconnectionFailed: " + error.getLocalizedMessage());
        }
    };


    //
    ////////////////////////////////////////// Carbons /////////////////////////////////////////////
    //

    Snippet enableCarbons = new Snippet("enable carbons") {
        @Override
        public void execute() {
            if (!chatService.isLoggedIn()) {
                log("Please login first");
                return;
            }

            try {
                chatService.enableCarbons();
            } catch (XMPPException e) {
                log("enable carbons error: " + e.getLocalizedMessage());
            } catch (SmackException e) {
                log("enable carbons error: " + e.getClass().getSimpleName());
            }
        }
    };

    Snippet disableCarbons = new Snippet("disable carbons") {
        @Override
        public void execute() {
            if (!chatService.isLoggedIn()) {
                log("Please login first");
                return;
            }

            try {
                chatService.disableCarbons();
            } catch (XMPPException e) {
                log("disable carbons error: " + e.getLocalizedMessage());
            } catch (SmackException e) {
                log("disable carbons error: " + e.getClass().getSimpleName());
            }

        }
    };

    Snippet getCarbonsEnabled = new Snippet("get carbons enabled") {
        @Override
        public void execute() {
            if (!chatService.isLoggedIn()) {
                log("Please login first");
                return;
            }

            boolean isEnabled = chatService.getCarbonsEnabled();
            log("carbons enabled: " + isEnabled);
        }
    };


    //
    ///////////////////////////////////////////// 1-1 Chat /////////////////////////////////////////////
    //


    private void initPrivateChatMessageListener() {
        // Create 1-1 chat is message listener
        //
        privateChatDialogMessageListener = new QBChatDialogMessageListener() {
            @Override
            public void processMessage(String s, QBChatMessage qbChatMessage, Integer integer) {
                log("received message: " + qbChatMessage.getId());

                if (qbChatMessage.getSenderId().equals(chatService.getUser().getId())) {
                    log("Message comes here from carbons");
                }
            }

            @Override
            public void processError(String s, QBChatException e, QBChatMessage qbChatMessage, Integer integer) {
                log("processError: " + e.getLocalizedMessage());
            }
        };
    }

    private void initIsTypingListener() {
//
//        // Create 'is typing' listener
//        //
        groupChatDialogTypingListener = new QBChatDialogTypingListener() {
            @Override
            public void processUserIsTyping(String dialogId, Integer senderId) {
                log("user " + senderId + " is typing. Group dialog id: " + dialogId);
            }

            @Override
            public void processUserStopTyping(String dialogId, Integer senderId) {
                log("user " + senderId + " stop typing. Group dialog id: " + dialogId);
            }
        };

        privateChatDialogTypingListener = new QBChatDialogTypingListener() {
            @Override
            public void processUserIsTyping(String dialogId, Integer senderId) {
                log("user " + senderId + " is typing. Private dialog id: " + dialogId);
            }

            @Override
            public void processUserStopTyping(String dialogId, Integer senderId) {
                log("user " + senderId + " stop typing. Private dialog id: " + dialogId);
            }
        };
    }

    private void initSystemMessagesManagerAndListener() {
        systemMessagesManager = chatService.getSystemMessagesManager();
        systemMessageListener = new QBSystemMessageListener() {
            @Override
            public void processMessage(QBChatMessage qbChatMessage) {
                log("process System Message: " + qbChatMessage);
            }

            @Override
            public void processError(QBChatException e, QBChatMessage qbChatMessage) {
                log("process System Message error: " + e);
            }
        };
        systemMessagesManager.addSystemMessageListener(systemMessageListener);
    }

    Snippet sendPrivateMessageExtended = new Snippet("send private message") {
        @Override
        public void execute() {
            if (!chatService.isLoggedIn()) {
                log("Please login first");
                return;
            } else if (privateChatDialog == null){
                log("Please create private dialog first");
                return;
            }

            // create a message
            final QBChatMessage chatMessage = new QBChatMessage();
            chatMessage.setBody("Hey " + new Random().nextInt());
            chatMessage.setProperty("name", "bob");
            chatMessage.setProperty("lastname", "boblast");
            chatMessage.setSaveToHistory(true);
            chatMessage.setMarkable(true);

//                long time = System.currentTimeMillis()/1000;
//                chatMessage.setProperty("date_sent", time + ".431");

            // attach a photo
            QBAttachment attachment = new QBAttachment("photo");
            attachment.setId("111");
            attachment.setUrl("www.img.com");
            chatMessage.addAttachment(attachment);
            //
            QBAttachment attachment2 = new QBAttachment("video");
            attachment2.setId("222");
            attachment2.setUrl("www.video.com");
            chatMessage.addAttachment(attachment2);

            privateChatDialog.addIsTypingListener(privateChatDialogTypingListener);
            privateChatDialog.addMessageSentListener(privateChatDialogMessageSentListener);

            try {
                privateChatDialog.sendMessage(chatMessage);
            } catch (SmackException.NotConnectedException e) {
                log("send message error: " + e.getClass().getSimpleName());
            }
        }
    };


    //
    ////////////////////////////////////// Typing notifications //////////////////////////////////////////
    //

    Snippet sendIsTypingInPrivateChat = new Snippet("send is typing (private chat)") {
        @Override
        public void execute() {
            if (privateChatDialog == null){
                log("Please create private dialog first");
                return;
            }

            privateChatDialog.addIsTypingListener(privateChatDialogTypingListener);

            try {
                privateChatDialog.sendIsTypingNotification();
            } catch (XMPPException | SmackException.NotConnectedException e) {
                log("send typing error: " + e.getClass().getSimpleName());
            }
        }
    };

    Snippet sendStopTypingInPrivateChat = new Snippet("send stop typing (private chat)") {
        @Override
        public void execute() {
            if (privateChatDialog == null){
                log("Please create private dialog first");
                return;
            }

            privateChatDialog.addIsTypingListener(privateChatDialogTypingListener);

            try {
                privateChatDialog.sendStopTypingNotification();
            } catch (XMPPException | SmackException.NotConnectedException e) {
                log("send stop typing error: " + e.getClass().getSimpleName());
            }
        }
    };

    Snippet sendIsTypingInGroupChat = new Snippet("send is typing (group chat)") {
        @Override
        public void execute() {
            if (groupChatDialog.isJoined()) {
                log("Please join room first");
                return;
            }

            try {
                groupChatDialog.sendIsTypingNotification();
            } catch (XMPPException e) {
                log("send typing error: " + e.getLocalizedMessage());
            } catch (SmackException.NotConnectedException e) {
                log("send typing error: " + e.getClass().getSimpleName());
            }
        }
    };

    Snippet sendStopTypingInGroupChat = new Snippet("send stop typing (group chat)") {
        @Override
        public void execute() {
            if (groupChatDialog.isJoined()) {
                log("Please join room first");
                return;
            }

            try {
                groupChatDialog.sendStopTypingNotification();
            } catch (XMPPException e) {
                log("send typing error: " + e.getLocalizedMessage());
            } catch (SmackException.NotConnectedException e) {
                log("send typing error: " + e.getClass().getSimpleName());
            }
        }
    };


    //
    /////////////////////////////////////// Read/Delivered //////////////////////////////////////////
    //

    Snippet readMessagePrivateChat = new Snippet("read message (private chat)") {
        @Override
        public void execute() {
            if (privateChatDialog == null){
                log("Please create private dialog first");
                return;
            }

            QBChatMessage status = new QBChatMessage();
            status.setId("267477ab5612312312414124");
            status.setDialogId("267477ab5612312312414124");

            try {
                privateChatDialog.readMessage(status);
            } catch (XMPPException | SmackException.NotConnectedException e) {
                log("read message error: " + e.getLocalizedMessage());
            }
        }
    };

    Snippet deliverMessagePrivateChat = new Snippet("deliver message (private chat)") {
        @Override
        public void execute() {
            if (privateChatDialog == null){
                log("Please create private dialog first");
                return;
            }

            QBChatMessage status = new QBChatMessage();
            status.setId("267477ab5612312312414124");
            status.setDialogId("267477ab5612312312414124");

            try {
                privateChatDialog.deliverMessage(status);
            } catch (XMPPException | SmackException.NotConnectedException e) {
                log("deliver message error: " + e.getLocalizedMessage());
            }
        }
    };

    Snippet readMessageGroupChat = new Snippet("deliver message (group chat)") {
        @Override
        public void execute() {
            if (!groupChatDialog.isJoined()) {
                log("Please join room first");
                return;
            }

            try {
                QBChatMessage status = new QBChatMessage();
                status.setId("267477ab5612312312414124");
                status.setSenderId(567);
                status.setDialogId("267477ab5612312312414124");

                groupChatDialog.readMessage(status);
            } catch (XMPPException e) {
                log("read message error: " + e.getLocalizedMessage());
            } catch (SmackException.NotConnectedException e) {
                log("read message error: " + e.getClass().getSimpleName());
            }
        }
    };

    Snippet deliverMessageGroupChat = new Snippet("deliver message (group chat)") {
        @Override
        public void execute() {
            if (!groupChatDialog.isJoined()) {
                log("Please join room first");
                return;
            }

            try {
                QBChatMessage status = new QBChatMessage();
                status.setId("267477ab5612312312414124");
                status.setSenderId(567);
                status.setDialogId("267477ab5612312312414124");

                groupChatDialog.deliverMessage(status);
            } catch (XMPPException e) {
                log("deliver message error: " + e.getLocalizedMessage());
            } catch (SmackException.NotConnectedException e) {
                log("deliver message error: " + e.getClass().getSimpleName());
            }
        }
    };


    //
    ///////////////////////////////////////////// Group Chat /////////////////////////////////////////////
    //


    private void initGroupChatMessageListener() {
        groupChatDialogMessageListener = new QBChatDialogMessageListener(){
            @Override
            public void processMessage(String s, QBChatMessage qbChatMessage, Integer integer) {
                log("group chat: " + s + ", processMessage: " + qbChatMessage);
            }

            @Override
            public void processError(String s, QBChatException e, QBChatMessage qbChatMessage, Integer integer) {
                log("Group chat: " + s + ", Error: " + e.getCondition().toString());
            }
        };
    }

    private void initParticipantListener() {

        participantListener = new QBChatDialogParticipantListener() {
            @Override
            public void processPresence(String s, QBPresence qbPresence) {
                log("groupChat: " + s + ", presence: " + qbPresence);
            }
        };
    }

    Snippet joinRoom = new Snippet("join Room") {
        @Override
        public void execute() {
            if (groupChatDialog == null) {
                log("Please create group dialog first");
                return;
            }

            final DiscussionHistory history = new DiscussionHistory();
            history.setMaxStanzas(0);

            final QBEntityCallback clbck = new QBEntityCallback<Void>() {
                @Override
                public void onSuccess(Void result, Bundle bundle) {
                    log("join Room success");

                    // add listeners
                    groupChatDialog.addMessageListener(groupChatDialogMessageListener);
                    groupChatDialog.addParticipantListener(participantListener);
                    groupChatDialog.addMessageSentListener(groupChatDialogMessageSentListener);
                    groupChatDialog.addIsTypingListener(groupChatDialogTypingListener);
                }

                @Override
                public void onError(final QBResponseException list) {
                    log("join Room error: " + list);
                }
            };

            groupChatDialog.join(history, clbck);
        }
    };

    Snippet joinRoomSynchronous = new SnippetAsync("join Room (synchronous)", context) {
        @Override
        public void executeAsync() {
            if (groupChatDialog == null) {
                log("Please create group dialog first");
                return;
            }

            DiscussionHistory history = new DiscussionHistory();
            history.setMaxStanzas(10);

            try {
                groupChatDialog = QBRestChatService.getChatDialogById(ApplicationConfig.getInstance().getTestDialogId()).perform();
                log("dialog ID: " + groupChatDialog.getDialogId());

                groupChatDialog.join(history);

                // add listeners
                groupChatDialog.addMessageListener(groupChatDialogMessageListener);
                groupChatDialog.addParticipantListener(participantListener);
                groupChatDialog.addIsTypingListener(groupChatDialogTypingListener);

            } catch (XMPPException | SmackException e) {
                setException(e);
            } catch (QBResponseException e) {
                log("Getting dialog error: " + e.getClass().getSimpleName());
            }
        }

        @Override
        protected void postExecute() {
            super.postExecute();

            if (!chatService.isLoggedIn()) {
                return;
            }

            final Exception exc = getException();

            if (exc == null) {
                log("Join room success");
            } else {
                log("Join error: " + exc.getClass().getSimpleName());
            }
        }
    };

    Snippet sendMessageToRoomExtended = new Snippet("send message to room") {
        @Override
        public void execute() {
            if (groupChatDialog == null) {
                log("Please create group dialog first");
                return;
            } else if (!groupChatDialog.isJoined()){
                log("Please join room first");
                return;
            }

            // create a message
            QBChatMessage chatMessage = new QBChatMessage();
            chatMessage.setProperty("save_to_history", "1"); // Save to Chat 2.0 history
            chatMessage.setBody("Testing qb awesome");
            chatMessage.setMarkable(true);

            try {
                groupChatDialog.sendMessage(chatMessage);
            } catch (SmackException.NotConnectedException e) {
                log("Send message error: " + e.getClass().getSimpleName());
            } catch (IllegalStateException e) {
                log("Send message error: " + e.getLocalizedMessage());
            }
        }
    };

    Snippet sendMessageToRoomWithoutJoin = new Snippet("send message to room", "w/o join") {
        @Override
        public void execute() {
            if (groupChatDialog == null) {
                log("Please create group dialog first");
                return;
            }

            groupChatDialog.addMessageListener(groupChatDialogMessageListener);

            log("currentChatRoom: " + groupChatDialog);

            // create a message
            QBChatMessage chatMessage = new QBChatMessage();
            chatMessage.setBody("Testing qb awesome");
            chatMessage.setProperty("save_to_history", "1"); // Save to Chat 2.0 history
            chatMessage.setMarkable(true);

            try {
                groupChatDialog.sendMessageWithoutJoin(chatMessage);
            } catch (SmackException.NotConnectedException e) {
                log("Send message error: " + e.getClass().getSimpleName());
            } catch (IllegalStateException e) {
                log("Send message error: " + e.getLocalizedMessage());
            }
        }
    };

    Snippet getOnlineRoomUsersSynchronous = new Snippet("get online room users") {
        @Override
        public void execute() {
            if (groupChatDialog == null) {
                log("Please create group dialog first");
                return;
            } else if (!groupChatDialog.isJoined()){
                log("Please join room first");
                return;
            }

            Collection<Integer> onlineRoomUsers = null;
            try {
                onlineRoomUsers = groupChatDialog.requestOnlineUsers();
            } catch (XMPPException|SmackException.NotConnectedException|SmackException.NoResponseException e) {
                log("get online users error: " + e.getLocalizedMessage());
            }

            String onlineUser = "online users: ";
            if (onlineRoomUsers != null) {
                for (Integer userID : onlineRoomUsers) {
                    onlineUser += userID;
                    onlineUser += ", ";
                }
            }
            log(onlineUser);
        }
    };

    Snippet leaveRoom = new SnippetAsync("leave room", context) {
        @Override
        public void executeAsync() {
            if (groupChatDialog == null) {
                log("Please create group dialog first");
                return;
            } else if (!groupChatDialog.isJoined()){
                log("Please join room first");
                return;
            }

            try {
                groupChatDialog.leave();
                groupChatDialog = null;
            } catch (XMPPException | SmackException.NotConnectedException e) {
                setException(e);
            }
        }

        @Override
        protected void postExecute() {
            super.postExecute();

            if (groupChatDialog == null) {
                return;
            }

            final Exception exc = getException();

            if (exc == null) {
                log("Leave success");
            } else {
                log("Leave error: " + exc.getClass().getSimpleName());
            }
        }
    };


    //
    ///////////////////////////////////////////// Chat_2.0 /////////////////////////////////////////////
    //


    Snippet getDialogs = new Snippet("Get Dialogs") {
        @Override
        public void execute() {
            if (!chatService.isLoggedIn()){   //TODO VT need delete after fix QBANDRSDK-526
                log("Please login first");
                return;
            }

            QBRequestGetBuilder requestBuilder = new QBRequestGetBuilder();
            requestBuilder.setLimit(100);
            requestBuilder.addRule("data[class_name]", QueryRule.EQ, "Advert");

            QBRestChatService.getChatDialogs(null, requestBuilder).performAsync(new QBEntityCallback<ArrayList<QBChatDialog>>() {
                @Override
                public void onSuccess(ArrayList<QBChatDialog> dialogs, Bundle args) {
                    Log.i(TAG, "dialogs: " + dialogs);

                    QBChatDialog dialog = dialogs.get(0);
                    Log.i(TAG, "arr: " + dialog.getCustomData().getArray("arr"));
                    Log.i(TAG, "bbb: " + dialog.getCustomData().getBoolean("bbb"));
                    Log.i(TAG, "fff: " + dialog.getCustomData().getFloat("fff"));
                    Log.i(TAG, "fff2: " + dialog.getCustomData().get("fff"));
                    Log.i(TAG, "iii: " + dialog.getCustomData().getInteger("iii"));
                    Log.i(TAG, "name: " + dialog.getCustomData().getString("name"));
                    try {
                        SimpleDateFormat format = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy");
                        Log.i(TAG, "ddd: " + dialog.getCustomData().getDate("ddd", format));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onError(QBResponseException errors) {
                    handleErrors(errors);
                }
            });
        }
    };

    Snippet getDialogsSynchronous = new SnippetAsync("Get Dialogs (synchronous)", context) {
        @Override
        public void executeAsync() {
            if (!chatService.isLoggedIn()){   //TODO VT need delete after fix QBANDRSDK-526
                log("Please login first");
                return;
            }

            QBRequestGetBuilder requestBuilder = new QBRequestGetBuilder();
            requestBuilder.setLimit(100);
            requestBuilder.addRule("data[class_name]", QueryRule.EQ, "Advert");
//            requestBuilder.addParameter("data[class_name]", "Advert");
            //
            List<QBChatDialog> chatDialogsList = null;

            try {
                chatDialogsList = QBRestChatService.getChatDialogs(null, requestBuilder).perform();
            } catch (QBResponseException e) {
                setException(e);
            }

            if (chatDialogsList != null) {
                Log.i(TAG, "chatDialogsList: " + chatDialogsList);
            }
        }
    };


    Snippet getDialogsCount = new Snippet("Get Dialogs count") {
        @Override
        public void execute() {
            //
            QBRequestGetBuilder requestBuilder = new QBRequestGetBuilder();
//            requestBuilder.all("occupants_ids", "76,58");

            Bundle bundle = new Bundle();
            //
            QBRestChatService.getChatDialogsCount(requestBuilder, bundle).performAsync(new QBEntityCallback<Integer>() {
                @Override
                public void onSuccess(Integer integer, Bundle bundle) {
                    Log.i(TAG, "dialogsCount: " + integer);
                }

                @Override
                public void onError(QBResponseException errors) {
                    handleErrors(errors);
                }
            });
        }
    };

    Snippet getDialogsCountSynchronous = new SnippetAsync("Get Dialogs count (synchronous)", context) {
        @Override
        public void executeAsync() {
            Bundle bundle = new Bundle();
            //
            QBRequestGetBuilder requestBuilder = new QBRequestGetBuilder();
//            requestBuilder.all("occupants_ids", "76,58");
            //
            int dialogsCount = -1;

            try {
                dialogsCount = QBRestChatService.getChatDialogsCount(requestBuilder, bundle).perform();
            } catch (QBResponseException e) {
                setException(e);
            }

            Log.i(TAG, "dialogsCount: " + dialogsCount);
        }
    };


    Snippet createGroupDialog = new Snippet("Create Group Dialog") {
        @Override
        public void execute() {
            if (!chatService.isLoggedIn()) {
                log("Please login first");
                return;
            }

            ArrayList<Integer> occupantIdsList = new ArrayList<>();
            occupantIdsList.add(ApplicationConfig.getInstance().getTestUserId2());
            occupantIdsList.add(301);

            QBChatDialog dialog = new QBChatDialog();
            dialog.setName("Chat with Garry and John");
            dialog.setPhoto("452444");
            dialog.setType(QBDialogType.GROUP);
            dialog.setOccupantsIds(occupantIdsList);

//            HashMap<String, Object> data = new HashMap<>();
//            data.put("data[class_name]", "Advert");
//            data.put("data[name]", "bingo");
            final String myLocation = Double.toString(23.45454) + "," + Double.toString(0.4456);
//            data.put("data[arr]", myLocation);
//            dialog.setData(data);

            QBDialogCustomData data = new QBDialogCustomData("Advert");
            data.putString("name", "bingo");
            data.putArray("arr", Arrays.asList(new Double[]{1.32, 2.56}));
            data.putBoolean("bbb", true);
            data.putFloat("fff", 45.676f);
            data.putInteger("iii", 56);
            data.putDate("ddd", new Date());
            data.putLocation("loc", Arrays.asList(new Double[]{3.78, 4.87}));

            dialog.setCustomData(data);

            QBRestChatService.createChatDialog(dialog).performAsync(new QBEntityCallback<QBChatDialog>() {
                @Override
                public void onSuccess(QBChatDialog dialog, Bundle args) {
                    groupChatDialog = dialog;
                    Log.i(TAG, "dialog: " + dialog);
                    Log.i(TAG, "arr: " + dialog.getCustomData().getArray("arr"));
                    Log.i(TAG, "bbb: " + dialog.getCustomData().getBoolean("bbb"));
                    Log.i(TAG, "fff: " + dialog.getCustomData().getFloat("fff"));
                    Log.i(TAG, "fff2: " + dialog.getCustomData().get("fff"));
                    Log.i(TAG, "iii: " + dialog.getCustomData().getInteger("iii"));
                    Log.i(TAG, "name: " + dialog.getCustomData().getString("name"));
                    try {
                        SimpleDateFormat format = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy");
                        Log.i(TAG, "ddd: " + dialog.getCustomData().getDate("ddd", format));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onError(QBResponseException errors) {
                    handleErrors(errors);
                }
            });
        }
    };

    Snippet createGroupDialogSynchronous = new SnippetAsync("Create Group Dialog (synchronous)", context) {
        @Override
        public void executeAsync() {
            if (!QBChatService.getInstance().isLoggedIn()) {
                log("Please login first");
                return;
            }

            ArrayList<Integer> occupantIdsList = new ArrayList<>();
            occupantIdsList.add(ApplicationConfig.getInstance().getTestUserId2());
            occupantIdsList.add(301);
            //
            QBChatDialog dialog = new QBChatDialog();
            dialog.setName("Chat with Garry and John");
            dialog.setPhoto("452444");
            dialog.setType(QBDialogType.GROUP);
            dialog.setOccupantsIds(occupantIdsList);

            HashMap<String, Object> data = new HashMap<>();
            data.put("data[class_name]", "Advert");
            data.put("data[name]", "bingo");
            dialog.setCustomData(new QBDialogCustomData(data));

            try {
                groupChatDialog = QBRestChatService.createChatDialog(dialog).perform();
            } catch (QBResponseException e) {
                setException(e);
            }

            if (groupChatDialog != null) {
                Log.i(TAG, "dialog: " + groupChatDialog);
            }
        }
    };

    Snippet createPrivateDialog = new Snippet("Create Private Dialog") {
        @Override
        public void execute() {
            if (!chatService.isLoggedIn()) {
                log("Please login first");
                return;
            }

            ArrayList<Integer> occupantIdsList = new ArrayList<>();
            occupantIdsList.add(ApplicationConfig.getInstance().getTestUserId2());

            QBChatDialog dialog = new QBChatDialog();
            dialog.setPhoto("452444");
            dialog.setType(QBDialogType.PRIVATE);
            dialog.setOccupantsIds(occupantIdsList);

//            HashMap<String, Object> data = new HashMap<>();
//            data.put("data[class_name]", "Advert");
//            data.put("data[name]", "bingo");
            final String myLocation = Double.toString(23.45454) + "," + Double.toString(0.4456);
//            data.put("data[arr]", myLocation);
//            dialog.setData(data);

            QBDialogCustomData data = new QBDialogCustomData("Advert");
            data.putString("name", "bingo");
            data.putArray("arr", Arrays.asList(new Double[]{1.32, 2.56}));
            data.putBoolean("bbb", true);
            data.putFloat("fff", 45.676f);
            data.putInteger("iii", 56);
            data.putDate("ddd", new Date());
            data.putLocation("loc", Arrays.asList(new Double[]{3.78, 4.87}));

            dialog.setCustomData(data);

            QBRestChatService.createChatDialog(dialog).performAsync(new QBEntityCallback<QBChatDialog>() {
                @Override
                public void onSuccess(QBChatDialog dialog, Bundle args) {
                    privateChatDialog = dialog;
                    Log.i(TAG, "dialog: " + dialog);
                    Log.i(TAG, "arr: " + dialog.getCustomData().getArray("arr"));
                    Log.i(TAG, "bbb: " + dialog.getCustomData().getBoolean("bbb"));
                    Log.i(TAG, "fff: " + dialog.getCustomData().getFloat("fff"));
                    Log.i(TAG, "fff2: " + dialog.getCustomData().get("fff"));
                    Log.i(TAG, "iii: " + dialog.getCustomData().getInteger("iii"));
                    Log.i(TAG, "name: " + dialog.getCustomData().getString("name"));
                    try {
                        SimpleDateFormat format = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy");
                        Log.i(TAG, "ddd: " + dialog.getCustomData().getDate("ddd", format));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onError(QBResponseException errors) {
                    handleErrors(errors);
                }
            });
        }
    };

    Snippet createPrivateDialogSynchronous = new SnippetAsync("Create Private Dialog (synchronous)", context) {
        @Override
        public void executeAsync() {
            if (!chatService.isLoggedIn()) {
                log("Please login first");
                return;
            }

            ArrayList<Integer> occupantIdsList = new ArrayList<>();
            occupantIdsList.add(ApplicationConfig.getInstance().getTestUserId2());
            //
            QBChatDialog dialog = new QBChatDialog();
            dialog.setPhoto("452444");
            dialog.setType(QBDialogType.PRIVATE);
            dialog.setOccupantsIds(occupantIdsList);

            HashMap<String, Object> data = new HashMap<>();
            data.put("data[class_name]", "Advert");
            data.put("data[name]", "bingo");
            dialog.setCustomData(new QBDialogCustomData(data));

            try {
                privateChatDialog = QBRestChatService.createChatDialog(dialog).perform();
            } catch (QBResponseException e) {
                setException(e);
            }

            if (groupChatDialog != null) {
                Log.i(TAG, "dialog: " + groupChatDialog);
            }
        }
    };


    Snippet updateDialog = new Snippet("Update Dialog") {
        @Override
        public void execute() {
            if (!QBChatService.getInstance().isLoggedIn()) {
                log("Please login first");
                return;
            }

            QBRequestUpdateBuilder requestBuilder = new QBRequestUpdateBuilder();
//            requestBuilder.pullAll(com.quickblox.chat.Consts.DIALOG_OCCUPANTS, 378);

            QBChatDialog dialog = new QBChatDialog("56aa3d7da28f9a5b1f0000cf");
            dialog.setName("Chat with Garry and John");
            dialog.setPhoto("452444");

//            HashMap<String, Object> data = new HashMap<>();
//            data.put("data[class_name]", "Advert");
//            data.put("data[name]", "bingo2");
//            dialog.setData(data);
            QBDialogCustomData data = new QBDialogCustomData("Advert");
            data.putString("name", "bingo4");
            data.putArray("arr", Arrays.asList(new Double[]{99.0, 100.0, 101.0}));

            dialog.setCustomData(data);


            QBRestChatService.updateGroupChatDialog(dialog, requestBuilder).performAsync(new QBEntityCallback<QBChatDialog>() {
                @Override
                public void onSuccess(QBChatDialog dialog, Bundle args) {
                    Log.i(TAG, "dialog: " + dialog);

                    Log.i(TAG, "arr: " + dialog.getCustomData().getArray("arr"));
                    Log.i(TAG, "bbb: " + dialog.getCustomData().getBoolean("bbb"));
                    Log.i(TAG, "fff: " + dialog.getCustomData().getFloat("fff"));
                    Log.i(TAG, "fff2: " + dialog.getCustomData().get("fff"));
                    Log.i(TAG, "iii: " + dialog.getCustomData().getInteger("iii"));
                    Log.i(TAG, "name: " + dialog.getCustomData().getString("name"));
                    try {
                        SimpleDateFormat format = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy");
                        Log.i(TAG, "ddd: " + dialog.getCustomData().getDate("ddd", format));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onError(QBResponseException errors) {
                    handleErrors(errors);
                }
            });
        }
    };

    Snippet updateDialogSynchronous = new SnippetAsync("Update Dialog (synchronous)", context) {
        @Override
        public void executeAsync() {

            if (!QBChatService.getInstance().isLoggedIn()) {
                log("Please login first");
                return;
            }

            QBChatDialog dialog = new QBChatDialog("5444bba7535c121d3302245f");
            dialog.setName("Chat with Garry and John");
            dialog.setPhoto("452444");

            QBRequestUpdateBuilder requestBuilder = new QBRequestUpdateBuilder();
            requestBuilder.pullAll(com.quickblox.chat.Consts.DIALOG_OCCUPANTS, 378);

//            Map<String, String> data = new HashMap<String, String>();
//            data.put("data[class_name]", "Advert");
//            data.put("data[title]", "bingo");
//            dialog.setData(data);

            QBChatDialog updatedDialog = null;
            try {
                updatedDialog = QBRestChatService.updateGroupChatDialog(dialog, requestBuilder).perform();
            } catch (QBResponseException e) {
                setException(e);
            }

            if (updatedDialog != null) {
                Log.i(TAG, "dialog: " + updatedDialog);
            }
        }
    };


    Snippet deleteDialog = new Snippet("Delete Dialog") {
        @Override
        public void execute() {
            if (!chatService.isLoggedIn()) {
                log("Please login first");
                return;
            }

            String dialogID = "5444bba7535c121d3302245f";

            QBRestChatService.deleteDialog(dialogID, true).performAsync(new QBEntityCallback<Void>() {
                @Override
                public void onSuccess(Void result, Bundle bundle) {
                    Log.i(TAG, "dialog deleted");
                }

                @Override
                public void onError(QBResponseException errors) {
                    handleErrors(errors);
                }
            });
        }
    };

    Snippet deleteDialogSynchronous = new SnippetAsync("Delete Dialog (synchronous)", context) {
        @Override
        public void executeAsync() {

            if (!chatService.isLoggedIn()) {
                log("Please login first");
                return;
            }

            String dialogID = "5444bbc7535c12e10f0233be";

            try {
                QBRestChatService.deleteDialog(dialogID, true).perform();
                Log.i(TAG, "dialog deleted");
            } catch (QBResponseException e) {
                setException(e);
            }
        }
    };


    Snippet getMessages = new Snippet("Get Messages", "with dialog id") {
        @Override
        public void execute() {
            QBChatDialog qbDialog = new QBChatDialog(ApplicationConfig.getInstance().getTestDialogId());

            QBRequestGetBuilder customObjectRequestBuilder = new QBRequestGetBuilder();
            customObjectRequestBuilder.setLimit(100);

            QBRestChatService.getDialogMessages(qbDialog, customObjectRequestBuilder).performAsync(new QBEntityCallback<ArrayList<QBChatMessage>>() {
                @Override
                public void onSuccess(ArrayList<QBChatMessage> messages, Bundle args) {
                    for (QBChatMessage msg : messages) {
                        Log.i(TAG, "message\n: " + msg);

// Deliver messages (group)
//                        try {
//                            groupChatDialog.deliverMessage(msg);
//                        } catch (XMPPException e) {
//                            e.printStackTrace();
//                        } catch (SmackException.NotConnectedException e) {
//                            e.printStackTrace();
//                        }

// Deliver messages (private)
//                        try {
//                            privateChatDialog.deliverMessage(msg);
//                        } catch (XMPPException e) {
//                            log("deliver message error: " + e.getLocalizedMessage());
//                        } catch (SmackException.NotConnectedException e) {
//                            log("deliver message error: " + e.getClass().getSimpleName());
//                        }
                    }
                }

                @Override
                public void onError(QBResponseException errors) {
                    handleErrors(errors);
                }
            });
        }
    };

    Snippet getMessagesSynchronous = new SnippetAsync("Get Messages (synchronous)", "with dialog id", context) {
        @Override
        public void executeAsync() {
            QBRequestGetBuilder customObjectRequestBuilder = new QBRequestGetBuilder();
            customObjectRequestBuilder.setLimit(100);

            QBChatDialog dialog = new QBChatDialog(ApplicationConfig.getInstance().getTestDialogId());

            List<QBChatMessage> dialogMessagesList = null;
            try {
                dialogMessagesList = QBRestChatService.getDialogMessages(dialog, customObjectRequestBuilder).perform();
            } catch (QBResponseException e) {
                setException(e);
            }

            if (dialogMessagesList != null) {
                for (QBChatMessage msg : dialogMessagesList) {
                    Log.i(TAG, "message\n: " + msg);
                }
            }
        }
    };


    Snippet markMessagesAsRead = new Snippet("Mark Messages as read") {
        @Override
        public void execute() {
            StringifyArrayList<String> messagesIDs = new StringifyArrayList<>();
            messagesIDs.add("53cfc62ee4b05ed6d7cf17d3");
            messagesIDs.add("53cfc62fe4b05ed6d7cf17d5");

            String dialogId = "53cfc593efa3573ebd000017";

            QBRestChatService.markMessagesAsRead(dialogId, messagesIDs).performAsync(new QBEntityCallback<Void>() {
                @Override
                public void onSuccess(Void result, Bundle bundle) {
                    Log.i(TAG, "read OK");
                }

                @Override
                public void onError(QBResponseException errors) {
                    handleErrors(errors);
                }
            });
        }
    };

    Snippet markMessagesAsReadSynchronous = new SnippetAsync("Mark Messages as read (synchronous)", context) {
        @Override
        public void executeAsync() {
            StringifyArrayList<String> messagesIDs = new StringifyArrayList<>();
            messagesIDs.add("53cfc62ee4b05ed6d7cf17d3");
            messagesIDs.add("53cfc62fe4b05ed6d7cf17d5");

            try {
                QBRestChatService.markMessagesAsRead("53cfc593efa3573ebd000017", messagesIDs).perform();
                Log.i(TAG, "read OK");
            } catch (QBResponseException e) {
                setException(e);
            }
        }
    };

    Snippet deleteMessages = new Snippet("Delete Messages") {
        @Override
        public void execute() {
            Set<String> messagesIds = new HashSet<String>() {{
                add("546cc8040eda8f2dd7ee449c");
                add("546cc80f0eda8f2dd7ee449d");
            }};

            QBRestChatService.deleteMessages(messagesIds, false).performAsync(new QBEntityCallback<Void>() {
                @Override
                public void onSuccess(Void result, Bundle bundle) {
                    Log.i(TAG, "deleted OK");
                }

                @Override
                public void onError(QBResponseException errors) {
                    handleErrors(errors);
                }
            });
        }
    };

    Snippet deleteMessagesSynchronous = new SnippetAsync("Delete Messages (synchronous)", context) {
        @Override
        public void executeAsync() {
            Set<String> messagesIds = new HashSet<String>() {{
                add("546cc8b1535c12942e00133a");
                add("546cce5e535c1203cb001cc0");
            }};

            try {
                QBRestChatService.deleteMessages(messagesIds, false).perform();
                Log.i(TAG, "deleted OK");
            } catch (QBResponseException e) {
                setException(e);
            }
        }
    };


    Snippet createMessage = new Snippet("Create Message ") {
        @Override
        public void execute() {
            QBChatMessage msg = new QBChatMessage();
            msg.setBody("hello2");
            msg.setRecipientId(ApplicationConfig.getInstance().getTestUserId2());

            QBAttachment attachment = new QBAttachment("photo");
            attachment.setId("123123");
            msg.addAttachment(attachment);
            QBAttachment attachment2 = new QBAttachment("video");
            attachment2.setUrl("api.qb.com/image.jpg");
            msg.addAttachment(attachment2);

            msg.setProperty("p1", "v1");
            msg.setProperty("p2", "v2");

            QBRestChatService.createMessage(msg, false).performAsync(new QBEntityCallback<QBChatMessage>() {
                @Override
                public void onSuccess(QBChatMessage result, Bundle params) {
                    Log.i(TAG, "created message\n: " + result);
                }

                @Override
                public void onError(QBResponseException errors) {
                    handleErrors(errors);
                }
            });
        }
    };

    Snippet createMessageSynchronous = new SnippetAsync("Create Message (synchronous)", context) {
        @Override
        public void executeAsync() {
            QBChatMessage msg = new QBChatMessage();
            msg.setBody("hello2");
            msg.setDialogId(ApplicationConfig.getInstance().getTestDialogId());

            QBAttachment attachment = new QBAttachment("photo");
            attachment.setId("123123");
            msg.addAttachment(attachment);
            QBAttachment attachment2 = new QBAttachment("video");
            attachment2.setUrl("api.qb.com/image.jpg");
            msg.addAttachment(attachment2);

            msg.setProperty("p1", "v1");
            msg.setProperty("p2", "v2");

            QBChatMessage createdMsg = null;
            try {
                createdMsg = QBRestChatService.createMessage(msg, false).perform();
            } catch (QBResponseException e) {
                e.printStackTrace();
            }

            if (createdMsg != null) {
                Log.i(TAG, "created message\n: " + createdMsg);
            }
        }
    };


    //
    ///////////////////////////////////////////// Roster /////////////////////////////////////////////
    //

    private void initRoster() {
        сhatRoster = chatService.getRoster(QBRoster.SubscriptionMode.mutual, subscriptionListener);
        сhatRoster.addRosterListener(rosterListener);
    }

    private void initRosterListener() {
        rosterListener = new QBRosterListener() {
            @Override
            public void entriesDeleted(Collection<Integer> userIds) {
                log("entriesDeleted: " + userIds);
            }

            @Override
            public void entriesAdded(Collection<Integer> userIds) {
                log("entriesAdded: " + userIds);
            }

            @Override
            public void entriesUpdated(Collection<Integer> userIds) {
                log("entriesUpdated: " + userIds);
            }

            @Override
            public void presenceChanged(QBPresence presence) {
                log("presenceChanged: " + presence);
            }
        };
    }

    private void initSubscriptionListener() {
        subscriptionListener = new QBSubscriptionListener() {
            @Override
            public void subscriptionRequested(int userId) {
                log("subscriptionRequested: " + userId);
            }
        };
    }

    Snippet sendPresence = new Snippet("send presence") {
        @Override
        public void execute() {
            if (сhatRoster == null) {
                log("Please login first");
                return;
            }

//            QBPresence presence = new QBPresence(QBPresence.Type.online);
            QBPresence presence = new QBPresence(QBPresence.Type.online, "I'm at home", 1, QBPresence.Mode.available);
            try {
                сhatRoster.sendPresence(presence);
            } catch (SmackException.NotConnectedException e) {
                log("error: " + e.getClass().getSimpleName());
            }
        }
    };

    Snippet getRosterUsers = new Snippet("get roster users") {
        @Override
        public void execute() {
            if (сhatRoster == null) {
                log("Please login first");
                return;
            }

            Collection<QBRosterEntry> entries = сhatRoster.getEntries();
            log("Roster users:  " + entries);
        }
    };

    Snippet getUserPresence = new Snippet("get user's presence") {
        @Override
        public void execute() {
            if (сhatRoster == null) {
                log("Please login first");
                return;
            }

            int userID = ApplicationConfig.getInstance().getTestUserId2();

            QBPresence presence = сhatRoster.getPresence(userID);
            if (presence == null) {
                log("No user in your roster");
                return;
            }
            if (presence.getType() == QBPresence.Type.online) {
                log("User " + userID + " is online");
            } else {
                log("User " + userID + " is offline");
            }
        }
    };

    Snippet addUserToRoster = new Snippet("add user to roster") {
        @Override
        public void execute() {
            int userID = ApplicationConfig.getInstance().getTestUserId2();

            if (сhatRoster.contains(userID)) {
                try {
                    сhatRoster.subscribe(userID);
                } catch (SmackException.NotConnectedException e) {
                    log("error: " + e.getClass().getSimpleName());
                }
            } else {
                try {
                    сhatRoster.createEntry(userID, null);
                } catch (XMPPException e) {
                    log("error: " + e.getLocalizedMessage());
                } catch (SmackException.NotLoggedInException | SmackException.NotConnectedException | SmackException.NoResponseException e) {
                    log("error: " + e.getClass().getSimpleName());
                }
            }
        }
    };

    Snippet removeUserFromRoster = new Snippet("remove user from roster") {
        @Override
        public void execute() {
            int userID = ApplicationConfig.getInstance().getTestUserId2();

            try {
                сhatRoster.unsubscribe(userID);
            } catch (SmackException.NotConnectedException e) {
                e.printStackTrace();
            }
        }
    };


    Snippet confirmAddRequest = new Snippet("confirm add request") {
        @Override
        public void execute() {
            int userID = ApplicationConfig.getInstance().getTestUserId2();

            try {
                сhatRoster.confirmSubscription(userID);
            } catch (SmackException.NotConnectedException | SmackException.NotLoggedInException | SmackException.NoResponseException e) {
                log("error: " + e.getClass().getSimpleName());
            } catch (XMPPException e) {
                log("error: " + e.getLocalizedMessage());
            }
        }
    };

    Snippet rejectAddRequest = new Snippet("reject add request") {
        @Override
        public void execute() {
            int userID = ApplicationConfig.getInstance().getTestUserId2();

            try {
                сhatRoster.reject(userID);
            } catch (SmackException.NotConnectedException e) {
                e.printStackTrace();
            }
        }
    };


    //
    ///////////////////////////////////// Privacy List /////////////////////////////////////////////
    //

    private void initPrivacyListsManager() {
        privacyListsManager = chatService.getPrivacyListsManager();
    }

    private void initPrivacyListsListener() {
        privacyListListener = new QBPrivacyListListener() {
            @Override
            public void setPrivacyList(String listName, List<QBPrivacyListItem> listItem) {
                log("on setPrivacyList:" + listName + ", items: " + listItem);
            }

            @Override
            public void updatedPrivacyList(String listName) {
                log("on setPrivacyList:" + listName);
            }
        };
        privacyListsManager.addPrivacyListListener(privacyListListener);
    }


    Snippet getPrivacyLists = new SnippetAsync("get privacy lists (synchronous)", context) {
        @Override
        public void executeAsync() {
            List<QBPrivacyList> lists = null;

            try {
                lists = privacyListsManager.getPrivacyLists();
            } catch (SmackException.NotConnectedException | XMPPException.XMPPErrorException | SmackException.NoResponseException e) {
                e.printStackTrace();
            }

            if (lists != null) {
                log("privcay lists:" + lists.toString());
            }
        }
    };

    Snippet getPrivacyList = new SnippetAsync("get privacy list (synchronous)", context) {
        @Override
        public void executeAsync() {
            QBPrivacyList list = null;

            try {
                list = privacyListsManager.getPrivacyList("public");
            } catch (SmackException.NotConnectedException | XMPPException.XMPPErrorException | SmackException.NoResponseException e) {
                e.printStackTrace();
            }

            if (list != null) {
                log("public privacy list: " + list.toString());
            }

        }
    };

    Snippet setPrivacyList = new SnippetAsync("set privacy list (synchronous)", context) {
        @Override
        public void executeAsync() {
            QBPrivacyList list = new QBPrivacyList();
            list.setName("public4");

            ArrayList<QBPrivacyListItem> items = new ArrayList<>();
            //
            QBPrivacyListItem item1 = new QBPrivacyListItem();
            item1.setAllow(false);
            item1.setType(QBPrivacyListItem.Type.USER_ID);
            item1.setValueForType(String.valueOf(ApplicationConfig.getInstance().getTestUserId2()));
            items.add(item1);
            //
            QBPrivacyListItem item2 = new QBPrivacyListItem();
            item2.setAllow(false);
            item2.setType(QBPrivacyListItem.Type.GROUP_USER_ID);
            item2.setValueForType(String.valueOf(ApplicationConfig.getInstance().getTestUserId2()));
            items.add(item2);
            //
            list.setItems(items);

            try {
                privacyListsManager.setPrivacyList(list);
            } catch (SmackException.NotConnectedException | XMPPException.XMPPErrorException | SmackException.NoResponseException e) {
                e.printStackTrace();
            }
        }
    };

    Snippet deletePrivacyList = new SnippetAsync("delete privacy list (synchronous)", context) {
        @Override
        public void executeAsync() {
            try {
                privacyListsManager.deletePrivacyList("public");
            } catch (SmackException.NotConnectedException | XMPPException.XMPPErrorException | SmackException.NoResponseException e) {
                e.printStackTrace();
            }
        }
    };

    Snippet setDefaultPrivacyList = new SnippetAsync("set default privacy list (synchronous)", context) {
        @Override
        public void executeAsync() {
            try {
                privacyListsManager.setPrivacyListAsDefault("public4");
                log("list set as default");
            } catch (SmackException.NotConnectedException | XMPPException.XMPPErrorException | SmackException.NoResponseException e) {
                e.printStackTrace();
            }
        }
    };


    //
    ///////////////////////////////////// Mobile optimisation //////////////////////////////////////
    //


    Snippet enterInactiveState = new Snippet("enter inactive state") {
        @Override
        public void execute() {
            try {
                chatService.enterInactiveState();
            } catch (SmackException.NotConnectedException e) {
                log("error: " + e.getClass().getSimpleName());
            }
        }
    };

    Snippet enterActiveState = new Snippet("enter active state") {
        @Override
        public void execute() {
            try {
                chatService.enterActiveState();
            } catch (SmackException.NotConnectedException e) {
                log("error: " + e.getClass().getSimpleName());
            }
        }
    };


    //
    //////////////////////////////////////// Ping manager //////////////////////////////////////////
    //

    Snippet pingServer = new Snippet("ping server") {
        @Override
        public void execute() {
            final QBPingManager pingManager = chatService.getPingManager();
            pingManager.pingServer(new QBEntityCallback<Void>() {

                @Override
                public void onSuccess(Void result, Bundle params) {
                    log("ping success");
                }

                @Override
                public void onError(QBResponseException e) {
                    log("ping error: " + e);
                }
            });
        }
    };

    Snippet pingServerSynchronous = new SnippetAsync("ping server (synchronous)", context) {
        @Override
        public void executeAsync() {
            final QBPingManager pingManager = chatService.getPingManager();
            try {
                boolean ping = pingManager.pingServer();
                log("ping success: " + ping);
            } catch (SmackException.NotConnectedException e) {
                e.printStackTrace();
            }
        }
    };


    //
    //////// System Message /////
    //

    Snippet sendSystemMessage = new Snippet("send system message") {
        @Override
        public void execute() {
            try {
                // create a message
                QBChatMessage chatMessage = new QBChatMessage();
                chatMessage.setProperty("param1", "value1");
                chatMessage.setProperty("param2", "value2");
                chatMessage.setBody("system body");

                int userID = ApplicationConfig.getInstance().getTestUserId1();
                chatMessage.setRecipientId(userID);

                systemMessagesManager.sendSystemMessage(chatMessage);

            } catch (SmackException.NotConnectedException | IllegalStateException e) {
                log("send system message error: " + e.getMessage());
            }
        }
    };
}