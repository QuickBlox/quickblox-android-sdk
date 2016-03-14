package com.quickblox.sample.content.activities;

import android.os.Bundle;

import com.quickblox.auth.QBAuth;
import com.quickblox.auth.model.QBSession;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.sample.content.R;
import com.quickblox.sample.content.utils.Consts;
import com.quickblox.sample.core.ui.activity.CoreSplashActivity;
import com.quickblox.sample.core.utils.Toaster;
import com.quickblox.users.model.QBUser;

public class SplashActivity extends CoreSplashActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        QBUser qbUser = new QBUser(Consts.USER_LOGIN, Consts.USER_PASSWORD);
        QBAuth.createSession(qbUser, new QBEntityCallback<QBSession>() {
            @Override
            public void onSuccess(QBSession qbSession, Bundle bundle) {
                proceedToTheNextActivity();
            }

            @Override
            public void onError(QBResponseException e) {
                Toaster.shortToast(e.getErrors().toString());
            }
        });
    }

    @Override
    protected String getAppName() {
        return getString(R.string.splash_app_title);
    }

    @Override
    protected void proceedToTheNextActivity() {
        GalleryActivity.start(this);
        finish();
    }
}