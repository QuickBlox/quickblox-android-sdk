package com.quickblox.sample.groupchatwebrtc.activities;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBSignaling;
import com.quickblox.chat.QBWebRTCSignaling;
import com.quickblox.chat.listeners.QBVideoChatSignalingManagerListener;
import com.quickblox.sample.core.utils.Toaster;
import com.quickblox.sample.groupchatwebrtc.R;
import com.quickblox.sample.groupchatwebrtc.adapters.OpponentsAdapter;
import com.quickblox.sample.groupchatwebrtc.fragments.ConversationFragment;
import com.quickblox.sample.groupchatwebrtc.fragments.IncomeCallFragment;
import com.quickblox.sample.groupchatwebrtc.fragments.OnCallSettingsController;
import com.quickblox.sample.groupchatwebrtc.fragments.OpponentsFragment;
import com.quickblox.sample.groupchatwebrtc.fragments.SessionController;
import com.quickblox.sample.groupchatwebrtc.holder.DataHolder;
import com.quickblox.sample.groupchatwebrtc.utils.ChatPingAlarmManager;
import com.quickblox.sample.groupchatwebrtc.utils.Consts;
import com.quickblox.sample.groupchatwebrtc.utils.FragmentExecuotr;
import com.quickblox.sample.groupchatwebrtc.utils.NetworkConnectionChecker;
import com.quickblox.sample.groupchatwebrtc.utils.RingtonePlayer;
import com.quickblox.sample.groupchatwebrtc.utils.SettingsUtil;
import com.quickblox.users.model.QBUser;
import com.quickblox.videochat.webrtc.AppRTCAudioManager;
import com.quickblox.videochat.webrtc.AppRTCAudioManager.AudioDevice;
import com.quickblox.videochat.webrtc.QBRTCClient;
import com.quickblox.videochat.webrtc.QBRTCConfig;
import com.quickblox.videochat.webrtc.QBRTCSession;
import com.quickblox.videochat.webrtc.QBRTCTypes;
import com.quickblox.videochat.webrtc.QBSignalingSpec;
import com.quickblox.videochat.webrtc.callbacks.QBRTCClientSessionCallbacks;
import com.quickblox.videochat.webrtc.callbacks.QBRTCClientVideoTracksCallbacks;
import com.quickblox.videochat.webrtc.callbacks.QBRTCSessionConnectionCallbacks;
import com.quickblox.videochat.webrtc.callbacks.QBRTCSignalingCallback;
import com.quickblox.videochat.webrtc.callbacks.QBRTCStatsReportCallback;
import com.quickblox.videochat.webrtc.exception.QBRTCException;
import com.quickblox.videochat.webrtc.exception.QBRTCSignalException;

import org.jivesoftware.smack.AbstractConnectionListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smackx.ping.PingFailedListener;
import org.webrtc.RendererCommon;
import org.webrtc.VideoCapturerAndroid;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * QuickBlox team
 */
