package com.quickblox.sample.videochatwebrtcnew.activities;


import android.app.Fragment;


import android.app.FragmentManager;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBSignaling;
import com.quickblox.chat.QBVideoChatWebRTCSignalingManager;
import com.quickblox.chat.QBWebRTCSignaling;
import com.quickblox.chat.listeners.QBVideoChatSignalingManagerListener;
import com.quickblox.sample.videochatwebrtcnew.ApplicationSingleton;
import com.quickblox.sample.videochatwebrtcnew.R;
import com.quickblox.sample.videochatwebrtcnew.adapters.OpponentsAdapter;
import com.quickblox.sample.videochatwebrtcnew.fragments.ConversationFragment;
import com.quickblox.sample.videochatwebrtcnew.fragments.IncomeCallFragment;
import com.quickblox.sample.videochatwebrtcnew.fragments.OpponentsFragment;
import com.quickblox.sample.videochatwebrtcnew.helper.DataHolder;
import com.quickblox.users.model.QBUser;
import com.quickblox.videochat.webrtcnew.QBRTCClient;
import com.quickblox.videochat.webrtcnew.QBRTCSession;
import com.quickblox.videochat.webrtcnew.QBRTCTypes;
import com.quickblox.videochat.webrtcnew.callbacks.QBRTCClientCallback;
import com.quickblox.videochat.webrtcnew.view.QBGLVideoView;
import com.quickblox.videochat.webrtcnew.view.QBRTCVideoTrack;
import com.quickblox.videochat.webrtcnew.view.VideoCallBacks;

import org.jivesoftware.smack.SmackException;
import org.webrtc.VideoRenderer;
import org.webrtc.VideoRendererGui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by tereha on 16.02.15.
 */
public class CallActivity extends BaseLogginedUserActivity implements QBRTCClientCallback {


    public static final String OPPONENTS_CALL_FRAGMENT = "opponents_call_fragment";
    public static final String INCOME_CALL_FRAGMENT = "income_call_fragment";
    public static final String CONVERSATION_CALL_FRAGMENT = "conversation_call_fragment";
    private static final String TAG = "NewDialogActivity";
    public static final String CALLER_NAME = "caller_name";
    private static final java.lang.String ADD_OPPONENTS_FRAGMENT_HANDLER = "opponentHandlerTask";
    private static final long TIME_BEGORE_CLOSE_CONVERSATION_FRAGMENT = 3;
    private static VideoRenderer.Callbacks REMOTE_RENDERER;
    private static VideoRenderer.Callbacks LOCAL_RENDERER;

    private int selectedOpponentId;

//    private OpponentsFragment opponentsFragment = null;
//    private IncomeCallFragment incomeCallFragment = null;
//    private ConversationFragment conversationFragment = null;

    public static final String START_CONVERSATION_REASON = "start_conversation_reason";
    public static final String SESSION_ID = "sessionID";
    private QBRTCVideoTrack localVideoTrack;
    private String currentSession;
//    private CallManger callManger;
//    private VideoRenderer.Callbacks localRenderer;
//    private List<VideoRenderer.Callbacks> opponentRenderers = new LinkedList<>();
    private QBGLVideoView videoView;
    public static String login;
    public static Map<Integer, QBRTCVideoTrack> videoTrackList = new HashMap<>();
    private ArrayList<QBUser> opponentsList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(TAG, "Activity. Thread id: " + Thread.currentThread().getId());



        if (savedInstanceState != null) {
            // Restore value of members from saved state
//            opponentsFragment = (OpponentsFragment) getFragmentManager().getFragment(savedInstanceState, OPPONENTS_CALL_FRAGMENT);
//            conversationFragment = (ConversationFragment) savedInstanceState.getSerializable("conversationFragment");
//            incomeCallFragment = (IncomeCallFragment) savedInstanceState.getSerializable("incomeCallFragment");
            Log.d("Track", "onCreate() from NewDialogActivity Level 2");
        } else {

            // Probably initialize members with default values for a new instance
            login = getIntent().getStringExtra("login");


            QBChatService instance = QBChatService.getInstance();
            QBVideoChatWebRTCSignalingManager videoChatWebRTCSignalingManager = instance.getVideoChatWebRTCSignalingManager();
            videoChatWebRTCSignalingManager.addSignalingManagerListener(
                    new QBVideoChatSignalingManagerListener() {
                        @Override
                        public void signalingCreated(QBSignaling signaling, boolean createdLocally) {
                            if (!createdLocally) {
                                QBRTCClient.getInstance().setQBWebRTCSignaling((QBWebRTCSignaling) signaling);
                            }
                        }
                    });
            addOpponentsFragment();
            Log.d("Track", "onCreate() from NewDialogActivity Level 1");
        }

