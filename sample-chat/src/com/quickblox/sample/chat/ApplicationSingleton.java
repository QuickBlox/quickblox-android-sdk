package com.quickblox.sample.chat;

import android.app.Application;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.quickblox.chat.model.QBDialog;
import com.quickblox.users.model.QBUser;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ApplicationSingleton extends Application {

    private static ApplicationSingleton instance;

    public static ApplicationSingleton getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        instance = this;
    }

    private QBUser currentUser;

    private Map<Integer, QBUser> dialogsUsers = new HashMap<Integer, QBUser>();

    public QBUser getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(QBUser currentUser) {
        this.currentUser = currentUser;
    }

    public Map<Integer, QBUser> getDialogsUsers() {
        return dialogsUsers;
    }

    public void setDialogsUsers(List<QBUser> setUsers) {
        dialogsUsers.clear();

        for (QBUser user : setUsers) {
            dialogsUsers.put(user.getId(), user);
        }
    }

    public void addDialogsUsers(List<QBUser> newUsers) {
        for (QBUser user : newUsers) {
            dialogsUsers.put(user.getId(), user);
        }
    }

    public Integer getOpponentIDForPrivateDialog(QBDialog dialog){
        Integer opponentID = -1;
        for(Integer userID : dialog.getOccupants()){
            if(!userID.equals(getCurrentUser().getId())){
                opponentID = userID;
                break;
            }
        }
        return opponentID;
    }

    public int getAppVersion() {
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }
}
