package com.quickblox.sample.videochatwebrtcnew.fragments;

import android.app.Fragment;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.CompoundButton;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.quickblox.sample.videochatwebrtcnew.ApplicationSingleton;
import com.quickblox.sample.videochatwebrtcnew.R;
import com.quickblox.sample.videochatwebrtcnew.activities.ListUsersActivity;
import com.quickblox.sample.videochatwebrtcnew.activities.NewDialogActivity;
import com.quickblox.sample.videochatwebrtcnew.helper.DataHolder;
import com.quickblox.users.model.QBUser;
import com.quickblox.videochat.webrtcnew.QBRTCClient;
import com.quickblox.videochat.webrtcnew.QBRTCSession;
import com.quickblox.videochat.webrtcnew.model.QBRTCSessionDescription;
import com.quickblox.videochat.webrtcnew.model.QBRTCTypes;
import com.quickblox.videochat.webrtcnew.view.QBGLVideoView;

import org.webrtc.VideoRenderer;
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
//    private QBRTCSessionDescription sessionDescription;
    private static VideoRenderer.Callbacks REMOTE_RENDERER;

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
    private ImageView imgMyCameraOff;
    private TextView incUserName;
    private View view;
    private Map<String, String> userInfo;
    private View opponentItemView;
    private HorizontalScrollView camerasOpponentsList;
    private LinearLayout opponentsFromCall;
    private LayoutInflater inflater;
    private boolean isVideoEnabled = true;
    private boolean isAudioEnabled = true;
    private List<QBUser> allUsers = new ArrayList<>();
