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
import com.quickblox.module.chat.QBChat;
import com.quickblox.module.chat.listeners.SessionListener;
import com.quickblox.module.chat.smack.SmackAndroid;
import com.quickblox.module.users.QBUsers;
import com.quickblox.module.users.model.QBUser;
import com.quickblox.sample.chat.QuickbloxSampleChat;
import com.quickblox.sample.chat.R;

public class LoginActivity extends Activity implements QBCallback, View.OnClickListener {
    private static final String DEFAULT_LOGIN = "romeo";
    private static final String DEFAULT_PASSWORD = "password";
    private static final String TAG = LoginActivity.class.getSimpleName();

    private Button loginButton;
    private Button registerButton;
    private EditText loginEdit;
    private EditText passwordEdit;
    private ProgressDialog progressDialog;

    private String login;
    private String password;
    private QBUser user;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // UI stuff
        loginEdit = (EditText) findViewById(R.id.loginEdit);
        passwordEdit = (EditText) findViewById(R.id.passwordEdit);
        loginEdit.setText(DEFAULT_LOGIN);
        passwordEdit.setText(DEFAULT_PASSWORD);
        loginButton = (Button) findViewById(R.id.loginButton);
        registerButton = (Button) findViewById(R.id.registerButton);
        loginButton.setOnClickListener(this);
        registerButton.setOnClickListener(this);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading");

        SmackAndroid.init(this);
    }

    @Override
    public void onClick(View view) {
        login = loginEdit.getText().toString();
        password = passwordEdit.getText().toString();

        user = new QBUser(login, password);

        progressDialog.show();
        switch (view.getId()) {
            case R.id.loginButton:

                // ================= QuickBlox ===== Step 3 =================
                // Login user into QuickBlox.
                // Pass this activity , because it implements QBCallback interface.
                // Callback result will come into onComplete method below.
                QBUsers.signIn(user, LoginActivity.this);
                break;
            case R.id.registerButton:

                // ================= QuickBlox ===== Step 3 =================
                // Register user in QuickBlox.
                QBUsers.signUpSignInTask(user, LoginActivity.this);
                break;
        }
    }

    @Override
    public void onComplete(Result result) {
        if (result.isSuccess()) {

            QuickbloxSampleChat.getInstance().setQbUser(user);
            QBChat.getInstance().loginWithUser(user, new SessionListener() {
                @Override
                public void onLoginSuccess() {
                    if (progressDialog != null) {
                        progressDialog.dismiss();
                    }
                    Log.i(TAG, "success when login");
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
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
