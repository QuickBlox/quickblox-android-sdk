package com.quickblox.sample.chat.utils.qb;

import com.quickblox.chat.model.QBChatDialog;

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
            dialogsMap.remove(dialog.getDialogId());
        }
    }

    public void deleteDialogs(ArrayList<String> dialogsIds) {
        for (String dialogId : dialogsIds) {
            dialogsMap.remove(dialogId);
        }
    }

    public boolean hadDialogWithId(String dialogId){
        return dialogsMap.containsKey(dialogId);
    }

    private Map<String, QBChatDialog> getSortedMap(Map <String, QBChatDialog> unsortedMap){
        Map <String, QBChatDialog> sortedMap = new TreeMap(new ValueComparator(unsortedMap));
        sortedMap.putAll(unsortedMap);
        return sortedMap;
    }

    class ValueComparator implements Comparator<String> {
        Map <String, QBChatDialog> map;

        public ValueComparator(Map <String, QBChatDialog> map) {

            this.map = map;
        }

        public int compare(String keyA, String keyB) {
            Comparable valueA = map.get(keyA).getLastMessageDateSent();
            Comparable valueB = map.get(keyB).getLastMessageDateSent();
            return valueB.compareTo(valueA);
        }
    }
}
