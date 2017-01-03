package com.quickblox.sample.pushnotifications.activities;

import android.os.Bundle;
import android.view.View;

import com.quickblox.auth.session.QBSessionManager;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.sample.core.ui.activity.CoreSplashActivity;
import com.quickblox.sample.core.utils.configs.ConfigParser;
import com.quickblox.sample.core.utils.constant.GcmConsts;
import com.quickblox.sample.pushnotifications.R;
import com.quickblox.sample.pushnotifications.utils.Consts;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class SplashActivity extends CoreSplashActivity {
    private String message;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            message = getIntent().getExtras().getString(GcmConsts.EXTRA_GCM_MESSAGE);
        }

        initAppConfig();
    }

    private void initAppConfig() {
        String userLogin;
        String userPassword;

        try {
            JSONObject appConfigs = new ConfigParser().getConfigsAsJson(Consts.APP_CONFIG_FILE_NAME);
            userLogin = appConfigs.getString(Consts.USER_LOGIN_FIELD_NAME);
            userPassword = appConfigs.getString(Consts.USER_PASSWORD_FIELD_NAME);
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            showSnackbarError(null, R.string.init_configs_error, null, null);
            return;
        }

        signInQB(new QBUser(userLogin, userPassword));
    }

    private void signInQB(final QBUser qbUser) {
        if (!checkSignIn()) {
            QBUsers.signIn(qbUser).performAsync(new QBEntityCallback<QBUser>() {
                @Override
                public void onSuccess(QBUser qbUser, Bundle bundle) {
                    proceedToTheNextActivity();
                }

                @Override
                public void onError(QBResponseException e) {
                    showSnackbarError(null, R.string.splash_create_session_error, e, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            signInQB(qbUser);
                        }
                    });
                }
            });
        } else {
            proceedToTheNextActivityWithDelay();
        }
    }

    @Override
    protected String getAppName() {
        return getString(R.string.app_title);
    }

    @Override
    protected void proceedToTheNextActivity() {
        MessagesActivity.start(this, message);
        finish();
    }

    private boolean checkSignIn() {
        return QBSessionManager.getInstance().getSessionParameters() != null;
    }
}