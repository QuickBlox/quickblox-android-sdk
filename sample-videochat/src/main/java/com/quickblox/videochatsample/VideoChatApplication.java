package com.quickblox.videochatsample;

import android.app.Application;

import com.quickblox.core.QBSettings;
import com.quickblox.users.model.QBUser;

public class VideoChatApplication extends Application {

    public static final int FIRST_USER_ID = 65421;
    public static final String FIRST_USER_LOGIN = "videoChatUser1";
    public static final String FIRST_USER_PASSWORD = "videoChatUser1pass";
    //
    public static final int SECOND_USER_ID = 65422;
    public static final String SECOND_USER_LOGIN = "videoChatUser2";
    public static final String SECOND_USER_PASSWORD = "videoChatUser2pass";

    private QBUser currentUser;

    @Override
    public void onCreate() {
        super.onCreate();

        // Set QuickBlox credentials here
        //
        QBSettings.getInstance().fastConfigInit("92", "wJHdOcQSxXQGWx5", "BTFsj7Rtt27DAmT");
    }

    public void setCurrentUser(int userId, String userPassword) {
        this.currentUser = new QBUser(userId);
        this.currentUser.setPassword(userPassword);
    }

    public QBUser getCurrentUser() {
        return currentUser;
    }
}
