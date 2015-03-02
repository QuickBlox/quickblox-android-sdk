package com.quickblox.sample.videochatwebrtcnew.fragments;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.request.QBPagedRequestBuilder;
import com.quickblox.sample.videochatwebrtcnew.R;
import com.quickblox.sample.videochatwebrtcnew.activities.CallActivity;
import com.quickblox.sample.videochatwebrtcnew.adapters.OpponentsAdapter;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;
import com.quickblox.videochat.webrtcnew.model.QBRTCTypes;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by tereha on 16.02.15.
 */
public class OpponentsFragment extends Fragment implements QBEntityCallback<ArrayList<QBUser>>, View.OnClickListener, Serializable {




    private OpponentsAdapter opponentsAdapter;
    private PullToRefreshListView opponentsList;
    public static String login;
    private Button btnAudioCall;
    private Button btnVideoCall;
    public List<QBUser> users = new ArrayList<QBUser>();
    private static final int PAGE_SIZE = 100;
    private int currentPage = 0;
    private int listViewIndex;
    private int listViewTop;
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

            opponentsList.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ListView>() {
                @Override
                public void onRefresh(PullToRefreshBase<ListView> refreshView) {
                    // Do work to refresh the list here.
                    loadOpponentsPage();
                    listViewIndex = opponentsList.getRefreshableView().getFirstVisiblePosition();
                    View v = opponentsList.getRefreshableView().getChildAt(0);
                    listViewTop = (v == null) ? 0 : v.getTop();
                }
            });

            // Show dialog till opponents loading
            progresDialog = new ProgressDialog(getActivity());
            progresDialog.setMessage("Load opponents ...");
            progresDialog.setCanceledOnTouchOutside(false);
            progresDialog.show();

            loadOpponentsPage();

        }

         Log.d("Track", "onCreateView() from OpponentsFragment Level 2");
        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
//        setRetainInstance(true);
        Log.d("Track", "onCreate() from OpponentsFragment");
        super.onCreate(savedInstanceState);
    }

    private void initUI(View view) {

        opponentsList = (PullToRefreshListView) view.findViewById(R.id.opponentsList);
        login = getActivity().getIntent().getStringExtra("login");

        btnAudioCall = (Button)view.findViewById(R.id.btnAudioCall);
        btnVideoCall = (Button)view.findViewById(R.id.btnVideoCall);

        btnAudioCall.setOnClickListener(this);
        btnVideoCall.setOnClickListener(this);
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

    @Override
    public void onClick(View v) {
        if (opponentsAdapter.getSelected().size() > 0) {
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
        } else {
            Toast.makeText(getActivity(), "Choose at least one opponent", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onSuccess(ArrayList<QBUser> qbUsers, Bundle bundle) {
        users.addAll(qbUsers);
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
        tags.add("webrtc");
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
        if(progresDialog.isShowing()) {
            progresDialog.dismiss();
        }}
}
