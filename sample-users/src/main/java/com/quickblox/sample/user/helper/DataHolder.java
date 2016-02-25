package com.quickblox.sample.user.helper;

import android.util.SparseArray;

import com.quickblox.users.model.QBUser;

import java.util.ArrayList;
import java.util.List;

import static com.quickblox.sample.user.definitions.Consts.EMPTY_STRING;

public class DataHolder {

    private static DataHolder instance;
    private SparseArray<QBUser> qbUsers;
    private QBUser signInQbUser;

    private DataHolder() {
        qbUsers = new SparseArray<>();
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

    public SparseArray<QBUser> getQBUsers() {
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





//    public int getQBUserListSize() {
//        return qbUsersList.size();
//    }
//
//    public String getQBUserName(int index) {
//        return qbUsersList.get(index).getFullName();
//    }
//
//    public List<String> getQbUserTags(int index) {
//        return qbUsersList.get(index).getTags();
//    }
//
//    public QBUser getLastQBUser() {
//        return qbUsersList.get(qbUsersList.size() - 1);
//    }
//
//    public void addQbUserToList(QBUser qbUser) {
//        qbUsersList.add(qbUser);
//    }
//

//
//    public String getSignInUserOldPassword() {
//        return signInQbUser.getOldPassword();
//    }
//
//    public int getSignInUserId() {
//        return signInQbUser.getId();
//    }
//
//    public void setSignInUserPassword(String singInUserPassword) {
//        signInQbUser.setOldPassword(singInUserPassword);
//    }
//
//    public String getSignInUserLogin() {
//        return signInQbUser.getLogin();
//    }
//
//    public String getSignInUserEmail() {
//        return signInQbUser.getEmail();
//    }
//
//    public String getSignInUserFullName() {
//        return signInQbUser.getFullName();
//    }
//
//    public String getSignInUserPhone() {
//        return signInQbUser.getPhone();
//    }
//
//    public String getSignInUserWebSite() {
//        return signInQbUser.getWebsite();
//    }
//
//    public String getSignInUserTags() {
//        if (signInQbUser.getTags() != null) {
//            return signInQbUser.getTags().getItemsAsString();
//        } else {
//            return EMPTY_STRING;
//        }
//    }
}
