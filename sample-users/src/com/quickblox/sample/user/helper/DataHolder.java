package com.quickblox.sample.user.helper;

import com.quickblox.users.model.QBUser;

import java.util.ArrayList;
import java.util.List;

import static com.quickblox.sample.user.definitions.Consts.EMPTY_STRING;

public class DataHolder {

    private static DataHolder dataHolder;
    private List<QBUser> qbUsersList = new ArrayList<QBUser>();
    private QBUser signInQbUser;

    public static synchronized DataHolder getDataHolder() {
        if (dataHolder == null) {
            dataHolder = new DataHolder();
        }
        return dataHolder;
    }

    public void setQbUsersList(List<QBUser> qbUsersList) {
        this.qbUsersList = qbUsersList;
    }

    public int getQBUserListSize() {
        return qbUsersList.size();
    }

    public String getQBUserName(int index) {
        return qbUsersList.get(index).getFullName();
    }

    public List<String> getQbUserTags(int index) {
        return qbUsersList.get(index).getTags();
    }

    public QBUser getQBUser(int index) {
        return qbUsersList.get(index);
    }

    public QBUser getLastQBUser() {
        return qbUsersList.get(qbUsersList.size() - 1);
    }

    public void addQbUserToList(QBUser qbUser) {
        qbUsersList.add(qbUser);
    }

    public QBUser getSignInQbUser() {
        return signInQbUser;
    }

    public void setSignInQbUser(QBUser singInQbUser) {
        this.signInQbUser = singInQbUser;
    }

    public String getSignInUserOldPassword() {
        return signInQbUser.getOldPassword();
    }

    public int getSignInUserId() {
        return signInQbUser.getId();
    }

    public void setSignInUserPassword(String singInUserPassword) {
        signInQbUser.setOldPassword(singInUserPassword);
    }

    public String getSignInUserLogin() {
        return signInQbUser.getLogin();
    }

    public String getSignInUserEmail() {
        return signInQbUser.getEmail();
    }

    public String getSignInUserFullName() {
        return signInQbUser.getFullName();
    }

    public String getSignInUserPhone() {
        return signInQbUser.getPhone();
    }

    public String getSignInUserWebSite() {
        return signInQbUser.getWebsite();
    }

    public String getSignInUserTags() {
        if (signInQbUser.getTags() != null) {
            return signInQbUser.getTags().getItemsAsString();
        } else {
            return EMPTY_STRING;
        }
    }
}
