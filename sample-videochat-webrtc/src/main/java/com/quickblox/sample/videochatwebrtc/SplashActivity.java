package com.quickblox.sample.videochatwebrtc;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.quickblox.chat.QBChatService;
import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.auth.QBAuth;
import com.quickblox.auth.model.QBSession;
import com.quickblox.users.model.QBUser;

import java.util.List;

/**
 * Created by QuickBlox on 24.02.14.
 */
public class SplashActivity extends Activity {
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initChatService();

        // setup UI
        //
        setContentView(R.layout.login_layout);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please wait");
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
    }

    private void createSession(String login, final String password) {
        QBAuth.createSession(login, password, new QBEntityCallbackImpl<QBSession>() {
            @Override
            public void onSuccess(QBSession qbSession, Bundle bundle) {

                // Save current user
                //
                QBUser currentUser = new QBUser();
                currentUser.setId(qbSession.getUserId());
                currentUser.setPassword(password);
                VideoChatApplication app = (VideoChatApplication)getApplication();
                app.setCurrentUser(currentUser);

                // Login to Chat
                //
                QBChatService.getInstance().login(app.getCurrentUser(), new QBEntityCallbackImpl() {
                    @Override
                    public void onSuccess() {
                        // show next activity
                        showMainActivity();
                    }

                    @Override
                    public void onError(List errors) {
                        Toast.makeText(SplashActivity.this, "Error when login", Toast.LENGTH_SHORT).show();
                    }
                });

            }

            @Override
            public void onError(List<String> errors) {
                progressDialog.dismiss();
                Toast.makeText(SplashActivity.this, "Error when login, check test users login and password", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initChatService(){
        QBChatService.setDebugEnabled(true);

        if (!QBChatService.isInitialized()) {
            Log.d("ActivityLogin", "InitChat");
            QBChatService.init(this);
        }else{
            Log.d("ActivityLogin", "InitChat not needed");
        }
    }

    private void showMainActivity() {
        progressDialog.dismiss();

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}