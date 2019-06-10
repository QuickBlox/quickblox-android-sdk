package com.quickblox.sample.videochat.conference.java.util;

import android.os.Bundle;

import com.quickblox.chat.QBRestChatService;
import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.chat.model.QBDialogType;
import com.quickblox.chat.request.QBDialogRequestBuilder;
import com.quickblox.chat.utils.DialogUtils;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.helper.StringifyArrayList;
import com.quickblox.core.request.QBPagedRequestBuilder;
import com.quickblox.core.request.QBRequestGetBuilder;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;


public class QBResRequestExecutor {
    private String TAG = QBResRequestExecutor.class.getSimpleName();

    public void signUpNewUser(final QBUser newQbUser, final QBEntityCallback<QBUser> callback) {
        QBUsers.signUp(newQbUser).performAsync(callback);
    }

    public void signInUser(final QBUser currentQbUser, final QBEntityCallback<QBUser> callback) {
        QBUsers.signIn(currentQbUser).performAsync(callback);
    }

    public void signOut() {
        QBUsers.signOut().performAsync(null);
    }

    public void deleteCurrentUser(int currentQbUserID, QBEntityCallback<Void> callback) {
        QBUsers.deleteUser(currentQbUserID).performAsync(callback);
    }

    public void loadDialogs(final QBEntityCallback<ArrayList<QBChatDialog>> callback) {
        QBRequestGetBuilder requestBuilder = new QBRequestGetBuilder();
        requestBuilder.setLimit(100);
        QBRestChatService.getChatDialogs(QBDialogType.GROUP, requestBuilder).performAsync(callback);
    }

    public void loadDialogByID(String dialogId, final QBEntityCallback<QBChatDialog> callback) {
        QBRestChatService.getChatDialogById(dialogId).performAsync(callback);
    }


    public void deleteDialog(QBChatDialog qbDialog, QBEntityCallback<Void> callback) {
        QBRestChatService.deleteDialog(qbDialog.getDialogId(), false)
                .performAsync(callback);
    }

    public void deleteDialogs(Collection<QBChatDialog> dialogs, final QBEntityCallback<ArrayList<String>> callback) {
        StringifyArrayList<String> dialogsIds = new StringifyArrayList<>();
        for (QBChatDialog dialog : dialogs) {
            dialogsIds.add(dialog.getDialogId());
        }

        QBRestChatService.deleteDialogs(dialogsIds, false, null).performAsync(callback);
    }

    public void createDialogWithSelectedUsers(final List<QBUser> users, QBUser currentUser,
                                              final QBEntityCallback<QBChatDialog> callback) {

        QBRestChatService.createChatDialog(createDialog(users, currentUser)).performAsync(new QBEntityCallback<QBChatDialog>() {
            @Override
            public void onSuccess(QBChatDialog dialog, Bundle args) {
                callback.onSuccess(dialog, args);
            }

            @Override
            public void onError(QBResponseException responseException) {
                callback.onError(responseException);
            }
        });
    }

    private static QBChatDialog createDialog(List<QBUser> users, QBUser currentUser) {
        return DialogUtils.buildDialog(DialogUtils.createChatNameFromUserList(users.toArray(new QBUser[users.size()])),
                QBDialogType.GROUP, DialogUtils.getUserIds(users));
    }

    public void loadUsersByTag(final String tag, final QBEntityCallback<ArrayList<QBUser>> callback) {
        QBPagedRequestBuilder requestBuilder = new QBPagedRequestBuilder();
        requestBuilder.setPerPage(50);
        List<String> tags = new LinkedList<>();
        tags.add(tag);

        QBUsers.getUsersByTags(tags, requestBuilder).performAsync(callback);
    }

    public void loadUsersByIds(final Collection<Integer> usersIDs, final QBEntityCallback<ArrayList<QBUser>> callback) {
        QBUsers.getUsersByIDs(usersIDs, null).performAsync(callback);
    }

    public void loadUserById(int userID, QBEntityCallback<QBUser> callback){
        QBUsers.getUser(userID).performAsync(callback);
    }

    public void updateUser(QBUser qbUser, QBEntityCallback<QBUser> callback) {
        QBUsers.updateUser(qbUser).performAsync(callback);
    }

    public void updateDialog(QBChatDialog dialog, QBUser[] users, QBEntityCallback<QBChatDialog> callback) {
        QBDialogRequestBuilder requestBuilder = new QBDialogRequestBuilder();
        requestBuilder.addUsers(users);

        QBRestChatService.updateGroupChatDialog(dialog, requestBuilder).performAsync(callback);
    }
}