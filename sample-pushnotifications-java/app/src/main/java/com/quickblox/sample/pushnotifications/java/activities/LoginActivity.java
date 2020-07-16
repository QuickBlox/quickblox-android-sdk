package com.quickblox.sample.pushnotifications.java.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.sample.pushnotifications.java.App;
import com.quickblox.sample.pushnotifications.java.R;
import com.quickblox.sample.pushnotifications.java.utils.Consts;
import com.quickblox.sample.pushnotifications.java.utils.SharedPrefsHelper;
import com.quickblox.sample.pushnotifications.java.utils.ToastUtils;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoginActivity extends BaseActivity {
    private static final String TAG = LoginActivity.class.getSimpleName();
    private static final int MAX_LOGIN_LENGTH = 50;
    private static final int UNAUTHORIZED = 401;

    private Button buttonLogin;
    private EditText editTextLogin;
    private String message;

    public static void start(Context context, String message) {
        Intent intent = new Intent(context, LoginActivity.class);
        intent.putExtra(Consts.EXTRA_FCM_MESSAGE, message);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            message = getIntent().getExtras().getString(Consts.EXTRA_FCM_MESSAGE);
        }

        buttonLogin = findViewById(R.id.btn_login);
        editTextLogin = findViewById(R.id.et_login);

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Pattern patternLogin = Pattern.compile("^[a-zA-Z][a-zA-Z0-9]{2," + (MAX_LOGIN_LENGTH - 1) + "}+$");
                Matcher matcherLogin = patternLogin.matcher(editTextLogin.getText().toString().trim());

                if (matcherLogin.matches()) {
                    signIn();
                } else {
                    ToastUtils.shortToast(R.string.login_hint);
                }
            }
        });
    }

    @Override
    public void onBackPressed() {

    }

    private void signIn() {
        showProgressDialog(R.string.dlg_sign_in);
        String login = editTextLogin.getText().toString().trim();
        final QBUser qbUser = new QBUser(login, App.DEFAULT_USER_PASSWORD);
        qbUser.setFullName(login);

        QBUsers.signIn(qbUser).performAsync(new QBEntityCallback<QBUser>() {
            @Override
            public void onSuccess(QBUser user, Bundle bundle) {
                Log.d(TAG, "SignIn Success: " + qbUser.getId().toString());
                SharedPrefsHelper.getInstance().saveQbUser(qbUser);
                hideProgressDialog();
                MessagesActivity.start(LoginActivity.this, message);
                finish();
            }

            @Override
            public void onError(QBResponseException e) {
                Log.d(TAG, "SignIn Error: " + e.getLocalizedMessage());
                if (e.getHttpStatusCode() == UNAUTHORIZED) {
                    signUp(qbUser);
                } else {
                    showErrorSnackbar(findViewById(R.id.text_splash_app_title),
                            R.string.splash_signin_error, e, new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    signIn();
                                }
                            });
                }
            }
        });
    }

    private void signUp(QBUser qbUser) {
        showProgressDialog(R.string.dlg_sign_up);
        QBUsers.signUp(qbUser).performAsync(new QBEntityCallback<QBUser>() {
            @Override
            public void onSuccess(QBUser qbUser, Bundle bundle) {
                Log.d(TAG, "SignUp Success: " + qbUser.getId().toString());
                signIn();
            }

            @Override
            public void onError(QBResponseException e) {
                Log.d(TAG, "SignUp Error: " + e.getMessage());
                showErrorSnackbar(findViewById(R.id.text_splash_app_title),
                        R.string.splash_signup_error, e, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                signIn();
                            }
                        });
            }
        });
    }
}