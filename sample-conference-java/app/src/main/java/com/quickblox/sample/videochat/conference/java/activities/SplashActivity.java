package com.quickblox.sample.videochat.conference.java.activities;

import static com.quickblox.sample.videochat.conference.java.utils.SystemPermissionsHelper.REQUEST_CADE_FOR_NOTIFICATION;

import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.quickblox.auth.session.QBSessionManager;
import com.quickblox.chat.QBChatService;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.sample.videochat.conference.java.R;
import com.quickblox.sample.videochat.conference.java.utils.SystemPermissionsHelper;
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            SystemPermissionsHelper permissionsHelper = new SystemPermissionsHelper(this);
            boolean isNotNotificationPermissionGranted = !permissionsHelper.isNotificationPermissionGranted();
            if (isNotNotificationPermissionGranted) {
                permissionsHelper.requestPermissionsForNotification();
                return;
            }
        }

        start();
    }

    private void start() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (getSharedPrefsHelper().hasQbUser()) {
                    restoreChatSession();
                } else {
                    LoginActivity.start(SplashActivity.this);
                    finish();
                }
            }
        }, SPLASH_DELAY);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CADE_FOR_NOTIFICATION) {
            start();
        }
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
            if (e.getMessage() != null) {
                Log.d(TAG, e.getMessage());
            }
        }
    }

    private void restoreChatSession() {
        if (getChatHelper().isLogged()) {
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
        QBUser user = getSharedPrefsHelper().getQbUser();
        QBSessionManager qbSessionManager = QBSessionManager.getInstance();
        if (qbSessionManager.getSessionParameters() == null || user == null) {
            QBChatService.getInstance().destroy();
            return null;
        }
        Integer userId = qbSessionManager.getSessionParameters().getUserId();
        user.setId(userId);
        return user;
    }

    private void loginToChat(final QBUser user) {
        showProgressDialog(R.string.dlg_restoring_chat_session);
        getChatHelper().loginToChat(user, new QBEntityCallback<Void>() {
            @Override
            public void onSuccess(Void result, Bundle bundle) {
                Log.v(TAG, "Chat login onSuccess()");
                hideProgressDialog();
                DialogsActivity.start(SplashActivity.this);
                finish();
            }

            @Override
            public void onError(QBResponseException e) {
                Log.d(TAG, "Chat Login Error: " + e.getMessage());
                if (e.getMessage() != null && e.getMessage().equals("You have already logged in chat")) {
                    loginToChat(user);
                } else {
                    hideProgressDialog();
                    Log.d(TAG, "Chat login onError(): " + e);
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