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
import com.quickblox.videochatsample.model.DataHolder;

import org.jivesoftware.smack.XMPPException;

public class ActivityLogin extends Activity {

    private final String FIRST_USER_PASSWORD = "videoChatUser1pass";
    private final String FIRST_USER_LOGIN = "videoChatUser1";
    private final String SECOND_USER_PASSWORD = "videoChatUser2pass";
    private final String SECOND_USER_LOGIN = "videoChatUser2";
    
    private final int firstUserId = 65421;
    private final String firstUserName = "first user";
    private final String secondUserName = "second user";
    private final int secondUserId = 65422;

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
                createSession(FIRST_USER_LOGIN, FIRST_USER_PASSWORD);
            }
        });

        findViewById(R.id.loginBySecondUserBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressDialog.show();
                createSession(SECOND_USER_LOGIN, SECOND_USER_PASSWORD);
            }
        });

        // Set QuickBlox credentials here
        //
        QBSettings.getInstance().fastConfigInit("92", "wJHdOcQSxXQGWx5", "BTFsj7Rtt27DAmT");
    }

    @Override
    public void onResume() {
        progressDialog.dismiss();
        super.onResume();
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
                // save current user
                DataHolder.getInstance().setCurrentQbUser(((QBSessionResult) result).getSession().getUserId(), password);
                QBChatService.getInstance().loginWithUser(DataHolder.getInstance().getCurrentQbUser(), loginListener);
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
        Intent intent = new Intent(this, ActivityCallUser.class);
        intent.putExtra("userId", DataHolder.getInstance().getCurrentQbUser().getId() == firstUserId ? secondUserId : firstUserId);
//        intent.putExtra("userName", DataHolder.getInstance().getCurrentQbUser().getId() == firstUserId ? secondUserName : firstUserName);
        intent.putExtra("myName", DataHolder.getInstance().getCurrentQbUser().getId() == firstUserId ? firstUserName : secondUserName);
        startActivity(intent);
        finish();
    }
}