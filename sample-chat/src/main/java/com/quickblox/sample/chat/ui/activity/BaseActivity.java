package com.quickblox.sample.chat.ui.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.sample.chat.R;
import com.quickblox.sample.chat.ui.fragment.dialog.ProgressDialogFragment;
import com.quickblox.sample.chat.utils.chat.ChatHelper;
import com.quickblox.sample.chat.utils.chat.ChatSessionStateCallback;
import com.quickblox.users.model.QBUser;

import java.util.List;

public abstract class BaseActivity extends AppCompatActivity implements ChatSessionStateCallback {
    private static final String TAG = BaseActivity.class.getSimpleName();

    private static final int RECREATE_SESSION_AFTER_ERROR_DELAY = 3000;

    private static final String BUNDLE_USER_LOGIN = "USER_LOGIN";
    private static final String BUNDLE_USER_PASSWORD = "USER_PASSWORD";

    private static final Handler handler = new Handler(Looper.getMainLooper());

    private boolean isSessionActive;
    private boolean needToRecreateSession;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 'isChatServiceInitialisedJustNow' will be true if it's the 1st start of the app
        // or if the app's process was restarted after background death
        boolean isChatServiceInitialisedJustNow = ChatHelper.initIfNeed(this);
        if (isChatServiceInitialisedJustNow && savedInstanceState != null) {
            needToRecreateSession = true;
            isSessionActive = false;
        } else {
            isSessionActive = true;
        }
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        if (needToRecreateSession) {
            needToRecreateSession = false;

            Log.d(TAG, "Need to recreate chat session");

            QBUser user = new QBUser();
            user.setLogin(savedInstanceState.getString(BUNDLE_USER_LOGIN));
            user.setPassword(savedInstanceState.getString(BUNDLE_USER_PASSWORD));

            savedInstanceState.remove(BUNDLE_USER_LOGIN);
            savedInstanceState.remove(BUNDLE_USER_PASSWORD);

            recreateChatSession(user);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        QBUser currentUser = ChatHelper.getInstance().getCurrentUser();
        if (currentUser != null) {
            outState.putString(BUNDLE_USER_LOGIN, currentUser.getLogin());
            outState.putString(BUNDLE_USER_PASSWORD, currentUser.getPassword());
        }

        super.onSaveInstanceState(outState);
    }

    private void recreateChatSession(final QBUser user) {
        ProgressDialogFragment.show(getSupportFragmentManager());

        ChatHelper.getInstance().login(user, new QBEntityCallbackImpl<String>() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "Chat login onSuccess");

                ProgressDialogFragment.hide(getSupportFragmentManager());

                isSessionActive = true;
                onSessionRecreationFinish(true);
            }

            @Override
            public void onError(List<String> errors) {
                Log.d(TAG, "Chat login onError: " + errors);

                Toast.makeText(BaseActivity.this, R.string.error_recreate_session, Toast.LENGTH_SHORT).show();

                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        recreateChatSession(user);
                    }
                }, RECREATE_SESSION_AFTER_ERROR_DELAY);

                onSessionRecreationFinish(false);
            }
        });
    }
    public boolean isSessionActive() {
        return isSessionActive;
    }
}
