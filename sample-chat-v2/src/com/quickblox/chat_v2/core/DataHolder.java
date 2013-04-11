package com.quickblox.chat_v2.core;

import com.quickblox.module.users.model.QBUser;

/**
 * Created with IntelliJ IDEA.
 * User: Andrew Dmitrenko
 * Date: 4/11/13
 * Time: 12:49 PM
 */
public class DataHolder {

    private static DataHolder dataHolder;
    private QBUser qbUser;

    private DataHolder() {

    }


    public static synchronized DataHolder getInstance() {
        if (dataHolder == null) {
            dataHolder = new DataHolder();
        }
        return dataHolder;
    }


    public void setQbUser(QBUser qbUser) {
        this.qbUser = qbUser;
    }

    public QBUser getQbUser() {
        return qbUser;
    }
}
