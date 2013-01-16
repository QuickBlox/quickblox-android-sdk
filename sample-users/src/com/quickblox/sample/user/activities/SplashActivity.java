package com.quickblox.sample.user.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.quickblox.core.QBCallback;
import com.quickblox.core.QBSettings;
import com.quickblox.core.result.Result;
import com.quickblox.module.auth.QBAuth;
import com.quickblox.module.users.QBUsers;
import com.quickblox.module.users.result.QBUserPagedResult;
import com.quickblox.sample.user.R;
import com.quickblox.sample.user.definitions.QBQueries;
import com.quickblox.sample.user.helper.DataHolder;

import static com.quickblox.sample.user.definitions.Consts.*;

/**
 * Created with IntelliJ IDEA.
 * User: android
 * Date: 03.12.12
 * Time: 12:38
 * To change this template use File | Settings | File Templates.
 */
public class SplashActivity extends Activity implements QBCallback {


    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);
        // ================= QuickBlox ===== Step 1 =================
        // Initialize QuickBlox application with credentials.
        // Getting app credentials -- http://quickblox.com/developers/Getting_application_credentials
        QBSettings.getInstance().fastConfigInit(APP_ID, AUTH_KEY, AUTH_SECRET);
        // Authorize application
        QBAuth.createSession(this, QBQueries.QB_QUERY_AUTHORIZE_APP);
    }


    private void getAllUser() {
        // Get all users for the current app
        QBUsers.getUsers(new QBCallback() {
            @Override
            public void onComplete(Result result) {
                // return QBUserPagedResult for getUsers query
                QBUserPagedResult qbUserPagedResult = (QBUserPagedResult) result;
                DataHolder.getDataHolder().setQbUserList(qbUserPagedResult.getUsers());
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
    public void onComplete(Result result, Object context) {
        QBQueries qbQueryType = (QBQueries) context;
        if (result.isSuccess()) {
            switch (qbQueryType) {
                case QB_QUERY_AUTHORIZE_APP:
                    getAllUser();
                    break;
            }
        } else {
            // print errors that came from server
            Toast.makeText(getBaseContext(), result.getErrors().get(0), Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.INVISIBLE);
        }

    }

    private void startGetAllUsersActivity() {
        Intent intent = new Intent(this, GetAllUsersActivity.class);
        startActivity(intent);
        finish();
    }

}
