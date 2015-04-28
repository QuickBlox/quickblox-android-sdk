package com.quickblox.sample.chat.ui.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;

import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.core.QBSettings;
import com.quickblox.auth.QBAuth;
import com.quickblox.auth.model.QBSession;
import com.quickblox.sample.chat.core.ChatService;
import com.quickblox.users.model.QBUser;
import com.quickblox.sample.chat.R;

import java.util.List;

public class SplashActivity extends Activity {

//    private static final String APP_ID = "92";
//    private static final String AUTH_KEY = "wJHdOcQSxXQGWx5";
//    private static final String AUTH_SECRET = "BTFsj7Rtt27DAmT";
//    //
//    private static final String USER_LOGIN = "bobbobbob";
//    private static final String USER_PASSWORD = "bobbobbob";

    private static final String APP_ID = "13037";
    private static final String AUTH_KEY = "cE68h4wB4eFNDvW";
    private static final String AUTH_SECRET = "P5Yt-ZeE9SuePja";
    //
    private static final String USER_LOGIN = "igorquickblox44";
    private static final String USER_PASSWORD = "igorquickblox44";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        QBSettings.getInstance().fastConfigInit(APP_ID, AUTH_KEY, AUTH_SECRET);

        // Login to REST API
        //
        final QBUser user = new QBUser();
        user.setLogin(USER_LOGIN);
        user.setPassword(USER_PASSWORD);

        QBAuth.createSession(user, new QBEntityCallbackImpl<QBSession>() {
            @Override
            public void onSuccess(QBSession session, Bundle args) {

                user.setId(session.getUserId());

                // login to Chat
                //
                ChatService.init(SplashActivity.this);
                ChatService.getInstance().loginToChat(user, new QBEntityCallbackImpl() {

                    @Override
                    public void onSuccess() {
                        // go to Dialogs screen
                        //
                        Intent intent = new Intent(SplashActivity.this, DialogsActivity.class);
                        startActivity(intent);
                        finish();
                    }

                    @Override
                    public void onError(List errors) {
                        AlertDialog.Builder dialog = new AlertDialog.Builder(SplashActivity.this);
                        dialog.setMessage("chat login errors: " + errors).create().show();
                    }
                });
            }

            @Override
            public void onError(List<String> errors) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(SplashActivity.this);
                dialog.setMessage("create session errors: " + errors).create().show();
            }
        });
    }
}