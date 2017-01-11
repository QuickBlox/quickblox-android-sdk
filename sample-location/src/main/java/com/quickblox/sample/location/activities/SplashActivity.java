package com.quickblox.sample.location.activities;

import android.os.Bundle;

import com.quickblox.auth.session.QBSessionManager;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.sample.core.ui.activity.CoreSplashActivity;
import com.quickblox.sample.core.utils.Toaster;
import com.quickblox.sample.core.utils.configs.CoreConfigUtils;
import com.quickblox.sample.location.R;
import com.quickblox.sample.location.utils.Consts;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

public class SplashActivity extends CoreSplashActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (checkConfigsWithSnackebarError()){
            signInQB();
        }
    }

    private void signInQB() {
        if (!checkSignIn()) {

            QBUser qbUser = CoreConfigUtils.getUserFromConfig(Consts.SAMPLE_CONFIG_FILE_NAME);

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

    @Override
    protected boolean sampleConfigIsCorrect() {
        boolean result = super.sampleConfigIsCorrect();
        result = result && CoreConfigUtils.getUserFromConfig(Consts.SAMPLE_CONFIG_FILE_NAME) != null;
        return result;
    }
}