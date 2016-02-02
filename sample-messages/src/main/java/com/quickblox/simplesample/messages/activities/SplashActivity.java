package com.quickblox.simplesample.messages.activities;

import android.os.Bundle;

import com.quickblox.auth.QBAuth;
import com.quickblox.auth.model.QBSession;
import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.sample.core.ui.activity.CoreSplashActivity;
import com.quickblox.sample.core.utils.Toaster;
import com.quickblox.simplesample.messages.Consts;
import com.quickblox.simplesample.messages.R;
import com.quickblox.users.model.QBUser;

import java.util.List;

public class SplashActivity extends CoreSplashActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Create QuickBlox session
        QBUser qbUser = new QBUser(Consts.USER_LOGIN, Consts.USER_PASSWORD);
        QBAuth.createSession(qbUser, new QBEntityCallbackImpl<QBSession>() {
            @Override
            public void onSuccess(QBSession qbSession, Bundle bundle) {
                proceedToTheNextActivity();
            }

            @Override
            public void onError(List<String> strings) {
                Toaster.longToast(strings.toString());
            }
        });
    }

    @Override
    protected String getAppName() {
        return getString(R.string.app_title);
    }

    @Override
    protected void proceedToTheNextActivity() {
        MessagesActivity.start(this);
        finish();
    }
}