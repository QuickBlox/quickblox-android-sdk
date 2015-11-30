package com.quickblox.sample.chat.core;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.quickblox.auth.QBAuth;
import com.quickblox.auth.model.QBSession;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.model.QBDialog;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.core.request.QBPagedRequestBuilder;
import com.quickblox.core.request.QBRequestGetBuilder;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by igorkhomenko on 4/28/15.
 */
public class ChatService {

    private static final String TAG = ChatService.class.getSimpleName();

    static final int AUTO_PRESENCE_INTERVAL_IN_SECONDS = 30;

    private static ChatService instance;

    public static synchronized ChatService getInstance() {
        if(instance == null) {
            instance = new ChatService();
        }
        return instance;
    }

    public static boolean initIfNeed(Context ctx) {
        if (!QBChatService.isInitialized()) {
            QBChatService.setDebugEnabled(true);
            QBChatService.init(ctx);
            Log.d(TAG, "Initialise QBChatService");

            return true;
        }

        return false;
    }

    private QBChatService chatService;

    private ChatService() {
        chatService = QBChatService.getInstance();
        chatService.addConnectionListener(chatConnectionListener);
    }

    public void addConnectionListener(ConnectionListener listener){
        chatService.addConnectionListener(listener);
    }

    public void removeConnectionListener(ConnectionListener listener){
        chatService.removeConnectionListener(listener);
    }

    public void login(final QBUser user, final QBEntityCallback callback){

        // Create REST API session
        //
        QBAuth.createSession(user, new QBEntityCallbackImpl<QBSession>() {
            @Override
            public void onSuccess(QBSession session, Bundle args) {

                user.setId(session.getUserId());

                // login to Chat
                //
                loginToChat(user, new QBEntityCallbackImpl() {

                    @Override
                    public void onSuccess() {
                        callback.onSuccess();
                    }

                    @Override
                    public void onError(List errors) {
                        callback.onError(errors);
                    }
                });
            }

            @Override
            public void onError(List<String> errors) {
                callback.onError(errors);
            }
        });
    }

    public void logout(){
        chatService.logout(new QBEntityCallbackImpl() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onError(List list) {

            }
        });
    }

    private void loginToChat(final QBUser user, final QBEntityCallback callback){

        chatService.login(user, new QBEntityCallbackImpl() {
            @Override
            public void onSuccess() {

                // Start sending presences
                //
                try {
                    chatService.startAutoSendPresence(AUTO_PRESENCE_INTERVAL_IN_SECONDS);
                } catch (SmackException.NotLoggedInException e) {
                    e.printStackTrace();
                }

                callback.onSuccess();
            }

            @Override
            public void onError(List errors) {
                callback.onError(errors);
            }
        });
    }

    public void getDialogs(final QBEntityCallback callback){
        // get dialogs
        //
        QBRequestGetBuilder customObjectRequestBuilder = new QBRequestGetBuilder();
        customObjectRequestBuilder.setPagesLimit(100);

        QBChatService.getChatDialogs(null, customObjectRequestBuilder, new QBEntityCallbackImpl<ArrayList<QBDialog>>() {
            @Override
            public void onSuccess(final ArrayList<QBDialog> dialogs, Bundle args) {

                // collect all occupants ids
                //
                List<Integer> usersIDs = new ArrayList<Integer>();
                for(QBDialog dialog : dialogs){
                    usersIDs.addAll(dialog.getOccupants());
                }

                // Get all occupants info
                //
                QBPagedRequestBuilder requestBuilder = new QBPagedRequestBuilder();
                requestBuilder.setPage(1);
                requestBuilder.setPerPage(usersIDs.size());
                //
                QBUsers.getUsersByIDs(usersIDs, requestBuilder, new QBEntityCallbackImpl<ArrayList<QBUser>>() {
                    @Override
                    public void onSuccess(ArrayList<QBUser> users, Bundle params) {

                        // Save users
                        //
                        setDialogsUsers(users);

                        callback.onSuccess(dialogs, null);
                    }

                    @Override
                    public void onError(List<String> errors) {
                        callback.onError(errors);
                    }

                });
            }

            @Override
            public void onError(List<String> errors) {
                callback.onError(errors);
            }
        });
    }


    private Map<Integer, QBUser> dialogsUsers = new HashMap<Integer, QBUser>();

    public Map<Integer, QBUser> getDialogsUsers() {
        return dialogsUsers;
    }

    public void setDialogsUsers(List<QBUser> setUsers) {
        dialogsUsers.clear();

        for (QBUser user : setUsers) {
            dialogsUsers.put(user.getId(), user);
        }
    }

    public void addDialogsUsers(List<QBUser> newUsers) {
        for (QBUser user : newUsers) {
            dialogsUsers.put(user.getId(), user);
        }
    }

    public QBUser getCurrentUser(){
        return QBChatService.getInstance().getUser();
    }

    public Integer getOpponentIDForPrivateDialog(QBDialog dialog){
        Integer opponentID = -1;
        for(Integer userID : dialog.getOccupants()){
            if(!userID.equals(getCurrentUser().getId())){
                opponentID = userID;
                break;
            }
        }
        return opponentID;
    }


    ConnectionListener chatConnectionListener = new ConnectionListener() {
        @Override
        public void connected(XMPPConnection connection) {
            Log.i(TAG, "connected");
        }

        @Override
        public void authenticated(XMPPConnection connection) {
            Log.i(TAG, "authenticated");
        }

        @Override
        public void connectionClosed() {
            Log.i(TAG, "connectionClosed");
        }

        @Override
        public void connectionClosedOnError(final Exception e) {
            Log.i(TAG, "connectionClosedOnError: " + e.getLocalizedMessage());
        }

        @Override
        public void reconnectingIn(final int seconds) {
            if(seconds % 5 == 0) {
                Log.i(TAG, "reconnectingIn: " + seconds);
            }
        }

        @Override
        public void reconnectionSuccessful() {
            Log.i(TAG, "reconnectionSuccessful");
        }

        @Override
        public void reconnectionFailed(final Exception error) {
            Log.i(TAG, "reconnectionFailed: " + error.getLocalizedMessage());
        }
    };
}
