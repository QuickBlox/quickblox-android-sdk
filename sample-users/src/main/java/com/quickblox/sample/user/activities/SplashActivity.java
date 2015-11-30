package com.quickblox.sample.user.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;

import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.core.QBSettings;
import com.quickblox.auth.QBAuth;
import com.quickblox.auth.model.QBSession;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;
import com.quickblox.sample.user.R;
import com.quickblox.sample.user.helper.DataHolder;
import com.quickblox.sample.user.utils.DialogUtils;

import java.util.ArrayList;
import java.util.List;

import static com.quickblox.sample.user.definitions.Consts.APP_ID;
import static com.quickblox.sample.user.definitions.Consts.AUTH_KEY;
import static com.quickblox.sample.user.definitions.Consts.AUTH_SECRET;

public class SplashActivity extends Activity{

    private Context context;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        context = this;

        initUI();

        // Initialize QuickBlox application with credentials.
        //
        QBSettings.getInstance().fastConfigInit(APP_ID, AUTH_KEY, AUTH_SECRET);

        // Create QuickBlox session
        //
        QBAuth.createSession(new QBEntityCallbackImpl<QBSession>() {
            @Override
            public void onSuccess(QBSession qbSession, Bundle bundle) {
                getAllUser();
            }

            @Override
            public void onError(List<String> errors) {
                // print errors that came from server
                DialogUtils.showLong(context, errors.get(0));
                progressBar.setVisibility(View.INVISIBLE);
            }
        });
    }

    private void initUI() {
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        progressBar.setVisibility(View.VISIBLE);
    }

    private void getAllUser() {

        QBUsers.getUsers(null, new QBEntityCallbackImpl<ArrayList<QBUser>>() {
            @Override
            public void onSuccess(ArrayList<QBUser> qbUsers, Bundle bundle) {
                DataHolder.getDataHolder().setQbUsersList(qbUsers);
                startGetAllUsersActivity();
            }

            @Override
            public void onError(List<String> errors) {
                DialogUtils.showLong(context, errors.get(0));
            }
        });
    }

    private void startGetAllUsersActivity() {
        Intent intent = new Intent(this, UsersListActivity.class);
        startActivity(intent);
        finish();
    }
}