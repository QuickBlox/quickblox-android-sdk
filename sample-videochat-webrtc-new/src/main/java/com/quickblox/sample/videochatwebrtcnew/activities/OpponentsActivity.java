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
    private TextView selectUsertextView;
    private ArrayList<Integer> opponentsListNew;


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
        login = getIntent().getStringExtra("login");

        btnAudioCall = (Button)findViewById(R.id.btnAudioCall);
        btnVideoCall = (Button)findViewById(R.id.btnVideoCall);
        selectUsertextView = (TextView)findViewById(R.id.selectUsertextView);

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
        numberOfListAB.setText(String.valueOf(searchIndexLogginedUser(createOpponentsCollection())+1));
        TextView loginAsAB = (TextView) mCustomView.findViewById(R.id.loginAsAB);
        loginAsAB.setText(R.string.logged_in_as);
        TextView userNameAB = (TextView) mCustomView.findViewById(R.id.userNameAB);
        userNameAB.setText(createOpponentsCollection()
                .get((searchIndexLogginedUser(createOpponentsCollection()))).getOpponentName());

        mActionBar.setCustomView(mCustomView);
        mActionBar.setDisplayShowCustomEnabled(true);

    }

    private ArrayList<Opponent> createOpponentsCollection(){

        ArrayList<Opponent> opponents = new ArrayList<>();
        opponents.add(new Opponent(1, "User 1", "user_1", "11111111"));
        opponents.add(new Opponent(2, "User 2", "user_2", "11111111"));
        opponents.add(new Opponent(3, "User 3", "user_3", "11111111"));
        opponents.add(new Opponent(4, "User 4", "user_4", "11111111"));
        opponents.add(new Opponent(5, "User 5", "user_5", "11111111"));
        opponents.add(new Opponent(6, "User 6", "user_6", "11111111"));
        opponents.add(new Opponent(7, "User 7", "user_7", "11111111"));
        opponents.add(new Opponent(8, "User 8", "user_8", "11111111"));
        opponents.add(new Opponent(9, "User 9", "user_9", "11111111"));
        opponents.add(new Opponent(10, "User 10", "user_10", "11111111"));

        return opponents;
    }

    private int searchIndexLogginedUser(ArrayList<Opponent> opponentsCollection) {
        int indexLogginedUser = -1;

        for (Opponent usr : opponentsCollection) {
            if (usr.getOpponentLogin().equals(login)) {
                indexLogginedUser = opponentsCollection.indexOf(usr);
                break;
            }
        }

        return indexLogginedUser;
    }



    private void initUsersList() {

        final ArrayList<Opponent> opponents = createOpponentsCollection();

        int indexLogginedUser = searchIndexLogginedUser(opponents);

        if (indexLogginedUser != -1) {
            opponentsAdapter = new OpponentsAdapter(this, opponents);
            opponentsList.setTextFilterEnabled(true);
            //opponentsList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
            opponentsList.setAdapter(opponentsAdapter);
            opponentsAdapter.notifyDataSetChanged();
            opponents.remove(indexLogginedUser);
            opponentsAdapter.notifyDataSetChanged();
        } else {
            opponentsAdapter = new OpponentsAdapter(this, opponents);
            //opponentsList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
            opponentsList.setAdapter(opponentsAdapter);


        }

        /*opponentsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                opponentsListNew = new ArrayList<Integer>();
                CheckBox cb = (CheckBox) view.findViewById(R.id.opponentsCheckBox);
                cb.performClick();
                if (cb.isChecked()){
                    opponentsListNew.add(position);
                    Log.d("Track", "Check " + position);


                } else if (!cb.isChecked()){
                    opponentsListNew.remove(position);
                    Log.d("Track", "Csdljfgslfgjhl ghheck " + position);

                }



            }
        });*/



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
                //opponentsListNew = addAll(OpponentsAdapter.positions);


                //Intent intent = new Intent(OpponentsActivity.this, OpponentsActivity.class);
               // intent.putExtra("login", login);
                //startActivity(intent);
                // actions
                break;
            case R.id.btnVideoCall:
                //
                break;
        }
    }
}
