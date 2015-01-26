package com.quickblox.sample.videochatwebrtcnew.activities;

import android.app.Activity;
import android.app.ListActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
//import android.support.v7.app.ActionBarActivity;

import com.quickblox.sample.videochatwebrtcnew.R;
import com.quickblox.sample.videochatwebrtcnew.adapters.UsersAdapter;

/**
 * Created by tereha on 25.01.15.
 */
public class ListUsersActivity extends Activity {

    private UsersAdapter usersListAdapter;
    private TextView welcomingMessage;
    private ListView usersList;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initUI();
        initUsersList();
    }

    private void initUI() {
        //welcomingMessage = (TextView) findViewById(R.id.welcomingMessage);
        //welcomingMessage.setText(R.string.welcoming_message);
        usersList = (ListView) findViewById(R.id.usersListView);

    }

    private String [] createArrayUsers(){
        String [] users = new String[10];
        for (int i =0; i<users.length; i++){
            users[i] = "User " + (i+1);
        }
        return users;
    }

    private void initUsersList() {

        usersListAdapter = new UsersAdapter(this, createArrayUsers());
        usersList.setAdapter(usersListAdapter);
        //usersList.setOnItemClickListener(this);
    }




}
