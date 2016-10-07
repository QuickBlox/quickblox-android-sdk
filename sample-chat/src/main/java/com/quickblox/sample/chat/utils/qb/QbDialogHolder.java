package com.quickblox.sample.chat.utils.qb;

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

public class QbDialogHolder {

    private static QbDialogHolder instance;
    private Map<String, QBChatDialog> dialogsMap;

    public static synchronized QbDialogHolder getInstance() {
        if (instance == null) {
            instance = new QbDialogHolder();
        }
        return instance;
    }

    private QbDialogHolder() {
        dialogsMap = new TreeMap<>();
    }

    public Map<String, QBChatDialog> getDialogs() {
        return getSortedMap(dialogsMap);
    }

    public QBChatDialog getChatDialogById(String dialogId){
        return dialogsMap.get(dialogId);
    }

    public void clear() {
        dialogsMap.clear();
    }

    public void addDialog(QBChatDialog dialog) {
        if (dialog != null) {
            dialogsMap.put(dialog.getDialogId(), dialog);
        }
    }

    public void addDialogs(List<QBChatDialog> dialogs) {
        for (QBChatDialog dialog : dialogs) {
            addDialog(dialog);
        }
    }

    public void deleteDialogs(Collection<QBChatDialog> dialogs) {
        for (QBChatDialog dialog : dialogs) {
            deleteDialog(dialog);
        }
    }

    public void deleteDialogs(ArrayList<String> dialogsIds) {
        for (String dialogId : dialogsIds) {
            deleteDialog(dialogId);
        }
    }

    public void deleteDialog(QBChatDialog chatDialog){
        dialogsMap.remove(chatDialog.getDialogId());
    }

    public void deleteDialog(String dialogId){
        dialogsMap.remove(dialogId);
    }

    public boolean hasDialogWithId(String dialogId){
        return dialogsMap.containsKey(dialogId);
    }

    public boolean hasPrivateDialogWithUser(QBUser user){
        return getPrivateDialogWithUser(user) != null;
    }

    public QBChatDialog getPrivateDialogWithUser(QBUser user){
        for (QBChatDialog chatDialog : dialogsMap.values()){
            if (QBDialogType.PRIVATE.equals(chatDialog.getType())
                    && chatDialog.getOccupants().contains(user.getId())){
                return chatDialog;
            }
        }

        return null;
    }

    private Map<String, QBChatDialog> getSortedMap(Map <String, QBChatDialog> unsortedMap){
        Map <String, QBChatDialog> sortedMap = new TreeMap(new LastMessageDateSentComparator(unsortedMap));
        sortedMap.putAll(unsortedMap);
        return sortedMap;
    }

    public void updateDialog(String dialogId, QBChatMessage qbChatMessage){
        QBChatDialog updatedDialog = getChatDialogById(dialogId);
        updatedDialog.setLastMessage(qbChatMessage.getBody());
        updatedDialog.setLastMessageDateSent(qbChatMessage.getDateSent());
        updatedDialog.setUnreadMessageCount(updatedDialog.getUnreadMessageCount() != null
                ? updatedDialog.getUnreadMessageCount() + 1 : 1);
        updatedDialog.setLastMessageUserId(qbChatMessage.getSenderId());

        dialogsMap.put(updatedDialog.getDialogId(), updatedDialog);
    }

    static class LastMessageDateSentComparator implements Comparator<String> {
        Map <String, QBChatDialog> map;

        public LastMessageDateSentComparator(Map <String, QBChatDialog> map) {

            this.map = map;
        }

        public int compare(String keyA, String keyB) {

            long valueA = map.get(keyA).getLastMessageDateSent();
            long valueB = map.get(keyB).getLastMessageDateSent();

            if (valueB < valueA){
                return -1;
            } else {
                return 1;
            }
        }
    }
}
