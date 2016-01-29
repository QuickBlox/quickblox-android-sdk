package com.quickblox.sample.core.ui.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.StringRes;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.quickblox.sample.core.R;
import com.quickblox.sample.core.utils.ErrorUtils;
import com.quickblox.sample.core.utils.VersionUtils;

import java.util.List;

public abstract class CoreSplashActivity extends CoreBaseActivity {
    private static final int SPLASH_DELAY = 1500;

    private static Handler mainThreadHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_ACTION_BAR);
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setContentView(R.layout.activity_splash);

        TextView appNameTextView = _findViewById(R.id.text_splash_app_title);
        TextView versionTextView = _findViewById(R.id.text_splash_app_version);

        appNameTextView.setText(getAppName());
        versionTextView.setText(getString(R.string.splash_app_version, VersionUtils.getAppVersionName()));
    }

    protected abstract String getAppName();

    protected abstract void proceedToTheNextActivity();

    protected void proceedToTheNextActivityWithDelay() {
        mainThreadHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                proceedToTheNextActivity();
            }
        }, SPLASH_DELAY);
    }

    protected void showSnackbarError(@StringRes int resId, List<String> errors, View.OnClickListener clickListener) {
        View rootLayout = findViewById(R.id.layout_root);
        ErrorUtils.showSnackbar(rootLayout, resId, errors, R.string.dlg_retry, clickListener);
    }
}
