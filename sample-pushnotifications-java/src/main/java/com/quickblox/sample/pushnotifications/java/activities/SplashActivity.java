package com.quickblox.sample.pushnotifications.java.activities;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.sample.pushnotifications.java.App;
import com.quickblox.sample.pushnotifications.java.R;
import com.quickblox.sample.pushnotifications.java.utils.Consts;
import com.quickblox.users.QBUsers;
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
        signInQB();
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

    private void signInQB() {
        QBUser qbUser = new QBUser(App.USER_LOGIN, App.USER_PASSWORD);
        QBUsers.signIn(qbUser).performAsync(new QBEntityCallback<QBUser>() {
            @Override
            public void onSuccess(QBUser qbUser, Bundle bundle) {
                Log.d(TAG, "SignIn Success: " + qbUser.getId().toString());
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        MessagesActivity.start(SplashActivity.this, message);
                        finish();
                    }
                }, SPLASH_DELAY);
            }

            @Override
            public void onError(QBResponseException e) {
                Log.d(TAG, "SignIn Error: " + e.getLocalizedMessage());
                showSnackbarError(findViewById(R.id.text_splash_app_title),
                        R.string.splash_create_session_error, e, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                signInQB();
                            }
                        });
            }
        });
    }
}