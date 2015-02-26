package com.quickblox.sample.videochatwebrtcnew.fragments;

import android.app.Fragment;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.quickblox.core.exception.QBResponseException;
import com.quickblox.sample.videochatwebrtcnew.ApplicationSingleton;
import com.quickblox.sample.videochatwebrtcnew.R;
import com.quickblox.sample.videochatwebrtcnew.activities.ListUsersActivity;
import com.quickblox.sample.videochatwebrtcnew.activities.NewDialogActivity;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;
import com.quickblox.videochat.webrtc.view.QBVideoStreamView;
import com.quickblox.videochat.webrtcnew.QBRTCClient;
import com.quickblox.videochat.webrtcnew.QBRTCSession;
import com.quickblox.videochat.webrtcnew.model.QBRTCSessionDescription;
import com.quickblox.videochat.webrtcnew.model.QBRTCTypes;
import com.quickblox.videochat.webrtcnew.view.QBGLVideoView;
import com.quickblox.videochat.webrtcnew.view.QBRTCVideoTrack;

import org.webrtc.VideoRendererGui;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by tereha on 16.02.15.
 */
public class ConversationFragment extends Fragment implements Serializable {

    private String TAG = "ConversationFragment";
    private ArrayList<Integer> opponents;
    private int qbConferenceType;
    private int startReason;
    private String sessionID;
    private QBRTCTypes.QBConferenceType conferenceType;
    //    private QBGLVideoView videoView;
    private GLSurfaceView videoView;

    private QBRTCSessionDescription sessionDescription;
    private QBGLVideoView opponentLittleCamera;
    private TextView opponentNumber;
    private TextView connectionStatus;
    private ImageView opponentAvatar;
    //    private HorizontalScrollView camerasOpponentsList;
    private ToggleButton cameraToggle;
    private ToggleButton switchCameraToggle;
    private ToggleButton dynamicToggleVideoCall;
    private ToggleButton micToggleVideoCall;
    private ImageButton handUpVideoCall;
    private TextView incUserName;
    private View view;
    private Map<String, String> userInfo;
    private View opponentItemView;
    private HorizontalScrollView camerasOpponentsList;
    private LinearLayout opponentsFromCall;
    private LayoutInflater inflater;
    private boolean isVideoEnabled = true;
    private boolean isAudioEnabled = true;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_conversation, container, false);
        this.inflater = inflater;

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

            }

            //Conference
            conferenceType =
                    qbConferenceType == 1 ? QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_VIDEO :
                            QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_AUDIO;

            VideoRendererGui.setView(videoView, new Runnable() {
                @Override
                public void run() {

                }
            });
                    ((NewDialogActivity) getActivity()).setCurrentVideoView(videoView);

            initCall(sessionID);
//            createOpponentsList(opponents, camerasOpponentsList);



            Log.d("Track", "onCreateView() from ConversationFragment Level 2");
        }

        if (conferenceType == QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_VIDEO) {
            cameraToggle.setVisibility(View.VISIBLE);

        } else {
            videoView.setVisibility(View.INVISIBLE);
            cameraToggle.setVisibility(View.GONE);

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
        if (sessionID == null ){
            // init RTCChat
            Log.d(TAG, "Try start call with one opponent");
                ((NewDialogActivity) getActivity()).setCurrentSession(QBRTCClient.getInstance()
                        .createNewSessionWithOpponents(opponents, conferenceType, null));
            } else {
                Toast.makeText(getActivity(), "Choose at least one opponent", Toast.LENGTH_LONG).show();
            }
    }

    private void initViews(View view) {

//        videoView = (QBGLVideoView)view.findViewById(R.id.videoView);
        videoView = (GLSurfaceView)view.findViewById(R.id.videoView);

//        camerasOpponentsList = (HorizontalScrollView)view.findViewById(R.id.camerasOpponentsList);
//        ScrollView camerasOpponentsListLand = (ScrollView)view.findViewById(R.id.camerasOpponentsListLand);

        opponentsFromCall = (LinearLayout)view.findViewById(R.id.opponentsFromCall);

        cameraToggle = (ToggleButton)view.findViewById(R.id.cameraToggle);
        switchCameraToggle = (ToggleButton) view.findViewById(R.id.switchCameraToggle);
        dynamicToggleVideoCall = (ToggleButton)view.findViewById(R.id.dynamicToggleVideoCall);
        micToggleVideoCall = (ToggleButton)view.findViewById(R.id.micToggleVideoCall);

        handUpVideoCall = (ImageButton)view.findViewById(R.id.handUpVideoCall);

        incUserName = (TextView)view.findViewById(R.id.incUserName);

//        LayoutInflater inflater = getActivity().getLayoutInflater();



        /*opponentItemView = inflater.inflate(R.layout.list_item_opponent_from_call, opponentsFromCall);

        opponentLittleCamera = (QBGLVideoView)opponentItemView.findViewById(R.id.opponentLittleCamera);
        opponentNumber = (TextView)opponentItemView.findViewById(R.id.opponentNumber);
        connectionStatus = (TextView)opponentItemView.findViewById(R.id.connectionStatus);
        opponentAvatar = (ImageView)opponentItemView.findViewById(R.id.opponentAvatar);*/

    }

    private void initButtonsListener() {

       switchCameraToggle.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               ((NewDialogActivity)getActivity()).getCurrentSession().switchCapturePosition();
                   Log.d(TAG, "Camera switched!");
           }
       });



        cameraToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isVideoEnabled) {
                    ((NewDialogActivity) getActivity()).getCurrentSession().setVideoEnabled(false);
                    isVideoEnabled = false;
                    Log.d("Track", "Camera is off!");
                } else {
                    ((NewDialogActivity) getActivity()).getCurrentSession().setVideoEnabled(true);
                    isVideoEnabled = true;
                    Log.d("Track", "Camera is on!");
                    switchCameraToggle.setVisibility(View.INVISIBLE);
                    switchCameraToggle.setVisibility(View.VISIBLE);
                }
            }
        });

        dynamicToggleVideoCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                ((NewDialogActivity)getActivity()).getCurrentSession().switchAudioOutput();
