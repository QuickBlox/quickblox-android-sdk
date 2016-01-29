package com.quickblox.sample.user.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.core.exception.QBResponseException;
import com.digits.sdk.android.AuthCallback;
import com.digits.sdk.android.Digits;
import com.digits.sdk.android.DigitsAuthButton;
import com.digits.sdk.android.DigitsException;
import com.digits.sdk.android.DigitsOAuthSigning;
import com.digits.sdk.android.DigitsSession;
import com.quickblox.auth.QBAuth;
import com.quickblox.auth.model.QBSession;
import com.quickblox.sample.user.definitions.Consts;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;
import com.quickblox.sample.user.R;
import com.quickblox.sample.user.helper.DataHolder;
import com.quickblox.sample.user.utils.DialogUtils;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterAuthToken;
import com.twitter.sdk.android.core.TwitterCore;

import java.util.List;
import java.util.Map;

import io.fabric.sdk.android.Fabric;

public class SignInActivity extends BaseActivity {

    private EditText loginEditText;
    private EditText passwordEditText;
    private TwitterAuthConfig authConfig;

    @Override
    public void onCreate(Bundle savedInstanceBundle) {
        super.onCreate(savedInstanceBundle);
        setContentView(R.layout.activity_sign_in);
        initTwitter();
        initUI();
    }

    private void initTwitter() {
        authConfig = new TwitterAuthConfig(Consts.TWITTER_KEY, Consts.TWITTER_SECRET);
        Fabric.with(this, new TwitterCore(authConfig), new Digits());
    }

    private void initUI() {
        actionBar.setDisplayHomeAsUpEnabled(true);
        loginEditText = (EditText) findViewById(R.id.login_edittext);
        passwordEditText = (EditText) findViewById(R.id.password_edittext);

        DigitsAuthButton digitsButton = (DigitsAuthButton) findViewById(R.id.auth_button);
        digitsButton.setBackgroundColor(getResources().getColor(R.color.blue));
        digitsButton.setAuthTheme(R.style.DigitsTheme);
        digitsButton.setCallback(getAuthCallback());
        digitsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog.show();
            }
        });
    }

    public AuthCallback getAuthCallback(){
        AuthCallback authCallback = new AuthCallback() {
            @Override
            public void success(DigitsSession session, String phoneNumber) {
                signInUsingTwitterDigits(session);
            }

            @Override
            public void failure(DigitsException exception) {
                progressDialog.hide();
                DialogUtils.showLong(context, exception.getMessage());
            }
        };
        return authCallback;
    }

    private void signInUsingTwitterDigits(DigitsSession session) {
        Map<String, String> authHeaders = getAuthHeadersBySession(session);

        String xAuthServiceProvider = authHeaders.get(Consts.X_AUTH_SERVICE_PROVIDER_KEY);
        String xVerifyCredentialsAuthorization = authHeaders.get(Consts.X_VERIFY_CREDENTIALS_AUTORIZATION_KEY);

        QBUsers.signInUsingTwitterDigits(xAuthServiceProvider, xVerifyCredentialsAuthorization, new QBEntityCallback<QBUser>() {
            @Override
            public void onSuccess(QBUser qbUser, Bundle params) {
                progressDialog.hide();

                setResult(RESULT_OK);

                DataHolder.getDataHolder().setSignInQbUser(qbUser);
                DialogUtils.showLong(context, getResources().getString(R.string.user_successfully_sign_in));

                finish();
            }

            @Override
            public void onError(QBResponseException errors) {
                progressDialog.hide();
                DialogUtils.showLong(context, errors.getLocalizedMessage());
            }
        });
    }

    private void createSessionUsingTwitterDigits(DigitsSession session) {
        Map<String, String> authHeaders = getAuthHeadersBySession(session);

        String xAuthServiceProvider = authHeaders.get(Consts.X_AUTH_SERVICE_PROVIDER_KEY);
        String xVerifyCredentialsAuthorization = authHeaders.get(Consts.X_VERIFY_CREDENTIALS_AUTORIZATION_KEY);

        QBAuth.createSessionUsingTwitterDigits(xAuthServiceProvider, xVerifyCredentialsAuthorization, new QBEntityCallback<QBSession>() {
            @Override
            public void onSuccess(QBSession result, Bundle params) {

            }

            @Override
            public void onError(QBResponseException errors) {

            }
        });
    }

    private Map<String, String> getAuthHeadersBySession(DigitsSession digitsSession) {
        TwitterAuthToken authToken = (TwitterAuthToken) digitsSession.getAuthToken();
        DigitsOAuthSigning oauthSigning = new DigitsOAuthSigning(authConfig, authToken);

        return oauthSigning.getOAuthEchoHeadersForVerifyCredentials();
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.sign_in_button:
                progressDialog.show();

                // Sign in application with user
                //
                QBUser qbUser = new QBUser(loginEditText.getText().toString(), passwordEditText.getText().toString());
                QBUsers.signIn(qbUser, new QBEntityCallback<QBUser>() {
                    @Override
                    public void onSuccess(QBUser qbUser, Bundle bundle) {
                        progressDialog.hide();

                        setResult(RESULT_OK);

                        DataHolder.getDataHolder().setSignInQbUser(qbUser);
                        // password does not come, so if you want use it somewhere else, try something like this:
                        DataHolder.getDataHolder().setSignInUserPassword(passwordEditText.getText().toString());
                        DialogUtils.showLong(context, getResources().getString(R.string.user_successfully_sign_in));

                        finish();
                    }

                    @Override
                    public void onError(QBResponseException errors) {
                        progressDialog.hide();
                        DialogUtils.showLong(context, errors.toString());
                    }
                });

                break;
        }
    }
}