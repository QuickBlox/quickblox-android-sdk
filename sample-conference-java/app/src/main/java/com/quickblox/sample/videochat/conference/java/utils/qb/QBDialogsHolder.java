package com.quickblox.sample.videochat.conference.java.utils.qb;

import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.users.model.QBUser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface QBDialogsHolder {

    QBChatDialog getDialogById(String dialogId);

    void clear();

    Map<String, QBChatDialog> getDialogs();

    void addDialog(QBChatDialog dialog);

    void addDialogs(List<QBChatDialog> dialogs);

    void deleteDialogs(Collection<QBChatDialog> dialogs);

    void deleteDialogs(ArrayList<String> dialogsIds);

    void deleteDialog(QBChatDialog chatDialog);

    boolean hasDialogWithId(String dialogId);

    boolean hasPrivateDialogWithUser(QBUser user);

    void updateDialog(String dialogId, QBChatMessage qbChatMessage);
}