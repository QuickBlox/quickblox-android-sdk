package com.quickblox.videochatsample.ui.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.quickblox.core.QBCallback;
import com.quickblox.core.QBSettings;
import com.quickblox.core.result.Result;
import com.quickblox.module.auth.QBAuth;
import com.quickblox.module.auth.result.QBSessionResult;
import com.quickblox.module.chat.QBChatService;
import com.quickblox.module.chat.listeners.SessionCallback;
import com.quickblox.module.videochat.core.QBVideoChatController;
import com.quickblox.videochatsample.R;
import com.quickblox.videochatsample.VideoChatApplication;

import org.jivesoftware.smack.XMPPException;

public class ActivityLogin extends Activity {

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // setup UI
        //
        setContentView(R.layout.login_layout);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.please_wait));
        progressDialog.setCancelable(false);

        findViewById(R.id.loginByFirstUserBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressDialog.show();
                createSession(VideoChatApplication.FIRST_USER_LOGIN, VideoChatApplication.FIRST_USER_PASSWORD);
            }
        });

        findViewById(R.id.loginBySecondUserBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressDialog.show();
                createSession(VideoChatApplication.SECOND_USER_LOGIN, VideoChatApplication.SECOND_USER_PASSWORD);
            }
        });

        // Set QuickBlox credentials here
        //
        QBSettings.getInstance().fastConfigInit("92", "wJHdOcQSxXQGWx5", "BTFsj7Rtt27DAmT");
    }

    private void createSession(String login, final String password) {
        QBAuth.createSession(login, password, new QBCreateSessionCallback(password));
    }

    class QBCreateSessionCallback implements QBCallback {
        private final String password;

        QBCreateSessionCallback(String password) {
            this.password = password;
        }

        @Override
        public void onComplete(Result result) {
            if (result.isSuccess()) {

                // Save current user
                //
                VideoChatApplication app = (VideoChatApplication)getApplication();
                app.setCurrentUser(((QBSessionResult) result).getSession().getUserId(), password);

                // Login to chat
                //
                QBChatService.getInstance().loginWithUser(app.getCurrentUser(), loginListener);
            }else{
                progressDialog.dismiss();
                Toast.makeText(ActivityLogin.this, "Error when login, check test users login and password", Toast.LENGTH_SHORT).show();
            }

        }

        @Override
        public void onComplete(Result result, Object context) {

        }
    }

    private SessionCallback loginListener = new SessionCallback() {
        @Override
        public void onLoginSuccess() {
            try {
                QBVideoChatController.getInstance().initQBVideoChatMessageListener();
            } catch (XMPPException e) {
                e.printStackTrace();
            }
            // show next activity
            showCallUserActivity();
        }

        @Override
        public void onLoginError(final String error)  {
            Toast.makeText(ActivityLogin.this, "Error when login", Toast.LENGTH_SHORT).show();
        }
    };

    private void showCallUserActivity() {
        progressDialog.dismiss();

        Intent intent = new Intent(this, ActivityVideoChat.class);
        startActivity(intent);
        finish();
    }
}