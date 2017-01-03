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
import com.quickblox.sample.core.utils.configs.CoreConfigUtils;
import com.quickblox.users.model.QBUser;

public class SplashActivity extends CoreSplashActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (checkConfigsWithSnackebarError()){
            createSession();
        }
    }

    private void createSession() {
        String userLogin = CoreConfigUtils.getStringConfigFromFileOrNull(Consts.APP_CONFIG_FILE_NAME, Consts.USER_LOGIN_FIELD_NAME);
        String userPassword = CoreConfigUtils.getStringConfigFromFileOrNull(Consts.APP_CONFIG_FILE_NAME, Consts.USER_PASSWORD_FIELD_NAME);

        QBAuth.createSession(new QBUser(userLogin, userPassword)).performAsync(new QBEntityCallback<QBSession>() {
            @Override
            public void onSuccess(QBSession qbSession, Bundle bundle) {
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

    @Override
    protected String getAppName() {
        return getString(R.string.splash_app_title);
    }

    @Override
    protected void proceedToTheNextActivity() {
        GalleryActivity.start(this);
        finish();
    }

    @Override
    protected boolean sampleConfigIsCorrect() {
        return CoreConfigUtils.isStringConfigFromFileNotEmpty(Consts.APP_CONFIG_FILE_NAME, Consts.USER_LOGIN_FIELD_NAME)
                && CoreConfigUtils.isStringConfigFromFileNotEmpty(Consts.APP_CONFIG_FILE_NAME, Consts.USER_PASSWORD_FIELD_NAME);
    }
}