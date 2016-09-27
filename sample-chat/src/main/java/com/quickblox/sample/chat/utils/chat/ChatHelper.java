package com.quickblox.sample.chat.utils.chat;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;

import com.quickblox.auth.QBAuth;
import com.quickblox.auth.model.QBSession;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBRestChatService;
import com.quickblox.chat.model.QBAttachment;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.chat.model.QBDialogType;
import com.quickblox.chat.request.QBDialogRequestBuilder;
import com.quickblox.chat.utils.DialogUtils;
import com.quickblox.content.QBContent;
import com.quickblox.content.model.QBFile;
import com.quickblox.core.LogLevel;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.core.QBProgressCallback;
import com.quickblox.core.QBSettings;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.helper.StringifyArrayList;
import com.quickblox.core.request.QBPagedRequestBuilder;
import com.quickblox.core.request.QBRequestGetBuilder;
import com.quickblox.core.request.QBRequestUpdateBuilder;
import com.quickblox.sample.chat.R;
import com.quickblox.sample.chat.utils.SharedPreferencesUtil;
import com.quickblox.sample.chat.utils.qb.QbDialogHolder;
import com.quickblox.sample.chat.utils.qb.QbDialogUtils;
import com.quickblox.sample.chat.utils.qb.QbUsersHolder;
import com.quickblox.sample.chat.utils.qb.callback.QbEntityCallbackTwoTypeWrapper;
import com.quickblox.sample.chat.utils.qb.callback.QbEntityCallbackWrapper;
import com.quickblox.sample.core.utils.Toaster;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.muc.DiscussionHistory;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class ChatHelper {
    private static final String TAG = ChatHelper.class.getSimpleName();

    private static final int AUTO_PRESENCE_INTERVAL_IN_SECONDS = 30;

    public static final int DIALOG_ITEMS_PER_PAGE = 100;
    public static final int CHAT_HISTORY_ITEMS_PER_PAGE = 50;
    private static final String CHAT_HISTORY_ITEMS_SORT_FIELD = "date_sent";

    private static ChatHelper instance;

    private QBChatService qbChatService;

    public static final Handler MAIN_HANDLER = new Handler(Looper.getMainLooper());

    public static synchronized ChatHelper getInstance() {
        if (instance == null) {
            QBSettings.getInstance().setLogLevel(LogLevel.DEBUG);
            QBChatService.setDebugEnabled(true);
            QBChatService.setDefaultAutoSendPresenceInterval(AUTO_PRESENCE_INTERVAL_IN_SECONDS);
            instance = new ChatHelper();
        }
        return instance;
    }

    public static QBUser getCurrentUser() {
        return QBChatService.getInstance().getUser();
    }

    private ChatHelper() {
        qbChatService = QBChatService.getInstance();
        qbChatService.setUseStreamManagement(true);
    }

    public void addConnectionListener(ConnectionListener listener) {
        qbChatService.addConnectionListener(listener);
    }

    public void removeConnectionListener(ConnectionListener listener) {
        qbChatService.removeConnectionListener(listener);
    }

    public void login(final QBUser user, final QBEntityCallback<Void> callback) {
        // Create REST API session on QuickBlox
        QBAuth.createSession(user).performAsync(new QbEntityCallbackTwoTypeWrapper<QBSession, Void>(callback) {
            @Override
            public void onSuccess(QBSession session, Bundle args) {
                user.setId(session.getUserId());
                loginToChat(user, new QbEntityCallbackWrapper<>(callback));
            }
        });
    }

    private void loginToChat(final QBUser user, final QBEntityCallback<Void> callback) {
        if (qbChatService.isLoggedIn()) {
            MAIN_HANDLER.post(new Runnable() {
                @Override
                public void run() {
                    callback.onSuccess(null, null);
                }
            });
            return;
        }

        qbChatService.login(user, new QbEntityCallbackWrapper<Void>(callback) {
            @Override
            public void onSuccess(Void o, Bundle bundle) {
                super.onSuccess(o, bundle);
            }

            @Override
            public void onError(QBResponseException e) {
                super.onError(e);
            }
        });
    }

    public void join(QBChatDialog chatDialog, final QBEntityCallback<Void> callback){
        DiscussionHistory history = new DiscussionHistory();
        history.setMaxStanzas(0);

        chatDialog.join(history, new QBEntityCallback<Void>() {
            @Override
            public void onSuccess(final Void result, final Bundle b) {
                MAIN_HANDLER.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onSuccess(result, b);
                    }
                });
            }

            @Override
            public void onError(final QBResponseException e) {
                MAIN_HANDLER.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onError(e);
                    }
                });
            }
        });
    }

    public void leave(QBChatDialog chatDialog){
        try {
            chatDialog.leave();
        } catch (SmackException.NotConnectedException | XMPPException e) {
            e.printStackTrace();
        }
    }

    public boolean logout() {
        try {
            qbChatService.logout();
            return true;
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void createDialogWithSelectedUsers(final List<QBUser> users,
                                              final QBEntityCallback<QBChatDialog> callback) {

        QBRestChatService.createChatDialog(QbDialogUtils.createDialog(users)).performAsync(
                new QbEntityCallbackWrapper<QBChatDialog>(callback) {
                    @Override
                    public void onSuccess(QBChatDialog dialog, Bundle args) {
                        QbDialogHolder.getInstance().addDialogToMap(dialog);
                        QbUsersHolder.getInstance().putUsers(users);
                        super.onSuccess(dialog, args);
                    }
                });
    }

    public void deleteDialogs(Collection<QBChatDialog> dialogs, final QBEntityCallback<ArrayList<String>> callback) {
        StringifyArrayList<String> dialogsIds = new StringifyArrayList<>();
        for (QBChatDialog dialog : dialogs){
            dialogsIds.add(dialog.getDialogId());
        }

        QBRestChatService.deleteDialogs(dialogsIds, false, null).performAsync(new QBEntityCallback<ArrayList<String>>() {
            @Override
            public void onSuccess(ArrayList<String> removedDialogsIds, Bundle bundle) {
                callback.onSuccess(removedDialogsIds, bundle);
            }

            @Override
            public void onError(QBResponseException e) {

            }
        });
    }

    public void deleteDialog(QBChatDialog qbDialog, QBEntityCallback<Void> callback) {
        if (qbDialog.getType() == QBDialogType.PUBLIC_GROUP){
            Toaster.shortToast(R.string.public_group_chat_cannot_be_deleted);
        } else {
            QBRestChatService.deleteDialog(qbDialog.getDialogId(), true)
                    .performAsync(new QbEntityCallbackWrapper<Void>(callback));
        }
    }

    public void leaveDialog(QBChatDialog qbDialog, QBEntityCallback<QBChatDialog> callback) {
        QBDialogRequestBuilder qbRequestBuilder = new QBDialogRequestBuilder();
        qbRequestBuilder.removeUsers(SharedPreferencesUtil.getQbUser().getId());

        QBRestChatService.updateGroupChatDialog(qbDialog, qbRequestBuilder).performAsync(
                new QbEntityCallbackWrapper<QBChatDialog>(callback) {
                    @Override
                    public void onSuccess(QBChatDialog qbDialog, Bundle bundle) {
                        super.onSuccess(qbDialog, bundle);
                    }
                });
    }

    public void updateDialogUsers(QBChatDialog qbDialog,
                                  final List<QBUser> newQbDialogUsersList,
                                  QBEntityCallback<QBChatDialog> callback) {
        List<QBUser> addedUsers = QbDialogUtils.getAddedUsers(qbDialog, newQbDialogUsersList);
        List<QBUser> removedUsers = QbDialogUtils.getRemovedUsers(qbDialog, newQbDialogUsersList);

        QbDialogUtils.logDialogUsers(qbDialog);
        QbDialogUtils.logUsers(addedUsers);
        Log.w(TAG, "=======================");
        QbDialogUtils.logUsers(removedUsers);

        QBRequestUpdateBuilder qbRequestBuilder = new QBRequestUpdateBuilder();
        if (!addedUsers.isEmpty()) {
            qbRequestBuilder.pushAll("occupants_ids", QbDialogUtils.getUserIds(addedUsers));
        }
        if (!removedUsers.isEmpty()) {
            qbRequestBuilder.pullAll("occupants_ids", QbDialogUtils.getUserIds(removedUsers));
        }
        qbDialog.setName(QbDialogUtils.createChatNameFromUserList(newQbDialogUsersList));

        QBRestChatService.updateGroupChatDialog(qbDialog, qbRequestBuilder).performAsync(
                new QbEntityCallbackWrapper<QBChatDialog>(callback) {
                    @Override
                    public void onSuccess(QBChatDialog qbDialog, Bundle bundle) {
                        QbUsersHolder.getInstance().putUsers(newQbDialogUsersList);
                        QbDialogUtils.logDialogUsers(qbDialog);
                        super.onSuccess(qbDialog, bundle);
                    }
                });
    }

    public void loadChatHistory(QBChatDialog dialog, int skipPagination,
                                final QBEntityCallback<ArrayList<QBChatMessage>> callback) {
        QBRequestGetBuilder customObjectRequestBuilder = new QBRequestGetBuilder();
        customObjectRequestBuilder.setSkip(skipPagination);
        customObjectRequestBuilder.setLimit(CHAT_HISTORY_ITEMS_PER_PAGE);
        customObjectRequestBuilder.sortDesc(CHAT_HISTORY_ITEMS_SORT_FIELD);

        QBRestChatService.getDialogMessages(dialog, customObjectRequestBuilder).performAsync(
                new QbEntityCallbackWrapper<ArrayList<QBChatMessage>>(callback) {
                    @Override
                    public void onSuccess(ArrayList<QBChatMessage> qbChatMessages, Bundle bundle) {
                        getUsersFromMessages(qbChatMessages, callback);
                        // Not calling super.onSuccess() because
                        // we're want to load chat users before triggering the callback
                    }
                });
    }

    public void getDialogs(QBRequestGetBuilder customObjectRequestBuilder, final QBEntityCallback<ArrayList<QBChatDialog>> callback) {
        customObjectRequestBuilder.setLimit(DIALOG_ITEMS_PER_PAGE);

        QBRestChatService.getChatDialogs(null, customObjectRequestBuilder).performAsync(
                new QbEntityCallbackWrapper<ArrayList<QBChatDialog>>(callback) {
                    @Override
                    public void onSuccess(ArrayList<QBChatDialog> dialogs, Bundle args) {
                        Iterator<QBChatDialog> dialogIterator = dialogs.iterator();
                        while (dialogIterator.hasNext()) {
                            QBChatDialog dialog = dialogIterator.next();
                            if (dialog.getType() == QBDialogType.PUBLIC_GROUP) {
                                dialogIterator.remove();
                            }
                        }

                        getUsersFromDialogs(dialogs, callback);
                        // Not calling super.onSuccess() because
                        // we want to load chat users before triggering callback
                    }
                });
    }

    public void getDialogById(String dialogId, final QBEntityCallback <QBChatDialog> callback) {
        QBRestChatService.getChatDialogById(dialogId).performAsync(
                new QBEntityCallback<QBChatDialog>() {
                    @Override
                    public void onSuccess(QBChatDialog chatDialog, Bundle bundle) {
                        callback.onSuccess(chatDialog, bundle);
                    }

                    @Override
                    public void onError(QBResponseException e) {
                        callback.onError(e);
                    }
                });
    }

    public void getUsersFromDialog(QBChatDialog dialog,
                                   final QBEntityCallback<ArrayList<QBUser>> callback) {
        List<Integer> userIds = dialog.getOccupants();

        final ArrayList<QBUser> users = new ArrayList<>(userIds.size());
        for (Integer id : userIds) {
            users.add(QbUsersHolder.getInstance().getUserById(id));
        }

        // If we already have all users in memory
        // there is no need to make REST requests to QB
        if (userIds.size() == users.size()) {
            MAIN_HANDLER.post(new Runnable() {
                @Override
                public void run() {
                    callback.onSuccess(users, null);
                }
            });
            return;
        }

        QBPagedRequestBuilder requestBuilder = new QBPagedRequestBuilder(userIds.size(), 1);
        QBUsers.getUsersByIDs(userIds, requestBuilder).performAsync(
                new QbEntityCallbackWrapper<ArrayList<QBUser>>(callback) {
                    @Override
                    public void onSuccess(ArrayList<QBUser> qbUsers, Bundle bundle) {
                        QbUsersHolder.getInstance().putUsers(qbUsers);
                        onSuccessInMainThread(qbUsers, bundle);
                    }
                });
    }

    public void loadFileAsAttachment(File file, QBEntityCallback<QBAttachment> callback) {
        loadFileAsAttachment(file, callback, null);
    }

    public void loadFileAsAttachment(File file, QBEntityCallback<QBAttachment> callback,
                                     QBProgressCallback progressCallback) {
        QBContent.uploadFileTask(file, true, null, progressCallback).performAsync(
                new QbEntityCallbackTwoTypeWrapper<QBFile, QBAttachment>(callback) {
                    @Override
                    public void onSuccess(QBFile qbFile, Bundle bundle) {
                        QBAttachment attachment = new QBAttachment(QBAttachment.PHOTO_TYPE);
                        attachment.setId(qbFile.getId().toString());
                        attachment.setUrl(qbFile.getPublicUrl());
                        onSuccessInMainThread(attachment, bundle);
                    }
                });
    }

    private void getUsersFromDialogs(final ArrayList<QBChatDialog> dialogs,
                                     final QBEntityCallback<ArrayList<QBChatDialog>> callback) {
        List<Integer> userIds = new ArrayList<>();
        for (QBChatDialog dialog : dialogs) {
            userIds.addAll(dialog.getOccupants());
            userIds.add(dialog.getLastMessageUserId());
        }

        QBPagedRequestBuilder requestBuilder = new QBPagedRequestBuilder(userIds.size(), 1);
        QBUsers.getUsersByIDs(userIds, requestBuilder).performAsync(
                new QbEntityCallbackTwoTypeWrapper<ArrayList<QBUser>, ArrayList<QBChatDialog>>(callback) {
                    @Override
                    public void onSuccess(ArrayList<QBUser> users, Bundle params) {
                        QbUsersHolder.getInstance().putUsers(users);
                        onSuccessInMainThread(dialogs, params);
                    }
                });
    }

    private void getUsersFromMessages(final ArrayList<QBChatMessage> messages,
                                      final QBEntityCallback<ArrayList<QBChatMessage>> callback) {
        Set<Integer> userIds = new HashSet<>();
        for (QBChatMessage message : messages) {
            userIds.add(message.getSenderId());
        }

        QBPagedRequestBuilder requestBuilder = new QBPagedRequestBuilder(userIds.size(), 1);
        QBUsers.getUsersByIDs(userIds, requestBuilder).performAsync(
                new QbEntityCallbackTwoTypeWrapper<ArrayList<QBUser>, ArrayList<QBChatMessage>>(callback) {
                    @Override
                    public void onSuccess(ArrayList<QBUser> users, Bundle params) {
                        QbUsersHolder.getInstance().putUsers(users);
                        onSuccessInMainThread(messages, params);
                    }
                });
    }
}