package com.quickblox.sample.conference.activities;

import android.os.Bundle;

import com.quickblox.sample.conference.R;
import com.quickblox.sample.core.ui.activity.CoreSplashActivity;
import com.quickblox.sample.core.utils.SharedPrefsHelper;

public class SplashActivity extends CoreSplashActivity {

    private SharedPrefsHelper sharedPrefsHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sharedPrefsHelper = SharedPrefsHelper.getInstance();

        if (sharedPrefsHelper.hasQbUser()) {
            startDialogsActivity();
            return;
        }

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
        LoginActivity.start(this);
        finish();
    }

    private void startDialogsActivity() {
        DialogsActivity.start(SplashActivity.this);
        finish();
    }
}