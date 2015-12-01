package com.quickblox.sample.chat.ui.activity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.sample.chat.R;
import com.quickblox.sample.chat.utils.chat.ChatSessionStateCallback;
import com.quickblox.sample.chat.utils.chat.ChatHelper;
import com.quickblox.users.model.QBUser;

import java.util.List;

public abstract class BaseActivity extends AppCompatActivity implements ChatSessionStateCallback {
    private static final String TAG = BaseActivity.class.getSimpleName();

    private static final int RECREATE_SESSION_DELAY_MILLIS = 3000;

    private static final String BUNDLE_USER_LOGIN = "USER_LOGIN";
    private static final String BUNDLE_USER_PASSWORD = "USER_PASSWORD";

    private final Handler handler = new Handler();

    private boolean sessionActive = false;
    private boolean needToRecreateSession = false;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 'isChatServiceInitialised' will be true if it's the 1st start of the app
        // or if the app's process was restarted after background death
        boolean isChatServiceInitialised = ChatHelper.initIfNeed(this);
        if (isChatServiceInitialised && savedInstanceState != null) {
            needToRecreateSession = true;
        } else {
            sessionActive = true;
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
        sessionActive = false;

        showProgressDialog();

        ChatHelper.getInstance().login(user, new QBEntityCallbackImpl<String>() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "Chat login onSuccess");

                progressDialog.dismiss();
                progressDialog = null;

                sessionActive = true;
                onSessionRecreationFinish(true);
            }

            @Override
            public void onError(List<String> errors) {
                Log.d(TAG, "Chat login onError: " + errors);

                Toast.makeText(BaseActivity.this, R.string.error_recreate_session, Toast.LENGTH_SHORT).show();

                // try again
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        recreateChatSession(user);
                    }
                }, RECREATE_SESSION_DELAY_MILLIS);

                onSessionRecreationFinish(false);
            }
        });
    }

    private void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setTitle(R.string.dlg_loading);
            progressDialog.setMessage(getString(R.string.dlg_restoring_chat_session));
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        }
        progressDialog.show();
    }


    public boolean isSessionActive() {
        return sessionActive;
    }
}
