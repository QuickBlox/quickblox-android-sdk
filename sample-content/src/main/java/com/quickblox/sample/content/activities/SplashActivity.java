package com.quickblox.sample.content.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.quickblox.auth.QBAuth;
import com.quickblox.auth.model.QBSession;
import com.quickblox.content.QBContent;
import com.quickblox.content.model.QBFile;
import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.core.request.QBPagedRequestBuilder;
import com.quickblox.sample.content.R;
import com.quickblox.sample.content.helper.DataHolder;
import com.quickblox.sample.content.utils.Constants;
import com.quickblox.sample.core.ui.activity.CoreSplashActivity;
import com.quickblox.users.model.QBUser;

import java.util.ArrayList;
import java.util.List;

public class SplashActivity extends CoreSplashActivity {

    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        progressBar = (ProgressBar) findViewById(R.id.progress_bar);

        createSession();
    }

    @Override
    protected String getAppName() {
        return getString(R.string.splash_app_title);
    }

    @Override
    protected void proceedToTheNextActivity() {
        GalleryActivity.start(this);
        finish();
    }

    private void createSession() {
        QBUser qbUser = new QBUser(Constants.USER_LOGIN, Constants.USER_PASSWORD);

        // Create QuickBlox session
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
        // Get all user's uploaded files
        QBPagedRequestBuilder builder = new QBPagedRequestBuilder();
        builder.setPerPage(Constants.QB_PER_PAGE);
        builder.setPage(Constants.QB_PAGE);

        QBContent.getFiles(builder, new QBEntityCallbackImpl<ArrayList<QBFile>>() {
            @Override
            public void onSuccess(ArrayList<QBFile> qbFiles, Bundle bundle) {
                DataHolder.getDataHolder().setQbFileList(qbFiles);
                proceedToTheNextActivity();
            }

            @Override
            public void onError(List<String> strings) {
                Log.d("SplashActivity", "onError: " + strings);
            }
        });
    }
}