package com.quickblox.sample.chat.utils.qb;

import com.quickblox.chat.model.QBChatDialog;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class QbDialogHolder {

    private static QbDialogHolder instance;
    private List<QBChatDialog> dialogList;

    public static synchronized QbDialogHolder getInstance() {
        if (instance == null) {
            instance = new QbDialogHolder();
        }
        return instance;
    }

    private QbDialogHolder() {
        dialogList = new ArrayList<>();
    }

    public List<QBChatDialog> getDialogList() {
        return dialogList;
    }

    public void clear() {
        dialogList.clear();
    }

    public void addDialogToList(QBChatDialog dialog) {
        if (!dialogList.contains(dialog)) {
            dialogList.add(dialog);
        }
    }

    public void addDialogs(List<QBChatDialog> dialogs) {
        for (QBChatDialog dialog : dialogs) {
            addDialogToList(dialog);
        }
    }

    public void deleteDialogs(Collection<QBChatDialog> dialogs) {
        for (QBChatDialog dialog : dialogs) {
            dialogList.remove(dialog);
        }
    }
}
