package com.quickblox.simplesample.messages.main.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;

import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.auth.QBAuth;
import com.quickblox.auth.model.QBSession;
import com.quickblox.users.model.QBUser;

import com.quickblox.simplesample.messages.R;
import com.quickblox.simplesample.messages.main.Consts;
import com.quickblox.simplesample.messages.main.utils.DialogUtils;

import java.util.List;

public class SplashActivity extends Activity{

    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        initUI();

        // Create QuickBlox session
        //
        QBUser qbUser = new QBUser();
        qbUser.setLogin(Consts.USER_LOGIN);
        qbUser.setPassword(Consts.USER_PASSWORD);

        QBAuth.createSession(qbUser, new QBEntityCallbackImpl<QBSession>() {
            @Override
            public void onSuccess(QBSession qbSession, Bundle bundle) {
                Intent intent = new Intent(SplashActivity.this, MessagesActivity.class);
                startActivity(intent);
                finish();
            }

            @Override
            public void onError(List<String> strings) {
                // Show errors
                DialogUtils.showLong(SplashActivity.this, strings.toString());
                progressBar.setVisibility(View.INVISIBLE);
            }
        });
    }

    private void initUI() {
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        progressBar.setVisibility(View.VISIBLE);
    }
}