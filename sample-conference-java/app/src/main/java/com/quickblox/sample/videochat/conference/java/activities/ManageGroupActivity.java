package com.quickblox.sample.videochat.conference.java.activities;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.sample.videochat.conference.java.R;
import com.quickblox.sample.videochat.conference.java.adapters.SpeakerCheckboxUsersAdapter;
import com.quickblox.sample.videochat.conference.java.services.CallService;
import com.quickblox.sample.videochat.conference.java.utils.Consts;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ManageGroupActivity extends BaseActivity implements CallService.UsersConnectDisconnectCallback {
    private static final String TAG = ManageGroupActivity.class.getSimpleName();

    private static final String EXTRA_CALL_PARTICIPANTS = "extra_call_participants";

    private SpeakerCheckboxUsersAdapter usersAdapter;
    private CallService callService;
    private ServiceConnection callServiceConnection;
    private List<QBUser> participantsList = new ArrayList<>();

    public static void startForResult(Activity activity, int code, String roomTitle, ArrayList<Integer> callParticipants) {
        Intent intent = new Intent(activity, ManageGroupActivity.class);
        intent.putExtra(Consts.EXTRA_ROOM_TITLE, roomTitle);
        intent.putIntegerArrayListExtra(EXTRA_CALL_PARTICIPANTS, callParticipants);
        activity.startActivityForResult(intent, code);
    }

    @Override
    protected void onResume() {
        super.onResume();
        bindCallService();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_group);
    }

    @Override
    protected void onPause() {
        callService.removeUsersConnectDisconnectCallback();
        unbindService(callServiceConnection);
        super.onPause();
    }

    private void bindCallService() {
        callServiceConnection = new CallServiceConnection();
        Intent intent = new Intent(this, CallService.class);
        bindService(intent, callServiceConnection, Context.BIND_AUTO_CREATE);
    }

    private void initUi() {
        ProgressBar progressBar = findViewById(R.id.progress_users);
        ListView usersListView = findViewById(R.id.list_users);
        callService.setUsersConnectDisconnectCallback(this);

        String roomTitle = "";
        if (getIntent() != null) {
            roomTitle = getIntent().getStringExtra(Consts.EXTRA_ROOM_TITLE);

            ArrayList<Integer> participantsIDs = getIntent().getIntegerArrayListExtra(EXTRA_CALL_PARTICIPANTS);
            if (participantsIDs != null && participantsIDs.size() > 0) {
                participantsList = getQBUsersHolder().getUsersByIds(participantsIDs);
            }

            Map<Integer, Boolean> map = makeMapAccordingAudio(participantsIDs);
            usersAdapter = new SpeakerCheckboxUsersAdapter(this, participantsList, map);
            usersListView.setAdapter(usersAdapter);
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(roomTitle);
        }
        setSubtitle();

        usersListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                usersAdapter.onItemClicked(position, view);

                QBUser currentUser = getChatHelper().getCurrentUser();
                Integer userID = usersAdapter.getItem(position).getId();
                if (!userID.equals(currentUser.getId())) {
                    boolean isEnabled = callService.isAudioEnabledForUser(userID);
                    callService.setAudioEnabledForUser(userID, !isEnabled);
                }
            }
        });
        progressBar.setVisibility(View.GONE);
    }

    private Map<Integer, Boolean> makeMapAccordingAudio(ArrayList<Integer> userIDs) {
        Map<Integer, Boolean> userAudioEnabledMap = new HashMap<>();
        if (userIDs != null) {
            for (Integer userID : userIDs) {
                boolean isEnabled = callService.isAudioEnabledForUser(userID);
                userAudioEnabledMap.put(userID, isEnabled);
            }
        }

        return userAudioEnabledMap;
    }

    @Override
    public void onUserConnected(Integer userID) {
        QBUser user = getQBUsersHolder().getUserById(userID);
        if (user == null) {
            updateUser(userID);
        } else {
            boolean isEnabled = callService.isAudioEnabledForUser(userID);
            usersAdapter.addUser(user, isEnabled);
            setSubtitle();
        }
    }

    @Override
    public void onUserDisconnected(Integer userID) {
        QBUser user = getQBUsersHolder().getUserById(userID);
        usersAdapter.removeUser(user);
        setSubtitle();
    }

    private void updateUser(Integer userID) {
        Log.d(TAG, "Loading User by ID");
        QBUsers.getUser(userID).performAsync(new QBEntityCallback<QBUser>() {
            @Override
            public void onSuccess(QBUser qbUser, Bundle bundle) {
                boolean isEnabled = callService.isAudioEnabledForUser(userID);
                usersAdapter.addUser(qbUser, isEnabled);
                setSubtitle();
            }

            @Override
            public void onError(QBResponseException e) {
                showErrorSnackbar(R.string.manage_group_update_users_error, e, v -> updateUser(userID));
            }
        });
    }

    private void setSubtitle() {
        if (getSupportActionBar() != null) {
            String subtitle;
            if (participantsList.size() != 1) {
                subtitle = getString(R.string.manage_group_chat_subtitle, String.valueOf(participantsList.size()));
            } else {
                subtitle = getString(R.string.manage_group_chat_subtitle_single, "1");
            }
            getSupportActionBar().setSubtitle(subtitle);
        }
    }

    private class CallServiceConnection implements ServiceConnection {
        @Override
        public void onServiceDisconnected(ComponentName name) {

        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            CallService.CallServiceBinder binder = (CallService.CallServiceBinder) service;
            callService = binder.getService();
            if (callService.currentSessionExist()) {
                initUi();

            } else {
                //we have already currentSession == null, so it's no reason to do further initialization
                finish();
            }
        }
    }
}