package com.quickblox.sample.chat.ui.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.ActionBar;
import android.util.Log;

import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.sample.chat.R;
import com.quickblox.sample.chat.utils.SharedPreferencesUtil;
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

    private static final Handler mainThreadHandler = new Handler(Looper.getMainLooper());

    protected ActionBar actionBar;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        actionBar = getSupportActionBar();

        // 'isChatServiceStartedJustNow' will be true if app was just started
        // or if app's process was restarted after background death
        boolean isChatServiceStartedJustNow = ChatHelper.initIfNeed(this);
        boolean wasAppRestored = savedInstanceState != null;
        final boolean isChatSessionActive = !(isChatServiceStartedJustNow || wasAppRestored);

        // Triggering callback via Handler#post() method
        // to let child's code in onCreate() to execute first
        mainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                if (isChatSessionActive) {
                    onSessionCreated(true);
                } else {
                    restoreChatSession();
                }
            }
        });
    }


    private void restoreChatSession() {
        Log.d(TAG, "Need to recreate chat session");

        QBUser user = SharedPreferencesUtil.getQbUser();
        if (user == null) {
            throw new RuntimeException("User is null, can't restore session");
        }

        reloginToChat(user);
    }


    private void reloginToChat(final QBUser user) {
        ProgressDialogFragment.show(getSupportFragmentManager(), R.string.dlg_restoring_chat_session);

        ChatHelper.getInstance().login(user, new QBEntityCallbackImpl<Void>() {
            @Override
            public void onSuccess() {
                Log.v(TAG, "Chat login onSuccess()");

                // We need to trigger callback in UI Thread
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
                Log.w(TAG, "Chat login onError(): " + errors);
                Toaster.shortToast(R.string.error_recreate_session);

                mainThreadHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        reloginToChat(user);
                    }
                }, RECREATE_SESSION_AFTER_ERROR_DELAY);

                // We need to trigger callback in UI Thread
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
