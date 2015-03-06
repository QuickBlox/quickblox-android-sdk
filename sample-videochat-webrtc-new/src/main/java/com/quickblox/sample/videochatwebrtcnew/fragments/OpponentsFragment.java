package com.quickblox.sample.videochatwebrtcnew.fragments;

import android.app.Dialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.quickblox.chat.QBChatService;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.QBRequestCanceler;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.request.QBPagedRequestBuilder;
import com.quickblox.sample.videochatwebrtcnew.R;
import com.quickblox.sample.videochatwebrtcnew.activities.CallActivity;
import com.quickblox.sample.videochatwebrtcnew.activities.ListUsersActivity;
import com.quickblox.sample.videochatwebrtcnew.adapters.OpponentsAdapter;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;
import com.quickblox.videochat.webrtcnew.model.QBRTCTypes;

import org.jivesoftware.smack.SmackException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by tereha on 16.02.15.
 */
public class OpponentsFragment extends Fragment implements View.OnClickListener, Serializable {




    private OpponentsAdapter opponentsAdapter;
    public static String login;
    private Button btnAudioCall;
    private Button btnVideoCall;
    private static List<QBUser> users = new ArrayList<QBUser>();
    private View view=null;
    private ProgressDialog progresDialog;


    public static OpponentsFragment getInstance() {
        return new OpponentsFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        ((CallActivity)getActivity()).initActionBar();

        if (savedInstanceState == null){
            Log.d("Track", "onCreateView() from OpponentsFragment Level 1");
            view = inflater.inflate(R.layout.fragment_opponents, container, false);

            initUI(view);

//            opponentsList.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ListView>() {
//                @Override
//                public void onRefresh(PullToRefreshBase<ListView> refreshView) {
//                    // Do work to refresh the list here.
//                    loadOpponentsPage();
//                    listViewIndex = opponentsList.getRefreshableView().getFirstVisiblePosition();
//                    View v = opponentsList.getRefreshableView().getChildAt(0);
//                    listViewTop = (v == null) ? 0 : v.getTop();
//                }
//            });
//
//            // Show dialog till opponents loading
//            progresDialog = new ProgressDialog(getActivity()) {
//                @Override
//                public void onBackPressed() {
//                    Toast.makeText(getActivity(), "Wait until loading finish", Toast.LENGTH_SHORT).show();
//                }
//            };
//            progresDialog.setMessage("Load opponents ...");
//            progresDialog.setCanceledOnTouchOutside(false);
//            progresDialog.show();
//
//            loadOpponentsPage();
            initOpponentListAdapter();
        }



         Log.d("Track", "onCreateView() from OpponentsFragment Level 2");

        return view;
    }

    private void initOpponentListAdapter() {
        final ListView opponentsList = (ListView) view.findViewById(R.id.opponentsList);

        if (users.size() == 0) {
            List<String> tags = new LinkedList<>();
            tags.add("webrtcusers");
            QBUsers.getUsersByTags(tags, new QBPagedRequestBuilder(), new QBEntityCallback<ArrayList<QBUser>>() {
                @Override
                public void onSuccess(ArrayList<QBUser> qbUsers, Bundle bundle) {
                    Log.d("Track", "download users from QickBlox");

                    users.addAll(reorderUsersByName(qbUsers));

                    int i = searchIndexLogginedUser((ArrayList<QBUser>) users);
                    if (i >= 0)
                        users.remove(i);

                    // Prepare users list for simple adapter.
                    //
                    opponentsAdapter = new OpponentsAdapter(getActivity(), users);
                    opponentsList.setAdapter(opponentsAdapter);
//                    opponentsList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

                }

                @Override
                public void onSuccess() {

                }

                @Override
                public void onError(List<String> strings) {
                    Log.d("Track", "onError()");

                }
            });
        }else {
            opponentsAdapter = new OpponentsAdapter(getActivity(), users);
            opponentsList.setAdapter(opponentsAdapter);
//            opponentsList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

            Log.d("Track", "create list from downloaded users");
        }


//        opponentsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                long iDselectedOpponent = id;
//
//
//
//            }
//        });

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
//        setRetainInstance(true);
        setHasOptionsMenu(true);
        Log.d("Track", "onCreate() from OpponentsFragment");
        super.onCreate(savedInstanceState);
    }

