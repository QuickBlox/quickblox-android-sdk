package com.quickblox.sample.chat.java.utils.qb;

import android.text.TextUtils;
import android.util.Log;

import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.chat.model.QBDialogType;
import com.quickblox.chat.utils.DialogUtils;
import com.quickblox.sample.chat.java.utils.chat.ChatHelper;
import com.quickblox.users.model.QBUser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class QbDialogUtils {
    private static final String TAG = QbDialogUtils.class.getSimpleName();


    public static QBChatDialog createDialog(List<QBUser> users, String chatName) {
        if (isPrivateChat(users)) {
            QBUser currentUser = ChatHelper.getCurrentUser();
            users.remove(currentUser);
        }
        QBChatDialog dialog = DialogUtils.buildDialog(users.toArray(new QBUser[users.size()]));
        if (!TextUtils.isEmpty(chatName)) {
            dialog.setName(chatName);
        }
        return dialog;
    }

    private static boolean isPrivateChat(List<QBUser> users) {
        return users.size() == 2;
    }

    public static List<QBUser> getAddedUsers(QBChatDialog dialog, List<QBUser> currentUsers) {
        return getAddedUsers(getQbUsersFromQbDialog(dialog), currentUsers);
    }

    public static List<QBUser> getAddedUsers(List<QBUser> previousUsers, List<QBUser> currentUsers) {
        List<QBUser> addedUsers = new ArrayList<>();
        for (QBUser currentUser : currentUsers) {
            boolean wasInChatBefore = false;
            for (QBUser previousUser : previousUsers) {
                if (currentUser.getId().equals(previousUser.getId())) {
                    wasInChatBefore = true;
                    break;
                }
            }
            if (!wasInChatBefore) {
                addedUsers.add(currentUser);
            }
        }

        QBUser currentUser = ChatHelper.getCurrentUser();
        addedUsers.remove(currentUser);

        return addedUsers;
    }

    public static List<QBUser> getRemovedUsers(QBChatDialog dialog, List<QBUser> currentUsers) {
        return getRemovedUsers(getQbUsersFromQbDialog(dialog), currentUsers);
    }

    public static List<QBUser> getRemovedUsers(List<QBUser> previousUsers, List<QBUser> currentUsers) {
        List<QBUser> removedUsers = new ArrayList<>();
        for (QBUser previousUser : previousUsers) {
            boolean isUserStillPresented = false;
            for (QBUser currentUser : currentUsers) {
                if (previousUser.getId().equals(currentUser.getId())) {
                    isUserStillPresented = true;
                    break;
                }
            }
            if (!isUserStillPresented) {
                removedUsers.add(previousUser);
            }
        }

        QBUser currentUser = ChatHelper.getCurrentUser();
        removedUsers.remove(currentUser);

        return removedUsers;
    }

    public static void logDialogUsers(QBChatDialog qbDialog) {
        Log.v(TAG, "Dialog " + getDialogName(qbDialog));
        logUsersByIds(qbDialog.getOccupants());
    }

    public static void logUsers(List<QBUser> users) {
        for (QBUser user : users) {
            Log.i(TAG, user.getId() + " " + user.getFullName());
        }
    }

    private static void logUsersByIds(List<Integer> users) {
        for (Integer id : users) {
            QBUser user = QbUsersHolder.getInstance().getUserById(id);
            Log.i(TAG, ((user != null && user.getId() != null) ? (user.getId() + " " + user.getLogin()) : "noId"));
        }
    }

    public static String getDialogName(QBChatDialog dialog) {
        if (dialog.getType().equals(QBDialogType.GROUP)) {
            return dialog.getName();
        } else {
            // It's a private dialog, let's use opponent's name as chat name
            Integer opponentId = dialog.getRecipientId();
            QBUser user = QbUsersHolder.getInstance().getUserById(opponentId);
            if (user != null) {
                return TextUtils.isEmpty(user.getFullName()) ? user.getLogin() : user.getFullName();
            } else {
                return dialog.getName();
            }
        }
    }

    private static List<QBUser> getQbUsersFromQbDialog(QBChatDialog dialog) {
        List<QBUser> previousDialogUsers = new ArrayList<>();
        for (Integer id : dialog.getOccupants()) {
            QBUser user = QbUsersHolder.getInstance().getUserById(id);
            if (user != null) {
                previousDialogUsers.add(user);
            }
        }
        return previousDialogUsers;
    }

    public static List<Integer> getOccupantsIdsListFromString(String occupantIds) {
        List<Integer> occupantIdsList = new ArrayList<>();
        if (occupantIds != null) {
            String[] occupantIdsArray = occupantIds.split(",");
            for (String occupantId : occupantIdsArray) {
                occupantIdsList.add(Integer.valueOf(occupantId.trim()));
            }
        }
        return occupantIdsList;
    }

    public static String getOccupantsIdsStringFromList(Collection<Integer> occupantIdsList) {
        return TextUtils.join(",", occupantIdsList);
    }

    public static String getOccupantsNamesStringFromList(Collection<QBUser> qbUsers) {
        ArrayList<String> userNameList = new ArrayList<>();
        for (QBUser user : qbUsers) {
            if (TextUtils.isEmpty(user.getFullName())) {
                userNameList.add(user.getLogin());
            } else {
                userNameList.add(user.getFullName());
            }
        }
        return TextUtils.join(", ", userNameList);
    }

    public static QBChatDialog buildPrivateChatDialog(String dialogId, Integer recipientId) {
        QBChatDialog chatDialog = DialogUtils.buildPrivateDialog(recipientId);
        chatDialog.setDialogId(dialogId);

        return chatDialog;
    }
}