//    private Chronometer timer;


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

            Log.d("Track", "onCreateView() from ConversationFragment Level 2");
        }

        if (conferenceType == QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_VIDEO) {
            cameraToggle.setVisibility(View.VISIBLE);

        } else {
            imgMyCameraOff.setVisibility(View.INVISIBLE);
            videoView.setVisibility(View.INVISIBLE);
            cameraToggle.setVisibility(View.GONE);

        }


        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setRetainInstance(true);
        Log.d("Track", "onCreate() from ConversationFragment");
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
    }

    private void initViews(View view) {

//        videoView = (QBGLVideoView)view.findViewById(R.id.videoView);
        videoView = (GLSurfaceView) view.findViewById(R.id.videoView);

//        camerasOpponentsList = (HorizontalScrollView)view.findViewById(R.id.camerasOpponentsList);
//        ScrollView camerasOpponentsListLand = (ScrollView)view.findViewById(R.id.camerasOpponentsListLand);

        opponentsFromCall = (LinearLayout) view.findViewById(R.id.opponentsFromCall);

        cameraToggle = (ToggleButton) view.findViewById(R.id.cameraToggle);
        switchCameraToggle = (ToggleButton) view.findViewById(R.id.switchCameraToggle);
        dynamicToggleVideoCall = (ToggleButton) view.findViewById(R.id.dynamicToggleVideoCall);
        micToggleVideoCall = (ToggleButton) view.findViewById(R.id.micToggleVideoCall);

        handUpVideoCall = (ImageButton) view.findViewById(R.id.handUpVideoCall);

        incUserName = (TextView) view.findViewById(R.id.incUserName);

        imgMyCameraOff = (ImageView)view.findViewById(R.id.imgMyCameraOff);

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
                ((NewDialogActivity) getActivity()).getCurrentSession().switchCapturePosition();
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
                    imgMyCameraOff.setVisibility(View.VISIBLE);
                    switchCameraToggle.setVisibility(View.INVISIBLE);
                    Log.d("Track", "Camera is off!");
                } else {
                    ((NewDialogActivity) getActivity()).getCurrentSession().setVideoEnabled(true);
                    isVideoEnabled = true;
                    imgMyCameraOff.setVisibility(View.VISIBLE);
                    Log.d("Track", "Camera is on!");
                    switchCameraToggle.setVisibility(View.VISIBLE);
                    imgMyCameraOff.setVisibility(View.INVISIBLE);
//                    switchCameraToggle.setVisibility(View.VISIBLE);
                }
            }
        });

        dynamicToggleVideoCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                ((NewDialogActivity) getActivity()).getCurrentSession().switchAudioOutput();
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
                    ((NewDialogActivity) getActivity()).getCurrentSession().setAudioEnabled(false);
                    isAudioEnabled = false;
                } else {
                    Log.d("Track", "Mic is on!");
                    ((NewDialogActivity) getActivity()).getCurrentSession().setAudioEnabled(true);
                    isAudioEnabled = true;
                }
            }
        });

        handUpVideoCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("Track", "Call is stopped");

                ((NewDialogActivity) getActivity()).getCurrentSession().hangUp(userInfo);
                ((NewDialogActivity) getActivity()).removeConversationFragment();
            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();
        QBRTCSession session = ((NewDialogActivity) getActivity()).getCurrentSession();
        QBRTCSession session = ((NewDialogActivity) getActivity()).getCurrentSession();
        if (startReason == NewDialogActivity.StartConversetionReason.INCOME_CALL_FOR_ACCEPTION.ordinal()) {
            session.acceptCall(session.getUserInfo());
        } else {
            session.startCall(session.getUserInfo());
        }
    }

    public static enum StartConversetionReason {
        INCOME_CALL_FOR_ACCEPTION,
        OUTCOME_CALL_MADE;
    }

    private List<QBUser> getOpponentsFromCall(ArrayList<Integer> opponents) {
        ArrayList<QBUser> opponentsList = new ArrayList<>();

        for (Integer opponentId : opponents) {
            try {
                opponentsList.add(QBUsers.getUser(opponentId));
            } catch (QBResponseException e) {
                e.printStackTrace();
            }
        }
        return opponentsList;
    }*/

    private void createOpponentsList(List<Integer> opponents, HorizontalScrollView camerasOpponentsList) {
        QBUser opponent;
//        View opponentItemView;/* = view.findViewById(R.layout.list_item_opponent_from_call);*/

        for (Integer i : opponents) {

            View opponentItemView = inflater.inflate(R.layout.list_item_opponent_from_call, opponentsFromCall, false);
//
            opponentItemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("Track", "Main opponent Selected");
                }
            });

            QBGLVideoView opponentLittleCamera = (QBGLVideoView) opponentItemView.findViewById(R.id.opponentLittleCamera);
            TextView opponentNumber = (TextView) opponentItemView.findViewById(R.id.opponentNumber);
//            TextView connectionStatus = (TextView)opponentItemView.findViewById(R.id.connectionStatus);
            ImageView opponentAvatar = (ImageView) opponentItemView.findViewById(R.id.opponentAvatar);

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
//            opponentAvatar.setImageResource(R.drawable.ic_user_camera_off);
            opponentAvatar.setVisibility(View.VISIBLE);

            if (videoTrack == null) {
                opponentAvatar.setVisibility(View.VISIBLE);
            }
            opponentAvatar.setVisibility(View.INVISIBLE);

//            if (videoTrack.)

            opponentsFromCall.addView(opponentItemView);
        }
    }

    private String getCallerName (QBRTCSession session){
        String s = new String();
        int i = session.getCallerID();

        allUsers.addAll(DataHolder.createUsersList());

        for (QBUser usr : allUsers){
            if (usr.getId().equals(i)){
                s = usr.getFullName();
            }
        }
        return s;
    }

    public void startTimer (/*Chronometer timer*/) {
        View mCustomView = inflater.inflate(R.layout.actionbar_with_timer, null);

        Chronometer timer = (Chronometer) mCustomView.findViewById(R.id.timerABWithTimer);

        timer.setBase(SystemClock.elapsedRealtime());
        timer.start();
    }


}


