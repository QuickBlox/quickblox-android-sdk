package com.quickblox.sample.videochatwebrtcnew.activities;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;


import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBSignaling;
import com.quickblox.chat.QBVideoChatWebRTCSignalingManager;
import com.quickblox.chat.QBWebRTCSignaling;
import com.quickblox.chat.listeners.QBVideoChatSignalingManagerListener;
import com.quickblox.sample.videochatwebrtcnew.ApplicationSingleton;
import com.quickblox.sample.videochatwebrtcnew.R;
import com.quickblox.videochat.webrtcnew.QBRTCClient;
import com.quickblox.videochat.webrtcnew.QBRTCSession;
import com.quickblox.videochat.webrtcnew.callbacks.QBRTCChatCallback;
import com.quickblox.videochat.webrtcnew.model.QBRTCTypes;
import com.quickblox.videochat.webrtcnew.view.QBGLVideoView;
import com.quickblox.videochat.webrtcnew.view.QBRTCVideoTrack;
import com.quickblox.videochat.webrtcnew.view.VideoCallBacks;

import org.webrtc.VideoRenderer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


/**
 * Created by tereha on 03.02.15.
 */
public class VideoChatActivity extends LogginedUserABActivity implements QBRTCChatCallback {

    private QBGLVideoView videoView;
//    private QBGLVideoView mainOpponentCamera;
    private HorizontalScrollView camerasOpponentsList;
    private ToggleButton switchCameraToggle;
    private ToggleButton dynamicToggleVideoCall;
    private ToggleButton micToggleVideoCall;
    private ImageButton handUpVideoCall;
    private TextView incUserName;

    private Map<String, QBRTCSession> sessionList = new HashMap<>();
    private QBRTCVideoTrack localVideoTrack;
    public static final String INCOME_CALL_FRAGMENT = "income_call_fragment";
    public static final String CONVERSATION_CALL_FRAGMENT = "conversation_call_fragment";
    public static final String START_CONVERSATION_REASON = "start_conversation_reason";
    public static final String SESSION_ID = "sessionID";
    private String currentSession;
    private int startReason;
    private String sessionID;
    private ArrayList<Integer> opponents;
    private QBRTCTypes.QBConferenceType conferenceType;
    private int qbConferenceType;
//    private CallManger callManger;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_call);

        super.initActionBar();
        initUI();
        initButtonsListener();

        opponents = (ArrayList<Integer>)getIntent().getSerializableExtra("opponents");
        //qbConferenceType = Integer.parseInt(String.valueOf(getIntent().getStringExtra("qbConferenceType")));




        /*if (getArguments() != null){
            opponents = getArguments().getIntegerArrayList(ApplicationSingleton.OPPONENTS);
            qbConferenceType = getArguments().getInt(ApplicationSingleton.CONFERENCE_TYPE);
            startReason = getArguments().getInt(START_CONVERSATION_REASON);
            sessionID = getArguments().getString(SESSION_ID);
        }*/

        //Conference
        conferenceType = QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_VIDEO;
//                qbConferenceType == 1 ? QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_VIDEO :
//                        QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_AUDIO;

        initCall(sessionID);



