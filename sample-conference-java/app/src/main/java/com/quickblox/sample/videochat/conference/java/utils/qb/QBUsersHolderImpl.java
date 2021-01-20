package com.quickblox.sample.videochat.conference.java.utils.qb;

import android.util.SparseArray;

import com.quickblox.users.model.QBUser;

import java.util.ArrayList;
import java.util.List;

public class QBUsersHolderImpl implements QBUsersHolder {

    private SparseArray<QBUser> qbUserSparseArray;

    public QBUsersHolderImpl() {
        qbUserSparseArray = new SparseArray<>();
    }

    @Override
    public void putUser(QBUser user) {
        qbUserSparseArray.put(user.getId(), user);
    }

    @Override
    public void putUsers(List<QBUser> users) {
        for (QBUser user : users) {
            if (user != null) {
                putUser(user);
            }
        }
    }

    @Override
    public QBUser getUserById(int userID) {
        return qbUserSparseArray.get(userID);
    }

    @Override
    public List<QBUser> getUsersByIds(List<Integer> usersIDs) {
        List<QBUser> users = new ArrayList<>();
        for (Integer id : usersIDs) {
            QBUser user = getUserById(id);
            if (user != null) {
                users.add(user);
            }
        }

        return users;
    }

    @Override
    public boolean hasAllUsers(List<Integer> usersIDs) {
        for (Integer userId : usersIDs) {
            if (qbUserSparseArray.get(userId) == null) {
                return false;
            }
        }
        return true;
    }
}