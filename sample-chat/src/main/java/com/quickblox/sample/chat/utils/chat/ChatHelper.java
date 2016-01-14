package com.quickblox.sample.chat.utils.chat;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.quickblox.auth.QBAuth;
import com.quickblox.auth.model.QBSession;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.model.QBAttachment;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.chat.model.QBDialog;
import com.quickblox.content.QBContent;
import com.quickblox.content.model.QBFile;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.core.exception.BaseServiceException;
import com.quickblox.core.request.QBPagedRequestBuilder;
import com.quickblox.core.request.QBRequestGetBuilder;
import com.quickblox.core.request.QBRequestUpdateBuilder;
import com.quickblox.sample.chat.utils.qb.QbDialogUtils;
import com.quickblox.sample.chat.utils.qb.QbUsersHolder;
import com.quickblox.sample.chat.utils.qb.VerboseQbChatConnectionListener;
import com.quickblox.sample.chat.utils.qb.callback.QbEntityCallbackTwoTypeWrapper;
import com.quickblox.sample.chat.utils.qb.callback.QbEntityCallbackWrapper;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.SmackException;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ChatHelper {
    private static final String TAG = ChatHelper.class.getSimpleName();

    private static final int AUTO_PRESENCE_INTERVAL_IN_SECONDS = 30;

    private static final int DIALOG_ITEMS_PER_PAGE = 100;
    private static final int CHAT_HISTORY_ITEMS_PER_PAGE = 100;
    private static final String CHAT_HISTORY_ITEMS_SORT_FIELD = "date_sent";

    private static ChatHelper instance;

    private QBChatService qbChatService;

    public static synchronized ChatHelper getInstance() {
        if (instance == null) {
            instance = new ChatHelper();
        }
        return instance;
    }

    public static QBUser getCurrentUser() {
        return QBChatService.getInstance().getUser();
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

        if (getCurrentUser() == null) {
            return true;
        }

        return false;
    }

    private ChatHelper() {
        qbChatService = QBChatService.getInstance();
        addConnectionListener(new VerboseQbChatConnectionListener());
    }

    public void addConnectionListener(ConnectionListener listener) {
        qbChatService.addConnectionListener(listener);
    }

    public void removeConnectionListener(ConnectionListener listener) {
        qbChatService.removeConnectionListener(listener);
    }

    public void login(final QBUser user, final QBEntityCallback<Void> callback) {
        // Create REST API session on QuickBlox
        QBAuth.createSession(user, new QbEntityCallbackTwoTypeWrapper<QBSession, Void>(callback) {
            @Override
            public void onSuccess(QBSession session, Bundle args) {
                user.setId(session.getUserId());
                loginToChat(user, new QbEntityCallbackWrapper<>(callback));
            }
        });
    }

    private void loginToChat(final QBUser user, final QBEntityCallback<Void> callback) {
        qbChatService.login(user, new QbEntityCallbackWrapper<Void>(callback) {
            @Override
            public void onSuccess() {
                try {
                    qbChatService.startAutoSendPresence(AUTO_PRESENCE_INTERVAL_IN_SECONDS);
                } catch (SmackException.NotLoggedInException e) {
                    e.printStackTrace();
                }
                super.onSuccess();
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
        QBChatService.getInstance().getGroupChatManager().createDialog(QbDialogUtils.createDialog(users),
                new QbEntityCallbackWrapper<QBDialog>(callback) {
                    @Override
                    public void onSuccess(QBDialog dialog, Bundle args) {
                        QbUsersHolder.getInstance().putUsers(users);
                        super.onSuccess(dialog, args);
                    }
                }
        );
    }

    public void deleteDialog(QBDialog qbDialog, final QBEntityCallback<Void> callback) {
        QBChatService.getInstance().getGroupChatManager().deleteDialog(qbDialog.getDialogId(),
                new QbEntityCallbackWrapper<>(callback));
    }

    public void updateDialogUsers(QBDialog qbDialog, final List<QBUser> currentDialogUsers, QBEntityCallbackImpl<QBDialog> callback) {
        QbDialogUtils.logDialogUsers(qbDialog);

        List<QBUser> addedUsers = QbDialogUtils.getAddedUsers(qbDialog, currentDialogUsers);
        List<QBUser> removedUsers = QbDialogUtils.getRemovedUsers(qbDialog, currentDialogUsers);

        QBRequestUpdateBuilder qbRequestBuilder = new QBRequestUpdateBuilder();
        for (Integer id : QbDialogUtils.getUserIds(addedUsers)) {
            // FIXME Replace with pushAll
            qbRequestBuilder.push("occupants_ids", id);
        }
        for (Integer id : QbDialogUtils.getUserIds(removedUsers)) {
            // FIXME Replace with pullAll
            qbRequestBuilder.pull("occupants_ids", id);
        }
        qbDialog.setName(QbDialogUtils.createChatNameFromUserList(currentDialogUsers));

        QBChatService.getInstance().getGroupChatManager().updateDialog(qbDialog, qbRequestBuilder,
                new QbEntityCallbackWrapper<QBDialog>(callback) {
                    @Override
                    public void onSuccess(QBDialog qbDialog, Bundle bundle) {
                        QbUsersHolder.getInstance().putUsers(currentDialogUsers);
                        QbDialogUtils.logDialogUsers(qbDialog);
                        super.onSuccess(qbDialog, bundle);
                    }
                });
    }

    public void loadChatHistory(QBDialog dialog, final QBEntityCallbackImpl<ArrayList<QBChatMessage>> callback) {
        QBRequestGetBuilder customObjectRequestBuilder = new QBRequestGetBuilder();
        customObjectRequestBuilder.setPagesLimit(CHAT_HISTORY_ITEMS_PER_PAGE);
        customObjectRequestBuilder.sortDesc(CHAT_HISTORY_ITEMS_SORT_FIELD);

        QBChatService.getDialogMessages(dialog, customObjectRequestBuilder,
                new QbEntityCallbackWrapper<>(callback));
    }

    public void getDialogs(final QBEntityCallback<ArrayList<QBDialog>> callback) {
        QBRequestGetBuilder customObjectRequestBuilder = new QBRequestGetBuilder();
        customObjectRequestBuilder.setPagesLimit(DIALOG_ITEMS_PER_PAGE);

        QBChatService.getChatDialogs(null, customObjectRequestBuilder,
                new QbEntityCallbackWrapper<ArrayList<QBDialog>>(callback) {
                    @Override
                    public void onSuccess(ArrayList<QBDialog> dialogs, Bundle args) {
                        getUsersFromDialogs(dialogs, callback);
                        // Not calling super.onSuccess() because
                        // we're want to load chat users before triggering callback
                    }
                });
    }

    public void getUsersFromDialog(QBDialog dialog, final QBEntityCallback<ArrayList<QBUser>> callback) {
        ArrayList<Integer> userIds = dialog.getOccupants();

        ArrayList<QBUser> users = new ArrayList<>(userIds.size());
        for (Integer id : userIds) {
            users.add(QbUsersHolder.getInstance().getUserById(id));
        }

        // If we already have all users in memory
        // there is no need to make REST requests to QB
        if (userIds.size() == users.size()) {
            callback.onSuccess(users, null);
            return;
        }

        QBPagedRequestBuilder requestBuilder = new QBPagedRequestBuilder(userIds.size(), 1);
        QBUsers.getUsersByIDs(userIds, requestBuilder, new QbEntityCallbackWrapper<ArrayList<QBUser>>(callback) {
            @Override
            public void onSuccess(ArrayList<QBUser> qbUsers, Bundle bundle) {
                QbUsersHolder.getInstance().putUsers(qbUsers);
                super.onSuccess(qbUsers, bundle);
            }
        });
    }

    public void loadFileAsAttachment(File file, QBEntityCallback<QBAttachment> callback) {
        QBContent.uploadFileTask(file, true, null, new QbEntityCallbackTwoTypeWrapper<QBFile, QBAttachment>(callback) {
            @Override
            public void onSuccess(QBFile qbFile, Bundle bundle) {
                QBAttachment attachment = new QBAttachment(QBAttachment.PHOTO_TYPE);
                attachment.setId(qbFile.getId().toString());
                attachment.setUrl(qbFile.getPublicUrl());
                onSuccessInMainThread(attachment, bundle);
            }
        });
    }

    private void getUsersFromDialogs(final ArrayList<QBDialog> dialogs, final QBEntityCallback<ArrayList<QBDialog>> callback) {
        List<Integer> userIds = new ArrayList<>();
        for (QBDialog dialog : dialogs) {
            userIds.addAll(dialog.getOccupants());
        }

        QBPagedRequestBuilder requestBuilder = new QBPagedRequestBuilder(userIds.size(), 1);
        QBUsers.getUsersByIDs(userIds, requestBuilder, new QbEntityCallbackTwoTypeWrapper<ArrayList<QBUser>, ArrayList<QBDialog>>(callback) {
            @Override
            public void onSuccess(ArrayList<QBUser> users, Bundle params) {
                QbUsersHolder.getInstance().putUsers(users);
                onSuccessInMainThread(dialogs, params);
            }
        });
    }
}