        // From hear we start listening income call
        if (!QBRTCClient.isInitiated()) {
            QBRTCClient.init(this);
        }
    }


    @Override
    protected void onStart() {
        super.onStart();

        // Add activity as callback to RTCClient
        if (QBRTCClient.isInitiated()) {
            QBRTCClient.getInstance().addCallback(this);
        }
    }

    public void addOpponentsFragmentWithDelay(){

        HandlerThread handlerThread = new HandlerThread(ADD_OPPONENTS_FRAGMENT_HANDLER);
        handlerThread.start();
        new Handler(handlerThread.getLooper()).postAtTime(new Runnable() {
            @Override
            public void run() {
                getFragmentManager().beginTransaction().replace(R.id.fragment_container, new OpponentsFragment(),OPPONENTS_CALL_FRAGMENT).commit();
            }
        }, SystemClock.uptimeMillis() + TimeUnit.SECONDS.toMillis(TIME_BEGORE_CLOSE_CONVERSATION_FRAGMENT));
    }

    public void addOpponentsFragment(){
                getFragmentManager().beginTransaction().replace(R.id.fragment_container, new OpponentsFragment(),OPPONENTS_CALL_FRAGMENT).commit();
    }




//    protected void onSaveInstanceState(Bundle outState) {

//        opponentsFragment  = (OpponentsFragment) getFragmentManager().findFragmentByTag(OPPONENTS_CALL_FRAGMENT);
//        getFragmentManager().putFragment(outState, OPPONENTS_CALL_FRAGMENT, opponentsFragment);
//        outState.putSerializable("conversationFragment", conversationFragment);
//        outState.putSerializable("incomeCallFragment", incomeCallFragment);
//        Log.d("Track", "onSaveInstanceState from NewDialogActivity Level 2");
//        super.onSaveInstanceState(outState);

//    }

//    protected void onRestoreInstanceState(Bundle savedInstanceState) {
//        super.onRestoreInstanceState(savedInstanceState);
//        Log.d("Track", "onRestoreInstanceState from NewDialogActivity Level 2");
//        opponentsFragment = (OpponentsFragment) savedInstanceState.getSerializable("opponentsFragment");
//        conversationFragment = (ConversationFragment) savedInstanceState.getSerializable("conversationFragment");
//        incomeCallFragment = (IncomeCallFragment) savedInstanceState.getSerializable("incomeCallFragment");

