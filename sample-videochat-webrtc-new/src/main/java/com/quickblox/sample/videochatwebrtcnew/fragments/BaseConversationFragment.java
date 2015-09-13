package com.quickblox.sample.videochatwebrtcnew.fragments;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.quickblox.sample.videochatwebrtcnew.R;
import com.quickblox.sample.videochatwebrtcnew.SessionManager;
import com.quickblox.sample.videochatwebrtcnew.activities.BaseLogginedUserActivity;
import com.quickblox.sample.videochatwebrtcnew.activities.CallActivity;
import com.quickblox.sample.videochatwebrtcnew.definitions.Consts;
import com.quickblox.sample.videochatwebrtcnew.holder.DataHolder;
import com.quickblox.users.model.QBUser;
import com.quickblox.videochat.webrtc.QBRTCSession;
import com.quickblox.videochat.webrtc.QBRTCTypes;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tereha on 15.07.15.
 */
public abstract class BaseConversationFragment extends Fragment implements View.OnClickListener{

    private static final String TAG = BaseConversationFragment.class.getSimpleName();

    protected List<Integer> opponents;
    protected QBRTCTypes.QBConferenceType qbConferenceType;
    protected int startReason;
    protected String sessionID;
    protected String callerName;

    private TextView opponentNumber;
    private TextView connectionStatus;
    private ImageView opponentAvatar;
    private ToggleButton dynamicToggleVideoCall;
    private ToggleButton micToggleVideoCall;
    private ImageButton handUpVideoCall;
    private TextView opponentNameView;
    private View opponentItemView;
    private HorizontalScrollView camerasOpponentsList;
    //    public static LinearLayout opponentsFromCall;
    private LayoutInflater inflater;
    private ViewGroup container;
    private Bundle savedInstanceState;
    private boolean isVideoEnabled = true;
    private boolean isAudioEnabled = true;
    private List<QBUser> allUsers = new ArrayList<>();
    private LinearLayout actionVideoButtonsLayout;
    private View actionBar;

    private LinearLayout noVideoImageContainer;
    private boolean isMessageProcessed;
    private MediaPlayer ringtone;
    private IntentFilter intentFilter;
    private AudioStreamReceiver audioStreamReceiver;
    private Integer callerID;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        View view = inflater.inflate(getContentView(), container, false);
        this.inflater = inflater;
        this.container = container;
        Log.d(TAG, "Fragment. Thread id: " + Thread.currentThread().getId());

        ((CallActivity) getActivity()).initActionBarWithTimer();

        if (getArguments() != null) {
            startReason = getArguments().getInt(Consts.CALL_DIRECTION_TYPE_EXTRAS);
            Log.d(TAG, "startReason: " + startReason);

        }
        initCallData();
        initViews(view);
        return view;
    }

    protected void initCallData() {
        QBRTCSession session = SessionManager.getCurrentSession();
        if (session != null){
            opponents = session.getOpponents();
            callerID = session.getCallerID();
            callerName = DataHolder.getUserNameByID(session.getCallerID());
            qbConferenceType = session.getConferenceType();
        }
    }

    protected abstract int getContentView();

    public void actionButtonsEnabled(boolean enability) {

        micToggleVideoCall.setEnabled(enability);
        dynamicToggleVideoCall.setEnabled(enability);

        // inactivate toggle buttons
        micToggleVideoCall.setActivated(enability);
        dynamicToggleVideoCall.setActivated(enability);
    }


    @Override
    public void onStart() {
        getActivity().registerReceiver(audioStreamReceiver, intentFilter);

        super.onStart();
        QBRTCSession session = SessionManager.getCurrentSession();
        if (!isMessageProcessed && session != null) {
            if (startReason == Consts.CALL_DIRECTION_TYPE.INCOMING.ordinal()) {
                Log.d(TAG, "acceptCall() from " + TAG);
                session.acceptCall(session.getUserInfo());
            } else {
                Log.d(TAG, "startCall() from " + TAG);
                session.startCall(session.getUserInfo());
            }
            isMessageProcessed = true;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        intentFilter = new IntentFilter();
        intentFilter.addAction(AudioManager.ACTION_HEADSET_PLUG);
        intentFilter.addAction(AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED);
        audioStreamReceiver = new AudioStreamReceiver();
    }

    protected void initViews(View view) {
//        opponentsFromCall = (LinearLayout) view.findViewById(R.id.opponentsFromCall);

        dynamicToggleVideoCall = (ToggleButton) view.findViewById(R.id.dynamicToggleVideoCall);
        dynamicToggleVideoCall.setOnClickListener(this);
        micToggleVideoCall = (ToggleButton) view.findViewById(R.id.micToggleVideoCall);
        micToggleVideoCall.setOnClickListener(this);

        opponentNameView = (TextView) view.findViewById(R.id.incUserName);
        if (startReason == Consts.CALL_DIRECTION_TYPE.OUTGOING.ordinal()) {
            opponentNameView.setText(DataHolder.getUserNameByID(opponents.get(0)));
            opponentNameView.setBackgroundResource(BaseLogginedUserActivity.selectBackgrounForOpponent((
                    DataHolder.getUserIndexByID(opponents.get(0))) + 1));
        } else if (startReason == Consts.CALL_DIRECTION_TYPE.INCOMING.ordinal())  {
            opponentNameView.setText(DataHolder.getUserNameByID(callerID));
            opponentNameView.setBackgroundResource(BaseLogginedUserActivity.selectBackgrounForOpponent((
                    DataHolder.getUserIndexByID(callerID)) + 1));
        }

        handUpVideoCall = (ImageButton) view.findViewById(R.id.handUpVideoCall);
        handUpVideoCall.setOnClickListener(this);

        noVideoImageContainer = (LinearLayout) view.findViewById(R.id.noVideoImageContainer);
    }

    @Override
    public void onStop() {
        super.onStop();
        getActivity().unregisterReceiver(audioStreamReceiver);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.dynamicToggleVideoCall:
                if (SessionManager.getCurrentSession() != null) {
                    Log.d(TAG, "Dynamic switched!");
                    SessionManager.getCurrentSession().switchAudioOutput();
                }
                break;
            case R.id.micToggleVideoCall:
                if (SessionManager.getCurrentSession() != null) {
                    if (isAudioEnabled) {
                        Log.d(TAG, "Mic is off!");
                        SessionManager.getCurrentSession().setAudioEnabled(false);
                        isAudioEnabled = false;
                    } else {
                        Log.d(TAG, "Mic is on!");
                        SessionManager.getCurrentSession().setAudioEnabled(true);
                        isAudioEnabled = true;
                    }
                }
                break;
            case R.id.handUpVideoCall:
                actionButtonsEnabled(false);
                handUpVideoCall.setEnabled(false);
                Log.d(TAG, "Call is stopped");

//                ((CallActivity) getActivity()).delayedHungUpCurrentSession();
                ((CallActivity) getActivity()).hangUpCurrentSession();
                handUpVideoCall.setEnabled(false);
                handUpVideoCall.setActivated(false);
                break;
            default:
                break;
        }
    }

