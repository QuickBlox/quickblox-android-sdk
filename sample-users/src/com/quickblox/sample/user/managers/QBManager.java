package com.quickblox.sample.user.managers;

import com.quickblox.core.QBCallback;
import com.quickblox.internal.core.helper.StringifyArrayList;
import com.quickblox.module.users.QBUsers;
import com.quickblox.module.users.model.QBUser;
import com.quickblox.sample.user.definitions.QBQueries;
import com.quickblox.sample.user.helper.DataHolder;

import static com.quickblox.sample.user.definitions.Consts.EMPTY_STRING;

/**
 * Created with IntelliJ IDEA.
 * User: android
 * Date: 23.11.12
 * Time: 16:05
 */
public class QBManager {

    public static void singIn(String login, String password, QBCallback callback, QBQueries context) {
        QBUser qbUser = new QBUser(login, password);
        QBUsers.signIn(qbUser, callback, context);
    }

    public static void updateUser(int userId, String login, String oldPassword, String password, String fullName, String email, String phone, String webSite, String tags, QBCallback callback, QBQueries context) {
        // create QBUser object
        QBUser qbUser = new QBUser();
        if (userId != -1) {
            qbUser.setId(userId);
        }
        if (!DataHolder.getDataHolder().getSignInUserLogin().equals(login)) {
            qbUser.setLogin(login);
        }
        if (!password.equals(EMPTY_STRING)) {
            qbUser.setPassword(password);
            qbUser.setOldPassword(oldPassword);
        }
        qbUser.setFullName(fullName);
        qbUser.setEmail(email);
        qbUser.setPhone(phone);
        qbUser.setWebsite(webSite);
        StringifyArrayList<String> tagList = new StringifyArrayList<String>();
        for (String tag : tags.toString().split(",")) {
            tagList.add(tag);
        }
        qbUser.setTags(tagList);
        QBUsers.updateUser(qbUser, callback, context);
    }

    public static void signOut(QBCallback callback, QBQueries context) {
        QBUsers.signOut(callback, context);
    }

    public static void signUp(String login, String password, QBCallback callback, QBQueries context) {
        QBUser qbUser = new QBUser();
        qbUser.setLogin(login);
        qbUser.setPassword(password);
        QBUsers.signUp(qbUser, callback, context);
    }

}
