package com.quickblox.sample.location.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;

import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.core.QBSettings;
import com.quickblox.auth.QBAuth;
import com.quickblox.auth.model.QBSession;
import com.quickblox.users.model.QBUser;
import com.quickblox.sample.location.R;
import com.quickblox.sample.location.utils.Constants;
import com.quickblox.sample.location.utils.DialogUtils;

import java.util.List;

public class SplashActivity extends Activity {

    private Context context;
    private Resources resources;
    private ProgressBar progressBar;

    private void startMapActivity() {
        finish();
        Intent intent = new Intent(this, MapActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        context = this;
        resources = getResources();

        initUI();

        // Initialize QuickBlox application with credentials.
        //
        QBSettings.getInstance().fastConfigInit(String.valueOf(Constants.APP_ID), Constants.AUTH_KEY, Constants.AUTH_SECRET);

        // Create QuickBlox session
        //
        QBUser qbUser = new QBUser(Constants.USER_LOGIN, Constants.USER_PASSWORD);
        QBAuth.createSession(qbUser, new QBEntityCallbackImpl<QBSession>() {
            @Override
            public void onSuccess(QBSession qbSession, Bundle bundle) {
                startMapActivity();
            }

            @Override
            public void onError(List<String> errors) {
                DialogUtils.showLong(context, resources.getString(R.string.dlg_location_error) + errors);
                progressBar.setVisibility(View.INVISIBLE);
            }
        });
    }

    private void initUI() {
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        progressBar.setVisibility(View.VISIBLE);
    }
}