package com.quickblox.sample.location.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;

import com.quickblox.core.QBCallback;
import com.quickblox.core.QBSettings;
import com.quickblox.core.result.Result;
import com.quickblox.module.auth.QBAuth;
import com.quickblox.sample.location.R;
import com.quickblox.sample.location.utils.Constants;
import com.quickblox.sample.location.utils.DialogUtils;

public class SplashActivity extends Activity implements QBCallback {

    private Context context;
    private Resources resources;
    private ProgressBar progressBar;

    @Override
    public void onComplete(Result result) {
        if (result.isSuccess()) {
            startMapActivity();
        } else {
            DialogUtils.showLong(context, resources.getString(R.string.dlg_location_error) + result
                    .getErrors());
            progressBar.setVisibility(View.INVISIBLE);
        }
    }

    private void startMapActivity() {
        finish();
        Intent intent = new Intent(this, MapActivity.class);
        startActivity(intent);
    }

    @Override
    public void onComplete(Result result, Object context) {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        context = this;
        resources = getResources();

        initUI();

        // ================= QuickBlox ===== Step 1 =================
        // Initialize QuickBlox application with credentials.
        // Getting app credentials -- http://quickblox.com/developers/Getting_application_credentials
        QBSettings.getInstance().fastConfigInit(String.valueOf(Constants.APP_ID), Constants.AUTH_KEY, Constants.AUTH_SECRET);

        // ================= QuickBlox ===== Step 2 =================
        // Authorize application with user.
        // You can create user on admin.quickblox.com, Users module or through QBUsers.signUp method

        QBAuth.createSession(Constants.USER_LOGIN, Constants.USER_PASSWORD, this);
    }

    private void initUI() {
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        progressBar.setVisibility(View.VISIBLE);
    }
}