package com.quickblox.sample.user.activities;

import android.os.Bundle;

import com.quickblox.sample.core.ui.activity.CoreSplashActivity;
import com.quickblox.sample.user.R;

public class SplashActivity extends CoreSplashActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (checkConfigsWithSnackebarError()) {
            proceedToTheNextActivityWithDelay();
        }
    }

    @Override
    protected String getAppName() {
        return getString(R.string.splash_app_title);
    }

    @Override
    protected void proceedToTheNextActivity() {
        UsersListActivity.start(this);
        finish();
    }
}