package com.quickblox.sample.user.helper;

import com.quickblox.users.model.QBUser;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DataHolder {

    private static DataHolder instance;
    private Map<Integer, QBUser> qbUsers;
    private QBUser signInQbUser;

    private DataHolder() {
        qbUsers = new LinkedHashMap<>();
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

    public Map<Integer, QBUser> getQBUsers() {
        return qbUsers;
    }

    public boolean isEmpty() {
        return qbUsers.size() == 0;
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
