package com.quickblox.sample.chat.utils.chat;

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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatHelper {
    private static final String TAG = ChatHelper.class.getSimpleName();

    private static final int AUTO_PRESENCE_INTERVAL_IN_SECONDS = 30;
    private static final int QB_REQUEST_PAGE_LIMIT = 100;

    private static ChatHelper instance;

    private QBChatService qbChatService;
    private Map<Integer, QBUser> dialogsUsersMap;

    public static synchronized ChatHelper getInstance() {
        if (instance == null) {
            instance = new ChatHelper();
        }
        return instance;
    }

    /**
     * Starts QBChatService initialization
     *
     * @param ctx any Context instance
     * @return true if service wasn't initialized before call
     */
    public static boolean initIfNeed(Context ctx) {
        if (!QBChatService.isInitialized()) {
            QBChatService.setDebugEnabled(true);
            QBChatService.init(ctx);
            Log.d(TAG, "Initialise QBChatService");

            return true;
        }

        return false;
    }

    private ChatHelper() {
        qbChatService = QBChatService.getInstance();
        dialogsUsersMap = new HashMap<>();

        addConnectionListener(new VerboseQbChatConnectionListener());
    }

    public void addConnectionListener(ConnectionListener listener) {
        qbChatService.addConnectionListener(listener);
    }

    public void removeConnectionListener(ConnectionListener listener) {
        qbChatService.removeConnectionListener(listener);
    }

    public void login(final QBUser user, final QBEntityCallback callback) {
        // Create REST API session on QuickBlox
        QBAuth.createSession(user, new QBEntityCallbackImpl<QBSession>() {
            @Override
            public void onSuccess(QBSession session, Bundle args) {
                user.setId(session.getUserId());

                loginToChat(user, new QBEntityCallbackImpl<String>() {
                    @Override
                    public void onSuccess() {
                        callback.onSuccess();
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

    private void loginToChat(final QBUser user, final QBEntityCallback callback) {
        qbChatService.login(user, new QBEntityCallbackImpl<String>() {
            @Override
            public void onSuccess() {
                try {
                    qbChatService.startAutoSendPresence(AUTO_PRESENCE_INTERVAL_IN_SECONDS);
                } catch (SmackException.NotLoggedInException e) {
                    e.printStackTrace();
                }

                callback.onSuccess();
            }

            @Override
            public void onError(List<String> errors) {
                callback.onError(errors);
            }
        });
    }

    public void logout() {
        try {
            qbChatService.logout();
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        }
    }

    public void getDialogs(final QBEntityCallback<ArrayList<QBDialog>> callback) {
        QBRequestGetBuilder customObjectRequestBuilder = new QBRequestGetBuilder();
        customObjectRequestBuilder.setPagesLimit(QB_REQUEST_PAGE_LIMIT);

        QBChatService.getChatDialogs(null, customObjectRequestBuilder, new QBEntityCallbackImpl<ArrayList<QBDialog>>() {
            @Override
            public void onSuccess(final ArrayList<QBDialog> dialogs, Bundle args) {
                getUsersFromDialogs(dialogs, callback);
            }

            @Override
            public void onError(List<String> errors) {
                callback.onError(errors);
            }
        });
    }

    private void getUsersFromDialogs(final ArrayList<QBDialog> dialogs, final QBEntityCallback<ArrayList<QBDialog>> callback) {
        List<Integer> userIds = new ArrayList<>();
        for (QBDialog dialog : dialogs) {
            userIds.addAll(dialog.getOccupants());
        }

        QBPagedRequestBuilder requestBuilder = new QBPagedRequestBuilder(userIds.size(), 1);
        QBUsers.getUsersByIDs(userIds, requestBuilder, new QBEntityCallbackImpl<ArrayList<QBUser>>() {
            @Override
            public void onSuccess(ArrayList<QBUser> users, Bundle params) {
                setDialogsUsers(users);
                callback.onSuccess(dialogs, null);
            }

            @Override
            public void onError(List<String> errors) {
                callback.onError(errors);
            }
        });
    }

    public Map<Integer, QBUser> getDialogsUsersMap() {
        return dialogsUsersMap;
    }

    public List<QBUser> getUsersByIds(List<Integer> ids) {
        List<QBUser> users = new ArrayList<>();
        for (Integer id : ids) {
            QBUser user = dialogsUsersMap.get(id);
            if (user != null) {
                users.add(user);
            }
        }

        return users;
    }

    public void setDialogsUsers(List<QBUser> users) {
        dialogsUsersMap.clear();

        for (QBUser user : users) {
            dialogsUsersMap.put(user.getId(), user);
        }
    }

    public void addDialogsUsers(List<QBUser> users) {
        for (QBUser user : users) {
            dialogsUsersMap.put(user.getId(), user);
        }
    }
}
