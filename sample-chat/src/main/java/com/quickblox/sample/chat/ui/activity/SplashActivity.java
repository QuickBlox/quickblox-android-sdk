package com.quickblox.sample.chat.ui.activity;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.sample.chat.R;
import com.quickblox.sample.chat.utils.Consts;
import com.quickblox.sample.chat.utils.ErrorUtils;
import com.quickblox.sample.chat.utils.chat.ChatHelper;
import com.quickblox.users.model.QBUser;

import java.util.List;

public class SplashActivity extends Activity {
    private static final int SPLASH_DELAY = 1500;

    private static Handler mainThreadHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        boolean isChatInitializedJustNow = ChatHelper.initIfNeed(this);
        // If QBChatService is already running that means that
        // we already have created session from the last app launch
        // so we just proceeding to the next activity
        if (!isChatInitializedJustNow) {
            proceedToTheNextActivityWithDelay();
            return;
        }

        // Login to the REST API
        QBUser user = new QBUser(Consts.QB_USER_LOGIN, Consts.QB_USER_PASSWORD);
        ChatHelper.getInstance().login(user, new QBEntityCallbackImpl<String>() {
            @Override
            public void onSuccess() {
                proceedToTheNextActivity();
            }

            @Override
            public void onError(List<String> errors) {
                ErrorUtils.showErrorDialog(SplashActivity.this, "Chat login errors: ", errors);
            }
        });
    }

    private void proceedToTheNextActivity() {
        DialogsActivity.start(this);
        finish();
    }

    private void proceedToTheNextActivityWithDelay() {
        mainThreadHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                proceedToTheNextActivity();
            }
        }, SPLASH_DELAY);
    }
}