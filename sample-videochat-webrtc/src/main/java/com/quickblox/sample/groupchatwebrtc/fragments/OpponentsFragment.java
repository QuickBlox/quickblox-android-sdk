package com.quickblox.sample.groupchatwebrtc.fragments;

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
import com.quickblox.sample.groupchatwebrtc.activities.CallActivity;
import com.quickblox.sample.groupchatwebrtc.adapters.OpponentsAdapter;
import com.quickblox.sample.groupchatwebrtc.R;
import com.quickblox.users.model.QBUser;
import com.quickblox.videochat.webrtc.QBRTCClient;
import com.quickblox.videochat.webrtc.QBRTCConfig;
import com.quickblox.videochat.webrtc.QBRTCTypes;

import org.jivesoftware.smack.SmackException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * QuickBlox team
 */
public class OpponentsFragment extends Fragment implements View.OnClickListener, Serializable {

    private static final String TAG = OpponentsFragment.class.getSimpleName();
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

        return view;
    }

    private void initOpponentListAdapter() {
        final ListView opponentsList = (ListView) view.findViewById(R.id.opponentsList);

        List<QBUser> userList = new ArrayList<>(((CallActivity) getActivity()).getOpponentsList());
        prepareUserList(opponentsList, userList);
        progresDialog.dismiss();


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
        setHasOptionsMenu(true);
        Log.d(TAG, "onCreate() from OpponentsFragment");
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

        if (opponentsAdapter.getSelected().isEmpty()){
            Toast.makeText(getActivity(), "Choose one opponent", Toast.LENGTH_LONG).show();
            return;
        }

        if (opponentsAdapter.getSelected().size() > QBRTCConfig.getMaxOpponentsCount()){
            Toast.makeText(getActivity(), "Max number of opponents is 6", Toast.LENGTH_LONG).show();
            return;
        }
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
                    .addConversationFragmentStartCall(opponentsAdapter.getSelected(),
                            qbConferenceType, userInfo);

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
                    QBRTCClient.getInstance(getActivity()).destroy();
                    QBChatService.getInstance().logout();
                } catch (SmackException.NotConnectedException e) {
                    e.printStackTrace();
                }
                getActivity().finish();
                return true;
            case R.id.settings:
                ((CallActivity)getActivity()).showSettings();
            default:
                return super.onOptionsItemSelected(item);
        }
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
