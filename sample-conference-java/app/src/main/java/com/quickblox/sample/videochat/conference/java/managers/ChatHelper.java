package com.quickblox.sample.videochat.conference.java.managers;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.quickblox.auth.session.QBSettings;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBRestChatService;
import com.quickblox.chat.model.QBAttachment;
import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.chat.model.QBDialogType;
import com.quickblox.chat.request.QBDialogRequestBuilder;
import com.quickblox.chat.request.QBMessageGetBuilder;
import com.quickblox.content.QBContent;
import com.quickblox.content.model.QBFile;
import com.quickblox.core.LogLevel;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.QBProgressCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.helper.StringifyArrayList;
import com.quickblox.core.request.QBPagedRequestBuilder;
import com.quickblox.core.request.QBRequestGetBuilder;
import com.quickblox.sample.videochat.conference.java.App;
import com.quickblox.sample.videochat.conference.java.async.BaseAsyncTask;
import com.quickblox.sample.videochat.conference.java.utils.SharedPrefsHelper;
import com.quickblox.sample.videochat.conference.java.utils.qb.QBDialogUtils;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.muc.DiscussionHistory;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ChatHelper {
    private static final String TAG = ChatHelper.class.getSimpleName();

    public static final int CHAT_HISTORY_ITEMS_PER_PAGE = 50;
    static final int USERS_PER_PAGE = 100;
    public static final String TOTAL_PAGES_BUNDLE_PARAM = "total_pages";
    static final String CURRENT_PAGE_BUNDLE_PARAM = "current_page";
    private static final String CHAT_HISTORY_ITEMS_SORT_FIELD = "date_sent";

    private Context context;
    private QBChatService qbChatService = QBChatService.getInstance();
    private ArrayList<QBUser> usersLoadedFromDialog = new ArrayList<>();
    private ArrayList<QBUser> usersLoadedFromDialogs = new ArrayList<>();
    private ArrayList<QBUser> usersLoadedFromMessage = new ArrayList<>();
    private ArrayList<QBUser> usersLoadedFromMessages = new ArrayList<>();

    public ChatHelper(Context context) {
        this.context = context;
        QBSettings.getInstance().setLogLevel(LogLevel.DEBUG);
        QBChatService.setDebugEnabled(true);
        QBChatService.setConfigurationBuilder(buildChatConfigs());
        QBChatService.setDefaultPacketReplyTimeout(10000);
        QBChatService.getInstance().setUseStreamManagement(true);
    }

    private static QBChatService.ConfigurationBuilder buildChatConfigs() {
        QBChatService.ConfigurationBuilder configurationBuilder = new QBChatService.ConfigurationBuilder();

        configurationBuilder.setSocketTimeout(App.SOCKET_TIMEOUT);
        configurationBuilder.setUseTls(App.USE_TLS);
        configurationBuilder.setKeepAlive(App.KEEP_ALIVE);
        configurationBuilder.setAutojoinEnabled(App.AUTO_JOIN);
        configurationBuilder.setAutoMarkDelivered(App.AUTO_MARK_DELIVERED);
        configurationBuilder.setReconnectionAllowed(App.RECONNECTION_ALLOWED);
        configurationBuilder.setAllowListenNetwork(App.ALLOW_LISTEN_NETWORK);
        configurationBuilder.setPort(App.CHAT_PORT);

        return configurationBuilder;
    }

    private static String buildDialogNameWithoutUser(String dialogName, String userName) {
        String regex = ", " + userName + "|" + userName + ", ";
        return dialogName.replaceAll(regex, "");
    }

    public boolean isLogged() {
        Log.d(TAG, "Is logged in chat -> " + QBChatService.getInstance().isLoggedIn());
        return QBChatService.getInstance().isLoggedIn();
    }

    public QBUser getCurrentUser() {
        SharedPrefsHelper sharedPrefsHelper = ((App) context).getSharedPrefsHelper();
        return sharedPrefsHelper.getQbUser();
    }

    public void loginToChat(final QBUser user, final QBEntityCallback<Void> callback) {
        Log.d(TAG, "Login to Chat");
        if (!qbChatService.isLoggedIn()) {
            Log.d(TAG, "Not logged in -> QBChatService.login");
            qbChatService.login(user, callback);
        } else {
            Log.d(TAG, "Already Logged In");
            callback.onSuccess(null, null);
        }
    }

    public void join(QBChatDialog chatDialog, final QBEntityCallback<Void> callback) {
        DiscussionHistory history = new DiscussionHistory();
        history.setMaxStanzas(0);
        chatDialog.join(history, callback);
    }

    public void join(List<QBChatDialog> dialogs) throws Exception {
        for (QBChatDialog dialog : dialogs) {
            DiscussionHistory history = new DiscussionHistory();
            history.setMaxStanzas(0);
            dialog.join(history);
        }
    }

    public void createDialogWithSelectedUsers(final List<QBUser> users, String chatName,
                                              final QBEntityCallback<QBChatDialog> callback) {
        QBChatDialog dialog = QBDialogUtils.createDialog(users, chatName);

        QBRestChatService.createChatDialog(dialog).performAsync(new QBEntityCallback<QBChatDialog>() {
            @Override
            public void onSuccess(QBChatDialog qbChatDialog, Bundle bundle) {
                ((App) context).getQBDialogsHolder().addDialog(dialog);
                ((App) context).getQBUsersHolder().putUsers(users);
                callback.onSuccess(qbChatDialog, bundle);
            }

            @Override
            public void onError(QBResponseException e) {
                callback.onError(e);
            }
        });
    }

    public void deletePrivateDialogs(List<QBChatDialog> privateDialogsToDelete, final QBEntityCallback<ArrayList<String>> callback) {
        if (privateDialogsToDelete.size() > 0) {
            StringifyArrayList<String> privateDialogsIds = new StringifyArrayList<>();
            for (QBChatDialog privateDialog : privateDialogsToDelete) {
                privateDialogsIds.add(privateDialog.getDialogId());
            }
            QBRestChatService.deleteDialogs(privateDialogsIds, false, null).performAsync(new QBEntityCallback<ArrayList<String>>() {
                @Override
                public void onSuccess(ArrayList<String> deletedDialogs, Bundle bundle) {
                    callback.onSuccess(deletedDialogs, bundle);
                }

                @Override
                public void onError(QBResponseException e) {
                    callback.onError(e);
                }
            });
        }
    }

    public void leaveGroupDialogs(final List<QBChatDialog> groupDialogsToLeave, final QBEntityCallback<List<QBChatDialog>> callback) {
        if (groupDialogsToLeave.size() > 0) {
            new DeleteGroupDialogsTask(getCurrentUser(), callback).execute(groupDialogsToLeave);
        }
    }

    public void exitFromDialog(QBChatDialog qbDialog, QBEntityCallback<QBChatDialog> callback) {
        try {
            qbDialog.leave();
        } catch (XMPPException | SmackException.NotConnectedException e) {
            if (callback != null) {
                callback.onError(new QBResponseException(e.getMessage()));
            }
        }
        QBUser currentUser = QBChatService.getInstance().getUser();
        QBDialogRequestBuilder qbRequestBuilder = new QBDialogRequestBuilder();
        qbRequestBuilder.removeUsers(currentUser.getId());

        qbDialog.setName(buildDialogNameWithoutUser(qbDialog.getName(), currentUser.getFullName()));

        QBRestChatService.updateChatDialog(qbDialog, qbRequestBuilder).performAsync(callback);
    }

    public void updateDialogUsers(QBUser currentUser, QBChatDialog qbDialog,
                                  final List<QBUser> newQbDialogUsersList,
                                  QBEntityCallback<QBChatDialog> callback) {
        List<QBUser> addedUsers = QBDialogUtils.getAddedUsers(context, currentUser, qbDialog, newQbDialogUsersList);

        QBDialogUtils.logDialogUsers(context, qbDialog);
        QBDialogUtils.logUsers(addedUsers);
        Log.w(TAG, "=======================");

        QBDialogRequestBuilder qbRequestBuilder = new QBDialogRequestBuilder();
        if (!addedUsers.isEmpty()) {
            qbRequestBuilder.addUsers(addedUsers.toArray(new QBUser[addedUsers.size()]));
        }

        QBRestChatService.updateChatDialog(qbDialog, qbRequestBuilder).performAsync(new QBEntityCallback<QBChatDialog>() {
            @Override
            public void onSuccess(QBChatDialog qbChatDialog, Bundle bundle) {
                ((App) context).getQBUsersHolder().putUsers(newQbDialogUsersList);
                QBDialogUtils.logDialogUsers(context, qbDialog);
                callback.onSuccess(qbChatDialog, bundle);
            }

            @Override
            public void onError(QBResponseException e) {
                callback.onError(e);
            }
        });
    }

    public void loadChatHistory(QBChatDialog dialog, int skipPagination,
                                final QBEntityCallback<ArrayList<QBChatMessage>> callback) {
        QBMessageGetBuilder messageGetBuilder = new QBMessageGetBuilder();
        messageGetBuilder.setSkip(skipPagination);
        messageGetBuilder.setLimit(CHAT_HISTORY_ITEMS_PER_PAGE);
        messageGetBuilder.sortDesc(CHAT_HISTORY_ITEMS_SORT_FIELD);
        messageGetBuilder.markAsRead(false);

        QBRestChatService.getDialogMessages(dialog, messageGetBuilder).performAsync(new QBEntityCallback<ArrayList<QBChatMessage>>() {
            @Override
            public void onSuccess(ArrayList<QBChatMessage> qbChatMessages, Bundle bundle) {
                Set<Integer> userIds = new HashSet<>();
                for (QBChatMessage message : qbChatMessages) {
                    userIds.add(message.getSenderId());
                }

                if (!userIds.isEmpty()) {
                    getUsersFromMessages(qbChatMessages, userIds, callback);
                } else {
                    callback.onSuccess(qbChatMessages, bundle);
                }
                // Not calling super.onSuccess() because
                // we're want to load chat users before triggering the callback
            }

            @Override
            public void onError(QBResponseException e) {
                callback.onError(e);
            }
        });
    }

    public void getDialogs(QBRequestGetBuilder requestBuilder, final QBEntityCallback<ArrayList<QBChatDialog>> callback) {
        QBRestChatService.getChatDialogs(QBDialogType.GROUP, requestBuilder).performAsync(new QBEntityCallback<ArrayList<QBChatDialog>>() {
            @Override
            public void onSuccess(ArrayList<QBChatDialog> qbChatDialogs, Bundle bundle) {
                getUsersFromDialogs(qbChatDialogs, callback);
                // Not calling super.onSuccess() because
                // we want to load chat users before triggering callback
            }

            @Override
            public void onError(QBResponseException e) {
                callback.onError(e);
            }
        });
    }

    public void getUsersFromDialog(QBChatDialog dialog, final QBEntityCallback<ArrayList<QBUser>> callback) {
        List<Integer> userIds = dialog.getOccupants();
        QBPagedRequestBuilder requestBuilder = new QBPagedRequestBuilder(USERS_PER_PAGE, 1);
        usersLoadedFromDialog.clear();
        loadUsersByIDsFromDialog(userIds, requestBuilder, callback);
    }

    private void loadUsersByIDsFromDialog(final List<Integer> userIDs, final QBPagedRequestBuilder requestBuilder, final QBEntityCallback<ArrayList<QBUser>> callback) {
        QBUsers.getUsersByIDs(userIDs, requestBuilder).performAsync(new QBEntityCallback<ArrayList<QBUser>>() {
            @Override
            public void onSuccess(ArrayList<QBUser> qbUsers, Bundle bundle) {
                if (qbUsers != null) {
                    usersLoadedFromDialog.addAll(qbUsers);
                    ((App) context).getQBUsersHolder().putUsers(qbUsers);
                    if (bundle != null) {
                        int totalPages = (int) bundle.get(TOTAL_PAGES_BUNDLE_PARAM);
                        int currentPage = (int) bundle.get(CURRENT_PAGE_BUNDLE_PARAM);
                        if (totalPages > currentPage) {
                            requestBuilder.setPage(currentPage + 1);
                            loadUsersByIDsFromDialog(userIDs, requestBuilder, callback);
                        } else {
                            callback.onSuccess(usersLoadedFromDialog, bundle);
                        }
                    }
                }
            }

            @Override
            public void onError(QBResponseException e) {
                callback.onError(e);
            }
        });
    }

    public void loadFileAsAttachment(File file, QBEntityCallback<QBAttachment> callback,
                                     QBProgressCallback progressCallback) {
        QBContent.uploadFileTask(file, false, null, progressCallback).performAsync(new QBEntityCallback<QBFile>() {
            @Override
            public void onSuccess(QBFile qbFile, Bundle bundle) {
                String type = "file";
                if (qbFile.getContentType().contains(QBAttachment.IMAGE_TYPE)) {
                    type = QBAttachment.IMAGE_TYPE;
                } else if (qbFile.getContentType().contains(QBAttachment.VIDEO_TYPE)) {
                    type = QBAttachment.VIDEO_TYPE;
                }
                QBAttachment attachment = new QBAttachment(type);
                attachment.setId(qbFile.getUid());
                attachment.setSize(qbFile.getSize());
                attachment.setName(qbFile.getName());
                attachment.setContentType(qbFile.getContentType());
                callback.onSuccess(attachment, bundle);
            }

            @Override
            public void onError(QBResponseException e) {
                callback.onError(e);
            }
        });
    }

    private void getUsersFromDialogs(final ArrayList<QBChatDialog> dialogs, final QBEntityCallback<ArrayList<QBChatDialog>> callback) {
        Set<Integer> userIds = new HashSet<>();
        for (QBChatDialog dialog : dialogs) {
            userIds.addAll(dialog.getOccupants());
            userIds.add(dialog.getLastMessageUserId());
        }

        QBPagedRequestBuilder requestBuilder = new QBPagedRequestBuilder(USERS_PER_PAGE, 1);
        usersLoadedFromDialogs.clear();
        loadUsersByIDsFromDialogs(userIds, requestBuilder, new QBEntityCallback<ArrayList<QBUser>>() {
            @Override
            public void onSuccess(ArrayList<QBUser> qbUsers, Bundle bundle) {
                callback.onSuccess(dialogs, bundle);
            }

            @Override
            public void onError(QBResponseException e) {
                callback.onError(e);
            }
        });
    }

    private void loadUsersByIDsFromDialogs(final Collection<Integer> userIDs, final QBPagedRequestBuilder requestBuilder, final QBEntityCallback<ArrayList<QBUser>> callback) {
        QBUsers.getUsersByIDs(userIDs, requestBuilder).performAsync(new QBEntityCallback<ArrayList<QBUser>>() {
            @Override
            public void onSuccess(ArrayList<QBUser> qbUsers, Bundle bundle) {
                if (qbUsers != null) {
                    usersLoadedFromDialogs.addAll(qbUsers);
                    ((App) context).getQBUsersHolder().putUsers(qbUsers);
                    if (bundle != null) {
                        int totalPages = (int) bundle.get(TOTAL_PAGES_BUNDLE_PARAM);
                        int currentPage = (int) bundle.get(CURRENT_PAGE_BUNDLE_PARAM);
                        if (totalPages > currentPage) {
                            requestBuilder.setPage(currentPage + 1);
                            loadUsersByIDsFromDialogs(userIDs, requestBuilder, callback);
                        } else {
                            callback.onSuccess(usersLoadedFromDialogs, bundle);
                        }
                    }
                }
            }

            @Override
            public void onError(QBResponseException e) {
                callback.onError(e);
            }
        });

    }

    private void getUsersFromMessages(final ArrayList<QBChatMessage> messages,
                                      final Set<Integer> userIds,
                                      final QBEntityCallback<ArrayList<QBChatMessage>> callback) {

        QBPagedRequestBuilder requestBuilder = new QBPagedRequestBuilder(USERS_PER_PAGE, 1);
        usersLoadedFromMessages.clear();
        loadUsersByIDsFromMessages(userIds, requestBuilder, new QBEntityCallback<ArrayList<QBUser>>() {
            @Override
            public void onSuccess(ArrayList<QBUser> qbUsers, Bundle bundle) {
                callback.onSuccess(messages, bundle);
            }

            @Override
            public void onError(QBResponseException e) {
                callback.onError(e);
            }
        });
    }

    private void loadUsersByIDsFromMessages(final Collection<Integer> userIDs, final QBPagedRequestBuilder requestBuilder, final QBEntityCallback<ArrayList<QBUser>> callback) {
        QBUsers.getUsersByIDs(userIDs, requestBuilder).performAsync(new QBEntityCallback<ArrayList<QBUser>>() {
            @Override
            public void onSuccess(ArrayList<QBUser> qbUsers, Bundle bundle) {
                if (qbUsers != null) {
                    usersLoadedFromMessages.addAll(qbUsers);
                    ((App) context).getQBUsersHolder().putUsers(qbUsers);
                    if (bundle != null) {
                        int totalPages = (int) bundle.get(TOTAL_PAGES_BUNDLE_PARAM);
                        int currentPage = (int) bundle.get(CURRENT_PAGE_BUNDLE_PARAM);
                        if (totalPages > currentPage) {
                            requestBuilder.setPage(currentPage + 1);
                            loadUsersByIDsFromMessages(userIDs, requestBuilder, callback);
                        } else {
                            callback.onSuccess(usersLoadedFromMessages, bundle);
                        }
                    }
                }
            }

            @Override
            public void onError(QBResponseException e) {
                callback.onError(e);
            }
        });
    }

    public void getUsersFromMessage(QBChatMessage message, QBEntityCallback<ArrayList<QBUser>> callback) {
        List<Integer> userIDs = new ArrayList<>();
        Collection<Integer> usersDelivered = message.getDeliveredIds();
        Collection<Integer> usersRead = message.getReadIds();

        userIDs.addAll(usersDelivered);
        userIDs.addAll(usersRead);

        QBPagedRequestBuilder requestBuilder = new QBPagedRequestBuilder(USERS_PER_PAGE, 1);
        usersLoadedFromMessage.clear();
        loadUsersByIDsFromMessage(userIDs, requestBuilder, callback);
    }

    private void loadUsersByIDsFromMessage(final Collection<Integer> userIDs, final QBPagedRequestBuilder requestBuilder, final QBEntityCallback<ArrayList<QBUser>> callback) {
        QBUsers.getUsersByIDs(userIDs, requestBuilder).performAsync(new QBEntityCallback<ArrayList<QBUser>>() {
            @Override
            public void onSuccess(ArrayList<QBUser> qbUsers, Bundle bundle) {
                if (qbUsers != null) {
                    usersLoadedFromMessage.addAll(qbUsers);
                    ((App) context).getQBUsersHolder().putUsers(qbUsers);
                    if (bundle != null) {
                        int totalPages = (int) bundle.get(TOTAL_PAGES_BUNDLE_PARAM);
                        int currentPage = (int) bundle.get(CURRENT_PAGE_BUNDLE_PARAM);
                        if (totalPages > currentPage) {
                            requestBuilder.setPage(currentPage + 1);
                            loadUsersByIDsFromMessage(userIDs, requestBuilder, callback);
                        } else {
                            callback.onSuccess(usersLoadedFromMessage, bundle);
                        }
                    }
                }
            }

            @Override
            public void onError(QBResponseException e) {
                callback.onError(e);
            }
        });
    }

    private static class DeleteGroupDialogsTask extends BaseAsyncTask<List<QBChatDialog>, Void, ArrayList<QBChatDialog>> {
        private QBUser currentUser;
        private QBEntityCallback<List<QBChatDialog>> callback;

        DeleteGroupDialogsTask(QBUser currentUser, QBEntityCallback<List<QBChatDialog>> callback) {
            this.currentUser = currentUser;
            this.callback = callback;
        }

        @Override
        protected ArrayList<QBChatDialog> performInBackground(List<QBChatDialog>... lists) throws Exception {
            List<QBChatDialog> groupsToLeave = lists[0];
            ArrayList<QBChatDialog> successfulDeletedDialogs = new ArrayList<>();

            for (QBChatDialog groupDialog : groupsToLeave) {
                groupDialog.leave();

                final QBDialogRequestBuilder qbRequestBuilder = new QBDialogRequestBuilder();
                qbRequestBuilder.removeUsers(currentUser.getId());

                QBRestChatService.updateChatDialog(groupDialog, qbRequestBuilder).perform();
                successfulDeletedDialogs.add(groupDialog);
            }
            return successfulDeletedDialogs;
        }

        @Override
        protected void onResult(ArrayList<QBChatDialog> qbChatDialogs) {
            if (callback != null) {
                callback.onSuccess(qbChatDialogs, null);
            }
        }

        @Override
        public void onException(Exception e) {
            super.onException(e);
            callback.onError(new QBResponseException(e.getMessage()));
        }
    }
}