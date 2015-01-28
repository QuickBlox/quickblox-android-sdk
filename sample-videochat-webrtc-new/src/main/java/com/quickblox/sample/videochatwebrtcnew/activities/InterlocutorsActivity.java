package com.quickblox.sample.videochatwebrtcnew.activities;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ListView;


import com.quickblox.auth.QBAuth;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.sample.videochatwebrtcnew.R;
import com.quickblox.sample.videochatwebrtcnew.adapters.InterlocutorsAdapter;


/**
 * Created by tereha on 27.01.15.
 */
public class InterlocutorsActivity  extends Activity {

    private InterlocutorsAdapter interlocutorsAdapter;
    private ListView interlocutorsList;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interlocutors);

        initUI();
        initUsersList();
    }

    private void initUI() {
        interlocutorsList = (ListView) findViewById(R.id.interlocutorsList);

    }

    private String [] createArrayInterlocutors(){
        String [] interlocutors = new String[10];
        for (int i =0; i<interlocutors.length; i++){
            interlocutors[i] = "User " + (i+1);
        }
        return interlocutors;
    }

    private void initUsersList() {

        interlocutorsAdapter = new InterlocutorsAdapter(this, createArrayInterlocutors());
        interlocutorsList.setAdapter(interlocutorsAdapter);
    }
}
