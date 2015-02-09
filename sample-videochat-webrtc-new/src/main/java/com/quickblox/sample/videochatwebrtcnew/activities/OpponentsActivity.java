package com.quickblox.sample.videochatwebrtcnew.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import com.quickblox.sample.videochatwebrtcnew.R;
import com.quickblox.sample.videochatwebrtcnew.User;
import com.quickblox.sample.videochatwebrtcnew.adapters.OpponentsAdapter;
import com.quickblox.sample.videochatwebrtcnew.helper.DataHolder;
import com.quickblox.users.model.QBUser;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by tereha on 27.01.15.
 */
public class OpponentsActivity  extends LogginedUserABActivity implements View.OnClickListener {

    private OpponentsAdapter opponentsAdapter;
    private ListView opponentsList;
    private static String login;
    private Button btnAudioCall;
    private Button btnVideoCall;
    private ArrayList<String> opponentsListToCall;
    private ArrayList<User> opponents;
    public static ArrayList<User> usersList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_opponents);

        usersList = DataHolder.createUsersList();

        initUI();
        super.initActionBar();
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

    private ArrayList<User> createOpponentsFromUserList(ArrayList<User> usersList){
        opponents = new ArrayList<>();
        opponents.addAll(usersList);
        opponents.remove(searchIndexLogginedUser(opponents));

        return opponents;

    }

    public static int searchIndexLogginedUser(ArrayList<User> usersList) {

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

                /*opponentsListToCall = new ArrayList<>();
                opponentsListToCall.addAll(OpponentsAdapter.positions);

                if (opponentsListToCall.size() == 0)
                    Log.d("Track", "Opponent list is NULL ");


                for (String s : opponentsListToCall) {
                    Log.d("Track", "Nubers of opponents " + s);

                }

                //Intent intent = new Intent(OpponentsActivity.this, IncAudioCallActivity.class);
                //intent.putExtra("login", login);
                //startActivity(intent);*/

                for (int id : getUserIds(opponentsAdapter.getSelected())){
                    Log.d("Track", "id = " + String.valueOf(id));

                }

                break;

            case R.id.btnVideoCall:

                break;
        }
    }

    public static ArrayList<Integer> getUserIds(List<User> users){
        ArrayList<Integer> ids = new ArrayList<Integer>();
        for(User user : users){
            ids.add(user.getUserNumber());
        }
        return ids;
    }
}
