package com.quickblox.sample.content.activities;

import android.os.Bundle;
import android.view.View;

import com.quickblox.auth.QBAuth;
import com.quickblox.auth.session.QBSession;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.sample.content.R;
import com.quickblox.sample.content.utils.Consts;
import com.quickblox.sample.core.ui.activity.CoreSplashActivity;
import com.quickblox.sample.core.utils.configs.AppConfigParser;
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

        createSession(new QBUser(userLogin, userPassword));
    }

    private void createSession(final QBUser qbUser) {
        QBAuth.createSession(qbUser).performAsync(new QBEntityCallback<QBSession>() {
            @Override
            public void onSuccess(QBSession qbSession, Bundle bundle) {
                proceedToTheNextActivity();
            }

            @Override
            public void onError(QBResponseException e) {
                showSnackbarError(null, R.string.splash_create_session_error, e, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        createSession(qbUser);
                    }
                });
            }
        });
    }

    @Override
    protected String getAppName() {
        return getString(R.string.splash_app_title);
    }

    @Override
    protected void proceedToTheNextActivity() {
        GalleryActivity.start(this);
        finish();
    }
}