package com.quickblox.sample.groupchatwebrtc.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ToggleButton;

import com.quickblox.chat.QBChatService;
import com.quickblox.sample.groupchatwebrtc.R;
import com.quickblox.sample.groupchatwebrtc.db.QbUsersDbManager;
import com.quickblox.sample.groupchatwebrtc.utils.Consts;
import com.quickblox.sample.groupchatwebrtc.utils.WebRtcSessionManager;
import com.quickblox.users.model.QBUser;
import com.quickblox.videochat.webrtc.QBRTCSession;
import com.quickblox.videochat.webrtc.QBRTCTypes;

import java.util.ArrayList;

/**
 * Created by tereha on 24.05.16.
 */
public abstract class BaseConversationFragment extends Fragment {

    private static final String TAG = BaseConversationFragment.class.getSimpleName();
    protected QbUsersDbManager dbManager;
    protected WebRtcSessionManager sessionManager;
    private boolean isIncomingCall;
    protected QBRTCSession currentSession;
    protected ArrayList<QBUser> opponents;
    private QBRTCTypes.QBConferenceType qbConferenceType;

    private ToggleButton micToggleVideoCall;
    private ImageButton handUpVideoCall;
    protected ConversationFragmentCallbackListener conversationFragmentCallbackListener;
    private Chronometer timerChronometer;
    private boolean isMessageProcessed;


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            conversationFragmentCallbackListener = (ConversationFragmentCallbackListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement ConversationFragmentCallbackListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(getFragmentLayout(), container, false);

        initFields();
        initViews(view);
        initButtonsListener();

        return view;
    }

    abstract int getFragmentLayout();

    protected void initFields() {
        dbManager = QbUsersDbManager.getInstance(getActivity().getApplicationContext());
        sessionManager = WebRtcSessionManager.getInstance(getActivity());

        if (getArguments() != null) {
            isIncomingCall = getArguments().getBoolean(Consts.EXTRA_IS_INCOMING_CALL);
        }

        if (isIncomingCall){
            opponents.add(dbManager.getUserById(currentSession.getCallerID()));
            opponents.remove(QBChatService.getInstance().getUser());
        }

        currentSession = sessionManager.getCurrentSession();
        opponents = dbManager.getUsersByIds(currentSession.getOpponents());

        if (isIncomingCall){
            opponents.add(dbManager.getUserById(currentSession.getCallerID()));
            opponents.remove(QBChatService.getInstance().getUser());
        }

        qbConferenceType = currentSession.getConferenceType();

        Log.d(TAG, "opponents: " + opponents.toString());
        Log.d(TAG, "currentSession " + currentSession.toString());
    }

    @Override
    public void onStart() {
        super.onStart();

        if (!isMessageProcessed) {
            if (isIncomingCall) {
                currentSession.acceptCall(currentSession.getUserInfo());
            } else {
                currentSession.startCall(currentSession.getUserInfo());
            }
            isMessageProcessed = true;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    protected void initViews(View view) {
        micToggleVideoCall = (ToggleButton) view.findViewById(R.id.micToggleVideoCall);
        handUpVideoCall = (ImageButton) view.findViewById(R.id.handUpVideoCall);
        timerChronometer = (Chronometer) getActivity().findViewById(R.id.timer_chronometer);

        actionButtonsEnabled(false);
    }

    protected void initButtonsListener() {

        micToggleVideoCall.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                conversationFragmentCallbackListener.onSetAudioEnabled(isChecked);
            }
        });

        handUpVideoCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionButtonsEnabled(false);
                handUpVideoCall.setEnabled(false);
                handUpVideoCall.setActivated(false);

                conversationFragmentCallbackListener.onHangUpCurrentSession();
                Log.d(TAG, "Call is stopped");
            }
        });
    }

    public void actionButtonsEnabled(boolean enability) {

        micToggleVideoCall.setEnabled(enability);

        // inactivate toggle buttons
        micToggleVideoCall.setActivated(enability);
    }

    public void startTimer() {
        if (timerChronometer != null) {
            timerChronometer.setBase(SystemClock.elapsedRealtime());
            timerChronometer.start();
        }
    }

    public void stopTimer(){
        if (timerChronometer != null){
            timerChronometer.stop();
        }
    }
}
