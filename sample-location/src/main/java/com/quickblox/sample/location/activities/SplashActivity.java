package com.quickblox.sample.location.activities;

import android.os.Bundle;

import com.quickblox.auth.session.QBSessionManager;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.sample.core.ui.activity.CoreSplashActivity;
import com.quickblox.sample.core.utils.Toaster;
import com.quickblox.sample.core.utils.configs.AppConfigParser;
import com.quickblox.sample.location.R;
import com.quickblox.sample.location.utils.Consts;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class SplashActivity extends CoreSplashActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initAppConfig();
    }

    private void initAppConfig() {
        String userLogin;
        String userPassword;

        try {
            JSONObject appConfigs = new AppConfigParser().getAppConfigsAsJson(Consts.APP_CONFIG_FILE_NAME);
            userLogin = appConfigs.getString(Consts.USER_LOGIN_FIELD_NAME);
            userPassword = appConfigs.getString(Consts.USER_PASSWORD_FIELD_NAME);
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            showSnackbarError(null, R.string.init_configs_error, null, null);
            return;
        }

        signInQB(new QBUser(userLogin, userPassword));
    }

    private void signInQB(QBUser qbUser) {
        if (!checkSignIn()) {

            QBUsers.signIn(qbUser).performAsync(new QBEntityCallback<QBUser>() {
                @Override
                public void onSuccess(QBUser qbUser, Bundle bundle) {
                    proceedToTheNextActivity();
                }

                @Override
                public void onError(QBResponseException e) {
                    Toaster.longToast(getString(R.string.dlg_location_error) + e.getErrors().toString());
                }
            });
        } else {
            proceedToTheNextActivityWithDelay();
        }
    }

    private boolean checkSignIn() {
        return QBSessionManager.getInstance().getSessionParameters() != null;
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