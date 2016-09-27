package com.quickblox.sample.chat.utils.qb;

import android.util.Log;

import com.quickblox.chat.model.QBChatDialog;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        Log.d("two_messages", "QbDialogHolder.newInstance");
        dialogsMap = new HashMap<>();
    }

    public Map<String, QBChatDialog> getDialogsMap() {
        return dialogsMap;
    }

    public QBChatDialog getChatDialogById(String dialogId){
        return dialogsMap.get(dialogId);
    }

    public void clear() {
        dialogsMap.clear();
    }

    public void addDialogToMap(QBChatDialog dialog) {
        if (dialog != null) {
            dialogsMap.put(dialog.getDialogId(), dialog);
        }
    }

    public void addDialogs(List<QBChatDialog> dialogs) {
        for (QBChatDialog dialog : dialogs) {
            addDialogToMap(dialog);
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
}
