package com.quickblox.sample.videochatwebrtcnew.activities;


import android.annotation.TargetApi;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBSignaling;
import com.quickblox.chat.QBWebRTCSignaling;
import com.quickblox.chat.listeners.QBVideoChatSignalingManagerListener;
import com.quickblox.sample.videochatwebrtcnew.ApplicationSingleton;
import com.quickblox.sample.videochatwebrtcnew.R;
import com.quickblox.sample.videochatwebrtcnew.adapters.OpponentsAdapter;
import com.quickblox.sample.videochatwebrtcnew.definitions.Consts;
import com.quickblox.sample.videochatwebrtcnew.fragments.ConversationFragment;
import com.quickblox.sample.videochatwebrtcnew.fragments.IncomeCallFragment;
import com.quickblox.sample.videochatwebrtcnew.fragments.OpponentsFragment;
import com.quickblox.sample.videochatwebrtcnew.holder.DataHolder;
import com.quickblox.users.model.QBUser;
import com.quickblox.videochat.webrtc.QBRTCClient;
import com.quickblox.videochat.webrtc.QBRTCConfig;
import com.quickblox.videochat.webrtc.QBRTCException;
import com.quickblox.videochat.webrtc.QBRTCSession;
import com.quickblox.videochat.webrtc.QBRTCTypes;
import com.quickblox.videochat.webrtc.callbacks.QBRTCClientConnectionCallbacks;
import com.quickblox.videochat.webrtc.callbacks.QBRTCClientSessionCallbacks;
import com.quickblox.videochat.webrtc.callbacks.QBRTCClientVideoTracksCallbacks;
import com.quickblox.videochat.webrtc.view.QBGLVideoView;
import com.quickblox.videochat.webrtc.view.QBRTCVideoTrack;
import com.quickblox.videochat.webrtc.view.VideoCallBacks;

import org.jivesoftware.smack.SmackException;
import org.webrtc.VideoCapturerAndroid;
import org.webrtc.VideoRenderer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 *
 * Created by tereha on 16.02.15.
 *
 */
public class CallActivity extends BaseLogginedUserActivity implements QBRTCClientSessionCallbacks, QBRTCClientConnectionCallbacks, QBRTCClientVideoTracksCallbacks {


    private static final String TAG = CallActivity.class.getSimpleName();
    private static final String ADD_OPPONENTS_FRAGMENT_HANDLER = "opponentHandlerTask";
    private static final long TIME_BEGORE_CLOSE_CONVERSATION_FRAGMENT = 3;
    private static final String INCOME_WINDOW_SHOW_TASK_THREAD = "INCOME_WINDOW_SHOW";
    public static final String OPPONENTS_CALL_FRAGMENT = "opponents_call_fragment";
    public static final String INCOME_CALL_FRAGMENT = "income_call_fragment";
    public static final String CONVERSATION_CALL_FRAGMENT = "conversation_call_fragment";
    public static final String CALLER_NAME = "caller_name";
    public static final String SESSION_ID = "sessionID";
    public static final String START_CONVERSATION_REASON = "start_conversation_reason";


    private QBRTCSession currentSession;
    public static String login;
    public static ArrayList<QBUser> opponentsList;

    private Runnable showIncomingCallWindowTask;
    private Handler showIncomingCallWindowTaskHandler;
    private BroadcastReceiver wifiStateReceiver;
    private boolean closeByWifiStateAllow = true;
    private String hangUpReason;
    private boolean isInCommingCall;
    private QBGLVideoView localVideoVidew;
    private QBGLVideoView remoteVideoView;
//    private boolean isLastConnectionStateEnabled;
    private boolean isInFront;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Log.d(TAG, "Activity. Thread id: " + Thread.currentThread().getId());

        // Probably initialize members with default values for a new instance
        login = getIntent().getStringExtra("login");

        if (savedInstanceState == null) {
            addOpponentsFragment();
        }

