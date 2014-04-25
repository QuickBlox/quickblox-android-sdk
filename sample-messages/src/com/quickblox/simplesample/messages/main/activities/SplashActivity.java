package com.quickblox.simplesample.messages.main.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.quickblox.core.QBCallback;
import com.quickblox.core.result.Result;
import com.quickblox.module.auth.QBAuth;
import com.quickblox.module.users.model.QBUser;
import com.quickblox.simplesample.messages.R;

import com.quickblox.simplesample.messages.main.utils.DialogUtils;

public class SplashActivity extends Activity implements QBCallback {

    @Override
    public void onComplete(Result result) {
        if (result.isSuccess()) {
            // Show Messages activity
            Intent intent = new Intent(this, MessagesActivity.class);
            startActivity(intent);
            finish();
        } else {
            // Show errors
            DialogUtils.showLong(this, result.getErrors() + "");
        }
    }

    @Override
    public void onComplete(Result result, Object context) {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // ================= QuickBlox ===== Step 2 =================
        // Authorize application with device & user.
        // You can create user on admin.quickblox.com, Users module or through QBUsers.signUp method
        QBUser qbUser = new QBUser();
        qbUser.setLogin(/*Consts.USER_LOGIN*/"sergey1");
        qbUser.setPassword(/*Consts.USER_PASSWORD*/"serik111");

        // Create session with additional parameters
        QBAuth.createSession(qbUser, this);
    }
}