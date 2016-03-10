package com.quickblox.sample.user.helper;

import com.quickblox.users.model.QBUser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class DataHolder {

    private static DataHolder instance;
    private Map<Integer, QBUser> qbUsers;
    private QBUser signInQbUser;

    private DataHolder() {
        qbUsers = new TreeMap<>(Collections.reverseOrder());
    }

    public static synchronized DataHolder getInstance() {
        if (instance == null) {
            instance = new DataHolder();
        }
        return instance;
    }

    public void addQbUsers(List<QBUser> qbUsersList) {
        for (QBUser qbUser : qbUsersList) {
            addQbUser(qbUser);
        }
    }

    public void addQbUser(QBUser qbUser) {
        qbUsers.put(qbUser.getId(), qbUser);
    }

    public ArrayList<QBUser> getQBUsersList() {
        return new ArrayList<>(qbUsers.values());
    }

    public void clear() {
        qbUsers.clear();
    }

    public QBUser getQBUser(int id) {
        return qbUsers.get(id);
    }

    public QBUser getSignInQbUser() {
        return signInQbUser;
    }

    public void setSignInQbUser(QBUser singInQbUser) {
        this.signInQbUser = singInQbUser;
    }
}