package com.quickblox.sample.chat.utils;

import com.quickblox.chat.QBChatService;
import com.quickblox.chat.model.QBDialog;
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
        for (QBUser user : users) {
            String prefix = chatName.equals("") ? "" : ", ";
            chatName = chatName + prefix + user.getLogin();
        }
        return chatName;
    }
}
