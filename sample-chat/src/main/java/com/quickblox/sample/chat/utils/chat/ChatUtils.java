package com.quickblox.sample.chat.utils.chat;

import android.text.TextUtils;

import com.quickblox.chat.QBChatService;
import com.quickblox.chat.model.QBDialog;
import com.quickblox.chat.model.QBDialogType;
import com.quickblox.users.model.QBUser;

import java.util.ArrayList;
import java.util.List;

public class ChatUtils {

    public static QBUser getCurrentUser() {
        return QBChatService.getInstance().getUser();
    }

    public static Integer getOpponentIdForPrivateDialog(QBDialog dialog) {
        Integer opponentId = -1;
        Integer currentUserId = getCurrentUser().getId();

        for (Integer userId : dialog.getOccupants()) {
            if (!userId.equals(currentUserId)) {
                opponentId = userId;
                break;
            }
        }
        return opponentId;
    }

    public static ArrayList<Integer> getUserIds(List<QBUser> users) {
        ArrayList<Integer> ids = new ArrayList<>();
        for (QBUser user : users) {
            ids.add(user.getId());
        }
        return ids;
    }

    public static String createChatNameFromUserList(List<QBUser> users) {
        String chatName = "";
        QBUser currentUser = getCurrentUser();
        for (QBUser user : users) {
            if (user.getId().equals(currentUser.getId())) {
                continue;
            }

            String prefix = chatName.equals("") ? "" : ", ";
            chatName = chatName + prefix + user.getFullName();
        }
        return chatName;
    }

    public static String getDialogName(QBDialog dialog) {
        if (dialog.getType().equals(QBDialogType.GROUP)) {
            return  dialog.getName();
        } else {
            // It's a private dialog, let's use opponent's name as chat name
            Integer opponentId = ChatUtils.getOpponentIdForPrivateDialog(dialog);
            QBUser user = QbUsersHolder.getInstance().getUserById(opponentId);
            if (user != null) {
                return  TextUtils.isEmpty(user.getFullName()) ? user.getLogin() : user.getFullName();
            }
        }

        return "";
    }
}