        initQBRTCClient();
        initWiFiManagerListener();
    }

    private void initQBRTCClient() {
        // Add signalling manager
        QBChatService.getInstance().getVideoChatWebRTCSignalingManager().addSignalingManagerListener(new QBVideoChatSignalingManagerListener() {
            @Override
            public void signalingCreated(QBSignaling qbSignaling, boolean createdLocally) {
                if (!createdLocally) {
                    QBRTCClient.getInstance().addSignaling((QBWebRTCSignaling) qbSignaling);
                }
            }
        });

        QBRTCClient.getInstance().setCameraErrorHendler(new VideoCapturerAndroid.CameraErrorHandler() {
            @Override
            public void onCameraError(final String s) {
                CallActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(CallActivity.this, s, Toast.LENGTH_LONG).show();
                    }
                });
            }
        });

        // Add activity as callback to RTCClient
        QBRTCClient.getInstance().addSessionCallbacksListener(this);
        QBRTCClient.getInstance().addConnectionCallbacksListener(this);
        QBRTCClient.getInstance().addVideoTrackCallbacksListener(this);

        // Start mange QBRTCSessions according to VideoCall parser's callbacks
        QBRTCClient.getInstance().prepareToProcessCalls(this);
    }

    private void initWiFiManagerListener() {
        wifiStateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(TAG, "WIFI was changed");
                processCurrentWifiState(context);
            }
        };
    }

    private void processCurrentWifiState(Context context) {
        WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (!wifi.isWifiEnabled()) {
//            isLastConnectionStateEnabled = false;
            Log.d(TAG, "WIFI is turned off");
            if (closeByWifiStateAllow) {
                if (currentSession != null) {
                    Log.d(TAG, "currentSession NOT null");
                    // Close session safely
                    disableConversationFragmentButtons();
                    stopConversationFragmentBeeps();

                    hangUpCurrentSession();

                    hangUpReason = Consts.WIFI_DISABLED;
                } else {
                    Log.d(TAG, "Call finish() on activity");
                    finish();
                }
            } else {
                Log.d(TAG, "WIFI is turned on");
                showToast(R.string.NETWORK_ABSENT);
            }
        } else {
//            isLastConnectionStateEnabled = true;
        }
    }

    private void stopConversationFragmentBeeps() {
        ConversationFragment fragment = (ConversationFragment) getFragmentManager().findFragmentByTag(CONVERSATION_CALL_FRAGMENT);
        if (fragment != null) {
            fragment.stopOutBeep();
        }
    }

    private void disableConversationFragmentButtons() {
        ConversationFragment fragment = (ConversationFragment) getFragmentManager().findFragmentByTag(CONVERSATION_CALL_FRAGMENT);
        if (fragment != null) {
            fragment.actionButtonsEnabled(false);
        }
    }


    private void initIncommingCallTask() {
        showIncomingCallWindowTaskHandler = new Handler(Looper.myLooper());
        showIncomingCallWindowTask = new Runnable() {
            @Override
            public void run() {
                IncomeCallFragment incomeCallFragment = (IncomeCallFragment) getFragmentManager().findFragmentByTag(INCOME_CALL_FRAGMENT);
                if (incomeCallFragment == null) {
                    ConversationFragment conversationFragment = (ConversationFragment) getFragmentManager().findFragmentByTag(CONVERSATION_CALL_FRAGMENT);
                    if (conversationFragment != null) {
                        disableConversationFragmentButtons();
                        stopConversationFragmentBeeps();
                        hangUpCurrentSession();
                    }
                } else {
                    rejectCurrentSession();
                }
                Toast.makeText(CallActivity.this, "Call was stopped by timer", Toast.LENGTH_LONG).show();
            }
        };
    }

    public void rejectCurrentSession() {
        if (getCurrentSession() != null) {
            getCurrentSession().rejectCall(new HashMap<String, String>());
        }
    }

    public void hangUpCurrentSession() {
        if (getCurrentSession() != null) {
            getCurrentSession().hangUp(new HashMap<String, String>());
        }
    }

    private void startIncomeCallTimer() {
        showIncomingCallWindowTaskHandler.postAtTime(showIncomingCallWindowTask, SystemClock.uptimeMillis() + TimeUnit.SECONDS.toMillis(QBRTCConfig.getAnswerTimeInterval()));
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void stopIncomeCallTimer() {
        Log.d(TAG, "stopIncomeCallTimer" );
//        Log.d(TAG, "showIncomingCallWindowTaskHandler is " + showIncomingCallWindowTaskHandler);
        showIncomingCallWindowTaskHandler.removeCallbacks(showIncomingCallWindowTask);
    }


    @Override
    protected void onStart() {
        super.onStart();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        registerReceiver(wifiStateReceiver, intentFilter);


    }

    @Override
    protected void onResume() {
        isInFront = true;

        if (currentSession == null){
            addOpponentsFragment();
        }
        super.onResume();
    }

    @Override
    protected void onPause() {
        isInFront = false;
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(wifiStateReceiver);
    }

    public QBRTCSession getCurrentSession() {
        return currentSession;
    }

    private void forbidenCloseByWifiState() {
        closeByWifiStateAllow = false;
    }


    public void setCurrentSession(QBRTCSession sesion) {
        Log.d("Crash", "setCurrentSession. Set session to " + sesion);
        this.currentSession = sesion;
    }

    // ---------------Chat callback methods implementation  ----------------------//

    @Override
    public void onReceiveNewSession(final QBRTCSession session) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                Log.d(TAG, "Session "+ session.getSessionID() + " are income");
                String curSession = (getCurrentSession() == null) ? null : getCurrentSession().getSessionID();
                Log.d(TAG, "Session " + curSession + " is current" );

                if (getCurrentSession() == null) {
                    Log.d(TAG, "Start new session");
                    Log.d(TAG, "Income call");

                    Log.d("Crash", "onReceiveNewSession. Set session to " + session);
                    setCurrentSession(session);
                    Log.d("Crash", "onReceiveNewSession. Set session to " + session);

                    addIncomeCallFragment(session);

                    isInCommingCall = true;
                    initIncommingCallTask();
                    startIncomeCallTimer();
                } else {
                    Log.d(TAG, "Stop new session. Device now is busy");
                    session.rejectCall(null);
                }

            }
        });
    }

    @Override
    public void onUserNotAnswer(QBRTCSession session, Integer userID) {
//        setStateTitle(userID, R.string.noAnswer, View.VISIBLE);
        showToast(R.string.noAnswer);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ConversationFragment fragment = (ConversationFragment) getFragmentManager().findFragmentByTag(CONVERSATION_CALL_FRAGMENT);
                if (fragment != null) {
                    fragment.actionButtonsEnabled(false);
                    fragment.stopOutBeep();
                }
            }
        });
    }

    @Override
    public void onStartConnectToUser(QBRTCSession session, Integer userID) {

//        setStateTitle(userID, R.string.checking, View.VISIBLE);
        showToast(R.string.checking);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ConversationFragment fragment = (ConversationFragment) getFragmentManager().findFragmentByTag(CONVERSATION_CALL_FRAGMENT);
                if (fragment != null) {
                    fragment.stopOutBeep();
                }
            }
        });
    }

    @Override
    public void onCallRejectByUser(QBRTCSession session, Integer userID, Map<String, String> userInfo) {
        showToast(R.string.rejected);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ConversationFragment fragment = (ConversationFragment) getFragmentManager().findFragmentByTag(CONVERSATION_CALL_FRAGMENT);
                if (fragment != null) {
                    fragment.stopOutBeep();
                }
            }
        });
    }

    @Override
    public void onLocalVideoTrackReceive(QBRTCSession session, QBRTCVideoTrack videoTrack) {
        localVideoVidew = (QBGLVideoView) findViewById(R.id.localVideoVidew);
        Log.d(TAG, "localVideoVidew is " + localVideoVidew);
        if (localVideoVidew != null) {
            videoTrack.addRenderer(new VideoRenderer(new VideoCallBacks(localVideoVidew, QBGLVideoView.Endpoint.LOCAL)));
            localVideoVidew.setVideoTrack(videoTrack, QBGLVideoView.Endpoint.LOCAL);
            Log.d(TAG, "onLocalVideoTrackReceive() is raned");
        }
    }

    @Override
    public void onRemoteVideoTrackReceive(QBRTCSession session, QBRTCVideoTrack videoTrack, Integer userID) {
        remoteVideoView = (QBGLVideoView) findViewById(R.id.remoteVideoView);
        Log.d(TAG, "remoteVideoView is " + remoteVideoView);
        if (remoteVideoView != null) {
            VideoRenderer remouteRenderer = new VideoRenderer(new VideoCallBacks(remoteVideoView, QBGLVideoView.Endpoint.REMOTE));
            videoTrack.addRenderer(remouteRenderer);
            remoteVideoView.setVideoTrack(videoTrack, QBGLVideoView.Endpoint.REMOTE);
            Log.d(TAG, "onRemoteVideoTrackReceive() is raned");
        }
    }

    @Override
    public void onConnectionClosedForUser(QBRTCSession session, Integer userID) {
        showToast(R.string.closed);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // Close app after session close of network was disabled
                Log.d(TAG, "onConnectionClosedForUser()");
                if (hangUpReason != null && hangUpReason.equals(Consts.WIFI_DISABLED)) {
                    Intent returnIntent = new Intent();
                    setResult(Consts.CALL_ACTIVITY_CLOSE_WIFI_DISABLED, returnIntent);
                    finish();
                }
            }
        });
    }

    @Override
    public void onConnectedToUser(QBRTCSession session, final Integer userID) {
        forbidenCloseByWifiState();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isInCommingCall) {
                    stopIncomeCallTimer();
                }

                startTimer();

                showToast(R.string.connected);

                Log.d(TAG, "onConnectedToUser() is started");

                ConversationFragment fragment = (ConversationFragment) getFragmentManager().findFragmentByTag(CONVERSATION_CALL_FRAGMENT);
                if (fragment != null) {
                    fragment.actionButtonsEnabled(true);
                }
            }
        });
    }


    @Override
    public void onDisconnectedTimeoutFromUser(QBRTCSession session, Integer userID) {
//        setStateTitle(userID, R.string.time_out, View.INVISIBLE);
//        if (isLastConnectionStateEnabled) {
//            showToast(R.string.NETWORK_ABSENT);
//        } else {
            showToast(R.string.time_out);
//        }
    }

    @Override
    public void onConnectionFailedWithUser(QBRTCSession session, Integer userID) {
//        setStateTitle(userID, R.string.failed, View.INVISIBLE);
        showToast(R.string.failed);
    }

    @Override
    public void onError(QBRTCSession qbrtcSession, QBRTCException e) {
//        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSessionClosed(final QBRTCSession session) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                Log.d(TAG, "Session " + session.getSessionID() + " start stop session");
                String curSession = (getCurrentSession() == null) ? null : getCurrentSession().getSessionID();
                Log.d(TAG, "Session " + curSession + " is current" );

                if (session.equals(getCurrentSession())) {

                    if (isInCommingCall) {
                        stopIncomeCallTimer();
                    }

                    Log.d(TAG, "Stop session");
//                    addOpponentsFragmentWithDelay();
                    addOpponentsFragment();

                    // Remove current session
                    Log.d(TAG, "Remove current session");
                    Log.d("Crash", "onSessionClosed. Set session to null");
                    currentSession = null;

                    stopTimer();
                    closeByWifiStateAllow = true;
                    processCurrentWifiState(CallActivity.this);
                }
            }
        });
    }

    @Override
    public void onSessionStartClose(final QBRTCSession session) {
        Log.d(TAG, "Start stopping session");

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ConversationFragment fragment = (ConversationFragment) getFragmentManager().findFragmentByTag(CONVERSATION_CALL_FRAGMENT);
                if (fragment != null && session.equals(getCurrentSession())) {
                    fragment.actionButtonsEnabled(false);
                }
            }
        });
    }

    @Override
    public void onDisconnectedFromUser(QBRTCSession session, Integer userID) {
//        setStateTitle(userID, R.string.disconnected, View.INVISIBLE);
        showToast(R.string.disconnected);
    }

    private void showToast(final int message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(CallActivity.this, getString(message), Toast.LENGTH_LONG).show();
            }
        });
    }