        QBChatService instance = QBChatService.getInstance();
        QBVideoChatWebRTCSignalingManager videoChatWebRTCSignalingManager = instance.getVideoChatWebRTCSignalingManager();
        videoChatWebRTCSignalingManager.addSignalingManagerListener(
                new QBVideoChatSignalingManagerListener() {
                    @Override
                    public void signalingCreated(QBSignaling signaling, boolean createdLocally) {
                        if (!createdLocally) {
                            // Init Conversation
                            QBRTCClient.init(VideoChatActivity.this);
                            QBRTCClient.getInstance().addCallback(VideoChatActivity.this);
                            QBRTCClient.getInstance().setQBWebRTCSignaling((QBWebRTCSignaling) signaling);
                        }
                    }
                });

//        getFragmentManager().beginTransaction().replace(R.id.faragment_container, new UsersFragment(), CONVERSATION_CALL_FRAGMENT).commit();



    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_video_chat, menu);
        return true;
    }

    private void initUI(){
        videoView = (QBGLVideoView) findViewById(R.id.videoView);
//        mainOpponentCamera = (QBGLVideoView) findViewById(R.id.mainOpponentCamera);

        camerasOpponentsList = (HorizontalScrollView)findViewById(R.id.camerasOpponentsList);

        switchCameraToggle = (ToggleButton)findViewById(R.id.switchCameraToggle);
        dynamicToggleVideoCall = (ToggleButton)findViewById(R.id.dynamicToggleVideoCall);
        micToggleVideoCall = (ToggleButton)findViewById(R.id.micToggleVideoCall);

        handUpVideoCall = (ImageButton)findViewById(R.id.handUpVideoCall);

        incUserName = (TextView) findViewById(R.id.incUserName);

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
            QBRTCSession session =/*((NewDialogActivity)getActivity()).*/getSession(sessionID);
            if(session != null){
                session.acceptCall(session.getUserInfo());
            }
        }
    }

    public static enum StartConversetionReason {
        INCOME_CALL_FOR_ACCEPTION,
        OUTCOME_CALL_MADE;
    }

    private void initCall(String sessionID) {
        if (sessionID == null){
            // init RTCChat
            /*((NewDialogActivity) getActivity()).*/setCurrentSession(QBRTCClient.getInstance()
                    .createNewSessionWithOpponents(opponents, conferenceType, null));
        }
    }

    public void setCurrentSession(QBRTCSession session) {
        if(!sessionList.containsKey(session.getSessionID())) {
            addSession(session);
        }
        currentSession = session.getSessionID();
    }

    public QBRTCSession getSession(String sessionID) {
        return sessionList.get(sessionID);
    }

    public void setVideoViewVisibility(int visibility){
        videoView.setVisibility(visibility);
    }

    public void addSession(QBRTCSession session){
        sessionList.put(session.getSessionID(), session);
    }

    public void setCurrentSesionId(String sesionId){
        this.currentSession = sesionId;
    }

    /*public void setCallManager(CallManger callManger){
        this.callManger = callManger;
    }*/

