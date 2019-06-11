package com.quickblox.sample.chat.java.managers;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBSystemMessagesManager;
import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.sample.chat.java.App;
import com.quickblox.sample.chat.java.R;
import com.quickblox.sample.chat.java.utils.chat.ChatHelper;
import com.quickblox.sample.chat.java.utils.qb.QbDialogHolder;
import com.quickblox.sample.chat.java.utils.qb.QbDialogUtils;
import com.quickblox.sample.chat.java.utils.qb.callback.QbEntityCallbackImpl;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smackx.muc.DiscussionHistory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class DialogsManager {
    private static final String TAG = DialogsManager.class.getSimpleName();

    private static final String PROPERTY_OCCUPANTS_IDS = "current_occupant_ids";
    private static final String PROPERTY_DIALOG_TYPE = "type";
    private static final String PROPERTY_DIALOG_NAME = "room_name";
    private static final String PROPERTY_NEW_OCCUPANTS_IDS = "new_occupants_ids";
    public static final String PROPERTY_NOTIFICATION_TYPE = "notification_type";
    private static final String CREATING_DIALOG = "1";
    private static final String OCCUPANTS_ADDED = "2";
    private static final String OCCUPANT_LEFT = "3";

    private Set<ManagingDialogsCallbacks> managingDialogsCallbackListener = new CopyOnWriteArraySet<>();

    private boolean isMessageCreatedDialog(QBChatMessage message) {
        return CREATING_DIALOG.equals(message.getProperty(PROPERTY_NOTIFICATION_TYPE));
    }

    private boolean isMessageAddedUser(QBChatMessage message) {
        return OCCUPANTS_ADDED.equals(message.getProperty(PROPERTY_NOTIFICATION_TYPE));
    }

    private boolean isMessageLeftUser(QBChatMessage message) {
        return OCCUPANT_LEFT.equals(message.getProperty(PROPERTY_NOTIFICATION_TYPE));
    }

    private QBChatMessage buildMessageCreatedGroupDialog(QBChatDialog dialog) {
        QBChatMessage qbChatMessage = new QBChatMessage();
        qbChatMessage.setDialogId(dialog.getDialogId());
        qbChatMessage.setProperty(PROPERTY_OCCUPANTS_IDS, QbDialogUtils.getOccupantsIdsStringFromList(dialog.getOccupants()));
        qbChatMessage.setProperty(PROPERTY_DIALOG_TYPE, String.valueOf(dialog.getType().getCode()));
        qbChatMessage.setProperty(PROPERTY_DIALOG_NAME, String.valueOf(dialog.getName()));
        qbChatMessage.setProperty(PROPERTY_NOTIFICATION_TYPE, CREATING_DIALOG);
        qbChatMessage.setDateSent(System.currentTimeMillis() / 1000);
        qbChatMessage.setBody(App.getInstance().getString(R.string.new_chat_created, getCurrentUserName()));
        qbChatMessage.setSaveToHistory(true);
        qbChatMessage.setMarkable(true);
        return qbChatMessage;
    }

    private QBChatMessage buildMessageAddedUsers(QBChatDialog dialog, String userIds, String usersNames) {
        QBChatMessage qbChatMessage = new QBChatMessage();
        qbChatMessage.setDialogId(dialog.getDialogId());
        qbChatMessage.setProperty(PROPERTY_NOTIFICATION_TYPE, OCCUPANTS_ADDED);
        qbChatMessage.setProperty(PROPERTY_NEW_OCCUPANTS_IDS, userIds);
        qbChatMessage.setBody(App.getInstance().getString(R.string.occupant_added, getCurrentUserName(), usersNames));
        qbChatMessage.setSaveToHistory(true);
        qbChatMessage.setMarkable(true);
        return qbChatMessage;
    }

    private QBChatMessage buildMessageLeftUser(QBChatDialog dialog) {
        QBChatMessage qbChatMessage = new QBChatMessage();
        qbChatMessage.setDialogId(dialog.getDialogId());
        qbChatMessage.setProperty(PROPERTY_NOTIFICATION_TYPE, OCCUPANT_LEFT);
        qbChatMessage.setBody(App.getInstance().getString(R.string.occupant_left, getCurrentUserName()));
        qbChatMessage.setSaveToHistory(true);
        qbChatMessage.setMarkable(true);
        return qbChatMessage;
    }

    private QBChatDialog buildChatDialogFromNotificationMessage(QBChatMessage qbChatMessage) {
        QBChatDialog chatDialog = new QBChatDialog();
        chatDialog.setDialogId(qbChatMessage.getDialogId());
        chatDialog.setUnreadMessageCount(0);
        return chatDialog;
    }

    private String getCurrentUserName() {
        QBUser currentUser = QBChatService.getInstance().getUser();
        return TextUtils.isEmpty(currentUser.getFullName()) ? currentUser.getLogin() : currentUser.getFullName();
    }

    ////// Sending Notification Messages

    public void sendMessageCreatedDialog(QBChatDialog dialog) {
        QBChatMessage messageCreatingDialog = buildMessageCreatedGroupDialog(dialog);
        try {
            dialog.sendMessage(messageCreatingDialog);
        } catch (SmackException.NotConnectedException ignored) {

        }
    }

    public void sendMessageAddedUsers(final QBChatDialog dialog, final List<Integer> newUsersIds) {
        QBUsers.getUsersByIDs(newUsersIds, null).performAsync(new QBEntityCallback<ArrayList<QBUser>>() {
            @Override
            public void onSuccess(ArrayList<QBUser> qbUsers, Bundle bundle) {
                String usersIds = QbDialogUtils.getOccupantsIdsStringFromList(newUsersIds);
                if (!newUsersIds.isEmpty()) {
                    String usersNames = QbDialogUtils.getOccupantsNamesStringFromList(qbUsers);
                    QBChatMessage messageUsersAdded = buildMessageAddedUsers(dialog, usersIds, usersNames);

                    try {
                        Log.d(TAG, "Sending Notification Message to Opponents about Adding Occupants");
                        dialog.sendMessage(messageUsersAdded);
                    } catch (SmackException.NotConnectedException e) {
                        Log.d(TAG, "Sending Notification Message Error: " + e.getMessage());
                    }
                }
            }

            @Override
            public void onError(QBResponseException ignored) {

            }
        });
    }

    public void sendMessageLeftUser(QBChatDialog dialog) {
        QBChatMessage messageLeftUser = buildMessageLeftUser(dialog);
        try {
            Log.d(TAG, "Sending Notification Message to Opponents about User Left");
            dialog.sendMessage(messageLeftUser);
        } catch (SmackException.NotConnectedException ignored) {

        }
    }

    ////// Sending System Messages

    public void sendSystemMessageAboutCreatingDialog(QBSystemMessagesManager systemMessagesManager, QBChatDialog dialog) {
        QBChatMessage messageCreatingDialog = buildMessageCreatedGroupDialog(dialog);
        prepareSystemMessage(systemMessagesManager, messageCreatingDialog, dialog.getOccupants());
    }

    public void sendSystemMessageAddedUser(final QBSystemMessagesManager systemMessagesManager, final QBChatDialog dialog, final List<Integer> newUsersIds) {
        QBUsers.getUsersByIDs(newUsersIds, null).performAsync(new QBEntityCallback<ArrayList<QBUser>>() {
            @Override
            public void onSuccess(ArrayList<QBUser> qbUsers, Bundle bundle) {
                String usersIds = QbDialogUtils.getOccupantsIdsStringFromList(newUsersIds);
                if (!newUsersIds.isEmpty()) {
                    String usersNames = QbDialogUtils.getOccupantsNamesStringFromList(qbUsers);

                    QBChatMessage messageUsersAdded = buildMessageAddedUsers(dialog, usersIds, usersNames);
                    prepareSystemMessage(systemMessagesManager, messageUsersAdded, dialog.getOccupants());

                    QBChatMessage messageDialogCreated = buildMessageCreatedGroupDialog(dialog);
                    prepareSystemMessage(systemMessagesManager, messageDialogCreated, newUsersIds);
                }
            }

            @Override
            public void onError(QBResponseException ignored) {

            }
        });
    }

    public void sendSystemMessageLeftUser(final QBSystemMessagesManager systemMessagesManager, final QBChatDialog dialog) {
        QBChatMessage messageLeftUser = buildMessageLeftUser(dialog);
        prepareSystemMessage(systemMessagesManager, messageLeftUser, dialog.getOccupants());
    }

    private void prepareSystemMessage(QBSystemMessagesManager systemMessagesManager, QBChatMessage message, List<Integer> occupants) {
        message.setSaveToHistory(false);
        message.setMarkable(false);

        try {
            for (Integer opponentID : occupants) {
                if (!opponentID.equals(QBChatService.getInstance().getUser().getId())) {
                    message.setRecipientId(opponentID);
                    Log.d(TAG, "Sending System Message to " + opponentID);
                    systemMessagesManager.sendSystemMessage(message);
                }
            }
        } catch (SmackException.NotConnectedException e) {
            Log.d(TAG, "Sending System Message Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    ////// Message Receivers

    public void onGlobalMessageReceived(String dialogId, final QBChatMessage chatMessage) {
        Log.d(TAG, "Global Message Received: " + chatMessage.getId());
        if (isMessageCreatedDialog(chatMessage) && !QbDialogHolder.getInstance().hasDialogWithId(dialogId)) {
            loadNewDialogByNotificationMessage(chatMessage);
        }

        if (isMessageAddedUser(chatMessage) || isMessageLeftUser(chatMessage)) {
            if (QbDialogHolder.getInstance().hasDialogWithId(dialogId)) {
                notifyListenersDialogUpdated(dialogId);
            } else {
                loadNewDialogByNotificationMessage(chatMessage);
            }
        }

        if (chatMessage.isMarkable()) {
            if (QbDialogHolder.getInstance().hasDialogWithId(dialogId)) {
                QbDialogHolder.getInstance().updateDialog(dialogId, chatMessage);
                notifyListenersDialogUpdated(dialogId);
            } else {
                ChatHelper.getInstance().getDialogById(dialogId, new QBEntityCallback<QBChatDialog>() {
                    @Override
                    public void onSuccess(QBChatDialog qbChatDialog, Bundle bundle) {
                        Log.d(TAG, "Loading Dialog Successful");
                        loadUsersFromDialog(qbChatDialog);
                        QbDialogHolder.getInstance().addDialog(qbChatDialog);
                        notifyListenersNewDialogLoaded(qbChatDialog);
                    }

                    @Override
                    public void onError(QBResponseException e) {
                        Log.d(TAG, "Loading Dialog Error: " + e.getMessage());
                    }
                });
            }
        }
    }

    public void onSystemMessageReceived(final QBChatMessage systemMessage) {
        Log.d(TAG, "System Message Received: " + systemMessage.getBody() + " Notification Type: " + systemMessage.getProperty(PROPERTY_NOTIFICATION_TYPE));
        onGlobalMessageReceived(systemMessage.getDialogId(), systemMessage);
    }

    ////// END

    private void loadNewDialogByNotificationMessage(QBChatMessage chatMessage) {
        QBChatDialog chatDialog = buildChatDialogFromNotificationMessage(chatMessage);
        ChatHelper.getInstance().getDialogById(chatDialog.getDialogId(), new QBEntityCallback<QBChatDialog>() {
            @Override
            public void onSuccess(final QBChatDialog qbChatDialog, Bundle bundle) {
                Log.d(TAG, "Loading Dialog Successful");
                qbChatDialog.initForChat(QBChatService.getInstance());

                DiscussionHistory history = new DiscussionHistory();
                history.setMaxStanzas(0);

                qbChatDialog.join(history, new QbEntityCallbackImpl() {
                    @Override
                    public void onSuccess(Object o, Bundle bundle) {
                        QbDialogHolder.getInstance().addDialog(qbChatDialog);
                        notifyListenersDialogCreated(qbChatDialog);
                    }
                });
            }

            @Override
            public void onError(QBResponseException e) {
                Log.d(TAG, "Loading Dialog Error: " + e.getMessage());
            }
        });
    }

    private void loadUsersFromDialog(QBChatDialog chatDialog) {
        ChatHelper.getInstance().getUsersFromDialog(chatDialog, new QbEntityCallbackImpl<ArrayList<QBUser>>());
    }

    private void notifyListenersDialogCreated(final QBChatDialog chatDialog) {
        for (ManagingDialogsCallbacks listener : getManagingDialogsCallbackListeners()) {
            listener.onDialogCreated(chatDialog);
        }
    }

    private void notifyListenersDialogUpdated(final String dialogId) {
        for (ManagingDialogsCallbacks listener : getManagingDialogsCallbackListeners()) {
            listener.onDialogUpdated(dialogId);
        }
    }

    private void notifyListenersNewDialogLoaded(final QBChatDialog chatDialog) {
        for (ManagingDialogsCallbacks listener : getManagingDialogsCallbackListeners()) {
            listener.onNewDialogLoaded(chatDialog);
        }
    }

    public void addManagingDialogsCallbackListener(ManagingDialogsCallbacks listener) {
        if (listener != null) {
            managingDialogsCallbackListener.add(listener);
        }
    }

    public void removeManagingDialogsCallbackListener(ManagingDialogsCallbacks listener) {
        managingDialogsCallbackListener.remove(listener);
    }

    private Collection<ManagingDialogsCallbacks> getManagingDialogsCallbackListeners() {
        return Collections.unmodifiableCollection(managingDialogsCallbackListener);
    }

    public interface ManagingDialogsCallbacks {

        void onDialogCreated(QBChatDialog chatDialog);

        void onDialogUpdated(String chatDialog);

        void onNewDialogLoaded(QBChatDialog chatDialog);
    }
}