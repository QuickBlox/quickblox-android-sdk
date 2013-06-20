package com.quickblox.chat_v2.ui.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.facebook.Session;
import com.facebook.SessionState;
import com.quickblox.chat_v2.R;
import com.quickblox.chat_v2.core.ChatApplication;
import com.quickblox.chat_v2.ui.dialogs.SplashDialog;
import com.quickblox.chat_v2.utils.SharedPreferencesHelper;
import com.quickblox.core.QBCallback;
import com.quickblox.core.QBCallbackImpl;
import com.quickblox.core.QBSettings;
import com.quickblox.core.result.Result;
import com.quickblox.internal.core.exception.BaseServiceException;
import com.quickblox.internal.core.server.BaseService;
import com.quickblox.module.auth.QBAuth;
import com.quickblox.module.auth.model.QBProvider;
import com.quickblox.module.chat.QBChat;
import com.quickblox.module.chat.listeners.LoginListener;
import com.quickblox.module.users.QBUsers;
import com.quickblox.module.users.model.QBUser;
import com.quickblox.module.users.result.QBUserResult;

import org.json.JSONException;

import java.io.IOException;
import java.net.MalformedURLException;

public class SplashActivity extends FragmentActivity implements QBCallback, Session.StatusCallback {

    private DialogFragment quickBloxDialog;
    private ProgressDialog progress;

    private ChatApplication app;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_splash);

        ImageButton facebookButton = (ImageButton) findViewById(R.id.splash_facebook_button);
        Button registrationButton = (Button) findViewById(R.id.splash_registration_button);
        Button siginButton = (Button) findViewById(R.id.splash_sign_in_button);

        switchProgressDialog(true);

        app = ChatApplication.getInstance();
        app.createData(this);

        OnClickListener clickButtonListener = new OnClickListener() {

            @Override
            public void onClick(View v) {

                switch (v.getId()) {
                    case R.id.splash_facebook_button:
                        if (!isOnline()) {
                            break;
                        }
                        onFbClickLogin();
                        break;

                    case R.id.splash_registration_button:
                        if (!isOnline()) {
                            break;
                        }
                        quickBloxDialog = new SplashDialog(true, SplashActivity.this);
                        quickBloxDialog.show(getSupportFragmentManager(), null);
                        break;

                    case R.id.splash_sign_in_button:
                        if (!isOnline()) {
                            break;
                        }
                        quickBloxDialog = new SplashDialog(false, SplashActivity.this);
                        quickBloxDialog.show(getSupportFragmentManager(), null);
                        break;

                }
            }

        };

        facebookButton.setOnClickListener(clickButtonListener);
        registrationButton.setOnClickListener(clickButtonListener);
        siginButton.setOnClickListener(clickButtonListener);

        if (isOnline()) {

            QBSettings.getInstance().fastConfigInit(getResources().getString(R.string.quickblox_app_id), getResources().getString(R.string.quickblox_auth_key),
                    getResources().getString(R.string.quickblox_auth_secret));

            QBAuth.createSession(new QBCallbackImpl() {
                @Override
                public void onComplete(Result result) {

                    Session session = new Session(SplashActivity.this);

                    // Auto login
                    if (TextUtils.isEmpty(session.getAccessToken())) {
                        if (!TextUtils.isEmpty(SharedPreferencesHelper.getLogin(SplashActivity.this))) {

                            QBUser user = new QBUser();
                            user.setLogin(SharedPreferencesHelper.getLogin(SplashActivity.this));
                            user.setPassword(SharedPreferencesHelper.getPassword(SplashActivity.this));

                            ChatApplication.getInstance().setQbUser(user);
                            QBUsers.signIn(user, SplashActivity.this, user.getPassword());

                        } else {
                            switchProgressDialog(false);
                        }
                    } else {
                        QBUsers.signInUsingSocialProvider(QBProvider.FACEBOOK, session.getAccessToken(), null, SplashActivity.this, "social");

                    }
                }
            });

        } else {
            switchProgressDialog(false);
            Toast.makeText(this, getResources().getString(R.string.splash_internet_error), Toast.LENGTH_LONG).show();

        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data == null) {
            this.finish();
        }

        Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
    }

    // FACEBOOK LOGIN
    private void onFbClickLogin() {

        Session session = Session.getActiveSession();

        if (session == null || session.getState().equals(SessionState.CLOSED_LOGIN_FAILED)) {
            session = new Session(this);
            Session.setActiveSession(session);

            if (session.getState().equals(SessionState.CREATED_TOKEN_LOADED)) {
                session.openForRead(new Session.OpenRequest(this).setCallback(this));
            }

        }
        if (!session.isOpened() && !session.isClosed()) {
            session.openForRead(new Session.OpenRequest(this).setCallback(this));
        }
    }

    // QB CALLBACK

    @Override
    public void onComplete(Result arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onComplete(Result result, Object context) {
        if (result.isSuccess()) {

            QBUser qbUser = ((QBUserResult) result).getUser();
            SharedPreferencesHelper.setLogin(SplashActivity.this, qbUser.getLogin());

            // Logged in using Facebook
            if (context.toString().equals("social")) {
                try {
                    // save QB user (logged in as Facebook)

                    qbUser.setPassword(BaseService.getBaseService().getToken());
                    app.setQbUser(qbUser);
                    qbUser.setLogin(qbUser.getFullName());

                    Session session = new Session(SplashActivity.this);
                    try {
                        app.getFbm().getUserInfo(true, null);
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                } catch (BaseServiceException e) {
                    e.printStackTrace();
                }

                // Logged in using login & password
            } else {
                // save QB User
                qbUser.setPassword((String) context);
                app.setQbUser(qbUser);
                app.getInviteUserList().add(String.valueOf(qbUser.getId()));

                SharedPreferencesHelper.setPassword(getBaseContext(), context.toString());
            }

            // Login to Chat and open Main screen
            QBChat.getInstance().loginWithUser(qbUser, new LoginListener() {

                @Override
                public void onLoginError() {
                    switchProgressDialog(false);
                }

                @Override
                public void onLoginSuccess() {
                    QBChat.getInstance().setChatMessageListener(app.getMsgManager());

                    app.setQbRoster(QBChat.getInstance().registerRoster(app.getRstManager()));
                    QBChat.getInstance().registerSubscriptionListener(app.getRstManager());
                    loadMainScreen();
                }
            });

        } else {
            switchProgressDialog(false);
            Toast.makeText(this, getResources().getString(R.string.splash_login_reject), Toast.LENGTH_LONG).show();
        }
    }

    // FACEBOOK CALLBACK
    @Override
    public void call(Session session, SessionState state, Exception exception) {
        if (TextUtils.isEmpty(session.getAccessToken())) {
            return;
        }
        app.setAccessTokien(session.getAccessToken());
        QBUsers.signInUsingSocialProvider(QBProvider.FACEBOOK, session.getAccessToken(), null, this, "social");
        switchProgressDialog(true);
    }

    private void loadMainScreen() {
        Intent intent = new Intent(getBaseContext(), MainActivity.class);
        startActivity(intent);
        finish();
    }

    public void switchProgressDialog(boolean enable) {
        if (enable) {
            progress = ProgressDialog.show(this, getResources().getString(R.string.app_name), getResources().getString(R.string.splash_ui_block),
                    true);
        } else {
            progress.dismiss();
        }
    }


    // INTERNET REVIEW
    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        }
        Toast.makeText(SplashActivity.this, getResources().getString(R.string.splash_internet_error), Toast.LENGTH_LONG).show();
        return false;
    }

}