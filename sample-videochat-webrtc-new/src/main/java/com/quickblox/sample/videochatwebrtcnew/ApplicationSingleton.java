package com.quickblox.sample.videochatwebrtcnew;

import android.app.Application;

import com.quickblox.chat.model.QBDialog;
import com.quickblox.users.model.QBUser;

import java.util.HashMap;
import java.util.Map;

public class ApplicationSingleton extends Application {

    public final static String OPPONENTS = "opponents";
    public static final String CONFERENCE_TYPE = "conference_type";

    private QBUser currentUser;

    private Map<Integer, QBUser> dialogsUsers = new HashMap<Integer, QBUser>();

    @Override
    public void onCreate() {
        super.onCreate();
    }


    public QBUser getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(QBUser currentUser) {
        this.currentUser = currentUser;
    }

    public Map<Integer, QBUser> getDialogsUsers() {
        return dialogsUsers;
    }


    public Integer getOpponentIDForPrivateDialog(QBDialog dialog) {
        Integer opponentID = -1;
        for (Integer userID : dialog.getOccupants()) {
            if (!userID.equals(getCurrentUser().getId())) {
                opponentID = userID;
                break;
            }
        }
        return opponentID;
    }
}
