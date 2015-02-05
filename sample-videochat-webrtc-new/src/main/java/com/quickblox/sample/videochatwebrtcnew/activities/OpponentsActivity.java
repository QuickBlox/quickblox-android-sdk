package com.quickblox.sample.videochatwebrtcnew.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.quickblox.sample.videochatwebrtcnew.R;
import com.quickblox.sample.videochatwebrtcnew.User;
import com.quickblox.sample.videochatwebrtcnew.adapters.OpponentsAdapter;
import com.quickblox.sample.videochatwebrtcnew.helper.DataHolder;

import java.util.ArrayList;


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
        numberOfListAB.setBackgroundResource(ListUsersActivity.resourceSelector(usersList
                .get((searchIndexLogginedUser(usersList))).getUserNumber()));
        numberOfListAB.setText(String.valueOf(usersList
                .get((searchIndexLogginedUser(usersList))).getUserNumber()));
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

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnAudioCall:

                //opponentsListToCall = new ArrayList<>();
               // opponentsListToCall.addAll(OpponentsAdapter.positions);


                //for (String s : opponentsListToCall)
                 //   Log.d("Track", "Nubers of opponents " + s);

                Intent intent = new Intent(OpponentsActivity.this, IncAudioCallActivity.class);
                //intent.putExtra("login", login);
                startActivity(intent);
                break;

            case R.id.btnVideoCall:

                break;
        }
    }
}
