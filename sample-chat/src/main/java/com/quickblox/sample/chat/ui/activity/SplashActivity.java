package com.quickblox.sample.chat.ui.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;

import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.sample.chat.R;
import com.quickblox.sample.chat.utils.Consts;
import com.quickblox.sample.chat.utils.chat.ChatHelper;
import com.quickblox.users.model.QBUser;

import java.util.List;

public class SplashActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Login to the REST API
        QBUser user = new QBUser();
        user.setLogin(Consts.QB_USER_LOGIN);
        user.setPassword(Consts.QB_USER_PASSWORD);

        ChatHelper.initIfNeed(this);
        ChatHelper.getInstance().login(user, new QBEntityCallbackImpl<String>() {
            @Override
            public void onSuccess() {
                DialogsActivity.start(SplashActivity.this);
                finish();
            }

            @Override
            public void onError(List<String> errors) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(SplashActivity.this);
                dialog.setMessage("Chat login errors: " + errors).create().show();
            }
        });
    }
}