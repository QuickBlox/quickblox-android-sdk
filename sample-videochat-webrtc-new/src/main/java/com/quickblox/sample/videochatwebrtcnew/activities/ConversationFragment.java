package com.quickblox.sample.videochatwebrtcnew.activities;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.quickblox.sample.videochatwebrtcnew.ApplicationSingleton;
import com.quickblox.sample.videochatwebrtcnew.R;
import com.quickblox.videochat.webrtcnew.QBRTCClient;
import com.quickblox.videochat.webrtcnew.QBRTCSession;
import com.quickblox.videochat.webrtcnew.model.QBRTCTypes;
import com.quickblox.videochat.webrtcnew.view.QBGLVideoView;
import com.quickblox.videochat.webrtcnew.view.QBRTCVideoTrack;

import org.webrtc.VideoRenderer;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by tereha on 16.02.15.
 */
public class ConversationFragment extends Fragment {

    private ArrayList<Integer> opponents;
    private int qbConferenceType;
    private int startReason;
    private String sessionID;
    private QBRTCTypes.QBConferenceType conferenceType;
    private QBGLVideoView videoView;
    private HorizontalScrollView camerasOpponentsList;
    private ToggleButton switchCameraToggle;
    private ToggleButton dynamicToggleVideoCall;
    private ToggleButton micToggleVideoCall;
    private ImageButton handUpVideoCall;
    private TextView incUserName;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_conversation, container, false);

        initViews(view);


        if (getArguments() != null){
            opponents = getArguments().getIntegerArrayList(ApplicationSingleton.OPPONENTS);
            qbConferenceType = getArguments().getInt(ApplicationSingleton.CONFERENCE_TYPE);
            startReason = getArguments().getInt(NewDialogActivity.START_CONVERSATION_REASON);
            sessionID = getArguments().getString(NewDialogActivity.SESSION_ID);
        }

        //Conference
        conferenceType =
                qbConferenceType == 1 ? QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_VIDEO :
                        QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_AUDIO;

        initCall(sessionID);

//        ((NewDialogActivity) getActivity()).getCurrentSession().startCall(null);

        return view;
    }

    private void initCall(String sessionID) {
        if (sessionID == null){
            // init RTCChat
            ((NewDialogActivity) getActivity()).setCurrentSession(QBRTCClient.getInstance()
                    .createNewSessionWithOpponents(opponents, conferenceType, null));
        }
    }

    private void initViews(View view) {
        videoView = (QBGLVideoView)view.findViewById(R.id.videoView);

        camerasOpponentsList = (HorizontalScrollView)view.findViewById(R.id.camerasOpponentsList);

        switchCameraToggle = (ToggleButton)view.findViewById(R.id.switchCameraToggle);
        dynamicToggleVideoCall = (ToggleButton)view.findViewById(R.id.dynamicToggleVideoCall);
        micToggleVideoCall = (ToggleButton)view.findViewById(R.id.micToggleVideoCall);

        handUpVideoCall = (ImageButton)view.findViewById(R.id.handUpVideoCall);

        incUserName = (TextView)view.findViewById(R.id.incUserName);

        ((NewDialogActivity)getActivity()).setVideoView(videoView);
    }

    private void initButtonsListener() {

        switchCameraToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Log.d("Track", "Dynamic is on!");
                } else {
                    Log.d("Track", "Dynamic is off!");
                }
            }
        });

        dynamicToggleVideoCall.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Log.d("Track", "Dynamic is on!");
                } else {
                    Log.d("Track", "Dynamic is off!");
                }
            }
        });

        micToggleVideoCall.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Log.d("Track", "Mic is on!");
                } else {
                    Log.d("Track", "Mic is off!");
                }
            }
        });

        handUpVideoCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("Track", "Call is stopped");
            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();
        if(startReason == StartConversetionReason.INCOME_CALL_FOR_ACCEPTION.ordinal()){
            QBRTCSession session =((NewDialogActivity)getActivity()).getSession(sessionID);
            if(session != null){
                session.acceptCall(session.getUserInfo());
            }
        }
    }

    public static enum StartConversetionReason {
        INCOME_CALL_FOR_ACCEPTION,
        OUTCOME_CALL_MADE;
    }
}


