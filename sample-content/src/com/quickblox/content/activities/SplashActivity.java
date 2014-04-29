package com.quickblox.content.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;

import com.quickblox.content.R;
import com.quickblox.content.helper.DataHolder;
import com.quickblox.content.utils.Constants;
import com.quickblox.core.QBCallback;
import com.quickblox.core.QBSettings;
import com.quickblox.core.result.Result;
import com.quickblox.internal.core.request.QBPagedRequestBuilder;
import com.quickblox.module.auth.QBAuth;
import com.quickblox.module.auth.result.QBSessionResult;
import com.quickblox.module.content.QBContent;
import com.quickblox.module.content.result.QBFilePagedResult;
import com.quickblox.module.users.model.QBUser;

public class SplashActivity extends Activity {

    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        initUI();

        // ================= QuickBlox ===== Step 1 =================
        // Initialize QuickBlox application with credentials.
        // Getting app credentials -- http://quickblox.com/developers/Getting_application_credentials
        QBSettings.getInstance().fastConfigInit(String.valueOf(Constants.APP_ID), Constants.AUTH_KEY,
                Constants.AUTH_SECRET);
        authorizeApp();
    }

    private void initUI() {
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        progressBar.setVisibility(View.VISIBLE);
    }

    private void authorizeApp() {
        QBUser qbUser = new QBUser(Constants.USER_LOGIN, Constants.USER_PASSWORD);
        // authorize app with default user
        QBAuth.createSession(qbUser, new QBCallback() {
            @Override
            public void onComplete(Result result) {
                if (result.isSuccess()) {
                    // return result from QBAuth.authorizeApp() query
                    QBSessionResult qbSessionResult = (QBSessionResult) result;
                    DataHolder.getDataHolder().setSignInUserId(qbSessionResult.getSession().getUserId());
                    // retrieve user's files
                    getFileList();
                } else {
                    progressBar.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onComplete(Result result, Object o) {
            }
        });
    }

    private void getFileList() {
        // ================= QuickBlox ===== Step 2 =================
        // Gey all user's files
        QBPagedRequestBuilder builder = new QBPagedRequestBuilder();
        builder.setPerPage(Constants.QB_PER_PAGE);
        builder.setPage(Constants.QB_PAGE);
        QBContent.getFiles(builder, new QBCallback() {
            @Override
            public void onComplete(Result result) {
                QBFilePagedResult qbFilePagedResult = (QBFilePagedResult) result;
                DataHolder.getDataHolder().setQbFileList(qbFilePagedResult.getFiles());
                // show activity_gallery
                startGalleryActivity();
            }

            @Override
            public void onComplete(Result result, Object o) {
            }
        });
    }

    private void startGalleryActivity() {
        Intent intent = new Intent(this, GalleryActivity.class);
        startActivity(intent);
        finish();
    }
}