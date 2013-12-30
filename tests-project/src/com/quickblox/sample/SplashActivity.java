package com.quickblox.sample;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.quickblox.core.QBCallback;
import com.quickblox.core.QBSettings;
import com.quickblox.core.result.Result;
import com.quickblox.internal.core.exception.BaseServiceException;
import com.quickblox.module.auth.QBAuth;
import com.quickblox.module.auth.result.QBSessionResult;
import com.quickblox.module.custom.QBCustomObjects;
import com.quickblox.module.custom.model.QBCustomObject;
import com.quickblox.module.custom.result.QBCustomObjectLimitedResult;
import com.quickblox.module.users.model.QBUser;
import com.quickblox.sample.definition.QBQueries;
import com.quickblox.sample.helper.DataHolder;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Created with IntelliJ IDEA.
 * User: android
 * Date: 03.12.12
 * Time: 12:38
 * To change this template use File | Settings | File Templates.
 */
public class SplashActivity extends Activity implements QBCallback {

    private final String NOTE1_ID = "529daf702195be5d8d478389";
    private final String NOTE2_ID = "529daf702195be5d8d478388";

    private final int APP_ID = 99;
    private final String AUTH_KEY = "63ebrp5VZt7qTOv";
    private final String AUTH_SECRET = "YavMAxm5T59-BRw";
    private final String USER_LOGIN = "Gerrit";
    private final String USER_PASSWORD = "qwerty123";
    private final String CLASS_NAME = "note";
    private final String TITLE = "title";
    private final String STATUS = "status";
    private final String COMMENTS = "comments";

    String[] types = {"Paranormal", "Warrior", "Daredevil", "Protector",

            "Tycoon", "Nerd", "Rogue", "Brooding", "Alpha"};

    private ProgressBar progressBar;
    private LinkedList<QBCustomObject> qbCustomObjectList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);

        // ================= QuickBlox ===== Step 1 =================
        // Initialize QuickBlox application with credentials.
        // Getting app credentials -- http://quickblox.com/developers/Getting_application_credentials
        QBSettings.getInstance().fastConfigInit(String.valueOf(APP_ID), AUTH_KEY, AUTH_SECRET);
        QBUser qbUser = new QBUser(USER_LOGIN, USER_PASSWORD);
        // authorize app with default user
        QBAuth.createSession(qbUser, this, QBQueries.SIGN_IN);

    }

    private void getNoteList() {
        // ================= QuickBlox ===== Step 2 =================
        // Get all notes

        QBCustomObjects.getObjects(CLASS_NAME,  this,  QBQueries.GET_NOTE_LIST);
    }


    @Override
    public void onComplete(Result result) {

    }

    @Override
    public void onComplete(Result result, Object context){
        QBQueries qbQueryType = (QBQueries) context;
        if (result.isSuccess()) {
            switch (qbQueryType) {
                case SIGN_IN:
                    // return result from QBAuth.authorizeApp() query
                    QBSessionResult qbSessionResult = (QBSessionResult) result;
                    DataHolder.getDataHolder().setSignInUserId(qbSessionResult.getSession().getUserId());
                    try {
                        QBAuth.getBaseService().getTokenExpirationDate();
                    } catch (BaseServiceException e) {
                        e.printStackTrace();
                    }
                    //getNoteList();
                    break;
                case GET_NOTE_LIST:
                    //return QBCustomObjectLimitedResult for getObjects query
                    // get all custom objects by .getCustomObjects()
                    ArrayList<QBCustomObject> qbCustomObjects = ((QBCustomObjectLimitedResult) result).getCustomObjects();
                    if (qbCustomObjects.size() != 0) {
                        for (QBCustomObject co : qbCustomObjects) {
                            DataHolder.getDataHolder().addNoteToList(co.getCustomObjectId(), co.getFields().get(TITLE).toString(),
                                    co.getFields().get(STATUS).toString(), co.getUpdatedAt().toLocaleString(), co.getFields().get(COMMENTS).toString());
                        }
                    }
                    startDisplayNoteListActivity();
                    break;
            }

        } else {
            // print errors that came from server
            Toast.makeText(getBaseContext(), result.getErrors().get(0), Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.INVISIBLE);
        }
    }

    private void startDisplayNoteListActivity(){

    }

}
