package com.quickblox.simplesample.messages.main.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;

import com.quickblox.core.QBCallback;
import com.quickblox.core.result.Result;
import com.quickblox.module.auth.QBAuth;
import com.quickblox.module.users.model.QBUser;
import com.quickblox.simplesample.messages.R;

import com.quickblox.simplesample.messages.main.definitions.Consts;
import com.quickblox.simplesample.messages.main.utils.DialogUtils;

public class SplashActivity extends Activity implements QBCallback {

    private ProgressBar progressBar;

    @Override
    public void onComplete(Result result) {
        if (result.isSuccess()) {
            // Show Messages activity
            Intent intent = new Intent(this, MessagesActivity.class);
            startActivity(intent);
            finish();
        } else {
            // Show errors
            DialogUtils.showLong(this, result.getErrors() + "");
            progressBar.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onComplete(Result result, Object context) {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        initUI();

        // ================= QuickBlox ===== Step 2 =================
        // Authorize application with device & user.
        // You can create user on admin.quickblox.com, Users module or through QBUsers.signUp method
        QBUser qbUser = new QBUser();
        qbUser.setLogin(Consts.USER_LOGIN);
        qbUser.setPassword(Consts.USER_PASSWORD);

        // Create session with additional parameters
        QBAuth.createSession(qbUser, this);
    }

    private void initUI() {
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        progressBar.setVisibility(View.VISIBLE);
    }
}