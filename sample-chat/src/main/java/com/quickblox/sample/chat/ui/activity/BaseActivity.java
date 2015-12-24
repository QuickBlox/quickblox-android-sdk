package com.quickblox.sample.chat.ui.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.ActionBar;
import android.util.Log;

import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.sample.chat.R;
import com.quickblox.sample.chat.utils.chat.ChatUtils;
import com.quickblox.sample.chat.utils.chat.ChatHelper;
import com.quickblox.sample.chat.utils.chat.QbSessionStateCallback;
import com.quickblox.sample.core.ui.activity.CoreBaseActivity;
import com.quickblox.sample.core.ui.dialog.ProgressDialogFragment;
import com.quickblox.sample.core.utils.Toaster;
import com.quickblox.users.model.QBUser;

import java.util.List;

public abstract class BaseActivity extends CoreBaseActivity implements QbSessionStateCallback {
    private static final String TAG = BaseActivity.class.getSimpleName();

    private static final int RECREATE_SESSION_AFTER_ERROR_DELAY = 3000;

    private static final String BUNDLE_USER_LOGIN = "USER_LOGIN";
    private static final String BUNDLE_USER_PASSWORD = "USER_PASSWORD";

    private static final Handler mainThreadHandler = new Handler(Looper.getMainLooper());

    protected ActionBar actionBar;
    private boolean isSessionActive;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 'isChatServiceStartedJustNow' will be true if it's the 1st start of the app
        // or if the app's process was restarted after background death
        boolean isChatServiceStartedJustNow = ChatHelper.initIfNeed(this);
        boolean isAppRestored = savedInstanceState != null;
        isSessionActive = !(isChatServiceStartedJustNow && isAppRestored);

        actionBar = getSupportActionBar();

        // Triggering callback via post() to let all child's code in onCreate() to execute first
        mainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                if (isSessionActive) {
                    onSessionCreated(true);
                }
            }
        });
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        if (!isSessionActive) {
            Log.w(TAG, "Need to recreate chat session");

            QBUser user = new QBUser();
            user.setLogin(savedInstanceState.getString(BUNDLE_USER_LOGIN));
            user.setPassword(savedInstanceState.getString(BUNDLE_USER_PASSWORD));

            recreateQbSession(user);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        QBUser currentUser = ChatUtils.getCurrentUser();
        if (currentUser != null) {
            outState.putString(BUNDLE_USER_LOGIN, currentUser.getLogin());
            outState.putString(BUNDLE_USER_PASSWORD, currentUser.getPassword());
        }

        super.onSaveInstanceState(outState);
    }

    private void recreateQbSession(final QBUser user) {
        ProgressDialogFragment.show(getSupportFragmentManager(), R.string.dlg_restoring_chat_session);

        ChatHelper.getInstance().login(user, new QBEntityCallbackImpl<String>() {
            @Override
            public void onSuccess() {
                Log.v(TAG, "Chat login onSuccess()");
                isSessionActive = true;

                // We need to trigger callback in UI Thread,
                // so calling with handler.post() method
                mainThreadHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        onSessionCreated(true);
                    }
                });

                ProgressDialogFragment.hide(getSupportFragmentManager());
            }

            @Override
            public void onError(List<String> errors) {
                Log.v(TAG, "Chat login onError(): " + errors);

                Toaster.shortToast(R.string.error_recreate_session);

                // Retry to create session
                // in RECREATE_SESSION_AFTER_ERROR_DELAY milliseconds
                mainThreadHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        recreateQbSession(user);
                    }
                }, RECREATE_SESSION_AFTER_ERROR_DELAY);

                // We need to trigger callback in UI Thread,
                // so calling with handler.post()
                mainThreadHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        onSessionCreated(false);
                    }
                });
            }
        });
    }
}
