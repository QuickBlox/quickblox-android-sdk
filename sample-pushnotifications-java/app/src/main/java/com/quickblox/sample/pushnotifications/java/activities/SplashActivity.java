package com.quickblox.sample.pushnotifications.java.activities;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

import com.quickblox.sample.pushnotifications.java.R;
import com.quickblox.sample.pushnotifications.java.utils.Consts;
import com.quickblox.sample.pushnotifications.java.utils.SharedPrefsHelper;
import com.quickblox.users.model.QBUser;

public class SplashActivity extends BaseActivity {
    private static final String TAG = SplashActivity.class.getSimpleName();
    private static final int SPLASH_DELAY = 1500;
    private String message;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            message = getIntent().getExtras().getString(Consts.EXTRA_FCM_MESSAGE);
        }
        fillUI();
        startNextScreen();
    }

    private void fillUI() {
        String versionName = getPackageInfo().versionName;
        TextView appNameTextView = findViewById(R.id.text_splash_app_title);
        TextView versionTextView = findViewById(R.id.text_splash_app_version);

        appNameTextView.setText(getString(R.string.app_title));
        versionTextView.setText(getString(R.string.splash_app_version, versionName));
    }

    private PackageInfo getPackageInfo() {
        try {
            return getPackageManager().getPackageInfo(getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    private void startNextScreen() {
        QBUser qbUser = SharedPrefsHelper.getInstance().getQbUser();

        if (qbUser != null) {
            MessagesActivity.start(SplashActivity.this, message);
            finish();
        } else {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    LoginActivity.start(SplashActivity.this, message);
                    finish();
                }
            }, SPLASH_DELAY);
        }
    }
}