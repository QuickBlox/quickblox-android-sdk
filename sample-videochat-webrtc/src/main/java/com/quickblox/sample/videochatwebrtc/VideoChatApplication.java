package com.quickblox.sample.videochatwebrtc;

import android.app.Application;

import com.quickblox.core.QBSettings;
import com.quickblox.users.model.QBUser;

public class VideoChatApplication extends Application {

    public static final int FIRST_USER_ID = 1725743;
    public static final String FIRST_USER_LOGIN = "videoChatWebRTCUser1";
    public static final String FIRST_USER_PASSWORD = "videoChatWebRTCUser1pass";
    //
    public static final int SECOND_USER_ID = 1725744;
    public static final String SECOND_USER_LOGIN = "videoChatWebRTCUser2";
    public static final String SECOND_USER_PASSWORD = "videoChatWebRTCUser2pass";


    @Override
    public void onCreate() {
        super.onCreate();

        // Set QuickBlox credentials here
        //
        QBSettings.getInstance().fastConfigInit("92", "wJHdOcQSxXQGWx5", "BTFsj7Rtt27DAmT");
    }

    private QBUser currentUser;

    public void setCurrentUser(QBUser user) {
        this.currentUser = user;
    }

    public QBUser getCurrentUser() {
        return currentUser;
    }
}
