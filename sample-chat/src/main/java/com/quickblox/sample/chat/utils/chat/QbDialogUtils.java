package com.quickblox.sample.chat.utils.chat;

import android.util.Log;

import com.quickblox.chat.model.QBDialog;
import com.quickblox.users.model.QBUser;

import java.util.ArrayList;
import java.util.List;

public class QbDialogUtils {
    private static final String TAG = QbDialogUtils.class.getSimpleName();

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

        QBUser currentUser = ChatUtils.getCurrentUser();
        addedUsers.remove(currentUser);

        return addedUsers;
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

        QBUser currentUser = ChatUtils.getCurrentUser();
        removedUsers.remove(currentUser);

        return removedUsers;
    }

    public static void logDialogUsers(QBDialog qbDialog) {
        Log.v(TAG, "Dialog " + ChatUtils.getDialogName(qbDialog));
        for (Integer id : qbDialog.getOccupants()) {
            QBUser user = QbUsersHolder.getInstance().getUserById(id);
            Log.v(TAG, user.getId() + " " + user.getFullName());
        }
        Log.v(TAG, "===============");
    }

}
