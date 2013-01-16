package com.quickblox.sample.chat;

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

/**
 * Date: 24.10.12
 * Time: 22:16
 */

/**
 * Activity provides interface for user auth.
 *
 * @author <a href="mailto:oleg@quickblox.com">Oleg Soroka</a>
 */
public class SplashActivity extends Activity implements QBCallback {

    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);

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
        QBSettings.getInstance().fastConfigInit("1028", "jCr7OwnvgV5wFmm", "4JmKPAnwN7ps5Xt");

        // ================= QuickBlox ===== Step 2 =================
        // Authorize application.
        QBAuth.createSession(this);
    }

    @Override
    public void onComplete(Result result) {
        progressBar.setVisibility(View.GONE);

        if (result.isSuccess()) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        } else {
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setMessage("Error(s) occurred. Look into DDMS log for details, " +
                    "please. Errors: " + result.getErrors()).create().show();
        }

    }

    @Override
    public void onComplete(Result result, Object context) {
    }
}