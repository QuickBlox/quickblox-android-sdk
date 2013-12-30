package com.quickblox.sample.chat.ui.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.quickblox.core.QBCallback;
import com.quickblox.core.result.Result;
import com.quickblox.module.chat.QBChatService;
import com.quickblox.module.chat.listeners.SessionListener;
import com.quickblox.module.chat.smack.SmackAndroid;
import com.quickblox.module.users.QBUsers;
import com.quickblox.module.users.model.QBUser;
import com.quickblox.sample.chat.App;
import com.quickblox.sample.chat.R;

public class LoginActivity extends Activity implements QBCallback, View.OnClickListener {

    private static final String TAG = LoginActivity.class.getSimpleName();
    private static final String DEFAULT_LOGIN = "ced";
    private static final String DEFAULT_PASSWORD = "ced";
    private Button loginButton;
    private EditText loginEdit;
    private EditText passwordEdit;
    private ProgressDialog progressDialog;
    private String login;
    private String password;
    private QBUser user;
    private SmackAndroid smackAndroid;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginEdit = (EditText) findViewById(R.id.loginEdit);
        passwordEdit = (EditText) findViewById(R.id.passwordEdit);
        loginEdit.setText(DEFAULT_LOGIN);
        passwordEdit.setText(DEFAULT_PASSWORD);
        loginButton = (Button) findViewById(R.id.loginButton);
        loginButton.setOnClickListener(this);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading");

        smackAndroid = SmackAndroid.init(this);
    }

    @Override
    protected void onDestroy() {
        smackAndroid.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onClick(View view) {
        login = loginEdit.getText().toString();
        password = passwordEdit.getText().toString();

        user = new QBUser(login, password);

        progressDialog.show();
        QBUsers.signIn(user, this);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent();
        setResult(RESULT_CANCELED, intent);
        finish();
    }

    @Override
    public void onComplete(Result result) {
        if (result.isSuccess()) {
            ((App)getApplication()).setQbUser(user);
            QBChatService.getInstance().loginWithUser(user, new SessionListener() {
                @Override
                public void onLoginSuccess() {
                    if (progressDialog != null) {
                        progressDialog.dismiss();
                    }
                    Log.i(TAG, "success when login");

                    Intent intent = new Intent();
                    setResult(RESULT_OK, intent);
                    finish();
                }

                @Override
                public void onLoginError() {
                    Log.i(TAG, "error when login");
                }

                @Override
                public void onDisconnect() {
                    Log.i(TAG, "disconnect when login");
                }

                @Override
                public void onDisconnectOnError(Exception exc) {
                    Log.i(TAG, "disconnect error when login");
                }
            });
        } else {
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setMessage("Error(s) occurred. Look into DDMS log for details, " +
                    "please. Errors: " + result.getErrors()).create().show();
        }
    }

    @Override
    public void onComplete(Result result, Object context) {
    }
}
