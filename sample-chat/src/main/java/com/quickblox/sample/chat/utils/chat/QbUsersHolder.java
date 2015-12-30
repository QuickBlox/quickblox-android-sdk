package com.quickblox.sample.chat.utils.chat;

import android.util.SparseArray;

import com.quickblox.users.model.QBUser;

import java.util.ArrayList;
import java.util.List;

/**
 * Basically in your app you should store users in database
 * And load users to memory on demand
 * We're using runtime SpaceArray holder just to simplify app logic
 */
public class QbUsersHolder {

    private static QbUsersHolder instance;

    private SparseArray<QBUser> dialogsUsersMap;

    public static synchronized QbUsersHolder getInstance() {
        if (instance == null) {
            instance = new QbUsersHolder();
        }

        return instance;
    }

    private QbUsersHolder() {
        dialogsUsersMap = new SparseArray<>();
    }

    public void putUsers(List<QBUser> users) {
        for (QBUser user : users) {
            putUser(user);
        }
    }

    public void putUser(QBUser user) {
        dialogsUsersMap.put(user.getId(), user);
    }

    public QBUser getUserById(int id) {
        return dialogsUsersMap.get(id);
    }

    public List<QBUser> getUsersByIds(List<Integer> ids) {
        List<QBUser> users = new ArrayList<>();
        for (Integer id : ids) {
            QBUser user = getUserById(id);
            if (user != null) {
                users.add(user);
            }
        }

        return users;
    }

}
