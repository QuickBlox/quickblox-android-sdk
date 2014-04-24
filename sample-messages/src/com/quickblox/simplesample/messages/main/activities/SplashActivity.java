package com.quickblox.simplesample.messages.main.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import com.quickblox.core.QBCallback;
import com.quickblox.core.QBSettings;
import com.quickblox.core.result.Result;
import com.quickblox.module.auth.QBAuth;
import com.quickblox.module.users.model.QBUser;
import com.quickblox.simplesample.messages.R;

/**
 * Date: 24.10.12
 * Time: 22:16
 */

/**
 * Activity creates QuickBlox session & then show Map activity.
 *
 * @author <a href="mailto:igos@quickblox.com">Igor Khomenko</a>
 */
public class SplashActivity extends Activity implements QBCallback {

    private ProgressBar progressBar;

    private final int APP_ID = 99;
    private final String AUTH_KEY = "63ebrp5VZt7qTOv";
    private final String AUTH_SECRET = "YavMAxm5T59-BRw";

    private final String USER_LOGIN = "bobbobbob";
    private final String USER_PASSWORD = "bobbobbob";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        progressBar = (ProgressBar) findViewById(R.id.progress_bar);

        // ================= QuickBlox ===== Step 1 =================
        // Initialize QuickBlox application with credentials.
        // Getting app credentials -- http://quickblox.com/developers/Getting_application_credentials
        QBSettings.getInstance().fastConfigInit(String.valueOf(APP_ID), AUTH_KEY, AUTH_SECRET);

        // ================= QuickBlox ===== Step 2 =================
        // Authorize application with device & user.
        // You can create user on admin.quickblox.com, Users module or through QBUsers.signUp method
        QBUser qbUser = new QBUser();
        qbUser.setLogin("bobbobbob");
        qbUser.setPassword("bobbobbob");

        //
        // Create session with additional parameters
        QBAuth.createSession(qbUser, this);
    }

    @Override
    public void onComplete(Result result) {
        if (result.isSuccess()) {
            // Show Messages activity
            Intent intent = new Intent(this, MessagesActivity.class);
            startActivity(intent);
            finish();
        } else {

            // Show errors
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setMessage("Error(s) occurred. Look into DDMS log for details, " +
                    "please. Errors: " + result.getErrors()).create().show();
        }
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void onComplete(Result result, Object context) {
    }
}