package com.quickblox.sample.videochatwebrtcnew.activities;


import android.app.Fragment;


import android.app.FragmentManager;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Chronometer;
import android.widget.Toast;

import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBSignaling;
import com.quickblox.chat.QBVideoChatWebRTCSignalingManager;
import com.quickblox.chat.QBWebRTCSignaling;
import com.quickblox.chat.listeners.QBVideoChatSignalingManagerListener;
import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.sample.videochatwebrtcnew.ApplicationSingleton;
import com.quickblox.sample.videochatwebrtcnew.R;
import com.quickblox.sample.videochatwebrtcnew.fragments.ConversationFragment;
import com.quickblox.sample.videochatwebrtcnew.fragments.IncomeCallFragment;
import com.quickblox.sample.videochatwebrtcnew.fragments.OpponentsFragment;
import com.quickblox.videochat.webrtcnew.QBRTCClient;
import com.quickblox.videochat.webrtcnew.QBRTCSession;
import com.quickblox.videochat.webrtcnew.callbacks.QBRTCChatCallback;
import com.quickblox.videochat.webrtcnew.model.QBRTCTypes;
import com.quickblox.videochat.webrtcnew.view.QBGLVideoView;
import com.quickblox.videochat.webrtcnew.view.QBRTCVideoTrack;
import com.quickblox.videochat.webrtcnew.view.VideoCallBacks;

import org.webrtc.VideoRenderer;
import org.webrtc.VideoRendererGui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.Inflater;

/**
 * Created by tereha on 16.02.15.
 */
public class NewDialogActivity extends LogginedUserABActivity implements QBRTCChatCallback {


    public static final String OPPONENTS_CALL_FRAGMENT = "opponents_call_fragment";
    public static final String INCOME_CALL_FRAGMENT = "income_call_fragment";
    public static final String CONVERSATION_CALL_FRAGMENT = "conversation_call_fragment";
    private static VideoRenderer.Callbacks REMOTE_RENDERER;
    private static VideoRenderer.Callbacks LOCAL_RENDERER;

    private OpponentsFragment opponentsFragment = null;
    private IncomeCallFragment incomeCallFragment = null;
    private ConversationFragment conversationFragment = null;

    public static final String START_CONVERSATION_REASON = "start_conversation_reason";
    public static final String SESSION_ID = "sessionID";
    private QBRTCSession session;
    private QBRTCVideoTrack localVideoTrack;
    private Map<String, QBRTCSession> sessionList = new HashMap<>();
    private String currentSession;
//    private CallManger callManger;
    //    private VideoRenderer.Callbacks localRenderer;
//    private List<VideoRenderer.Callbacks> opponentRenderers = new LinkedList<>();
    private QBGLVideoView videoView;
    public static String login;
    public static Map<Integer, QBRTCVideoTrack> videoTrackList = new HashMap<>();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState != null) {
            // Restore value of members from saved state
            opponentsFragment = (OpponentsFragment) savedInstanceState.getSerializable("opponentsFragment");
            conversationFragment = (ConversationFragment) savedInstanceState.getSerializable("conversationFragment");
            incomeCallFragment = (IncomeCallFragment) savedInstanceState.getSerializable("incomeCallFragment");
            Log.d("Track", "onCreate() from NewDialogActivity Level 2");
        } else {

            // Probably initialize members with default values for a new instance
            login = getIntent().getStringExtra("login");

            // Init income videochat messages listener
            QBChatService instance = QBChatService.getInstance();
            QBVideoChatWebRTCSignalingManager videoChatWebRTCSignalingManager = instance.getVideoChatWebRTCSignalingManager();
            videoChatWebRTCSignalingManager.addSignalingManagerListener(
                    new QBVideoChatSignalingManagerListener() {
                        @Override
                        public void signalingCreated(QBSignaling signaling, boolean createdLocally) {
                            if (!createdLocally) {
                                // Init Conversation
                                QBRTCClient.init(NewDialogActivity.this);
                                QBRTCClient.getInstance().addCallback(NewDialogActivity.this);
                                QBRTCClient.getInstance().setQBWebRTCSignaling((QBWebRTCSignaling) signaling);
                            }
                        }
                    });
            startOpponentsFragment();
            Log.d("Track", "onCreate() from NewDialogActivity Level 1");
        }

    }


    public void startOpponentsFragment(){
        opponentsFragment = (OpponentsFragment) getFragmentManager().findFragmentByTag(OPPONENTS_CALL_FRAGMENT);
        if(opponentsFragment == null){
            opponentsFragment = new OpponentsFragment();
            getFragmentManager().beginTransaction().replace(R.id.fragment_container, opponentsFragment, OPPONENTS_CALL_FRAGMENT).commit();
        }
    }



    protected void onSaveInstanceState(Bundle outState) {

//        outState.putSerializable("opponentsFragment", opponentsFragment);
//        outState.putSerializable("conversationFragment", conversationFragment);
//        outState.putSerializable("incomeCallFragment", incomeCallFragment);
        Log.d("Track", "onSaveInstanceState from NewDialogActivity Level 2");
        super.onSaveInstanceState(outState);

    }

    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Log.d("Track", "onRestoreInstanceState from NewDialogActivity Level 2");
