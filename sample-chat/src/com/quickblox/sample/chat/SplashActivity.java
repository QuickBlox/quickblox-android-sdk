package com.quickblox.sample.chat;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import com.quickblox.core.QBCallbackImpl;
import com.quickblox.core.QBSettings;
import com.quickblox.core.result.Result;
import com.quickblox.module.auth.QBAuth;

/**
 * Splash screen
 *
 * @author <a href="mailto:assist@quickblox.com">QuickBlox team</a>
 */
public class SplashActivity extends Activity {

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
        // Read 5 min guide http://quickblox.com/developers/5_Minute_Guide
        QBSettings.getInstance().fastConfigInit("1028", "jCr7OwnvgV5wFmm", "4JmKPAnwN7ps5Xt");

        // ================= QuickBlox ===== Step 2 =================
        // Authorize application.
        QBAuth.createSession(new QBCallbackImpl(){
            @Override
            public void onComplete(Result result, Object context) {
                progressBar.setVisibility(View.GONE);

                if (result.isSuccess()) {
                    // Show Main activity
                    Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                    startActivity(intent);
                } else {
                    // Show error
                    AlertDialog.Builder dialog = new AlertDialog.Builder(SplashActivity.this);
                    dialog.setMessage("Error(s) occurred. Look into DDMS log for details, " +
                            "please. Errors: " + result.getErrors()).create().show();
                }
            }
        });
    }
}