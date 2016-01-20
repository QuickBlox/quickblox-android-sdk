package com.quickblox.sample.content.activities;

import android.os.Bundle;

import com.quickblox.auth.QBAuth;
import com.quickblox.auth.model.QBSession;
import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.sample.content.R;
import com.quickblox.sample.content.helper.DataHolder;
import com.quickblox.sample.content.utils.Consts;
import com.quickblox.sample.core.ui.activity.CoreSplashActivity;
import com.quickblox.sample.core.utils.Toaster;
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
//                ToDo Is it really necessary to getUserId? I do not think so.
                DataHolder.getInstance().setSignInUserId(qbSession.getUserId());
                proceedToTheNextActivity();
            }

            @Override
            public void onError(List<String> errors) {
                Toaster.shortToast(errors.get(0));
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