//                if (isChecked) {
//                    Log.d("Track", "Dynamic is off!");
//                } else {
//                    Log.d("Track", "Dynamic is on!");
//                }
            }
        });

        micToggleVideoCall./*setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {*/
                setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isAudioEnabled) {
                    Log.d("Track", "Mic is off!");
                    ((NewDialogActivity)getActivity()).getCurrentSession().setAudioEnabled(false);
                    isAudioEnabled = false;
                } else {
                    Log.d("Track", "Mic is on!");
                    ((NewDialogActivity)getActivity()).getCurrentSession().setAudioEnabled(true);
                    isAudioEnabled = true;
                }
            }
        });

        handUpVideoCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("Track", "Call is stopped");

                if (sessionID == null){
                    ((NewDialogActivity)getActivity()).getCurrentSession().hangUp(userInfo);
                } else {
                    ((NewDialogActivity)getActivity()).getSession(sessionID)
                            .hangUp(userInfo);
                }

                ((NewDialogActivity)getActivity()).removeConversationFragment();
            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();
            if(startReason == NewDialogActivity.StartConversetionReason.INCOME_CALL_FOR_ACCEPTION.ordinal()){
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

    private List<QBUser> getOpponentsFromCall(ArrayList<Integer> opponents){
        ArrayList<QBUser> opponentsList = new ArrayList<>();

        for (Integer opponentId : opponents){
            try {
                opponentsList.add(QBUsers.getUser(opponentId));
            } catch (QBResponseException e) {
                e.printStackTrace();
            }
        }
        return opponentsList;
    }

    private void createOpponentsList(List<Integer> opponents, HorizontalScrollView camerasOpponentsList){
        QBUser opponent;
//        View opponentItemView;/* = view.findViewById(R.layout.list_item_opponent_from_call);*/

        for (Integer i : opponents){

            View opponentItemView = inflater.inflate(R.layout.list_item_opponent_from_call, opponentsFromCall, false);

            opponentItemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("Track", "Main opponent Selected");
                }
            });

            QBGLVideoView opponentLittleCamera = (QBGLVideoView)opponentItemView.findViewById(R.id.opponentLittleCamera);
            TextView opponentNumber = (TextView)opponentItemView.findViewById(R.id.opponentNumber);
//            TextView connectionStatus = (TextView)opponentItemView.findViewById(R.id.connectionStatus);
            ImageView opponentAvatar = (ImageView)opponentItemView.findViewById(R.id.opponentAvatar);

            /*try {
                opponent = QBUsers.getUser(i);
            } catch (QBResponseException e) {
                e.printStackTrace();
            }*/
            opponentNumber.setText(String.valueOf(ListUsersActivity.getUserIndex(i)));
            opponentNumber.setBackgroundResource(ListUsersActivity.resourceSelector
                    (ListUsersActivity.getUserIndex(i)));

//            connectionStatus.setText(i.toString());
            QBRTCVideoTrack videoTrack = NewDialogActivity.videoTrackList.get(i);
            opponentLittleCamera.setVideoTrack(videoTrack, QBGLVideoView.Endpoint.REMOTE);
            opponentAvatar.setImageResource(R.drawable.ic_noavatar);

            if (videoTrack == null){
                opponentAvatar.setVisibility(View.VISIBLE);
            }
                opponentAvatar.setVisibility(View.INVISIBLE);

            opponentsFromCall.addView(opponentItemView);
        }
    }
}