//    private List<QBUser> getOpponentsFromCall(ArrayList<Integer> opponents) {
//        ArrayList<QBUser> opponentsList = new ArrayList<>();
//
//        for (Integer opponentId : opponents) {
//            try {
//                opponentsList.add(QBUsers.getUser(opponentId));
//            } catch (QBResponseException e) {
//                e.printStackTrace();
//            }
//        }
//        return opponentsList;
//    }

//    private void createOpponentsList(List<Integer> opponents) {
//        if (opponents.size() != 0) {
//            for (Integer i : opponents) {
//                addOpponentPreviewToList(i, opponentsFromCall);
//            }
//        }
//    }
//
//    private void addOpponentPreviewToList(Integer userID, LinearLayout opponentsFromCall) {
//
//        if (opponentsFromCall.findViewById(userID) == null) {
//
//            View opponentItemView = inflater.inflate(R.layout.list_item_opponent_from_call, opponentsFromCall, false);
//            opponentItemView.setId(userID);
//
//            opponentItemView.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    Log.d(TAG, "Main opponent Selected");
//                }
//            });
//
//            TextView opponentNumber = (TextView) opponentItemView.findViewById(R.id.opponentNumber);
//            opponentNumber.setText(String.valueOf(ListUsersActivity.getUserIndex(userID)));
//            opponentNumber.setBackgroundResource(BaseLogginedUserActivity.resourceSelector
//                    (ListUsersActivity.getUserIndex(userID)));
//
//            ImageView opponentAvatar = (ImageView) opponentItemView.findViewById(R.id.opponentAvatar);
//            opponentAvatar.setImageResource(R.drawable.ic_noavatar);
//
//            opponentsFromCall.addView(opponentItemView);
//        } else {
//            opponentsFromCall.addView(opponentsFromCall.findViewById(userID));
//        }
//    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private class AudioStreamReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction().equals(AudioManager.ACTION_HEADSET_PLUG)){
                Log.d(TAG, "ACTION_HEADSET_PLUG " + intent.getIntExtra("state", -1));
            } else if (intent.getAction().equals(AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED)){
                Log.d(TAG, "ACTION_SCO_AUDIO_STATE_UPDATED " + intent.getIntExtra("EXTRA_SCO_AUDIO_STATE", -2));
            }

            if (intent.getIntExtra("state", -1) == 0 /*|| intent.getIntExtra("EXTRA_SCO_AUDIO_STATE", -1) == 0*/){
                dynamicToggleVideoCall.setChecked(false);
            } else if (intent.getIntExtra("state", -1) == 1) {
                dynamicToggleVideoCall.setChecked(true);
            } else {
//                Toast.makeText(context, "Output audio stream is incorrect", Toast.LENGTH_LONG).show();
            }
            dynamicToggleVideoCall.invalidate();
        }
    }
}