//        opponentsFragment = (OpponentsFragment) savedInstanceState.getSerializable("opponentsFragment");
//        conversationFragment = (ConversationFragment) savedInstanceState.getSerializable("conversationFragment");
//        incomeCallFragment = (IncomeCallFragment) savedInstanceState.getSerializable("incomeCallFragment");

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
        startIncomeCallFragment(session);
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
        videoTrack.addRenderer(new VideoRenderer(LOCAL_RENDERER));
//        videoTrack.addRenderer(new VideoRenderer(new VideoCallBacks(videoView, QBGLVideoView.Endpoint.LOCAL)));
//        videoView.setVideoTrack(videoTrack, QBGLVideoView.Endpoint.LOCAL);
    }

    @Override
    public void onRemoteVideoTrackReceive(QBRTCSession session, QBRTCVideoTrack videoTrack, Integer userID) {
        VideoCallBacks videoCallBacks = new VideoCallBacks(videoView, QBGLVideoView.Endpoint.REMOTE);
//        videoCallBacks.setSize(200, 300);
//        VideoRenderer remouteRenderer = new VideoRenderer(videoCallBacks);
//        videoTrack.addRenderer(remouteRenderer);
        videoTrack.addRenderer(new VideoRenderer(REMOTE_RENDERER));
        videoTrackList.put(userID, videoTrack);
    }

    @Override
    public void onSessionEnd(QBRTCSession session) {

    }

    @Override
    public void onConnectedToUser(QBRTCSession session, Integer userID) {
        startTimer();


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

    public void removeConversationFragment(){
        FragmentManager fragmentManager = getFragmentManager();
        Fragment fragment = fragmentManager.findFragmentByTag(CONVERSATION_CALL_FRAGMENT);

        if (fragment != null){
            fragmentManager.beginTransaction().remove(fragment).commit();
        }
    }

    private void startIncomeCallFragment(QBRTCSession session) {
        Fragment fragment = new IncomeCallFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("sessionDescription", session.getSessionDescription());
        bundle.putIntegerArrayList("opponents", new ArrayList<Integer>(session.getOpponents()));
        bundle.putBoolean("marker", false);
        fragment.setArguments(bundle);
        getFragmentManager().beginTransaction().add(R.id.fragment_container, fragment, INCOME_CALL_FRAGMENT).commit();
    }

    public void addCanversationFragmentOnSession(String sessionID,
                                                 StartConversetionReason conversetionReason) {
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
            getFragmentManager().beginTransaction().add(R.id.fragment_container, fragment).commit();
        }
    }
//  Done
    public void startCanversationFragmentWithParameters(List<Integer> opponents,
                                                        QBRTCTypes.QBConferenceType qbConferenceType,
                                                        Map<String, String> userInfo,   StartConversetionReason conversetionReason) {

        QBRTCClient.init(this);
        QBRTCClient.getInstance().addCallback(this);

        ConversationFragment fragment = new ConversationFragment();
        Bundle bundle = new Bundle();
        bundle.putIntegerArrayList(ApplicationSingleton.OPPONENTS,
                new ArrayList<Integer>(opponents));
        bundle.putInt(ApplicationSingleton.CONFERENCE_TYPE, qbConferenceType.getValue());
        bundle.putInt(START_CONVERSATION_REASON, conversetionReason.ordinal());

        for (String key : userInfo.keySet()){
            bundle.putString("UserInfo:" + key, userInfo.get(key));
        }
        fragment.setArguments(bundle);

        getFragmentManager().beginTransaction().add(R.id.fragment_container, fragment).commit();

    }



    public QBRTCSession getCurrentSession() {
        return sessionList.get(currentSession);
    }

    public Map<String,QBRTCSession> getSessions() {
        return sessionList;
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
            getFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).commit();
//            LogginedUserABActivity.init
        }
    }

    public void setCurrentVideoView(GLSurfaceView videoView) {
        VideoRendererGui.ScalingType scaleType = VideoRendererGui.ScalingType.SCALE_ASPECT_FILL;

        REMOTE_RENDERER = VideoRendererGui.create(0,0,100,100,scaleType,true);
        LOCAL_RENDERER = VideoRendererGui.create(70,0,30,30,scaleType,true);


    }

    public void startTimer () {
        View mCustomView = getLayoutInflater().inflate(R.layout.actionbar_with_timer, null, false);

        Chronometer timer = (Chronometer) mCustomView.findViewById(R.id.timerABWithTimer);

        timer.setBase(SystemClock.elapsedRealtime());
        timer.start();
    }



    public static enum StartConversetionReason {
        INCOME_CALL_FOR_ACCEPTION,
        OUTCOME_CALL_MADE;
    }
}

