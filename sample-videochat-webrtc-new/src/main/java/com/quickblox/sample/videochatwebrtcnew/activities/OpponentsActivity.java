package com.quickblox.sample.videochatwebrtcnew.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.ListView;
import android.widget.TextView;


import com.quickblox.sample.videochatwebrtcnew.R;
import com.quickblox.sample.videochatwebrtcnew.User;
import com.quickblox.sample.videochatwebrtcnew.adapters.OpponentsAdapter;

import java.lang.reflect.Field;
import java.util.ArrayList;


/**
 * Created by tereha on 27.01.15.
 */
public class OpponentsActivity  extends Activity {

    private OpponentsAdapter opponentsAdapter;
    private ListView opponentsList;

    private Intent intent;
    private String login;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_opponents);

        initUI();
        initActionBar();
        initUsersList();
    }

    private void initUI() {
        opponentsList = (ListView) findViewById(R.id.opponentsList);
        intent = getIntent();
        login = intent.getStringExtra("loginedUserLogin");


    }

    private void initActionBar() {
        ActionBar mActionBar = getActionBar();
        mActionBar.setDisplayShowHomeEnabled(false);
        mActionBar.setDisplayShowTitleEnabled(false);
        LayoutInflater mInflater = LayoutInflater.from(this);

        View mCustomView = mInflater.inflate(R.layout.actionbar_view, null);
        TextView numberOfListAB = (TextView) mCustomView.findViewById(R.id.numberOfListAB);
        numberOfListAB.setText("N");
        TextView loginAsAB = (TextView) mCustomView.findViewById(R.id.loginAsAB);
        loginAsAB.setText(R.string.logged_in_as);
        TextView userNameAB = (TextView) mCustomView.findViewById(R.id.userNameAB);
        userNameAB.setText("Userlogin: " + login);

        mActionBar.setCustomView(mCustomView);
        mActionBar.setDisplayShowCustomEnabled(true);

    }

    private ArrayList<User> createOpponentsCollection(){

        ArrayList<User> opponents = new ArrayList<>();
        opponents.add(new User("User 1", "user_1", "11111111"));
        opponents.add(new User("User 2", "user_2", "11111111"));
        opponents.add(new User("User 3", "user_3", "11111111"));
        opponents.add(new User("User 4", "user_4", "11111111"));
        opponents.add(new User("User 5", "user_5", "11111111"));
        opponents.add(new User("User 6", "user_6", "11111111"));
        opponents.add(new User("User 7", "user_7", "11111111"));
        opponents.add(new User("User 8", "user_8", "11111111"));
        opponents.add(new User("User 9", "user_9", "11111111"));
        opponents.add(new User("User 10", "user_10", "11111111"));

        int indexLogginedUser = -1;
        for (User usr : opponents) {

            if (usr.getLogin().equals(login)) {
                indexLogginedUser = opponents.indexOf(usr);
                break;
            }
        }
        if (indexLogginedUser != -1)
        opponents.remove(indexLogginedUser);

        return opponents;
    }



    private void initUsersList() {

        ArrayList<User> opponents = createOpponentsCollection();
        opponentsAdapter = new OpponentsAdapter(this, opponents);
        opponentsList.setAdapter(opponentsAdapter);


    }

    /*@Override
    public boolean onOptionsItemSelected(MenuItem item) {


        switch (item.getItemId()) {
            case R.id.exit_to_app:
                // actions is there
                break;

            default:
                return super.onOptionsItemSelected(item);
        }
    }*/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }
}
