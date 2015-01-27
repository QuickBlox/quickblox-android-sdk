package com.quickblox.sample.videochatwebrtcnew.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.quickblox.sample.videochatwebrtcnew.R;
import com.quickblox.sample.videochatwebrtcnew.adapters.UsersAdapter;

/**
 * Created by tereha on 25.01.15.
 */
public class ListUsersActivity extends Activity /*implements View.OnClickListener*/ {

    private UsersAdapter usersListAdapter;
    private ListView usersList;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initUI();
        initUsersList();
    }

    private void initUI() {
        usersList = (ListView) findViewById(R.id.usersListView);

    }

    private String [] createArrayUsers(){
        String [] users = new String[10];
        users[0] = "User 1";
        users[1] = "User 2";
        users[2] = "User 3";
        users[3] = "User 4";
        users[4] = "User 5";
        users[5] = "User 6";
        users[6] = "User 7";
        users[7] = "User 8";
        users[8] = "User 9";
        users[9] = "User 10";

        return users;
    }

    private void initUsersList() {

        usersListAdapter = new UsersAdapter(this, createArrayUsers());
        usersList.setAdapter(usersListAdapter);
        usersList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);


        usersList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //if ();



                Intent intent = new Intent(ListUsersActivity.this, InterlocutorsActivity.class);
                startActivity(intent);
            }
        });
    }
}
