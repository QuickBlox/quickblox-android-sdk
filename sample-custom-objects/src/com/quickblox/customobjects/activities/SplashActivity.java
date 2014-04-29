package com.quickblox.customobjects.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.quickblox.core.QBCallback;
import com.quickblox.core.QBSettings;
import com.quickblox.core.result.Result;
import com.quickblox.customobjects.R;
import com.quickblox.customobjects.definition.Consts;
import com.quickblox.customobjects.definition.QBQueries;
import com.quickblox.customobjects.helper.DataHolder;
import com.quickblox.module.auth.QBAuth;
import com.quickblox.module.auth.result.QBSessionResult;
import com.quickblox.module.custom.QBCustomObjects;
import com.quickblox.module.custom.model.QBCustomObject;
import com.quickblox.module.custom.result.QBCustomObjectLimitedResult;
import com.quickblox.module.users.model.QBUser;

import java.util.List;

public class SplashActivity extends Activity implements QBCallback {

    private ProgressBar progressBar;

    @Override
    public void onComplete(Result result) {
    }

    @Override
    public void onComplete(Result result, Object context) {
        QBQueries qbQueryType = (QBQueries) context;
        if (result.isSuccess()) {
            switch (qbQueryType) {
                case SIGN_IN:
                    // return result from QBAuth.authorizeApp() query
                    QBSessionResult qbSessionResult = (QBSessionResult) result;
                    DataHolder.getDataHolder().setSignInUserId(qbSessionResult.getSession().getUserId());
                    getNoteList();
                    break;
                case GET_NOTE_LIST:
                    //return QBCustomObjectLimitedResult for getObjects query
                    if (DataHolder.getDataHolder().size() > 0) {
                        DataHolder.getDataHolder().clear();
                    }
                    // get all custom objects by .getCustomObjects()
                    List<QBCustomObject> qbCustomObjects = ((QBCustomObjectLimitedResult) result)
                            .getCustomObjects();
                    if (qbCustomObjects != null && qbCustomObjects.size() != 0) {
                        for (QBCustomObject customObject : qbCustomObjects) {
                            DataHolder.getDataHolder().addNoteToList(customObject);
                        }
                    }
                    //DataHolder.getDataHolder().sort();
                    startDisplayNoteListActivity();
                    break;
            }
        } else {
            // print errors that came from server
            Toast.makeText(getBaseContext(), result.getErrors().get(0), Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.INVISIBLE);
        }
    }

    private void getNoteList() {
        // ================= QuickBlox ===== Step 2 =================
        // Get all notes
        QBCustomObjects.getObjects(Consts.CLASS_NAME, this, QBQueries.GET_NOTE_LIST);
    }

    private void startDisplayNoteListActivity() {
        Intent intent = new Intent(this, DisplayNoteListActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        initUI();

        // ================= QuickBlox ===== Step 1 =================
        // Initialize QuickBlox application with credentials.
        // Getting app credentials -- http://quickblox.com/developers/Getting_application_credentials
        QBSettings.getInstance().fastConfigInit(String.valueOf(Consts.APP_ID), Consts.AUTH_KEY, Consts.AUTH_SECRET);
        QBUser qbUser = new QBUser(Consts.USER_LOGIN, Consts.USER_PASSWORD);
        // authorize app with default user
        QBAuth.createSession(qbUser, this, QBQueries.SIGN_IN);
    }

    private void initUI() {
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        progressBar.setVisibility(View.VISIBLE);
    }
}