//    private void setStateTitle(final Integer userID, final int stringID, final int progressBarVisibility) {
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                View opponentItemView = findViewById(userID);
//                if (opponentItemView != null) {
//                    TextView connectionStatus = (TextView) opponentItemView.findViewById(R.id.connectionStatus);
//                    connectionStatus.setText(getString(stringID));
//
//                    ProgressBar connectionStatusPB = (ProgressBar) opponentItemView.findViewById(R.id.connectionStatusPB);
//                    connectionStatusPB.setVisibility(progressBarVisibility);
//                    Log.d(TAG, "Opponent state changed to " + getString(stringID));
//                }
//            }
//        });
//    }

    @Override
    public void onReceiveHangUpFromUser(QBRTCSession session, final Integer userID) {
        if (session.equals(getCurrentSession())) {

            // TODO update view of this user
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
//                setStateTitle(userID, R.string.hungUp, View.INVISIBLE);
                    showToast(R.string.hungUp);
                }
            });
        }
    }


    public void addOpponentsFragmentWithDelay() {
        HandlerThread handlerThread = new HandlerThread(ADD_OPPONENTS_FRAGMENT_HANDLER);
        handlerThread.start();
        new Handler(handlerThread.getLooper()).postAtTime(new Runnable() {
            @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
            @Override
            public void run() {
                if (isInFront) {
                    getFragmentManager().beginTransaction().replace(R.id.fragment_container, new OpponentsFragment(), OPPONENTS_CALL_FRAGMENT).commit();
                    currentSession = null;
                }
            }
        }, SystemClock.uptimeMillis() + TimeUnit.SECONDS.toMillis(TIME_BEGORE_CLOSE_CONVERSATION_FRAGMENT));
    }

    public void addOpponentsFragment() {
        if (isInFront) {
            getFragmentManager().beginTransaction().replace(R.id.fragment_container, new OpponentsFragment(), OPPONENTS_CALL_FRAGMENT).commit();
        }
    }


    public void removeIncomeCallFragment() {
        FragmentManager fragmentManager = getFragmentManager();
        Fragment fragment = fragmentManager.findFragmentByTag(INCOME_CALL_FRAGMENT);

        if (fragment != null) {
            fragmentManager.beginTransaction().remove(fragment).commit();
        }
    }


    private void addIncomeCallFragment(QBRTCSession session) {

        Log.d(TAG, "QBRTCSession in addIncomeCallFragment is " + session);
        if(session != null && isInFront) {
            Fragment fragment = new IncomeCallFragment();
            Bundle bundle = new Bundle();
            bundle.putSerializable("sessionDescription", session.getSessionDescription());
            bundle.putIntegerArrayList("opponents", new ArrayList<>(session.getOpponents()));
            bundle.putInt(ApplicationSingleton.CONFERENCE_TYPE, session.getConferenceType().getValue());
            fragment.setArguments(bundle);
            getFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment, INCOME_CALL_FRAGMENT).commit();
        } else {
            Log.d(TAG, "SKIP addIncomeCallFragment method");
        }
    }


    public void addConversationFragmentStartCall(List<Integer> opponents,
                                                 QBRTCTypes.QBConferenceType qbConferenceType,
                                                 Map<String, String> userInfo) {

        // init session for new call
        try {
            QBRTCSession newSessionWithOpponents = QBRTCClient.getInstance().createNewSessionWithOpponents(opponents, qbConferenceType);
            Log.d("Crash", "addConversationFragmentStartCall. Set session " + newSessionWithOpponents);
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

        } catch (IllegalStateException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }


    public void addConversationFragmentReceiveCall() {

        QBRTCSession session = getCurrentSession();

        if (getCurrentSession() != null) {
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
            bundle.putString(SESSION_ID, session.getSessionID());
            bundle.putString(CALLER_NAME, DataHolder.getUserNameByID(session.getCallerID()));

            if (session.getUserInfo() != null) {
                for (String key : session.getUserInfo().keySet()) {
                    bundle.putString("UserInfo:" + key, session.getUserInfo().get(key));
                }
            }

            fragment.setArguments(bundle);

            // Start conversation fragment
            getFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment, CONVERSATION_CALL_FRAGMENT).commit();
        }
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
        if (fragment == null) {
            super.onBackPressed();
            if (QBChatService.isInitialized()) {
                try {
                    QBRTCClient.getInstance().close(true);
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
    }
}

