package com.quickblox.sample.chat.utils;

import com.quickblox.sample.core.utils.SharedPrefsHelper;
import com.quickblox.users.model.QBUser;

public class SharedPreferencesUtil {
    private static final String QB_USER_ID = "qb_user_id";
    private static final String QB_USER_LOGIN = "qb_user_login";
    private static final String QB_USER_PASSWORD = "qb_user_password";
    private static final String QB_USER_FULL_NAME = "qb_user_full_name";

    public static void saveQbUser(QBUser qbUser) {
        SharedPrefsHelper helper = SharedPrefsHelper.getInstance();
        helper.save(QB_USER_ID, qbUser.getId());
        helper.save(QB_USER_LOGIN, qbUser.getLogin());
        helper.save(QB_USER_PASSWORD, qbUser.getPassword());
        helper.save(QB_USER_FULL_NAME, qbUser.getFullName());
    }

    public static void removeQbUser() {
        SharedPrefsHelper helper = SharedPrefsHelper.getInstance();
        helper.delete(QB_USER_ID);
        helper.delete(QB_USER_LOGIN);
        helper.delete(QB_USER_PASSWORD);
        helper.delete(QB_USER_FULL_NAME);
    }

    public static boolean hasQbUser() {
        SharedPrefsHelper helper = SharedPrefsHelper.getInstance();
        return helper.has(QB_USER_LOGIN) && helper.has(QB_USER_PASSWORD);
    }

    public static QBUser getQbUser() {
        SharedPrefsHelper helper = SharedPrefsHelper.getInstance();

        if (hasQbUser()) {
            Integer id = helper.get(QB_USER_ID);
            String login = helper.get(QB_USER_LOGIN);
            String password = helper.get(QB_USER_PASSWORD);
            String fullName = helper.get(QB_USER_FULL_NAME);

            QBUser user = new QBUser(login, password);
            user.setId(id);
            user.setFullName(fullName);
            return user;
        } else {
            return null;
        }
    }

}
