package com.quickblox.sample.chat.utils.qb;

import com.quickblox.chat.model.QBDialog;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class QbDialogHolder {

    private static QbDialogHolder instance;
    private List<QBDialog> dialogList;

    public static synchronized QbDialogHolder getInstance() {
        if (instance == null) {
            instance = new QbDialogHolder();
        }
        return instance;
    }

    private QbDialogHolder() {
        dialogList = new ArrayList<>();
    }

    public List<QBDialog> getDialogList() {
        return dialogList;
    }

    public void clear() {
        dialogList.clear();
    }

    public void addDialogToList(QBDialog dialog) {
        if (!dialogList.contains(dialog)) {
            dialogList.add(dialog);
        }
    }

    public void addDialogs(List<QBDialog> dialogs) {
        for (QBDialog dialog : dialogs) {
            addDialogToList(dialog);
        }
    }

    public void deleteDialogs(Collection<QBDialog> dialogs) {
        for (QBDialog dialog : dialogs) {
            dialogList.remove(dialog);
        }
    }
}