//    public void setLocalRenderer(VideoRenderer.Callbacks localRenderer){
//        this.localRenderer = localRenderer;
//    }
//
//    public void setRemouteRenderer(List<VideoRenderer.Callbacks> remouteRenderers){
//        this.opponentRenderers = remouteRenderers;
//    }

    public void setVideoView(QBGLVideoView videoView) {
        this.videoView = videoView;
    }



    // ---------------Chat callback methods implementation  ----------------------//

    @Override
    public void onReceiveNewCallWithSession(QBRTCSession session) {
        Toast.makeText(this, "IncomeCall", Toast.LENGTH_SHORT).show();
        sessionList.put(session.getSessionID(), session);
        ///startIncomeCallFragment(session);
    }

    @Override
    public void onReceiveDialingWithSession(QBRTCSession session) {

    }

    @Override
    public void onUserNotAnswer(QBRTCSession session, Integer userID) {
        removeUserWithID(userID);
    }

    @Override
    public void onBeginConnectToUser(QBRTCSession session, Integer userID) {

    }

    @Override
    public void onCallRejectByUser(QBRTCSession session, Integer userID, Map<String, String> userInfo) {
        removeUserWithID(userID);
    }

    @Override
    public void onLocalVideoTrackReceive(QBRTCSession session, QBRTCVideoTrack videoTrack) {
        this.localVideoTrack = videoTrack;
        videoTrack.addRenderer(new VideoRenderer(new VideoCallBacks(videoView, QBGLVideoView.Endpoint.LOCAL)));
//        videoView.setVideoTrack(videoTrack);
    }

    @Override
    public void onRemoteVideoTrackReceive(QBRTCSession session, QBRTCVideoTrack videoTrack, Integer userID) {
        VideoCallBacks videoCallBacks = new VideoCallBacks(videoView, QBGLVideoView.Endpoint.REMOTE);
//        videoCallBacks.setSize(200, 300);
        VideoRenderer remouteRenderer = new VideoRenderer(videoCallBacks);
        videoTrack.addRenderer(remouteRenderer);
    }

    @Override
    public void onSessionEnd(QBRTCSession session) {

    }

    @Override
    public void onConnectedToUser(QBRTCSession session, Integer userID) {

    }

    @Override
    public void onUserDisconnected(QBRTCSession session, Integer userID) {

    }

    @Override
    public void onReceiveHangUpFromUser(QBRTCSession session, Integer userID) {
        removeUserWithID(userID);
    }

    private void removeUserWithID(Integer userID) {
//
//        QBUser user = ConnectionManager.instanceUserWithID(userID);
//        IndexPath *indexPath = indexPathAtUserID(userID);
//
//        List<QBUser> users = new ArrayList<>();
//        users.remove(user);
//        users = users.copy;
//
//        opponentsCollectionView deleteItemsAtIndexPaths(indexPath);
    }

    public void removeIncomeCallFragment() {
        FragmentManager fragmentManager = getFragmentManager();
        Fragment fragment = fragmentManager.findFragmentByTag(INCOME_CALL_FRAGMENT);

        if (fragment != null){
            fragmentManager.beginTransaction().remove(fragment).commit();
        }
    }

   /* private void startIncomeCallFragment(QBRTCSession session) {
        Fragment fragment = new IncomeCallFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("sessionDescription", session.getSessionDescription());
        bundle.putIntegerArrayList("opponents", new ArrayList<Integer>(session.getOpponents()));
        fragment.setArguments(bundle);
        getFragmentManager().beginTransaction().add(R.id.faragment_container, fragment, INCOME_CALL_FRAGMENT).commit();
    }

    public void addCanversationFragmentOnSession(String sessionID,
                                                 ConversationFragment.StartConversetionReason conversetionReason) {
        // init conversation fragment
        QBRTCSession currentSession = sessionList.get(sessionID);
        if (currentSession != null) {
            ConversationFragment fragment = new ConversationFragment();
            Bundle bundle = new Bundle();
            bundle.putIntegerArrayList(ApplicationSingleton.OPPONENTS,
                    new ArrayList<Integer>(currentSession.getOpponents()));
            bundle.putInt(ApplicationSingleton.CONFERENCE_TYPE, currentSession.getConferenceType().getValue());
            bundle.putInt(START_CONVERSATION_REASON, conversetionReason.ordinal());
            bundle.putString(SESSION_ID, sessionID);

            if(currentSession.getUserInfo() != null) {
                for (String key : currentSession.getUserInfo().keySet()) {
                    bundle.putString("UserInfo:" + key, currentSession.getUserInfo().get(key));
                }
            }
            fragment.setArguments(bundle);

            // Start conversation fragment
            getFragmentManager().beginTransaction().add(R.id.faragment_container, fragment).commit();
        }
    }*/

    public void startVideoChatActivity(List<Integer> opponents,
                                       QBRTCTypes.QBConferenceType qbConferenceType,
                                       Map<String, String> userInfo) {

        QBRTCClient.init(this);
        QBRTCClient.getInstance().addCallback(this);

        Intent intent = new Intent (this, VideoChatActivity.class);
        intent.putExtra("opponents", (java.io.Serializable) opponents);
        intent.putExtra("qbConferenceType", (java.io.Serializable) qbConferenceType);
        intent.putExtra("userInfo", (java.io.Serializable) userInfo);

        for (String key : userInfo.keySet()){
            intent.putExtra("UserInfo:" + key, userInfo.get(key));
        }

        startActivity(intent);

    }

    public QBRTCSession getCurrentSession() {
        return sessionList.get(currentSession);
    }

    public Map<String,QBRTCSession> getSessions() {
        return sessionList;
    }
}