    private void initUI(View view) {

        login = getActivity().getIntent().getStringExtra("login");

        btnAudioCall = (Button)view.findViewById(R.id.btnAudioCall);
        btnVideoCall = (Button)view.findViewById(R.id.btnVideoCall);

        btnAudioCall.setOnClickListener(this);
        btnVideoCall.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (opponentsAdapter.getSelected().size() == 1) {
            QBRTCTypes.QBConferenceType qbConferenceType = null;


            //Init conference type
            switch (v.getId()) {
                case R.id.btnAudioCall:
                    qbConferenceType = QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_AUDIO;
                    break;

                case R.id.btnVideoCall:
                    // get call type
                    qbConferenceType = QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_VIDEO;
                    break;
            }

            Map<String, String> userInfo = new HashMap<>();
            userInfo.put("any_custom_data", "some data");
            userInfo.put("my_avatar_url", "avatar_reference");

            ((CallActivity) getActivity())
                    .addConversationFragmentStartCall(getOpponentsIds(opponentsAdapter.getSelected()),
                            qbConferenceType, userInfo);
        } else if (opponentsAdapter.getSelected().size() > 1){
            Toast.makeText(getActivity(), "Only 1-to-1 calls are available", Toast.LENGTH_LONG).show();
        } else if (opponentsAdapter.getSelected().size() < 1){
            Toast.makeText(getActivity(), "Choose one opponent", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onSuccess(ArrayList<QBUser> qbUsers, Bundle bundle) {
        users.addAll(reorderUsersByName(qbUsers));

        int i = searchIndexLogginedUser((ArrayList<QBUser>) users);
        if (i >= 0)
            users.remove(i);

        // Prepare users list for simple adapter.
        //
        opponentsAdapter = new OpponentsAdapter(getActivity(), users);
        opponentsList.setAdapter(opponentsAdapter);
        opponentsList.onRefreshComplete();
        opponentsList.getRefreshableView().setSelectionFromTop(listViewIndex, listViewTop);
        progresDialog.dismiss();
    }

    private Collection<? extends QBUser> reorderUsersByName(ArrayList<QBUser> qbUsers) {
        // Make clone collection to avoid modify input param qbUsers
        List<QBUser> resultList = new ArrayList<>(qbUsers.size());
        resultList.addAll(qbUsers);

        // Rearrange list by user IDs
        Collections.sort(resultList, new Comparator<QBUser>() {
            @Override
            public int compare(QBUser firstUsr, QBUser secondUsr) {
                if (firstUsr.getId().equals(secondUsr.getId())) {
                    return 0;
                } else if (firstUsr.getId() < secondUsr.getId()) {
                    return -1;
                } else {
                    return 1;
                }
            }
        });
        return resultList;
    }

    @Override
    public void onSuccess() {

    }


    @Override
    public void onError(List<String> errors){
//        AlertDialog.Builder dialog = new AlertDialog.Builder(OpponentsFragment.getInstance().getActivity());
//        dialog.setMessage("get users errors: " + errors).create().show();
    }

    public static QBPagedRequestBuilder getQBPagedRequestBuilder(int page) {
        QBPagedRequestBuilder pagedRequestBuilder = new QBPagedRequestBuilder();
        pagedRequestBuilder.setPage(page);
        pagedRequestBuilder.setPerPage(PAGE_SIZE);

        return pagedRequestBuilder;
    }

    private void loadOpponentsPage() {
        ++currentPage;
        List<String> tags = new LinkedList<>();
        tags.add("webrtcusers");
        QBUsers.getUsersByTags(tags, getQBPagedRequestBuilder(currentPage), OpponentsFragment.this);
//        QBUsers.getUsers(getQBPagedRequestBuilder(currentPage), OpponentsFragment.this);


    }

    public static ArrayList<Integer> getOpponentsIds(List<QBUser> opponents){
        ArrayList<Integer> ids = new ArrayList<Integer>();
        for(QBUser user : opponents){
            ids.add(user.getId());
        }
        return ids;
    }

    @Override
    public void onPause() {
        super.onPause();
//        if(progresDialog.isShowing()) {
//            progresDialog.dismiss();
//        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu, menu);
        super.onCreateOptionsMenu(menu, inflater);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.log_out:
                try {
                    QBChatService.getInstance().logout();
                } catch (SmackException.NotConnectedException e) {
                    e.printStackTrace();
                }
                getActivity().finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public boolean inProgress(){
        Log.d("Dialog progress is show", progresDialog.isShowing() + "");
        return progresDialog.isShowing();
    }

    private Collection<? extends QBUser> reorderUsersByName(ArrayList<QBUser> qbUsers) {
        // Make clone collection to avoid modify input param qbUsers
        List<QBUser> resultList = new ArrayList<>(qbUsers.size());
        resultList.addAll(qbUsers);

        // Rearrange list by user IDs
        Collections.sort(resultList, new Comparator<QBUser>() {
            @Override
            public int compare(QBUser firstUsr, QBUser secondUsr) {
                if (firstUsr.getId().equals(secondUsr.getId())) {
                    return 0;
                } else if (firstUsr.getId() < secondUsr.getId()) {
                    return -1;
                } else {
                    return 1;
                }
            }
        });
        return resultList;
    }

    public static int searchIndexLogginedUser(List<QBUser> usersList) {

        int indexLogginedUser = -1;

        for (QBUser usr : usersList) {
            if (usr.getLogin().equals(login)) {
                indexLogginedUser = usersList.indexOf(usr);
                break;
            }
        }
        return indexLogginedUser;
    }
}