public class CallActivity extends BaseLogginedUserActivity implements QBRTCClientSessionCallbacks,
        QBRTCSessionConnectionCallbacks, QBRTCSignalingCallback,
        SessionController, OnCallSettingsController,
        NetworkConnectionChecker.OnConnectivityChangedListener {

    private static final String TAG = CallActivity.class.getSimpleName();

    public static final String OPPONENTS_CALL_FRAGMENT = "opponents_call_fragment";
    public static final String INCOME_CALL_FRAGMENT = "income_call_fragment";
    public static final String CONVERSATION_CALL_FRAGMENT = "conversation_call_fragment";
    public static final String CALLER_NAME = "caller_name";
    public static final String SESSION_ID = "sessionID";
    public static final String START_CONVERSATION_REASON = "start_conversation_reason";


    private QBRTCSession currentSession;
    public List<QBUser> opponentsList;
    private Runnable showIncomingCallWindowTask;
    private Handler showIncomingCallWindowTaskHandler;
    private BroadcastReceiver wifiStateReceiver;
    private boolean closeByWifiStateAllow = true;
    private String hangUpReason;
    private boolean isInCommingCall;
    private QBRTCClient rtcClient;
    private QBRTCSessionUserCallback sessionUserCallback;
    private boolean wifiEnabled = true;
    private SharedPreferences sharedPref;
    private RingtonePlayer ringtonePlayer;
    private LinearLayout connectionView;
    private AppRTCAudioManager audioManager;
    private NetworkConnectionChecker networkConnectionChecker;

    private AudioStateCallback audioStateCallback;

    public static void start(Activity activity, String login) {
        Intent intent = new Intent(activity, CallActivity.class);
        intent.putExtra("login", login);
        activity.startActivityForResult(intent, Consts.CALL_ACTIVITY_CLOSE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_main);
        opponentsList = DataHolder.getUsers();

        Log.d(TAG, "Activity. Thread id: " + Thread.currentThread().getId());

        if (!(isLoggedIn() && QBChatService.getInstance().isLoggedIn())) {
            Log.d(TAG, "User has logged out!");
            Log.d(TAG, "Pop up back stack!");
            getFragmentManager().popBackStackImmediate(0, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            ListUsersActivity.start(this);
            finish();
            return;
        }
        Log.d(TAG, "User  logged in!");
        if (savedInstanceState == null) {
            addOpponentsFragment();
        }

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        initQBRTCClient();
        initWiFiManagerListener();
        ///initPingListener();
        initAudioManager();
        ringtonePlayer = new RingtonePlayer(this, R.raw.beep);
        connectionView = (LinearLayout) View.inflate(this, R.layout.connection_popup, null);
    }

    public boolean isLoggedIn() {
        return DataHolder.getLoggedUser() != null;
    }

    private void initAudioManager() {
        audioManager = AppRTCAudioManager.create(this,  new AppRTCAudioManager.OnAudioManagerStateListener() {
            @Override
            public void onAudioChangedState(AppRTCAudioManager.AudioDevice audioDevice) {
                Toaster.longToast( "Audio device swicthed to  " + audioDevice);
            }
        });
        audioManager.setDefaultAudioDevice(AudioDevice.EARPIECE);
        audioManager.setOnWiredHeadsetStateListener(new AppRTCAudioManager.OnWiredHeadsetStateListener() {
            @Override
            public void onWiredHeadsetStateChanged(boolean plugged, boolean hasMicrophone) {
                Toaster.longToast( "Headset " + (plugged ? "plugged" : "unplugged"));
                if (audioStateCallback != null){
                    audioStateCallback.onWiredHeadsetStateChanged(plugged);
                }
            }
        });
    }

    private void initQBRTCClient() {
        rtcClient = QBRTCClient.getInstance(this);
        // Add signalling manager
        QBChatService.getInstance().getVideoChatWebRTCSignalingManager().addSignalingManagerListener(new QBVideoChatSignalingManagerListener() {
            @Override
            public void signalingCreated(QBSignaling qbSignaling, boolean createdLocally) {
                if (!createdLocally) {
                    rtcClient.addSignaling((QBWebRTCSignaling) qbSignaling);
                }
            }
        });

        rtcClient.setCameraErrorHendler(new VideoCapturerAndroid.CameraEventsHandler() {
            @Override
            public void onCameraError(final String s) {

                showToast( "Camera error: " + s);
            }

            @Override
            public void onCameraFreezed(String s) {
                showToast( "Camera freezed: " + s);
                hangUpCurrentSession("camera freezed" + s);
            }

            @Override
            public void onCameraOpening(int i) {
                showToast( "Camera " + i + " opening");
            }

            @Override
            public void onFirstFrameAvailable() {
                showToast( "onFirstFrameAvailable");
            }

            @Override
            public void onCameraClosed() {
                showToast("onCameraClosed");
            }
        });


        // Configure
        //
        QBRTCConfig.setMaxOpponentsCount(6);
        QBRTCConfig.setDisconnectTime(30);
        QBRTCConfig.setAnswerTimeInterval(30l);
        QBRTCConfig.setStatsReportInterval(60);
        QBRTCConfig.setDebugEnabled(true);


        // Add activity as callback to RTCClient
        rtcClient.addSessionCallbacksListener(this);
        // Start mange QBRTCSessions according to VideoCall parser's callbacks
        rtcClient.prepareToProcessCalls();

        QBChatService.getInstance().addConnectionListener(new AbstractConnectionListener() {

            @Override
            public void connectionClosedOnError(Exception e) {
                showNotificationPopUp(R.string.connection_was_lost, true);
            }

            @Override
            public void reconnectionSuccessful() {
                showNotificationPopUp(R.string.connection_was_lost, false);
            }

            @Override
            public void reconnectingIn(int seconds) {
                Log.i(TAG, "reconnectingIn" + seconds);
            }
        });
    }

    @Override
    public void connectivityChanged(boolean availableNow) {
        showToast("Internet connection " + (availableNow ? "available" : " unavailable"));
    }

    private void showNotificationPopUp(final int text, final boolean show) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (show) {
                    ((TextView) connectionView.findViewById(R.id.notification)).setText(text);
                    if (connectionView.getParent() == null) {
                        ((ViewGroup) CallActivity.this.findViewById(R.id.fragment_container)).addView(connectionView);
                    }
                } else {
                    ((ViewGroup) CallActivity.this.findViewById(R.id.fragment_container)).removeView(connectionView);
                }
            }
        });

    }

    private void initWiFiManagerListener() {
        networkConnectionChecker = new NetworkConnectionChecker(getApplication());
    }

    private void initPingListener() {
        ChatPingAlarmManager.onCreate(this);
        ChatPingAlarmManager.getInstanceFor().addPingListener(new PingFailedListener() {
            @Override
            public void pingFailed() {
                showToast("Ping chat server failed");
            }
        });
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
                        ringtonePlayer.stop();
                        hangUpCurrentSession("  due to opponent has no action ");
                    }
                } else {
                    rejectCurrentSession(" opponent didn't answer");
                }
                Toaster.longToast( "Call was stopped by timer");
            }
        };
    }

    public void rejectCurrentSession(String rejectReason) {
        if (getCurrentSession() != null) {
            Map<String, String> infoMap = new HashMap<>();
            infoMap.put(Consts.REJECT_REASON, rejectReason);
            getCurrentSession().rejectCall(infoMap);
        }
    }

    @Override
    public void hangUpCurrentSession(String hangUpReason) {
        if (ringtonePlayer != null) {
            ringtonePlayer.stop();
        }
        if (getCurrentSession() != null) {
            Map<String, String> infoMap = new HashMap<>();
            infoMap.put(Consts.HANG_UP_REASON, hangUpReason);
            getCurrentSession().hangUp(infoMap);
        }
    }

    private void startIncomeCallTimer(long time) {
        if (showIncomingCallWindowTaskHandler != null) {
            showIncomingCallWindowTaskHandler.postAtTime(showIncomingCallWindowTask, SystemClock.uptimeMillis() + time);
        }
    }

    private void stopIncomeCallTimer() {
        Log.d(TAG, "stopIncomeCallTimer");
        showIncomingCallWindowTaskHandler.removeCallbacks(showIncomingCallWindowTask);
    }


    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        if (currentSession == null) {
            addOpponentsFragment();
        }
        super.onResume();
        networkConnectionChecker.registerListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "onPause");
        networkConnectionChecker.unregisterListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG, "onStop");
    }

    @Override
    public QBRTCSession getCurrentSession() {
        return currentSession;
    }

    private void forbidenCloseByWifiState() {
        closeByWifiStateAllow = false;
    }


    public void initCurrentSession(QBRTCSession sesion) {
        this.currentSession = sesion;
        this.currentSession.addSessionCallbacksListener(CallActivity.this);
        this.currentSession.addSignalingCallback(CallActivity.this);
    }

    public void releaseCurrentSession() {
        this.currentSession.removeSessionCallbacksListener(CallActivity.this);
        this.currentSession.removeSignalingCallback(CallActivity.this);
        this.currentSession = null;
    }

    // ---------------Chat callback methods implementation  ----------------------//

    @Override
    public void onReceiveNewSession(final QBRTCSession session) {

        Log.d(TAG, "Session " + session.getSessionID() + " are income");
        String curSession = (getCurrentSession() == null) ? null : getCurrentSession().getSessionID();

        if (getCurrentSession() == null) {
            Log.d(TAG, "Start new session");
            initCurrentSession(session);
            addIncomeCallFragment(session);

            isInCommingCall = true;
            initIncommingCallTask();
        } else {
            Log.d(TAG, "Stop new session. Device now is busy");
            Map<String, String> infoMap = new HashMap<>();
            infoMap.put(Consts.REJECT_REASON, "I'm on a call right now!");
            session.rejectCall(infoMap);
        }


    }

    @Override
    public void onUserNotAnswer(QBRTCSession session, Integer userID) {
        if (!session.equals(getCurrentSession())) {
            return;
        }
        if (sessionUserCallback != null) {
            sessionUserCallback.onUserNotAnswer(session, userID);
        }

        ringtonePlayer.stop();

    }

    @Override
    public void onUserNoActions(QBRTCSession qbrtcSession, Integer integer) {
        startIncomeCallTimer(0);
    }

    @Override
    public void onStartConnectToUser(QBRTCSession session, Integer userID) {

    }

    @Override
    public void onCallAcceptByUser(QBRTCSession session, Integer userId, Map<String, String> userInfo) {
        if (!session.equals(getCurrentSession())) {
            return;
        }
        if (sessionUserCallback != null) {
            sessionUserCallback.onCallAcceptByUser(session, userId, userInfo);
        }

        ringtonePlayer.stop();

    }

    @Override
    public void onCallRejectByUser(QBRTCSession session, final Integer userID, final Map<String, String> userInfo) {
        if (!session.equals(getCurrentSession())) {
            return;
        }
        final String reason = userInfo != null ? userInfo.get(Consts.REJECT_REASON) : "";
        if (sessionUserCallback != null) {
            sessionUserCallback.onCallRejectByUser(session, userID, reason);
        }


        String participantName = DataHolder.getUserNameByID(userID);
        showToast( "User " + participantName + " " + getString(R.string.rejected) + " conversation:" + reason);
        ringtonePlayer.stop();

    }

    @Override
    public void onConnectionClosedForUser(QBRTCSession session, Integer userID) {

        // Close app after session close of network was disabled
        if (hangUpReason != null && hangUpReason.equals(Consts.WIFI_DISABLED)) {
            Intent returnIntent = new Intent();
            setResult(Consts.CALL_ACTIVITY_CLOSE_WIFI_DISABLED, returnIntent);
            finish();
        }

    }

    @Override
    public void onConnectedToUser(QBRTCSession session, final Integer userID) {
        forbidenCloseByWifiState();

        if (isInCommingCall) {
            stopIncomeCallTimer();
        }

        startTimer();
        Log.d(TAG, "onConnectedToUser() is started");


    }


    @Override
    public void onDisconnectedTimeoutFromUser(QBRTCSession session, Integer userID) {

    }

    @Override
    public void onConnectionFailedWithUser(QBRTCSession session, Integer userID) {

    }

    @Override
    public void onError(QBRTCSession qbrtcSession, QBRTCException e) {
    }

    @Override
    public void onSessionClosed(final QBRTCSession session) {
        Log.d(TAG, "Session " + session.getSessionID() + " start stop session");
        String curSession = (getCurrentSession() == null) ? null : getCurrentSession().getSessionID();

        if (session.equals(getCurrentSession())) {

            Fragment currentFragment = getCurrentFragment();
            if (isInCommingCall) {
                stopIncomeCallTimer();
                if (currentFragment instanceof IncomeCallFragment) {
                    removeIncomeCallFragment();
                }
            }

            Log.d(TAG, "Stop session");
            if (!(currentFragment instanceof OpponentsFragment)) {
                addOpponentsFragment();
            }

            if (audioManager != null) {
                audioManager.close();
            }
            releaseCurrentSession();

            stopTimer();
            closeByWifiStateAllow = true;
        }

    }

    @Override
    public void onSessionStartClose(final QBRTCSession session) {
        session.removeSessionCallbacksListener(CallActivity.this);

        ConversationFragment fragment = (ConversationFragment) getFragmentManager().findFragmentByTag(CONVERSATION_CALL_FRAGMENT);
        if (fragment != null && session.equals(getCurrentSession())) {
            fragment.actionButtonsEnabled(false);
        }

    }

    @Override
    public void onDisconnectedFromUser(QBRTCSession session, Integer userID) {

    }

    private void showToast(final int message) {
        Toaster.longToast( message);
    }

    private void showToast(final String message) {
        Toaster.longToast(message);
    }

    @Override
    public void onReceiveHangUpFromUser(final QBRTCSession session, final Integer userID, Map<String, String> userInfo) {
        String reason = userInfo != null ? userInfo.get(Consts.HANG_UP_REASON) : "";
        if (session.equals(getCurrentSession())) {

            if (sessionUserCallback != null) {
                sessionUserCallback.onReceiveHangUpFromUser(session, userID, reason);
            }
        }
        final String participantName = DataHolder.getUserNameByID(userID);

        showToast("User " + participantName + " " + getString(R.string.hungUp) + " conversation:" + reason);
    }

    @Override
    public void onVideoScalingSwitch(RendererCommon.ScalingType scalingType) {

    }

    @Override
    public void onCaptureFormatChange(int width, int height, int framerate) {
        currentSession.getMediaStreamManager().changeCaptureFormat(width, height, framerate);
    }

    public static enum StartConversetionReason {
        INCOME_CALL_FOR_ACCEPTION,
        OUTCOME_CALL_MADE;
    }

    private Fragment getCurrentFragment() {
        return getFragmentManager().findFragmentById(R.id.fragment_container);
    }

    public void addOpponentsFragment() {
        FragmentExecuotr.addFragment(getFragmentManager(), R.id.fragment_container, new OpponentsFragment(), OPPONENTS_CALL_FRAGMENT);
    }

    public void removeIncomeCallFragment() {
        FragmentManager fragmentManager = getFragmentManager();
        Fragment fragment = fragmentManager.findFragmentByTag(INCOME_CALL_FRAGMENT);

        if (fragment != null) {
            FragmentExecuotr.removeFragment(fragmentManager, fragment);
        }
    }

    private void addIncomeCallFragment(QBRTCSession session) {

        Log.d(TAG, "QBRTCSession in addIncomeCallFragment is " + session);
        if (session != null) {
            setOptionsForSession(session, getDefaultSharedPrefs());
            Fragment fragment = new IncomeCallFragment();
            Bundle bundle = new Bundle();
            bundle.putSerializable("sessionDescription", session.getSessionDescription());
            bundle.putIntegerArrayList("opponents", new ArrayList<>(session.getOpponents()));
            bundle.putInt(Consts.CONFERENCE_TYPE, session.getConferenceType().getValue());
            fragment.setArguments(bundle);
            FragmentExecuotr.addFragment(getFragmentManager(), R.id.fragment_container, fragment, INCOME_CALL_FRAGMENT);
        } else {
            Log.d(TAG, "SKIP addIncomeCallFragment method");
        }
    }

    public void addConversationFragmentStartCall(List<QBUser> opponents,
                                                 QBRTCTypes.QBConferenceType qbConferenceType,
                                                 Map<String, String> userInfo) {
        QBRTCSession newSessionWithOpponents = rtcClient.createNewSessionWithOpponents(
                getOpponentsIds(opponents), qbConferenceType);
        SettingsUtil.setSettingsStrategy(opponents,
                getDefaultSharedPrefs(),
                this);

        setOptionsForSession(newSessionWithOpponents, getDefaultSharedPrefs());

        Log.d(TAG, "addConversationFragmentStartCall. Set session " + newSessionWithOpponents);
        initCurrentSession(newSessionWithOpponents);
        ConversationFragment fragment = ConversationFragment.newInstance(opponents, opponents.get(0).getFullName(),
                qbConferenceType, userInfo,
                StartConversetionReason.OUTCOME_CALL_MADE, getCurrentSession().getSessionID());
        FragmentExecuotr.addFragment(getFragmentManager(), R.id.fragment_container, fragment, CONVERSATION_CALL_FRAGMENT);
        audioManager.init();
        ringtonePlayer.play(true);
    }


    public static ArrayList<Integer> getOpponentsIds(List<QBUser> opponents) {
        ArrayList<Integer> ids = new ArrayList<Integer>();
        for (QBUser user : opponents) {
            ids.add(user.getId());
        }
        return ids;
    }


    public void addConversationFragmentReceiveCall() {

        QBRTCSession session = getCurrentSession();

        if (getCurrentSession() != null) {
            Integer myId = QBChatService.getInstance().getUser().getId();
            ArrayList<Integer> opponentsWithoutMe = new ArrayList<>(session.getOpponents());
            opponentsWithoutMe.remove(new Integer(myId));
            opponentsWithoutMe.add(session.getCallerID());

            ArrayList<QBUser> opponents = DataHolder.getUsersByIDs(opponentsWithoutMe.toArray(new Integer[opponentsWithoutMe.size()]));
            SettingsUtil.setSettingsStrategy(opponents, getDefaultSharedPrefs(), this);

            setOptionsForSession(session, getDefaultSharedPrefs());

            ConversationFragment fragment = ConversationFragment.newInstance(opponents,
                    DataHolder.getUserNameByID(session.getCallerID()),
                    session.getConferenceType(), session.getUserInfo(),
                    StartConversetionReason.INCOME_CALL_FOR_ACCEPTION, getCurrentSession().getSessionID());
            // Start conversation fragment
            FragmentExecuotr.addFragment(getFragmentManager(), R.id.fragment_container, fragment, CONVERSATION_CALL_FRAGMENT);
            audioManager.init();
        }
    }


    public void setOpponentsList(List<QBUser> qbUsers) {
        this.opponentsList = qbUsers;
    }

    public List<QBUser> getOpponentsList() {
        return opponentsList;
    }

    @Override
    public void addVideoTrackCallbacksListener(QBRTCClientVideoTracksCallbacks videoTracksCallbacks) {
        if (currentSession != null) {
            currentSession.addVideoTrackCallbacksListener(videoTracksCallbacks);
        }
    }

    @Override
    public void addTCClientConnectionCallback(QBRTCSessionConnectionCallbacks sessionConnectionCallbacks) {
        if (currentSession != null) {
            currentSession.addSessionCallbacksListener(sessionConnectionCallbacks);
        }
    }

    private void setOptionsForSession(QBRTCSession session, SharedPreferences sharedPref){
        QBRTCSession.SessionOptions sessionOptions = new QBRTCSession.SessionOptions();
        sessionOptions.leaveSessionIfInitiatorHangUp =  sharedPref.getBoolean(getString(R.string.pref_initiator_behaviour_key),
                Boolean.valueOf(getString(R.string.pref_initiator_behaviour_default)));
        session.setOptions(sessionOptions);
    }

    public void removeRTCClientConnectionCallback(QBRTCSessionConnectionCallbacks clientConnectionCallbacks) {
        if (currentSession != null) {
            currentSession.removeSessionCallbacksListener(clientConnectionCallbacks);
        }
    }

    @Override
    public void addAudioStateCallback(AudioStateCallback audioStateCallback) {
        this.audioStateCallback = audioStateCallback;
    }

    @Override
    public void switchAudio() {
        if (audioManager.getSelectedAudioDevice() == AudioDevice.WIRED_HEADSET
                || audioManager.getSelectedAudioDevice() == AudioDevice.EARPIECE) {
            audioManager.setAudioDevice(AudioDevice.SPEAKER_PHONE);
        } else {
            audioManager.setAudioDevice(AudioDevice.EARPIECE);
        }
    }

    public void addRTCSessionUserCallback(QBRTCSessionUserCallback sessionUserCallback) {
        this.sessionUserCallback = sessionUserCallback;
    }

    public void removeRTCSessionUserCallback(QBRTCSessionUserCallback sessionUserCallback) {
        this.sessionUserCallback = null;
    }

    public void addRTCStatsReportCallback(QBRTCStatsReportCallback statsReportCallback) {
        if (currentSession != null) {
            currentSession.addStatsReportCallback(statsReportCallback);
        }
    }

    public void removeRTCStatsReportCallback(QBRTCStatsReportCallback statsReportCallback) {
        if (currentSession != null) {
            currentSession.removeStatsReportCallback(statsReportCallback);
        }
    }

    public void showSettings() {
        SettingsActivity.start(this);
    }

    public void logout() {
        logoutSession();
        ListUsersActivity.start(this);
        finish();
    }

    private void logoutSession() {
        try {
            DataHolder.setLoggedUser(null);
            QBRTCClient.getInstance(this).destroy();
            //ChatPingAlarmManager.onDestroy();
            QBChatService.getInstance().logout();
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        }
    }

    public SharedPreferences getDefaultSharedPrefs() {
        return sharedPref;
    }

    @Override
    public void onSuccessSendingPacket(QBSignalingSpec.QBSignalCMD qbSignalCMD, Integer integer) {
    }

    @Override
    public void onErrorSendingPacket(QBSignalingSpec.QBSignalCMD qbSignalCMD, Integer userId, QBRTCSignalException e) {
        showToast(R.string.dlg_signal_error);
    }

    @Override
    public void onSwitchAudio() {
        if (audioManager.getSelectedAudioDevice() == AudioDevice.WIRED_HEADSET
                || audioManager.getSelectedAudioDevice() == AudioDevice.EARPIECE) {
            audioManager.setAudioDevice(AudioDevice.SPEAKER_PHONE);
        } else {
            audioManager.setAudioDevice(AudioDevice.EARPIECE);
        }
    }

    @Override
    public void onUseHeadSet(boolean use) {
        audioManager.setManageHeadsetByDefault(use);
    }

    @Override
    public void onBackPressed() {
        Fragment fragment = getFragmentManager().findFragmentByTag(CONVERSATION_CALL_FRAGMENT);
        if (fragment == null) {
            super.onBackPressed();
            logoutSession();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy");
        opponentsList = null;
        OpponentsAdapter.i = 0;
        hangUpCurrentSession("due tp app was closed ");
    }

}