//    }

    public QBRTCSession getCurrentSession() {
        return QBRTCClient.getInstance().getSessions().get(currentSession);
    }

    public void setCurrentSession(QBRTCSession session) {
        if(!QBRTCClient.getInstance().getSessions().containsKey(session.getSessionID())) {
            addSession(session);
        }
        currentSession = session.getSessionID();
    }

    public QBRTCSession getSession(String sessionID) {
        return QBRTCClient.getInstance().getSessions().get(sessionID);
    }

    public void setVideoViewVisibility(int visibility){
        videoView.setVisibility(visibility);
    }

    public void addSession(QBRTCSession session){
        QBRTCClient.getInstance().getSessions().put(session.getSessionID(), session);
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
    public void onReceiveNewSession(QBRTCSession session) {
//        Toast.makeText(this, "IncomeCall", Toast.LENGTH_SHORT).show();

        if (currentSession == null) {
            Log.d(TAG, "Start new session");
            setCurrentSession(session);

            Log.d(TAG, "Income call");
            QBRTCClient.getInstance().getSessions().put(session.getSessionID(), session);
            addIncomeCallFragment(session);
        } else {
            Log.d(TAG, "Stop new session. Device now is busy");
            session.rejectCall(null);
        }

    }

    @Override
    public void onUserNotAnswer(QBRTCSession session, Integer userID) {
        setStateTitle(userID, R.string.notAnswer, View.VISIBLE);
//        addOpponentsFragmentWithDelay();
    }

    @Override
    public void onStartConnectToUser(QBRTCSession session, Integer userID) {
        setStateTitle(userID , R.string.checking, View.VISIBLE);
    }

    @Override
    public void onCallRejectByUser(QBRTCSession session, Integer userID, Map<String, String> userInfo) {
        setStateTitle(userID , R.string.rejected, View.INVISIBLE);
    }

    @Override
    public void onLocalVideoTrackReceive(QBRTCSession session, QBRTCVideoTrack videoTrack) {
        this.localVideoTrack = videoTrack;
        videoTrack.addRenderer(new VideoRenderer(LOCAL_RENDERER));
//        videoTrack.addRenderer(new VideoRenderer(new VideoCallBacks(videoView, QBGLVideoView.Endpoint.LOCAL)));
//        videoView.setVideoTrack(videoTrack, QBGLVideoView.Endpoint.LOCAL);
        Log.d("Track", "onLocalVideoTrackReceive() is raned");
    }

    @Override
    public void onRemoteVideoTrackReceive(QBRTCSession session, QBRTCVideoTrack videoTrack, Integer userID) {
        VideoCallBacks videoCallBacks = new VideoCallBacks(videoView, QBGLVideoView.Endpoint.REMOTE);
//        videoCallBacks.setSize(200, 300);
//        VideoRenderer remouteRenderer = new VideoRenderer(videoCallBacks);
//        videoTrack.addRenderer(remouteRenderer);
        videoTrack.addRenderer(new VideoRenderer(REMOTE_RENDERER));
        videoTrackList.put(userID, videoTrack);
        Log.d("Track", "onRemoteVideoTrackReceive() is raned");
    }

    @Override
    public void onConnectionClosedForUser(QBRTCSession session, Integer userID) {
        setStateTitle(userID, R.string.closed, View.INVISIBLE);
        }

    @Override
    public void onConnectedToUser(QBRTCSession session, Integer userID) {
        startTimer();

        setStateTitle(userID,R.string.connected, View.INVISIBLE);

        ConversationFragment conversFragment = (ConversationFragment)getFragmentManager().findFragmentByTag(CONVERSATION_CALL_FRAGMENT);
        if(conversFragment != null){
            conversFragment.setActionVideoButtonsLayoutVisibility(true);
        }
        Log.d("Track", "onConnectedToUser() is started");
    }

    @Override
    public void onDisconnectedTimeoutFromUser(QBRTCSession session, Integer userID) {
        setStateTitle(userID,R.string.time_out, View.INVISIBLE);
    }

    @Override
    public void onConnectionFaildWithUser(QBRTCSession session, Integer userID) {
        setStateTitle(userID,R.string.failed, View.INVISIBLE);
    }

    @Override
    public void onSessionClosed(QBRTCSession session) {
        if (session.getSessionID().equals(currentSession)) {
            Log.d(TAG, "Stop session");
            if (session.getState().ordinal() > QBRTCSession.QBRTCSessionState.QB_RTC_SESSION_REJECTED.ordinal()) {
                addOpponentsFragmentWithDelay();
            } else {
                Log.d(TAG, "Can't hangup session with status -->" + session.getState().name());
            }
            // Remove current session
            Log.d(TAG, "Remove current session");
            currentSession = null;
        }
    }

    @Override
    public void onSessionStartClose(QBRTCSession session) {
        Log.d(TAG, "Start stopping session");
    }

    @Override
    public void onDisconnectedFromUser(QBRTCSession session, Integer userID) {
        setStateTitle(userID , R.string.disconnected, View.INVISIBLE);
    }

    private void setStateTitle(Integer userID, int stringID, int progressBarVisibility) {
        Log.d(TAG, "Start Change opponent state");
        View opponentItemView = findViewById(userID);
        if(opponentItemView != null) {
            TextView connectionStatus = (TextView) opponentItemView.findViewById(R.id.connectionStatus);
            connectionStatus.setText(getString(stringID));

            ProgressBar connectionStatusPB = (ProgressBar)opponentItemView.findViewById(R.id.connectionStatusPB);
            connectionStatusPB.setVisibility(progressBarVisibility);
            Log.d(TAG, "Opponent state changed to " + getString(stringID));
        }
    }

    @Override
    public void onReceiveHangUpFromUser(QBRTCSession session, Integer userID) {

        // TODO update view of this user

        setStateTitle(userID , R.string.hangUp, View.INVISIBLE);

        Log.d(TAG, "CHECK SESSION STATE");
        for (String key : QBRTCClient.getInstance().getSessions().keySet()){
            Log.d(TAG, QBRTCClient.getInstance().getSessions().get(key).toString());
        }

////        Toast.makeText(this, "User with ID:" + userID + "disconnected", Toast.LENGTH_SHORT).show();
//        if (session.getState().ordinal() < QBRTCSession.QBRTCSessionState.QB_RTC_SESSION_REJECTED.ordinal()){
//            addOpponentsFragmentWithDelay();
//        } else {
//            Log.d(TAG, "Can't hangup session with status -->" + session.getState().name());
//        }

        for (String key : QBRTCClient.getInstance().getSessions().keySet()){
            Log.d(TAG, QBRTCClient.getInstance().getSessions().get(key).toString());
        }

    }

//    private void removeUserWithID(Integer userID) {
//        QBRTCSession session = getCurrentSession();
//        if (session != null) {
//            session.removeUser(userID, new HashMap<String, String>());
//        }
//    }


    public void removeIncomeCallFragment() {
        FragmentManager fragmentManager = getFragmentManager();
        Fragment fragment = fragmentManager.findFragmentByTag(INCOME_CALL_FRAGMENT);

        if (fragment != null){
            fragmentManager.beginTransaction().remove(fragment).commit();
        }
    }


    private void addIncomeCallFragment(QBRTCSession session) {
        Fragment fragment = new IncomeCallFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("sessionDescription", session.getSessionDescription());
        bundle.putIntegerArrayList("opponents", new ArrayList<Integer>(session.getOpponents()));
        bundle.putInt(ApplicationSingleton.CONFERENCE_TYPE, session.getConferenceType().getValue());
        fragment.setArguments(bundle);
        getFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment, INCOME_CALL_FRAGMENT).commit();
    }



    public void addConversationFragmentStartCall(List<Integer> opponents,
                                                 QBRTCTypes.QBConferenceType qbConferenceType,
                                                 Map<String, String> userInfo) {

        // init session for new call
        try {
            QBRTCSession newSessionWithOpponents = QBRTCClient.getInstance().createNewSessionWithOpponents(opponents, qbConferenceType, userInfo);
            setCurrentSession(newSessionWithOpponents);

            ConversationFragment fragment = new ConversationFragment();
            Bundle bundle = new Bundle();
            bundle.putIntegerArrayList(ApplicationSingleton.OPPONENTS,
                    new ArrayList<Integer>(opponents));
            bundle.putInt(ApplicationSingleton.CONFERENCE_TYPE, qbConferenceType.getValue());
            bundle.putInt(START_CONVERSATION_REASON, StartConversetionReason.OUTCOME_CALL_MADE.ordinal());
            bundle.putString(CALLER_NAME, DataHolder.getUserNameByID(opponents.get(0)));

            for (String key : userInfo.keySet()) {
                bundle.putString("UserInfo:" + key, userInfo.get(key));
            }
            fragment.setArguments(bundle);

            getFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment, CONVERSATION_CALL_FRAGMENT).commit();
        } catch (IllegalStateException e){
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }

    }

    public void addConversationFragmentReceiveCall(String sessionID) {

        // set current session
        setCurrentSesionId(sessionID);
        QBRTCSession session = getCurrentSession();
        Integer myId = QBChatService.getInstance().getUser().getId();
        ArrayList<Integer> opponentsWithoutMe = new ArrayList<>(session.getOpponents());
        opponentsWithoutMe.remove(new Integer(myId));
        opponentsWithoutMe.add(session.getCallerID());

        // init conversation fragment
            ConversationFragment fragment = new ConversationFragment();
            Bundle bundle = new Bundle();
            bundle.putIntegerArrayList(ApplicationSingleton.OPPONENTS,
                    opponentsWithoutMe);
            bundle.putInt(ApplicationSingleton.CONFERENCE_TYPE, session.getConferenceType().getValue());
            bundle.putInt(START_CONVERSATION_REASON, StartConversetionReason.INCOME_CALL_FOR_ACCEPTION.ordinal());
            bundle.putString(SESSION_ID, sessionID);
            bundle.putString(CALLER_NAME, DataHolder.getUserNameByID(session.getCallerID()));

            if(session.getUserInfo() != null) {
                for (String key : session.getUserInfo().keySet()) {
                    bundle.putString("UserInfo:" + key, session.getUserInfo().get(key));
                }
            }
            fragment.setArguments(bundle);

            // Start conversation fragment
            getFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment, CONVERSATION_CALL_FRAGMENT).commit();
    }




    public void setCurrentVideoView(GLSurfaceView videoView) {
        VideoRendererGui.ScalingType scaleType = VideoRendererGui.ScalingType.SCALE_ASPECT_FILL;

        REMOTE_RENDERER = VideoRendererGui.create(0,0,100,100,scaleType,true);
        LOCAL_RENDERER = VideoRendererGui.create(70,0,30,30,scaleType,true);


    }

    public void startTimer () {
        super.startTimer();
    }

    public void setOpponentsList(ArrayList<QBUser> qbUsers) {
        this.opponentsList = qbUsers;
    }

    public ArrayList<QBUser> getOpponentsList() {
        return opponentsList;
    }

    public static enum StartConversetionReason {
        INCOME_CALL_FOR_ACCEPTION,
        OUTCOME_CALL_MADE;
    }

    @Override
    public void onBackPressed() {
        // Logout on back btn click
        Fragment fragment = getFragmentManager().findFragmentByTag(CONVERSATION_CALL_FRAGMENT);
        if(fragment == null) {
            super.onBackPressed();
            if (QBChatService.isInitialized()) {
                try {
                    QBChatService.getInstance().logout();
                } catch (SmackException.NotConnectedException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        opponentsList = null;
        OpponentsAdapter.i = 0;

        // Remove activity as callback to RTCClient
//        if (QBRTCClient.isInitiated()) {
//            try {
//                QBChatService.getInstance().logout();
//            } catch (SmackException.NotConnectedException e) {
//                e.printStackTrace();
//            }
//            QBRTCClient.getInstance().removeCallback(this);
//            QBChatService.getInstance().destroy();
//        }
    }

}

