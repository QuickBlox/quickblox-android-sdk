package com.quickblox.sample.videochatwebrtcnew.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.request.QBPagedRequestBuilder;
import com.quickblox.sample.videochatwebrtcnew.R;
import com.quickblox.sample.videochatwebrtcnew.User;
import com.quickblox.sample.videochatwebrtcnew.adapters.OpponentsAdapter;
import com.quickblox.sample.videochatwebrtcnew.helper.DataHolder;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by tereha on 27.01.15.
 */
public class OpponentsActivity  extends LogginedUserABActivity implements View.OnClickListener, QBEntityCallback<ArrayList<QBUser>> {

    private OpponentsAdapter opponentsAdapter;
    private PullToRefreshListView opponentsList;
    public static String login;
    private Button btnAudioCall;
    private Button btnVideoCall;
    private ArrayList<String> opponentsListToCall;
    private ArrayList<QBUser> opponents;
    public static ArrayList<QBUser> usersList1;
    private List<QBUser> users = new ArrayList<QBUser>();
    private static final int PAGE_SIZE = 10;
    private int currentPage = 0;
    private int listViewIndex;
    private int listViewTop;
    private PullToRefreshListView usersList;

    public static ArrayList<QBUser> testUsers;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_opponents);
        //testUsers.addAll((java.util.Collection<? extends QBUser>) QBUsers.getUsers(getQBPagedRequestBuilder(1), OpponentsActivity.this));

//        usersList1 = DataHolder.createUsersList();
        usersList = (PullToRefreshListView) findViewById(R.id.opponentsList);

        usersList.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ListView>() {
            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {
                // Do work to refresh the list here.
                loadNextPage();
                listViewIndex = usersList.getRefreshableView().getFirstVisiblePosition();
                View v = usersList.getRefreshableView().getChildAt(0);
                listViewTop = (v == null) ? 0 : v.getTop();
            }
        });

        loadNextPage();



        initUI();
        super.initActionBar();
        //initUsersList();
    }

    private void initUI() {

        opponentsList = (PullToRefreshListView) findViewById(R.id.opponentsList);
        login = getIntent().getStringExtra("login");

        btnAudioCall = (Button)findViewById(R.id.btnAudioCall);
        btnVideoCall = (Button)findViewById(R.id.btnVideoCall);

        btnAudioCall.setOnClickListener(this);
        btnVideoCall.setOnClickListener(this);

    }

    private ArrayList<QBUser> createOpponentsFromUserList(ArrayList<QBUser> usersList){
        opponents = new ArrayList<>();
        opponents.addAll(usersList);
        opponents.remove(searchIndexLogginedUser(opponents));

        return opponents;

    }

    public static int searchIndexLogginedUser(ArrayList<QBUser> usersList) {

        int indexLogginedUser = -1;

        for (QBUser usr : usersList) {
            if (usr.getLogin().equals(login)) {
                indexLogginedUser = usersList.indexOf(usr);
                break;
            }
        }

        return indexLogginedUser;
    }

    /*private void initUsersList() {

        opponentsAdapter = new OpponentsAdapter(this, createOpponentsFromUserList(usersList1));
        opponentsList.setAdapter(opponentsAdapter);



    }
*/
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

                }*/

                Intent intent = new Intent(OpponentsActivity.this, IncAudioCallActivity.class);
//                intent.putExtra("login", login);
                startActivity(intent);

//                for (int id : getUserIds(opponentsAdapter.getSelected())){
//                    Log.d("Track", "id = " + String.valueOf(id));

//                }

                break;

            case R.id.btnVideoCall:

                break;
        }
    }

    /*public static ArrayList<Integer> getUserIds(List<QBUser> users){
        ArrayList<Integer> ids = new ArrayList<Integer>();
        for(QBUser user : users){
            ids.add(user.getUserNumber());
        }
        return ids;
    }*/

    @Override
    public void onSuccess(ArrayList<QBUser> qbUsers, Bundle bundle) {
        users.addAll(qbUsers);
        int i = searchIndexLogginedUser((ArrayList<QBUser>) users);
        if (i>=0)
        users.remove(i);

        // Prepare users list for simple adapter.
        //
        opponentsAdapter = new OpponentsAdapter(OpponentsActivity.this, (ArrayList<QBUser>) users, login);
        opponentsList.setAdapter(opponentsAdapter);
        opponentsList.onRefreshComplete();
        opponentsList.getRefreshableView().setSelectionFromTop(listViewIndex, listViewTop);
        //progressBar.setVisibility(View.GONE);
    }

    @Override
    public void onSuccess() {

    }

    @Override
    public void onError(List<String> strings) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(OpponentsActivity.this);
        dialog.setMessage("get users errors: " + strings).create().show();

    }

    public static QBPagedRequestBuilder getQBPagedRequestBuilder(int page) {
        QBPagedRequestBuilder pagedRequestBuilder = new QBPagedRequestBuilder();
        pagedRequestBuilder.setPage(page);
        pagedRequestBuilder.setPerPage(PAGE_SIZE);

        return pagedRequestBuilder;
    }
    private void loadNextPage() {
        ++currentPage;

        QBUsers.getUsers(getQBPagedRequestBuilder(currentPage), OpponentsActivity.this);

    }
}
