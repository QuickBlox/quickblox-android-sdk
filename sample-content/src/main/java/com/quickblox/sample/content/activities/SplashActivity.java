package com.quickblox.sample.content.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.quickblox.sample.content.R;
import com.quickblox.sample.content.helper.DataHolder;
import com.quickblox.sample.content.utils.Constants;
import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.core.QBSettings;
import com.quickblox.core.request.QBPagedRequestBuilder;
import com.quickblox.auth.QBAuth;
import com.quickblox.auth.model.QBSession;
import com.quickblox.content.QBContent;
import com.quickblox.content.model.QBFile;
import com.quickblox.users.model.QBUser;

import java.util.ArrayList;
import java.util.List;

public class SplashActivity extends Activity {

    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        initUI();

        // Initialize QuickBlox application with credentials.
        //
        QBSettings.getInstance().fastConfigInit(String.valueOf(Constants.APP_ID), Constants.AUTH_KEY,
                Constants.AUTH_SECRET);

        createSession();
    }

    private void initUI() {
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        progressBar.setVisibility(View.VISIBLE);
    }

    private void createSession() {
        QBUser qbUser = new QBUser(Constants.USER_LOGIN, Constants.USER_PASSWORD);

        // Create QuickBlox session
        //
        QBAuth.createSession(qbUser, new QBEntityCallbackImpl<QBSession>() {
            @Override
            public void onSuccess(QBSession qbSession, Bundle bundle) {
                DataHolder.getDataHolder().setSignInUserId(qbSession.getUserId());

                // retrieve user's files
                getFileList();
            }

            @Override
            public void onError(List<String> strings) {
                progressBar.setVisibility(View.INVISIBLE);
            }
        });
    }

    private void getFileList() {

        // Gey all user's files
        //
        QBPagedRequestBuilder builder = new QBPagedRequestBuilder();
        builder.setPerPage(Constants.QB_PER_PAGE);
        builder.setPage(Constants.QB_PAGE);

        QBContent.getFiles(builder, new QBEntityCallbackImpl<ArrayList<QBFile>>() {
            @Override
            public void onSuccess(ArrayList<QBFile> qbFiles, Bundle bundle) {
                DataHolder.getDataHolder().setQbFileList(qbFiles);
                // show activity_gallery
                startGalleryActivity();
            }

            @Override
            public void onError(List<String> strings) {
                Log.d("SplashActivity", "onError: " + strings);
            }
        });
    }

    private void startGalleryActivity() {
        Intent intent = new Intent(this, GalleryActivity.class);
        startActivity(intent);
        finish();
    }
}