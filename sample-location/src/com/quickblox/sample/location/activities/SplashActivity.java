package com.quickblox.sample.location.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.ProgressBar;

import com.quickblox.core.QBCallback;
import com.quickblox.core.QBSettings;
import com.quickblox.core.result.Result;
import com.quickblox.module.auth.QBAuth;
import com.quickblox.sample.location.R;
import com.quickblox.sample.location.utils.DialogUtils;

public class SplashActivity extends Activity implements QBCallback {

    private final int APP_ID = 99;
    private final String AUTH_KEY = "63ebrp5VZt7qTOv";
    private final String AUTH_SECRET = "YavMAxm5T59-BRw";

    private final String USER_LOGIN = "testuser";
    private final String USER_PASSWORD = "testpassword";

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

        progressBar = (ProgressBar) findViewById(R.id.progress_bar);

        // ================= QuickBlox ===== Step 1 =================
        // Initialize QuickBlox application with credentials.
        // Getting app credentials -- http://quickblox.com/developers/Getting_application_credentials
        QBSettings.getInstance().fastConfigInit(String.valueOf(APP_ID), AUTH_KEY, AUTH_SECRET);

        // ================= QuickBlox ===== Step 2 =================
        // Authorize application with user.
        // You can create user on admin.quickblox.com, Users module or through QBUsers.signUp method

        QBAuth.createSession(USER_LOGIN, USER_PASSWORD, this);
    }
}