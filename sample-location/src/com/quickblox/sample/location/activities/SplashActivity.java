package com.quickblox.sample.location.activities;

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
import com.quickblox.sample.location.R;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        ImageView qbLinkPanel = (ImageView) findViewById(R.id.splash_qb_link);
        qbLinkPanel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("http://quickblox.com/developers/Android"));
                startActivity(browserIntent);
            }
        });

        // ================= QuickBlox ===== Step 1 =================
        // Initialize QuickBlox application with credentials.
        // Getting app credentials -- http://quickblox.com/developers/Getting_application_credentials
        QBSettings.getInstance().fastConfigInit("99", "63ebrp5VZt7qTOv", "YavMAxm5T59-BRw");

        // ================= QuickBlox ===== Step 2 =================
        // Authorize application with user.
        // You can create user on admin.quickblox.com, Users module or through QBUsers.signUp method

        QBAuth.createSession("testuser", "testpassword", this);
    }

    @Override
    public void onComplete(Result result) {
        progressBar.setVisibility(View.GONE);

        if (result.isSuccess()) {
            // Show Map activity
            finish();
            Intent intent = new Intent(this, MapActivity.class);
            startActivity(intent);
        } else {

            // Show errors
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setMessage("Error(s) occurred. Look into DDMS log for details, " +
                    "please. Errors: " + result.getErrors()).create().show();
        }

    }

    @Override
    public void onComplete(Result result, Object context) { }
}
