package com.quickblox.sample.chat.utils;

import com.quickblox.sample.core.utils.SharedPrefsHelper;
import com.quickblox.users.model.QBUser;

public class SharedPreferencesUtil {
    private static final String QB_USER_LOGIN = "qb_user_login";
    private static final String QB_USER_PASSWORD = "qb_user_password";

    public static void saveQbUser(QBUser qbUser) {
        SharedPrefsHelper helper = SharedPrefsHelper.getInstance();
        helper.savePref(QB_USER_LOGIN, qbUser.getLogin());
        helper.savePref(QB_USER_PASSWORD, qbUser.getPassword());
    }

    public static boolean hasQbUser() {
        SharedPrefsHelper helper = SharedPrefsHelper.getInstance();
        return helper.has(QB_USER_LOGIN) && helper.has(QB_USER_PASSWORD);
    }

    public static QBUser getQbUser() {
        SharedPrefsHelper helper = SharedPrefsHelper.getInstance();

        if (hasQbUser()) {
            String login = helper.getPref(QB_USER_LOGIN);
            String password = helper.getPref(QB_USER_PASSWORD);

            return new QBUser(login, password);
        } else {
            return null;
        }
    }

}
