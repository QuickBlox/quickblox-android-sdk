package com.quickblox.sample.chat.ui.activity;

import android.os.Bundle;

import com.quickblox.auth.QBAuth;
import com.quickblox.auth.model.QBSession;
import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.sample.chat.R;
import com.quickblox.sample.chat.utils.chat.ChatHelper;
import com.quickblox.sample.core.ui.activity.CoreSplashActivity;
import com.quickblox.sample.core.utils.ErrorUtils;

import java.util.List;

public class SplashActivity extends CoreSplashActivity {

    private boolean isChatServiceInitializedJustNow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        isChatServiceInitializedJustNow = ChatHelper.initIfNeed(this);
        QBAuth.createSession(new QBEntityCallbackImpl<QBSession>() {
            @Override
            public void onSuccess(QBSession result, Bundle params) {
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
        // If QBChatService was just initialized that means that
        // we do not have created session from the last app launch
        // so we just proceeding to the Login activity
        if (isChatServiceInitializedJustNow) {
            LoginActivity.start(this);
        } else {
            DialogsActivity.start(this);
        }
        finish();
    }
}