package com.quickblox.sample.videochat.conference.java.activities;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

import com.quickblox.sample.videochat.conference.java.R;
import com.quickblox.sample.videochat.conference.java.utils.ErrorUtils;
import com.quickblox.sample.videochat.conference.java.utils.SharedPrefsHelper;


public class SplashActivity extends BaseActivity {
    private static final int SPLASH_DELAY = 1500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        fillUI();

        SharedPrefsHelper sharedPrefsHelper = SharedPrefsHelper.getInstance();
        if (sharedPrefsHelper.hasQbUser()) {
            DialogsActivity.start(this);
            finish();
            return;
        }

        new Handler().postDelayed(() -> {
            LoginActivity.start(SplashActivity.this);
            finish();
        }, SPLASH_DELAY);
    }

    private void fillUI() {
        if (actionBar != null) {
            actionBar.hide();
        }
        TextView appNameTextView = findViewById(R.id.text_splash_app_title);
        TextView versionTextView = findViewById(R.id.text_splash_app_version);

        appNameTextView.setText(R.string.app_name);

        try {
            String versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            versionTextView.setText(versionName);
        } catch (PackageManager.NameNotFoundException e) {
            ErrorUtils.showSnackbar(findViewById(R.id.layout_root), R.string.error, e, R.string.dlg_ok, null);
        }
    }
}