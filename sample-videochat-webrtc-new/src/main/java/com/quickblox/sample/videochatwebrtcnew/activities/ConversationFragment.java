package com.quickblox.sample.videochatwebrtcnew.activities;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.quickblox.sample.videochatwebrtcnew.ApplicationSingleton;
import com.quickblox.sample.videochatwebrtcnew.R;
import com.quickblox.videochat.webrtcnew.QBRTCClient;
import com.quickblox.videochat.webrtcnew.QBRTCSession;
import com.quickblox.videochat.webrtcnew.model.QBRTCSessionDescription;
import com.quickblox.videochat.webrtcnew.model.QBRTCTypes;
import com.quickblox.videochat.webrtcnew.view.QBGLVideoView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by tereha on 16.02.15.
 */
public class ConversationFragment extends Fragment implements Serializable {

    private ArrayList<Integer> opponents;
    private int qbConferenceType;
    private int startReason;
    private String sessionID;
    private QBRTCTypes.QBConferenceType conferenceType;
    private QBGLVideoView videoView;
    private QBRTCSessionDescription sessionDescription;
//    private HorizontalScrollView camerasOpponentsList;
    private ToggleButton cameraToggle;
    private ToggleButton dynamicToggleVideoCall;
    private ToggleButton micToggleVideoCall;
    private ImageButton handUpVideoCall;
    private TextView incUserName;
    private View view;
    private Map<String, String> userInfo;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_conversation, container, false);
        ((NewDialogActivity) getActivity()).initActionBarWithTimer();

        Log.d("Track", "onCreateView() from ConversationFragment Level 1");



        if (savedInstanceState == null) {

            initViews(view);
            initButtonsListener();

            if (getArguments() != null) {
                opponents = getArguments().getIntegerArrayList(ApplicationSingleton.OPPONENTS);
                qbConferenceType = getArguments().getInt(ApplicationSingleton.CONFERENCE_TYPE);
                startReason = getArguments().getInt(NewDialogActivity.START_CONVERSATION_REASON);
                sessionID = getArguments().getString(NewDialogActivity.SESSION_ID);
                sessionDescription = (QBRTCSessionDescription) getArguments().getSerializable("sessionDescription");

                userInfo = new HashMap<>();
                String key;
                String value;

                userInfo.put("any_custom_data", "some data");
                userInfo.put("my_avatar_url", "avatar_reference");



            }

            //Conference
            conferenceType =
                    qbConferenceType == 1 ? QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_VIDEO :
                            QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_AUDIO;

            initCall(sessionID);

//            ((NewDialogActivity) getActivity()).getCurrentSession().startCall(null);
            Log.d("Track", "onCreateView() from ConversationFragment Level 2");
        }

        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setRetainInstance(true);
        Log.d("Track", "onCreate() from ConversationFragment");
        super.onCreate(savedInstanceState);
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


        HorizontalScrollView camerasOpponentsList = (HorizontalScrollView)view.findViewById(R.id.camerasOpponentsList);
        ListView camerasOpponentsListLand = (ListView)view.findViewById(R.id.camerasOpponentsListLand);

        cameraToggle = (ToggleButton)view.findViewById(R.id.cameraToggle);
        dynamicToggleVideoCall = (ToggleButton)view.findViewById(R.id.dynamicToggleVideoCall);
        micToggleVideoCall = (ToggleButton)view.findViewById(R.id.micToggleVideoCall);

        handUpVideoCall = (ImageButton)view.findViewById(R.id.handUpVideoCall);

        incUserName = (TextView)view.findViewById(R.id.incUserName);

        ((NewDialogActivity)getActivity()).setVideoView(videoView);
    }

    private void initButtonsListener() {

        cameraToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    ((NewDialogActivity)getActivity()).getCurrentSession().setVideoEnabled(true);
                    Log.d("Track", "Camera is on!");
                } else {
                    ((NewDialogActivity)getActivity()).getCurrentSession().setVideoEnabled(false);
                    Log.d("Track", "Camera is off!");
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
                    ((NewDialogActivity)getActivity()).getCurrentSession().setAudioEnabled(true);
                } else {
                    Log.d("Track", "Mic is off!");
                    ((NewDialogActivity)getActivity()).getCurrentSession().setAudioEnabled(false);
                }
            }
        });

        handUpVideoCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("Track", "Call is stopped");
                ((NewDialogActivity)getActivity()).removeConversationFragment();

                if (sessionID == null){
                    ((NewDialogActivity)getActivity()).getCurrentSession().hangUp(userInfo);
                } else {
                    ((NewDialogActivity)getActivity()).getSession(sessionID)
                            .hangUp(userInfo);
                }
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
        } else {
            ((NewDialogActivity) getActivity()).getCurrentSession().startCall(new HashMap<String, String>());
        }
    }

    public static enum StartConversetionReason {
        INCOME_CALL_FOR_ACCEPTION,
        OUTCOME_CALL_MADE;
    }
}


