package com.quickblox.sample.videochatwebrtcnew.fragments;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.quickblox.chat.QBChatService;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.request.QBPagedRequestBuilder;
import com.quickblox.sample.videochatwebrtcnew.R;
import com.quickblox.sample.videochatwebrtcnew.activities.CallActivity;
import com.quickblox.sample.videochatwebrtcnew.adapters.OpponentsAdapter;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;
import com.quickblox.videochat.webrtc.QBRTCClient;
import com.quickblox.videochat.webrtc.QBRTCTypes;

import org.jivesoftware.smack.SmackException;

import java.io.Serializable;
import java.util.ArrayList;
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
    private View view=null;
    private ProgressDialog progresDialog;
    private ListView opponentsList;


    public static OpponentsFragment getInstance() {
        return new OpponentsFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        ((CallActivity)getActivity()).initActionBar();

        view = inflater.inflate(R.layout.fragment_opponents, container, false);

        initUI(view);

        // Show dialog till opponents loading
        progresDialog = new ProgressDialog(getActivity()) {
            @Override
            public void onBackPressed() {
                Toast.makeText(getActivity(), "Wait until loading finish", Toast.LENGTH_SHORT).show();
            }
        };
        progresDialog.setMessage("Load opponents ...");
        progresDialog.setCanceledOnTouchOutside(false);
        progresDialog.show();

        initOpponentListAdapter();

         Log.d("Track", "onCreateView() from OpponentsFragment Level 2");
        return view;
    }

    private void initOpponentListAdapter() {
        final ListView opponentsList = (ListView) view.findViewById(R.id.opponentsList);
        List<QBUser> users = ((CallActivity) getActivity()).getOpponentsList();

        QBPagedRequestBuilder requestBuilder = new QBPagedRequestBuilder();
        requestBuilder.setPerPage(100);

        if (users == null) {
            List<String> tags = new LinkedList<>();
            tags.add("webrtcusers");
            QBUsers.getUsersByTags(tags, requestBuilder, new QBEntityCallback<ArrayList<QBUser>>() {
                @Override
                public void onSuccess(ArrayList<QBUser> qbUsers, Bundle bundle) {
                    Log.d("Track", "download users from QickBlox");
                    ArrayList<QBUser> orderedUsers = reorderUsersByName(qbUsers);
                    if(isAdded()) {
                        ((CallActivity) getActivity()).setOpponentsList(orderedUsers);
                        prepareUserList(opponentsList, orderedUsers);
                        progresDialog.dismiss();
                    } else {
                        Log.e("getActivity() error", "get Activity is null, because adapter wasn't added");
                    }
                }

                @Override
                public void onSuccess() {
                }

                @Override
                public void onError(List<String> strings) {
                    Log.d("Track", "onError()");
                }
            });
        } else {

            ArrayList<QBUser> userList = ((CallActivity) getActivity()).getOpponentsList();
            prepareUserList(opponentsList, userList);
            progresDialog.dismiss();

        }
    }

    private void prepareUserList(ListView opponentsList, List<QBUser> users) {
        int i = searchIndexLogginedUser(users);
        if (i >= 0)
            users.remove(i);

        // Prepare users list for simple adapter.
        //
        opponentsAdapter = new OpponentsAdapter(getActivity(), users);
        opponentsList.setAdapter(opponentsAdapter);
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

        opponentsList = (ListView) view.findViewById(R.id.opponentsList);
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
        }
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
                    QBRTCClient.getInstance().close();
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

    private ArrayList<QBUser> reorderUsersByName(ArrayList<QBUser> qbUsers) {
        // Make clone collection to avoid modify input param qbUsers
        ArrayList<QBUser> resultList = new ArrayList<>(qbUsers.size());
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
