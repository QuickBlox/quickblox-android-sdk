package com.quickblox.sample.conference.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.quickblox.conference.ConferenceSession;
import com.quickblox.sample.conference.R;
import com.quickblox.sample.conference.activities.CallActivity;
import com.quickblox.sample.conference.db.QbUsersDbManager;
import com.quickblox.sample.conference.utils.CollectionsUtils;
import com.quickblox.sample.conference.utils.Consts;
import com.quickblox.sample.conference.utils.WebRtcSessionManager;
import com.quickblox.sample.core.utils.SharedPrefsHelper;
import com.quickblox.users.model.QBUser;
import com.quickblox.videochat.webrtc.BaseSession;

import java.util.ArrayList;

public abstract class BaseConversationFragment extends BaseToolBarFragment implements CallActivity.CurrentCallStateCallback {

    private static final String TAG = BaseConversationFragment.class.getSimpleName();
    protected QbUsersDbManager dbManager;
    protected WebRtcSessionManager sessionManager;
    protected ConferenceSession currentSession;
    protected ArrayList<QBUser> opponents;
    protected ArrayList<Integer> opponentsIds;

    private ToggleButton micToggleVideoCall;
    private ImageButton handUpVideoCall;
    protected ConversationFragmentCallbackListener conversationFragmentCallbackListener;
    protected View outgoingOpponentsRelativeLayout;
    protected TextView allOpponentsTextView;
    protected TextView ringingTextView;
    protected QBUser currentUser;
    protected SharedPrefsHelper sharedPrefsHelper;

    public static BaseConversationFragment newInstance(BaseConversationFragment baseConversationFragment) {
        Bundle args = new Bundle();

        baseConversationFragment.setArguments(args);

        return baseConversationFragment;
    }


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
        super.onCreate(savedInstanceState);
        sharedPrefsHelper = SharedPrefsHelper.getInstance();
        conversationFragmentCallbackListener.addCurrentCallStateCallback(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        opponentsIds = this.getArguments().getIntegerArrayList(Consts.EXTRA_DIALOG_OCCUPANTS);
        sessionManager = WebRtcSessionManager.getInstance(getActivity());
        currentSession = sessionManager.getCurrentSession();
        if (currentSession == null) {
            Log.d(TAG, "currentSession = null onCreateView");
            return view;
        }
        initFields();
        initViews(view);
        initActionBar();
        initButtonsListener();
        prepareAndShowOutgoingScreen();

        return view;
    }

    private void prepareAndShowOutgoingScreen() {
        configureOutgoingScreen();
        allOpponentsTextView.setText(CollectionsUtils.makeStringFromUsersFullNames(opponents));
    }

    protected abstract void configureOutgoingScreen();

    private void initActionBar() {
        configureToolbar();
        configureActionBar();
    }

    protected abstract void configureActionBar();

    protected abstract void configureToolbar();

    protected void initFields() {
        currentUser = sharedPrefsHelper.getQbUser();
        dbManager = QbUsersDbManager.getInstance(getActivity().getApplicationContext());
        sessionManager = WebRtcSessionManager.getInstance(getActivity());
        currentSession = sessionManager.getCurrentSession();

        initOpponentsList();

        Log.d(TAG, "opponents: " + opponents.toString());
        Log.d(TAG, "currentSession " + currentSession.toString());
    }

    @Override
    public void onStart() {
        super.onStart();
        if (currentSession == null) {
            Log.d(TAG, "currentSession = null onStart");
            return;
        }

        if (currentSession.getState() != BaseSession.QBRTCSessionState.QB_RTC_SESSION_ACTIVE) {
            startJoinConference();
        }
    }

    @Override
    public void onDestroy() {
        conversationFragmentCallbackListener.removeCurrentCallStateCallback(this);
        super.onDestroy();
    }

    private void startJoinConference(){
        conversationFragmentCallbackListener.onStartJoinConference();
    }

    protected void initViews(View view) {
        micToggleVideoCall = (ToggleButton) view.findViewById(R.id.toggle_mic);
        handUpVideoCall = (ImageButton) view.findViewById(R.id.button_hangup_call);
        outgoingOpponentsRelativeLayout = view.findViewById(R.id.layout_background_outgoing_screen);
        allOpponentsTextView = (TextView) view.findViewById(R.id.text_outgoing_opponents_names);
        ringingTextView = (TextView) view.findViewById(R.id.text_ringing);
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

                    conversationFragmentCallbackListener.onLeaveCurrentSession();
                    Log.d(TAG, "Call is stopped");
            }
        });
    }

    protected void actionButtonsEnabled(boolean inability) {

        micToggleVideoCall.setEnabled(inability);

        // inactivate toggle buttons
        micToggleVideoCall.setActivated(inability);
    }

    private void hideOutgoingScreen() {
        outgoingOpponentsRelativeLayout.setVisibility(View.GONE);
    }

    @Override
    public void onCallStarted() {
        hideOutgoingScreen();
        actionButtonsEnabled(true);
    }

    private void initOpponentsList() {
        Log.v(TAG, "initOpponentsList() opponentsIds= " + opponentsIds);
        opponents = dbManager.getUsersByIds(opponentsIds);
    }
}