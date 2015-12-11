package com.quickblox.sample.chat.utils.chat;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.quickblox.auth.QBAuth;
import com.quickblox.auth.model.QBSession;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.chat.model.QBDialog;
import com.quickblox.chat.model.QBDialogType;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.core.exception.BaseServiceException;
import com.quickblox.core.request.QBPagedRequestBuilder;
import com.quickblox.core.request.QBRequestGetBuilder;
import com.quickblox.core.request.QBRequestUpdateBuilder;
import com.quickblox.sample.chat.utils.ChatUtils;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.SmackException;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatHelper {
    private static final String TAG = ChatHelper.class.getSimpleName();

    private static final int AUTO_PRESENCE_INTERVAL_IN_SECONDS = 30;

    private static final int DIALOG_ITEMS_PER_PAGE = 100;
    private static final int CHAT_HISTORY_ITEMS_PER_PAGE = 100;
    private static final String CHAT_HISTORY_ITEMS_SORT_FIELD = "date_sent";

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
     * @return true if QuickBlox session needs to be created
     */
    public static boolean initIfNeed(Context ctx) {
        if (!QBChatService.isInitialized()) {
            Log.d(TAG, "Initialise QBChatService");
            QBChatService.init(ctx);

            return true;
        }

        try {
            String token = QBAuth.getBaseService().getToken();
            Date tokenExpireDate = QBAuth.getBaseService().getTokenExpirationDate();
            boolean isTokenExpired = tokenExpireDate != null && System.currentTimeMillis() >= tokenExpireDate.getTime();
            if (TextUtils.isEmpty(token) || isTokenExpired) {
                Log.d(TAG, "Token is either empty or expired");
                return true;
            }
        } catch (BaseServiceException e) {
            Log.w(TAG, e);
            return true;
        }

        if (ChatUtils.getCurrentUser() == null) {
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

    public void createDialogWithSelectedUsers(final List<QBUser> users, final QBEntityCallbackImpl<QBDialog> callback) {
        QBDialog dialogToCreate = new QBDialog();
        dialogToCreate.setName(ChatUtils.createChatNameFromUserList(users));
        if (users.size() == 1) {
            dialogToCreate.setType(QBDialogType.PRIVATE);
        } else {
            dialogToCreate.setType(QBDialogType.GROUP);
        }
        dialogToCreate.setOccupantsIds(ChatUtils.getUserIds(users));

        QBChatService.getInstance().getGroupChatManager().createDialog(dialogToCreate,
                new QBEntityCallbackImpl<QBDialog>() {
                    @Override
                    public void onSuccess(QBDialog dialog, Bundle args) {
                        addDialogsUsers(users);
                        callback.onSuccess(dialog, args);
                    }

                    @Override
                    public void onError(List<String> errors) {
                        callback.onError(errors);
                    }
                }
        );
    }

    public void addUsersToDialog(final QBDialog qbDialog, final List<QBUser> addedUsers, final QBEntityCallbackImpl<QBDialog> callback) {
        List<QBUser> allDialogUsers = new ArrayList<>();
        for (Integer id : qbDialog.getOccupants()) {
            allDialogUsers.add(dialogsUsersMap.get(id));
        }
        allDialogUsers.addAll(addedUsers);

        QBRequestUpdateBuilder qbRequestBuilder = new QBRequestUpdateBuilder();
        qbRequestBuilder.push("occupants_ids", ChatUtils.getUserIds(addedUsers));
        qbRequestBuilder.push("name", ChatUtils.createChatNameFromUserList(allDialogUsers));

        QBChatService.getInstance().getGroupChatManager().updateDialog(qbDialog, qbRequestBuilder, new QBEntityCallbackImpl<QBDialog>() {
            @Override
            public void onSuccess(QBDialog qbDialog, Bundle bundle) {
                addDialogsUsers(addedUsers);
                callback.onSuccess(qbDialog, bundle);
            }

            @Override
            public void onError(List<String> errors) {
                callback.onError(errors);
            }
        });
    }

    public void loadChatHistory(QBDialog dialog, final QBEntityCallbackImpl<ArrayList<QBChatMessage>> callback) {
        QBRequestGetBuilder customObjectRequestBuilder = new QBRequestGetBuilder();
        customObjectRequestBuilder.setPagesLimit(CHAT_HISTORY_ITEMS_PER_PAGE);
        customObjectRequestBuilder.sortDesc(CHAT_HISTORY_ITEMS_SORT_FIELD);

        QBChatService.getDialogMessages(dialog, customObjectRequestBuilder,
                new QBEntityCallbackImpl<ArrayList<QBChatMessage>>() {
                    @Override
                    public void onSuccess(ArrayList<QBChatMessage> result, Bundle params) {
                        callback.onSuccess(result, params);
                    }

                    @Override
                    public void onError(List<String> errors) {
                        callback.onError(errors);
                    }
                });
    }

    public void getDialogs(final QBEntityCallback<ArrayList<QBDialog>> callback) {
        QBRequestGetBuilder customObjectRequestBuilder = new QBRequestGetBuilder();
        customObjectRequestBuilder.setPagesLimit(DIALOG_ITEMS_PER_PAGE);

        QBChatService.getChatDialogs(null, customObjectRequestBuilder,
                new QBEntityCallbackImpl<ArrayList<QBDialog>>() {
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

    public void getUsersFromDialog(QBDialog dialog, final QBEntityCallback<List<QBUser>> callback) {
        ArrayList<Integer> userIds = dialog.getOccupants();

        List<QBUser> users = new ArrayList<>(userIds.size());
        for (Integer id : userIds) {
            users.add(dialogsUsersMap.get(id));
        }

        // If we already have all users in memory
        // there is no need to make REST request to QB
        if (userIds.size() == users.size()) {
            callback.onSuccess(users, null);
            return;
        }

        QBPagedRequestBuilder requestBuilder = new QBPagedRequestBuilder(userIds.size(), 1);
        QBUsers.getUsersByIDs(userIds, requestBuilder, new QBEntityCallbackImpl<ArrayList<QBUser>>() {
            @Override
            public void onSuccess(ArrayList<QBUser> qbUsers, Bundle bundle) {
                addDialogsUsers(qbUsers);
                callback.onSuccess(qbUsers, bundle);
            }

            @Override
            public void onError(List<String> list) {
                callback.onError(list);
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
                addDialogsUsers(users);
                callback.onSuccess(dialogs, null);
            }

            @Override
            public void onError(List<String> errors) {
                callback.onError(errors);
            }
        });
    }

    public QBUser getQbUserById(Integer id) {
        return dialogsUsersMap.get(id);
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

    public void addDialogsUsers(List<QBUser> users) {
        // Basically in your app you should store users in database
        // And load users to memory on demand
        // We're using runtime map only to simplify app logic
        for (QBUser user : users) {
            dialogsUsersMap.put(user.getId(), user);
        }
    }
}