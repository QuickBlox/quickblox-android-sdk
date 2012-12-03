package com.quickblox.sample.user.helper;

import com.quickblox.module.users.model.QBUser;

import java.util.ArrayList;
import java.util.List;

import static com.quickblox.sample.user.definitions.Consts.EMPTY_STRING;

/**
 * Created with IntelliJ IDEA.
 * User: android
 * Date: 20.11.12
 * Time: 14:18
 */
public class DataHolder {


    private static DataHolder dataHolder;
    private List<QBUser> qbUserList = new ArrayList<QBUser>();
    private QBUser signInQbUser;

    public static synchronized DataHolder getDataHolder() {
        if (dataHolder == null) {
            dataHolder = new DataHolder();
        }
        return dataHolder;
    }

    public void setQbUserList(List<QBUser> qbUserList) {
        this.qbUserList = qbUserList;
    }


    public int getQBUserListSize() {
        return qbUserList.size();
    }

    public String getQBUserName(int index) {
        return qbUserList.get(index).getFullName();
    }

    public List<String> getQbUserTags(int index) {
        return qbUserList.get(index).getTags();
    }

    public QBUser getQBUser(int index) {
        return qbUserList.get(index);
    }

    public QBUser getLastQBUser() {
        return qbUserList.get(qbUserList.size() - 1);
    }

    public void addQbUserToList(QBUser qbUser) {
        qbUserList.add(qbUser);
    }


    public void setSignInQbUser(QBUser singInQbUser) {
        this.signInQbUser = singInQbUser;
    }

    public QBUser getSignInQbUser() {
        return signInQbUser;
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
