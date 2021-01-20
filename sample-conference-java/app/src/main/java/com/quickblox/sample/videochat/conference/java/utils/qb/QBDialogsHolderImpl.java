package com.quickblox.sample.videochat.conference.java.utils.qb;

import android.util.Log;

import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.chat.model.QBDialogType;
import com.quickblox.users.model.QBUser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class QBDialogsHolderImpl implements QBDialogsHolder {
    private static final String TAG = QBDialogsHolderImpl.class.getSimpleName();

    private Map<String, QBChatDialog> dialogsMap;

    public QBDialogsHolderImpl() {
        dialogsMap = new TreeMap<>();
    }

    @Override
    public QBChatDialog getDialogById(String dialogId) {
        return dialogsMap.get(dialogId);
    }

    @Override
    public void clear() {
        dialogsMap.clear();
    }

    @Override
    public Map<String, QBChatDialog> getDialogs() {
        return getSortedMap(dialogsMap);
    }

    @Override
    public void addDialog(QBChatDialog dialog) {
        if (dialog != null) {
            dialogsMap.put(dialog.getDialogId(), dialog);
            Log.d(TAG, "Dialog " + dialog.getDialogId() + " added. Capacity: " + dialogsMap.size());
        }
    }

    @Override
    public void addDialogs(List<QBChatDialog> dialogs) {
        for (QBChatDialog dialog : dialogs) {
            addDialog(dialog);
        }
    }

    @Override
    public void deleteDialogs(Collection<QBChatDialog> dialogs) {
        for (QBChatDialog dialog : dialogs) {
            deleteDialog(dialog);
        }
    }

    @Override
    public void deleteDialogs(ArrayList<String> dialogsIds) {
        for (String dialogId : dialogsIds) {
            deleteDialog(dialogId);
        }
    }

    @Override
    public void deleteDialog(QBChatDialog chatDialog) {
        dialogsMap.remove(chatDialog.getDialogId());
        Log.d(TAG, "Dialog " + chatDialog.getDialogId() + " removed. Capacity: " + dialogsMap.size());
    }

    @Override
    public boolean hasDialogWithId(String dialogId) {
        return dialogsMap.containsKey(dialogId);
    }

    @Override
    public boolean hasPrivateDialogWithUser(QBUser user) {
        return getPrivateDialogWithUser(user) != null;
    }

    @Override
    public void updateDialog(String dialogId, QBChatMessage qbChatMessage) {
        QBChatDialog updatedDialog = getDialogById(dialogId);
        updatedDialog.setLastMessage(qbChatMessage.getBody());
        updatedDialog.setLastMessageDateSent(qbChatMessage.getDateSent());
        updatedDialog.setUnreadMessageCount(updatedDialog.getUnreadMessageCount() != null
                ? updatedDialog.getUnreadMessageCount() + 1 : 1);
        updatedDialog.setLastMessageUserId(qbChatMessage.getSenderId());

        dialogsMap.put(updatedDialog.getDialogId(), updatedDialog);
        Log.d(TAG, "Dialog " + dialogId + " updated. Capacity: " + dialogsMap.size());
    }

    private void deleteDialog(String dialogId) {
        dialogsMap.remove(dialogId);
        Log.d(TAG, "Dialog " + dialogId + " removed. Capacity: " + dialogsMap.size());
    }

    private QBChatDialog getPrivateDialogWithUser(QBUser user) {
        for (QBChatDialog chatDialog : dialogsMap.values()) {
            if (QBDialogType.PRIVATE.equals(chatDialog.getType())
                    && chatDialog.getOccupants().contains(user.getId())) {
                return chatDialog;
            }
        }

        return null;
    }

    private Map<String, QBChatDialog> getSortedMap(Map<String, QBChatDialog> unsortedMap) {
        Map<String, QBChatDialog> sortedMap = new TreeMap<>(new LastMessageDateSentComparator(unsortedMap));
        sortedMap.putAll(unsortedMap);
        return sortedMap;
    }

    static class LastMessageDateSentComparator implements Comparator<String> {
        Map<String, QBChatDialog> map;

        LastMessageDateSentComparator(Map<String, QBChatDialog> map) {
            this.map = map;
        }

        public int compare(String keyA, String keyB) {

            long valueA = map.get(keyA).getLastMessageDateSent();
            long valueB = map.get(keyB).getLastMessageDateSent();

            if (valueB < valueA) {
                return -1;
            } else {
                return 1;
            }
        }
    }
}