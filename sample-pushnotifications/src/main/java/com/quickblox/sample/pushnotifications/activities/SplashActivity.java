package com.quickblox.sample.pushnotifications.activities;

import android.os.Bundle;
import android.view.View;

import com.quickblox.auth.session.QBSessionManager;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.sample.core.ui.activity.CoreSplashActivity;
import com.quickblox.sample.core.utils.configs.CoreConfigUtils;
import com.quickblox.sample.core.utils.constant.GcmConsts;
import com.quickblox.sample.pushnotifications.R;
import com.quickblox.sample.pushnotifications.utils.Consts;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

public class SplashActivity extends CoreSplashActivity {
    private String message;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            message = getIntent().getExtras().getString(GcmConsts.EXTRA_GCM_MESSAGE);
        }

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
                    showSnackbarError(null, R.string.splash_create_session_error, e, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            signInQB();
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

    @Override
    protected boolean sampleConfigIsCorrect() {
        boolean result = super.sampleConfigIsCorrect();
        result = result && CoreConfigUtils.getUserFromConfig(Consts.SAMPLE_CONFIG_FILE_NAME) != null;
        return result;
    }


}