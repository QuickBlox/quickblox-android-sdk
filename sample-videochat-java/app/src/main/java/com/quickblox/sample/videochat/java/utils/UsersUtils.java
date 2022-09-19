package com.quickblox.sample.videochat.java.utils;

import com.quickblox.sample.videochat.java.db.UsersDbManager;
import com.quickblox.users.model.QBUser;

import java.util.ArrayList;
import java.util.List;

public class UsersUtils {
    private static SharedPrefsHelper sharedPrefsHelper;
    private static UsersDbManager dbManager;

    public static ArrayList<QBUser> getUsersFromIds(ArrayList<QBUser> existedUsers, List<Integer> allIds) {
        ArrayList<QBUser> users = new ArrayList<>();
        for (Integer userId : allIds) {
            QBUser stubUser = createStubUserById(userId);
            if (!existedUsers.contains(stubUser)) {
                users.add(stubUser);
            }
        }
        users.addAll(existedUsers);

        return users;
    }

    private static QBUser createStubUserById(Integer userId) {
        QBUser stubUser = new QBUser(userId);
        stubUser.setFullName(String.valueOf(userId));
        return stubUser;
    }

    public static ArrayList<Integer> getIdsNotLoadedUsers(ArrayList<QBUser> existedUsers, List<Integer> allIds) {
        ArrayList<Integer> idsNotLoadedUsers = new ArrayList<>();
        for (Integer userId : allIds) {
            QBUser stubUser = createStubUserById(userId);
            if (!existedUsers.contains(stubUser)) {
                idsNotLoadedUsers.add(userId);
            }
        }

        return idsNotLoadedUsers;
    }

    public static void removeUserData() {
        if (sharedPrefsHelper == null) {
            sharedPrefsHelper = SharedPrefsHelper.getInstance();
        }
        sharedPrefsHelper.clearAllData();
        if (dbManager == null) {
            dbManager = UsersDbManager.getInstance();
        }
        dbManager.clearDB();
    }
}