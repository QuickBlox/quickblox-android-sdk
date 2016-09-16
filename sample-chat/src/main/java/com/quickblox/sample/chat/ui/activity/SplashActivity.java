package com.quickblox.sample.chat.ui.activity;

import android.os.Bundle;
import android.view.View;

import com.quickblox.auth.QBAuth;
import com.quickblox.auth.model.QBSession;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.sample.chat.R;
import com.quickblox.sample.chat.utils.SharedPreferencesUtil;
import com.quickblox.sample.core.ui.activity.CoreSplashActivity;

public class SplashActivity extends CoreSplashActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (SharedPreferencesUtil.hasQbUser()) {
            proceedToTheNextActivityWithDelay();
            return;
        }

        createSession();
    }

    @Override
    protected String getAppName() {
        return getString(R.string.splash_app_title);
    }

    @Override
    protected void proceedToTheNextActivity() {
        if (SharedPreferencesUtil.hasQbUser()) {
            DialogsActivity.start(this);
        } else {
            LoginActivity.start(this);
        }
        finish();
    }

    private void createSession() {
        QBAuth.createSession().performAsync(new QBEntityCallback<QBSession>() {
            @Override
            public void onSuccess(QBSession result, Bundle params) {
                proceedToTheNextActivity();
            }

            @Override
            public void onError(QBResponseException e) {
                showSnackbarError(null, R.string.splash_create_session_error, e, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        createSession();
                    }
                });
            }
        });
    }
}