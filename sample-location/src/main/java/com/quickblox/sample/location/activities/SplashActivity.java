package com.quickblox.sample.location.activities;

import android.os.Bundle;

import com.quickblox.auth.QBAuth;
import com.quickblox.auth.model.QBSession;
import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.sample.core.ui.activity.CoreSplashActivity;
import com.quickblox.sample.core.utils.Toaster;
import com.quickblox.sample.location.R;
import com.quickblox.sample.location.utils.Constants;
import com.quickblox.users.model.QBUser;

import java.util.List;

public class SplashActivity extends CoreSplashActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Create QuickBlox session
        QBUser qbUser = new QBUser(Constants.USER_LOGIN, Constants.USER_PASSWORD);
        QBAuth.createSession(qbUser, new QBEntityCallbackImpl<QBSession>() {
            @Override
            public void onSuccess(QBSession qbSession, Bundle bundle) {
                proceedToTheNextActivity();
            }

            @Override
            public void onError(List<String> errors) {
                Toaster.longToast(getString(R.string.dlg_location_error) + errors);
            }
        });
    }

    @Override
    protected String getAppName() {
        return getString(R.string.splash_app_title);
    }

    @Override
    protected void proceedToTheNextActivity() {
        MapActivity.start(this);
        finish();
    }
}