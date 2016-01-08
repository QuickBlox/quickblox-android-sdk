package com.quickblox.sample.content.activities;

import android.os.Bundle;

import com.quickblox.auth.QBAuth;
import com.quickblox.auth.model.QBSession;
import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.sample.content.R;
import com.quickblox.sample.content.helper.DataHolder;
import com.quickblox.sample.content.utils.Consts;
import com.quickblox.sample.core.ui.activity.CoreSplashActivity;
import com.quickblox.sample.core.utils.ErrorUtils;
import com.quickblox.users.model.QBUser;

import java.util.List;

public class SplashActivity extends CoreSplashActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        QBUser qbUser = new QBUser(Consts.USER_LOGIN, Consts.USER_PASSWORD);

        QBAuth.createSession(qbUser, new QBEntityCallbackImpl<QBSession>() {
            @Override
            public void onSuccess(QBSession qbSession, Bundle bundle) {
                DataHolder.getInstance().setSignInUserId(qbSession.getUserId());
                proceedToTheNextActivity();
            }

            @Override
            public void onError(List<String> errors) {
                ErrorUtils.showErrorDialog(SplashActivity.this, R.string.splash_create_session_error, errors);
            }
        });
    }

    @Override
    protected String getAppName() {
        return getString(R.string.splash_app_title);
    }

    @Override
    protected void proceedToTheNextActivity() {
        GetFileListActivity.start(this);
        finish();
    }
}