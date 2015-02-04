package com.quickblox.sample.videochatwebrtcnew.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;


import com.quickblox.sample.videochatwebrtcnew.Opponent;
import com.quickblox.sample.videochatwebrtcnew.R;
import com.quickblox.sample.videochatwebrtcnew.User;
import com.quickblox.sample.videochatwebrtcnew.adapters.OpponentsAdapter;
import com.quickblox.sample.videochatwebrtcnew.helper.DataHolder;

import java.lang.reflect.Field;
import java.util.ArrayList;

import static java.util.Collections.addAll;


/**
 * Created by tereha on 27.01.15.
 */
public class OpponentsActivity  extends Activity implements View.OnClickListener {

    private OpponentsAdapter opponentsAdapter;
    private ListView opponentsList;
    private String login;
    private Button btnAudioCall;
    private Button btnVideoCall;
    private ArrayList<String> opponentsListToCall;
    private ArrayList<User> opponents;
    private ArrayList<User> usersList;

    public OpponentsActivity() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_opponents);

        usersList = DataHolder.createUsersList();

        initUI();
        initActionBar();
        initUsersList();
    }

    private void initUI() {

        opponentsList = (ListView) findViewById(R.id.opponentsList);
        login = getIntent().getStringExtra("login");

        btnAudioCall = (Button)findViewById(R.id.btnAudioCall);
        btnVideoCall = (Button)findViewById(R.id.btnVideoCall);

        btnAudioCall.setOnClickListener(this);
        btnVideoCall.setOnClickListener(this);

    }

    private void initActionBar() {

        ActionBar mActionBar = getActionBar();
        mActionBar.setDisplayShowHomeEnabled(false);
        mActionBar.setDisplayShowTitleEnabled(false);
        LayoutInflater mInflater = LayoutInflater.from(this);

        View mCustomView = mInflater.inflate(R.layout.actionbar_view, null);
        TextView numberOfListAB = (TextView) mCustomView.findViewById(R.id.numberOfListAB);
        numberOfListAB.setBackgroundResource(ListUsersActivity.resourceSelector(searchIndexLogginedUser(usersList)+1));
        numberOfListAB.setText(String.valueOf(searchIndexLogginedUser(usersList)+1));
        TextView loginAsAB = (TextView) mCustomView.findViewById(R.id.loginAsAB);
        loginAsAB.setText(R.string.logged_in_as);
        TextView userNameAB = (TextView) mCustomView.findViewById(R.id.userNameAB);
        userNameAB.setText(usersList
                .get((searchIndexLogginedUser(usersList))).getFullName());

        mActionBar.setCustomView(mCustomView);
        mActionBar.setDisplayShowCustomEnabled(true);

    }

    private ArrayList<User> createOpponentsFromUserList(ArrayList<User> usersList){
        opponents = new ArrayList<>();
        opponents.addAll(usersList);
        opponents.remove(searchIndexLogginedUser(opponents));

        return opponents;

    }

    private int searchIndexLogginedUser (ArrayList<User> usersList) {

        int indexLogginedUser = -1;

        for (User usr : usersList) {
            if (usr.getLogin().equals(login)) {
                indexLogginedUser = usersList.indexOf(usr);
                break;
            }
        }

        return indexLogginedUser;
    }



    private void initUsersList() {

        opponentsAdapter = new OpponentsAdapter(this, createOpponentsFromUserList(usersList));
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

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnAudioCall:

                opponentsListToCall = new ArrayList<>();
                opponentsListToCall.addAll(OpponentsAdapter.positions);


                for (String s : opponentsListToCall)
                    Log.d("Track", "Nubers of opponents " + s);

                /*Intent intent = new Intent(OpponentsActivity.this, OpponentsActivity.class);
                intent.putExtra("login", login);
                startActivity(intent);*/
                break;

            case R.id.btnVideoCall:
                //
                break;
        }
    }
}
