package com.quickblox.sample.user.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;

import com.quickblox.core.QBCallback;
import com.quickblox.core.QBSettings;
import com.quickblox.core.result.Result;
import com.quickblox.module.auth.QBAuth;
import com.quickblox.module.users.QBUsers;
import com.quickblox.module.users.result.QBUserPagedResult;
import com.quickblox.sample.user.R;
import com.quickblox.sample.user.definitions.QBQueries;
import com.quickblox.sample.user.helper.DataHolder;
import com.quickblox.sample.user.utils.DialogUtils;

import static com.quickblox.sample.user.definitions.Consts.APP_ID;
import static com.quickblox.sample.user.definitions.Consts.AUTH_KEY;
import static com.quickblox.sample.user.definitions.Consts.AUTH_SECRET;

public class SplashActivity extends Activity implements QBCallback {

    private Context context;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        context = this;

        initUI();

        // ================= QuickBlox ===== Step 1 =================
        // Initialize QuickBlox application with credentials.
        // Getting app credentials -- http://quickblox.com/developers/Getting_application_credentials
        QBSettings.getInstance().fastConfigInit(APP_ID, AUTH_KEY, AUTH_SECRET);
        // Authorize application
        QBAuth.createSession(this, QBQueries.QB_QUERY_AUTHORIZE_APP);
    }

    private void initUI() {
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        progressBar.setVisibility(View.VISIBLE);
    }

    private void getAllUser() {
        // Get all users for the current app
        QBUsers.getUsers(new QBCallback() {
            @Override
            public void onComplete(Result result) {
                // return QBUserPagedResult for getUsers query
                QBUserPagedResult qbUserPagedResult = (QBUserPagedResult) result;
                DataHolder.getDataHolder().setQbUsersList(qbUserPagedResult.getUsers());
                startGetAllUsersActivity();
            }

            @Override
            public void onComplete(Result result, Object o) {
            }
        });
    }


    @Override
    public void onComplete(Result result) {

    }

    @Override
    public void onComplete(Result result, Object data) {
        QBQueries qbQueryType = (QBQueries) data;
        if (result.isSuccess() && qbQueryType == qbQueryType.QB_QUERY_AUTHORIZE_APP) {
            getAllUser();
        } else {
            // print errors that came from server
            DialogUtils.showLong(context, result.getErrors().get(0));
            progressBar.setVisibility(View.INVISIBLE);
        }
    }

    private void startGetAllUsersActivity() {
        Intent intent = new Intent(this, UsersListActivity.class);
        startActivity(intent);
        finish();
    }
}