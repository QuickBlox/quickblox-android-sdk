package com.quickblox.sample.chat.java.ui.activity;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.quickblox.auth.session.QBSessionManager;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.sample.chat.java.R;
import com.quickblox.sample.chat.java.utils.SharedPrefsHelper;
import com.quickblox.sample.chat.java.utils.chat.ChatHelper;
import com.quickblox.users.model.QBUser;

public class SplashActivity extends BaseActivity {
    private static final int SPLASH_DELAY = 1500;

    private static final String TAG = SplashActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        fillVersion();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (SharedPrefsHelper.getInstance().hasQbUser()) {
                    restoreChatSession();
                } else {
                    LoginActivity.start(SplashActivity.this);
                    finish();
                }
            }
        }, SPLASH_DELAY);
    }

    @Override
    public void onBackPressed() {

    }

    private void fillVersion() {
        String appName = getString(R.string.app_name);
        ((TextView) findViewById(R.id.tv_splash_app_title)).setText(appName);
        try {
            String versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            ((TextView) findViewById(R.id.tv_splash_app_version)).setText(getString(R.string.splash_app_version, versionName));
        } catch (PackageManager.NameNotFoundException e) {
            Log.d(TAG, e.getMessage());
        }
    }

    private void restoreChatSession() {
        if (ChatHelper.getInstance().isLogged()) {
            DialogsActivity.start(this);
            finish();
        } else {
            QBUser currentUser = getUserFromSession();
            if (currentUser == null) {
                LoginActivity.start(this);
                finish();
            } else {
                loginToChat(currentUser);
            }
        }
    }

    private QBUser getUserFromSession() {
        QBUser user = SharedPrefsHelper.getInstance().getQbUser();
        QBSessionManager qbSessionManager = QBSessionManager.getInstance();
        if (qbSessionManager.getSessionParameters() == null || user == null) {
            ChatHelper.getInstance().destroy();
            return null;
        }
        Integer userId = qbSessionManager.getSessionParameters().getUserId();
        user.setId(userId);
        return user;
    }

    private void loginToChat(final QBUser user) {
        showProgressDialog(R.string.dlg_restoring_chat_session);

        ChatHelper.getInstance().loginToChat(user, new QBEntityCallback<Void>() {
            @Override
            public void onSuccess(Void result, Bundle bundle) {
                Log.v(TAG, "Chat login onSuccess()");
                hideProgressDialog();
                DialogsActivity.start(SplashActivity.this);
                finish();
            }

            @Override
            public void onError(QBResponseException e) {
                if (e.getMessage().equals("You have already logged in chat")) {
                    loginToChat(user);
                } else {
                    hideProgressDialog();
                    Log.w(TAG, "Chat login onError(): " + e);
                    showErrorSnackbar(R.string.error_recreate_session, e,
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    loginToChat(user);
                                }
                            });
                }
            }
        });
    }
}