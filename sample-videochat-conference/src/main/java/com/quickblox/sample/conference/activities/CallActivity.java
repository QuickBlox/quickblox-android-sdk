package com.quickblox.sample.conference.activities;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.quickblox.chat.QBChatService;
import com.quickblox.conference.ConferenceClient;
import com.quickblox.conference.ConferenceSession;
import com.quickblox.conference.WsException;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.sample.core.ui.dialog.ProgressDialogFragment;
import com.quickblox.sample.core.utils.Toaster;
import com.quickblox.sample.conference.R;
import com.quickblox.sample.conference.db.QbUsersDbManager;
import com.quickblox.sample.conference.fragments.AudioConversationFragment;
import com.quickblox.sample.conference.fragments.BaseConversationFragment;
import com.quickblox.sample.conference.fragments.ConversationFragmentCallbackListener;
import com.quickblox.sample.conference.fragments.IncomeCallFragment;
import com.quickblox.sample.conference.fragments.IncomeCallFragmentCallbackListener;
import com.quickblox.sample.conference.fragments.OnCallEventsController;
import com.quickblox.sample.conference.fragments.ScreenShareFragment;
import com.quickblox.sample.conference.fragments.VideoConversationFragment;
import com.quickblox.sample.conference.util.NetworkConnectionChecker;
import com.quickblox.sample.conference.utils.Consts;
import com.quickblox.sample.conference.utils.FragmentExecuotr;
import com.quickblox.sample.conference.utils.PermissionsChecker;
import com.quickblox.sample.conference.utils.QBEntityCallbackImpl;
import com.quickblox.sample.conference.utils.RingtonePlayer;
import com.quickblox.sample.conference.utils.SettingsUtil;
import com.quickblox.sample.conference.utils.UsersUtils;
import com.quickblox.sample.conference.utils.WebRtcSessionManager;
import com.quickblox.users.model.QBUser;
import com.quickblox.videochat.webrtc.AppRTCAudioManager;
import com.quickblox.videochat.webrtc.BaseSession;
import com.quickblox.videochat.webrtc.QBRTCCameraVideoCapturer;
import com.quickblox.videochat.webrtc.QBRTCClient;
import com.quickblox.videochat.webrtc.QBRTCConfig;
import com.quickblox.videochat.webrtc.QBRTCScreenCapturer;
import com.quickblox.videochat.webrtc.QBRTCSession;
import com.quickblox.videochat.webrtc.QBRTCTypes;
import com.quickblox.videochat.webrtc.QBSignalingSpec;
import com.quickblox.videochat.webrtc.callbacks.QBRTCClientSessionCallbacks;
import com.quickblox.videochat.webrtc.callbacks.QBRTCSessionStateCallback;
import com.quickblox.videochat.webrtc.callbacks.QBRTCSignalingCallback;
import com.quickblox.videochat.webrtc.exception.QBRTCSignalException;

import org.jivesoftware.smack.AbstractConnectionListener;
import org.webrtc.CameraVideoCapturer;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * QuickBlox team
 */
public class CallActivity extends BaseActivity implements QBRTCSessionStateCallback<ConferenceSession>, ConferenceSession.JanusCallbackListener,
        OnCallEventsController, IncomeCallFragmentCallbackListener, ConversationFragmentCallbackListener, NetworkConnectionChecker.OnConnectivityChangedListener, ScreenShareFragment.OnSharingEvents {

    private static final String TAG = CallActivity.class.getSimpleName();

    public static final String OPPONENTS_CALL_FRAGMENT = "opponents_call_fragment";
    public static final String INCOME_CALL_FRAGMENT = "income_call_fragment";
    public static final String CONVERSATION_CALL_FRAGMENT = "conversation_call_fragment";
    public static final String CALLER_NAME = "caller_name";
    public static final String SESSION_ID = "sessionID";
    public static final String START_CONVERSATION_REASON = "start_conversation_reason";

    private static final int REQUEST_MEDIA_PROJECTION = 1;

    private ConferenceSession currentSession;
    public List<QBUser> opponentsList;
    private Runnable showIncomingCallWindowTask;
    private Handler showIncomingCallWindowTaskHandler;
    private boolean closeByWifiStateAllow = true;
    private String hangUpReason;
    private boolean isInCommingCall;
    private ConferenceClient rtcClient;
    private OnChangeDynamicToggle onChangeDynamicCallback;
    private ConnectionListener connectionListener;
    private boolean wifiEnabled = true;
    private SharedPreferences sharedPref;
    private LinearLayout connectionView;
    private AppRTCAudioManager audioManager;
    private NetworkConnectionChecker networkConnectionChecker;
    private WebRtcSessionManager sessionManager;
    private QbUsersDbManager dbManager;
    private ArrayList<CurrentCallStateCallback> currentCallStateCallbackList = new ArrayList<>();
    private List<Integer> opponentsIdsList;
    private boolean callStarted;
    private long expirationReconnectionTime;
    private int reconnectHangUpTimeMillis;
    private boolean headsetPlugged;
    private boolean previousDeviceEarPiece;
    private boolean showToastAfterHeadsetPlugged = true;
    private PermissionsChecker checker;
    private Set<Integer> publishers = new CopyOnWriteArraySet<>();
    private volatile boolean connectedToJanus;

    public static void start(Context context,
                             boolean isIncomingCall) {

        Intent intent = new Intent(context, CallActivity.class);
        intent.putExtra(Consts.EXTRA_IS_INCOMING_CALL, isIncomingCall);

        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        parseIntentExtras();

        sessionManager = WebRtcSessionManager.getInstance(this);
        if (!currentSessionExist()) {
//            we have already currentSession == null, so it's no reason to do further initialization
            finish();
            Log.d(TAG, "finish CallActivity");
            return;
        }

        initFields();
        initCurrentSession(currentSession);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        initQBRTCClient();
        initAudioManager();
        initWiFiManagerListener();

        connectionView = (LinearLayout) View.inflate(this, R.layout.connection_popup, null);
        checker = new PermissionsChecker(getApplicationContext());

        startSuitableFragment(isInCommingCall);
    }


    private void startScreenSharing(final Intent data) {
        ScreenShareFragment screenShareFragment = ScreenShareFragment.newIntstance();
        FragmentExecuotr.addFragmentWithBackStack(getSupportFragmentManager(), R.id.fragment_container, screenShareFragment, ScreenShareFragment.TAG);
        currentSession.getMediaStreamManager().setVideoCapturer(new QBRTCScreenCapturer(data, null));
    }

    private void returnToCamera() {
        try {
            currentSession.getMediaStreamManager().setVideoCapturer(new QBRTCCameraVideoCapturer(this, null));
        } catch (QBRTCCameraVideoCapturer.QBRTCCameraCapturerException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        Log.i(TAG, "onActivityResult requestCode=" + requestCode + ", resultCode= " + resultCode);
        if (requestCode == QBRTCScreenCapturer.REQUEST_MEDIA_PROJECTION) {
            if (resultCode == Activity.RESULT_OK) {
                startScreenSharing(data);
                Log.i(TAG, "Starting screen capture");
            } else {

            }
        }
    }

    private void startSuitableFragment(boolean isInComingCall) {
        if (isInComingCall) {
            initIncomingCallTask();
            startLoadAbsentUsers();
            addIncomeCallFragment();
            checkPermission();
        } else {
            addConversationFragment(isInComingCall);
        }
    }

    private void checkPermission() {
        if (checker.lacksPermissions(Consts.PERMISSIONS)) {
            startPermissionsActivity(false);
        }
    }

    private void startPermissionsActivity(boolean checkOnlyAudio) {
        PermissionsActivity.startActivity(this, checkOnlyAudio, Consts.PERMISSIONS);
    }

    private void startLoadAbsentUsers() {
        ArrayList<QBUser> usersFromDb = dbManager.getAllUsers();
        ArrayList<Integer> allParticipantsOfCall = new ArrayList<>();
        allParticipantsOfCall.addAll(opponentsIdsList);

        if (isInCommingCall) {
            allParticipantsOfCall.add(currentSession.getCallerID());
        }

        ArrayList<Integer> idsUsersNeedLoad = UsersUtils.getIdsNotLoadedUsers(usersFromDb, allParticipantsOfCall);
        if (!idsUsersNeedLoad.isEmpty()) {
            requestExecutor.loadUsersByIds(idsUsersNeedLoad, new QBEntityCallbackImpl<ArrayList<QBUser>>() {
                @Override
                public void onSuccess(ArrayList<QBUser> result, Bundle params) {
                    dbManager.saveAllUsers(result, false);
                    needUpdateOpponentsList(result);
                }
            });
        }
    }

    private void needUpdateOpponentsList(ArrayList<QBUser> newUsers) {
        notifyCallStateListenersNeedUpdateOpponentsList(newUsers);
    }

    private boolean currentSessionExist() {
        currentSession = sessionManager.getCurrentSession();
        return currentSession != null;
    }

    private void initFields() {
        dbManager = QbUsersDbManager.getInstance(getApplicationContext());
        opponentsIdsList = currentSession.getOpponents();
    }

    @Override
    protected View getSnackbarAnchorView() {
        return null;
    }

    private void parseIntentExtras() {
        isInCommingCall = getIntent().getExtras().getBoolean(Consts.EXTRA_IS_INCOMING_CALL);
    }

    private void initAudioManager() {
        audioManager = AppRTCAudioManager.create(this, new AppRTCAudioManager.OnAudioManagerStateListener() {
            @Override
            public void onAudioChangedState(AppRTCAudioManager.AudioDevice audioDevice) {
                if (callStarted) {
                    if (audioManager.getSelectedAudioDevice() == AppRTCAudioManager.AudioDevice.EARPIECE) {
                        previousDeviceEarPiece = true;
                    } else if (audioManager.getSelectedAudioDevice() == AppRTCAudioManager.AudioDevice.SPEAKER_PHONE) {
                        previousDeviceEarPiece = false;
                    }
                    if (showToastAfterHeadsetPlugged) {
                        Toaster.shortToast("Audio device switched to  " + audioDevice);
                    }
                }
            }
        });

        audioManager.setDefaultAudioDevice(AppRTCAudioManager.AudioDevice.SPEAKER_PHONE);
        Log.d(TAG, "AppRTCAudioManager.AudioDevice.SPEAKER_PHONE");

        audioManager.setOnWiredHeadsetStateListener(new AppRTCAudioManager.OnWiredHeadsetStateListener() {
            @Override
            public void onWiredHeadsetStateChanged(boolean plugged, boolean hasMicrophone) {
                headsetPlugged = plugged;
                if (callStarted) {
                    Toaster.shortToast("Headset " + (plugged ? "plugged" : "unplugged"));
                }
                if (onChangeDynamicCallback != null) {
                    if (!plugged) {
                        showToastAfterHeadsetPlugged = false;
                        if (previousDeviceEarPiece) {
                            setAudioDeviceDelayed(AppRTCAudioManager.AudioDevice.EARPIECE);
                        } else {
                            setAudioDeviceDelayed(AppRTCAudioManager.AudioDevice.SPEAKER_PHONE);
                        }
                    }
                    onChangeDynamicCallback.enableDynamicToggle(plugged, previousDeviceEarPiece);
                }
            }
        });
        audioManager.init();
    }

    private void setAudioDeviceDelayed(final AppRTCAudioManager.AudioDevice audioDevice) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                showToastAfterHeadsetPlugged = true;
                audioManager.setAudioDevice(audioDevice);
            }
        }, 500);
    }

    private void initQBRTCClient() {
        rtcClient = ConferenceClient.getInstance(this);

        rtcClient.setCameraErrorHandler(new CameraVideoCapturer.CameraEventsHandler() {
            @Override
            public void onCameraError(final String s) {

                showToast("Camera error: " + s);
            }

            @Override
            public void onCameraDisconnected() {
                showToast("Camera onCameraDisconnected: ");
            }

            @Override
            public void onCameraFreezed(String s) {
                showToast("Camera freezed: " + s);
                hangUpCurrentSession();
            }

            @Override
            public void onCameraOpening(String s) {
                showToast("Camera aOpening: " + s);
            }

            @Override
            public void onFirstFrameAvailable() {
                showToast("onFirstFrameAvailable: ");
            }

            @Override
            public void onCameraClosed() {
            }
        });


        // Configure
        //
//        QBRTCConfig.setMaxOpponentsCount(Consts.MAX_OPPONENTS_COUNT);
//        SettingsUtil.setSettingsStrategy(opponentsIdsList, sharedPref, CallActivity.this);
//        SettingsUtil.configRTCTimers(CallActivity.this);
        QBRTCConfig.setDebugEnabled(true);

//        connectionListener = new ConnectionListener();
//        QBChatService.getInstance().addConnectionListener(connectionListener);
    }

    private void setExpirationReconnectionTime() {
        reconnectHangUpTimeMillis = SettingsUtil.getPreferenceInt(sharedPref, this, R.string.pref_disconnect_time_interval_key,
                R.string.pref_disconnect_time_interval_default_value) * 1000;
        expirationReconnectionTime = System.currentTimeMillis() + reconnectHangUpTimeMillis;
    }

    private void hangUpAfterLongReconnection() {
        if (expirationReconnectionTime < System.currentTimeMillis()) {
            hangUpCurrentSession();
        }
    }

    @Override
    public void connectivityChanged(boolean availableNow) {
        if (callStarted) {
            showToast("Internet connection " + (availableNow ? "available" : " unavailable"));
        }
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

    private void initIncomingCallTask() {
        showIncomingCallWindowTaskHandler = new Handler(Looper.myLooper());
        showIncomingCallWindowTask = new Runnable() {
            @Override
            public void run() {
                if (currentSession == null) {
                    return;
                }

                QBRTCSession.QBRTCSessionState currentSessionState = currentSession.getState();
                if (QBRTCSession.QBRTCSessionState.QB_RTC_SESSION_NEW.equals(currentSessionState)) {
                    rejectCurrentSession();
                } else {
                    hangUpCurrentSession();
                }
                Toaster.longToast("Call was stopped by timer");
            }
        };
    }


    private ConferenceSession getCurrentSession() {
        return currentSession;
    }

    public void rejectCurrentSession() {
//        if (getCurrentSession() != null) {
//            getCurrentSession().rejectCall(new HashMap<String, String>());
//        }
    }

    public void hangUpCurrentSession() {

        if (getCurrentSession() != null) {
            getCurrentSession().hangUp(new HashMap<String, String>());

            try {
                getCurrentSession().leave();
                getCurrentSession().destroySession();
            } catch (WsException e) {
                Log.e(TAG, e.getMessage());
            }
        }
    }

    private void setAudioEnabled(boolean isAudioEnabled) {
        if (currentSession != null && currentSession.getMediaStreamManager() != null) {
            currentSession.getMediaStreamManager().getLocalAudioTrack().setEnabled(isAudioEnabled);
        }
    }

    private void setVideoEnabled(boolean isVideoEnabled) {
        if (currentSession != null && currentSession.getMediaStreamManager() != null) {
            currentSession.getMediaStreamManager().getLocalVideoTrack().setEnabled(isVideoEnabled);
        }
    }

    private void startIncomeCallTimer(long time) {
        showIncomingCallWindowTaskHandler.postAtTime(showIncomingCallWindowTask, SystemClock.uptimeMillis() + time);
    }

    private void stopIncomeCallTimer() {
        Log.d(TAG, "stopIncomeCallTimer");
        showIncomingCallWindowTaskHandler.removeCallbacks(showIncomingCallWindowTask);
    }


    @Override
    protected void onResume() {
        super.onResume();
        networkConnectionChecker.registerListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        networkConnectionChecker.unregisterListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
//        QBChatService.getInstance().removeConnectionListener(connectionListener);
    }


    private void forbiddenCloseByWifiState() {
        closeByWifiStateAllow = false;
    }


    public void initCurrentSession(ConferenceSession session) {
        if (session != null) {
            Log.d(TAG, "Init new ConferenceSession");
            this.currentSession = session;
            this.currentSession.addSessionCallbacksListener(CallActivity.this);
            this.currentSession.addJanusListener(CallActivity.this);
//            this.currentSession.addJanusListener();
        }
    }

    public void releaseCurrentSession() {
        Log.d(TAG, "Release current session");
        if (currentSession != null) {
            this.currentSession.removeSessionCallbacksListener(CallActivity.this);
            this.currentSession.removeJanusListener(CallActivity.this);
            this.currentSession = null;
        }
    }

    // ---------------Chat callback methods implementation  ----------------------//



    @Override
    public void onConnectionClosedForUser(ConferenceSession session, Integer userID) {
        Log.d(TAG, "QBRTCSessionStateCallbackImpl onConnectionClosedForUser userID=" + userID);
        // Close app after session close of network was disabled
        if (hangUpReason != null && hangUpReason.equals(Consts.WIFI_DISABLED)) {
            Intent returnIntent = new Intent();
            setResult(Consts.CALL_ACTIVITY_CLOSE_WIFI_DISABLED, returnIntent);
            finish();
        }
    }

    @Override
    public void onConnectedToUser(ConferenceSession session, final Integer userID) {
        Log.d(TAG, "onConnectedToUser userID= " + userID + " sessionID= " + session.getSessionID());
        callStarted = true;
        notifyCallStateListenersCallStarted();
        forbiddenCloseByWifiState();
        if (isInCommingCall) {
            stopIncomeCallTimer();
        }
        Log.d(TAG, "onConnectedToUser() is started");
    }


    @Override
    public void onDisconnectedFromUser(ConferenceSession session, Integer userID) {
        Log.d(TAG, "QBRTCSessionStateCallbackImpl onDisconnectedFromUser userID=" + userID);
    }

    private void showToast(final int message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toaster.shortToast(message);
            }
        });
    }

    private void showToast(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toaster.shortToast(message);
            }
        });
    }


    private android.support.v4.app.Fragment getCurrentFragment() {
        return getSupportFragmentManager().findFragmentById(R.id.fragment_container);
    }

    private void addIncomeCallFragment() {
        Log.d(TAG, "QBRTCSession in addIncomeCallFragment is " + currentSession);

        if (currentSession != null) {
            IncomeCallFragment fragment = new IncomeCallFragment();
            FragmentExecuotr.addFragment(getSupportFragmentManager(), R.id.fragment_container, fragment, INCOME_CALL_FRAGMENT);
        } else {
            Log.d(TAG, "SKIP addIncomeCallFragment method");
        }
    }

    private void addConversationFragment(boolean isIncomingCall) {
        BaseConversationFragment conversationFragment = BaseConversationFragment.newInstance(new VideoConversationFragment(), isIncomingCall);
        FragmentExecuotr.addFragment(getSupportFragmentManager(), R.id.fragment_container, conversationFragment, conversationFragment.getClass().getSimpleName());
    }

    public SharedPreferences getDefaultSharedPrefs() {
        return sharedPref;
    }

//    @Override
//    public void onErrorSendingPacket(Integer userId, Exception e) {
//        showToast(R.string.dlg_signal_error);
//    }


    public void onUseHeadSet(boolean use) {
        audioManager.setManageHeadsetByDefault(use);
    }

    public void sendHeadsetState() {
        if (isInCommingCall) {
            onChangeDynamicCallback.enableDynamicToggle(headsetPlugged, previousDeviceEarPiece);
        }
    }

    ////////////////////////////// IncomeCallFragmentCallbackListener ////////////////////////////

    @Override
    public void onAcceptCurrentSession() {
        if (currentSession != null) {
            addConversationFragment(true);
        } else {
            Log.d(TAG, "SKIP addConversationFragment method");
        }
    }

    @Override
    public void onRejectCurrentSession() {
        rejectCurrentSession();
    }
    //////////////////////////////////////////   end   /////////////////////////////////////////////


    @Override
    public void onBackPressed() {
        android.support.v4.app.Fragment fragmentByTag = getSupportFragmentManager().findFragmentByTag(ScreenShareFragment.TAG);
        if (fragmentByTag instanceof ScreenShareFragment) {
            returnToCamera();
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    ////////////////////////////// ConversationFragmentCallbackListener ////////////////////////////

    @Override
    public void addTCClientConnectionCallback(QBRTCSessionStateCallback clientConnectionCallbacks) {
        if (currentSession != null) {
            currentSession.addSessionCallbacksListener(clientConnectionCallbacks);
        }
    }

    @Override
    public void onSetAudioEnabled(boolean isAudioEnabled) {
        setAudioEnabled(isAudioEnabled);
    }

    @Override
    public void onHangUpCurrentSession() {
        hangUpCurrentSession();
    }

    @TargetApi(21)
    @Override
    public void onStartScreenSharing() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return;
        }
        QBRTCScreenCapturer.requestPermissions(CallActivity.this);
    }

    @Override
    public void onSwitchCamera(CameraVideoCapturer.CameraSwitchHandler cameraSwitchHandler) {
        ((QBRTCCameraVideoCapturer) (currentSession.getMediaStreamManager().getVideoCapturer()))
                .switchCamera(cameraSwitchHandler);
    }

    @Override
    public void onStartJoinConference() {
        int userID = currentSession.getCallerID();
        currentSession.joinDialog(new JoinedCallback(userID));
    }

    @Override
    public void onSetVideoEnabled(boolean isNeedEnableCam) {
        setVideoEnabled(isNeedEnableCam);
    }

    @Override
    public void onSwitchAudio() {
        if (audioManager.getSelectedAudioDevice() == AppRTCAudioManager.AudioDevice.WIRED_HEADSET
                || audioManager.getSelectedAudioDevice() == AppRTCAudioManager.AudioDevice.EARPIECE) {
            audioManager.setAudioDevice(AppRTCAudioManager.AudioDevice.SPEAKER_PHONE);
        } else {
            audioManager.setAudioDevice(AppRTCAudioManager.AudioDevice.EARPIECE);
        }
    }

    @Override
    public void removeRTCClientConnectionCallback(QBRTCSessionStateCallback clientConnectionCallbacks) {
        if (currentSession != null) {
            currentSession.removeSessionCallbacksListener(clientConnectionCallbacks);
        }
    }

    @Override
    public void addCurrentCallStateCallback(CurrentCallStateCallback currentCallStateCallback) {
        currentCallStateCallbackList.add(currentCallStateCallback);
    }

    @Override
    public void removeCurrentCallStateCallback(CurrentCallStateCallback currentCallStateCallback) {
        currentCallStateCallbackList.remove(currentCallStateCallback);
    }

    @Override
    public void addOnChangeDynamicToggle(OnChangeDynamicToggle onChangeDynamicCallback) {
        this.onChangeDynamicCallback = onChangeDynamicCallback;
        sendHeadsetState();
    }

    @Override
    public void removeOnChangeDynamicToggle(OnChangeDynamicToggle onChangeDynamicCallback) {
        this.onChangeDynamicCallback = null;
    }

    @Override
    public void onStopPreview() {
        onBackPressed();
    }

    @Override
    public void OnConnected(BigInteger senderID) {
        connectedToJanus = true;
        Log.d(TAG, "OnConnected senderID= " + senderID);
        subscribeToAllGotPublisher();
    }

    private void subscribeToAllGotPublisher() {
        Log.d(TAG, "subscribeToAllGotPublisher");
        currentSession.subscribeToPublisher(new ArrayList<>(publishers), null);
    }

    @Override
    public void OnPublishersReceived(ArrayList<Integer> publishersList) {
        Log.d(TAG, "OnPublishersReceived connectedToJanus" + connectedToJanus);
        if(!connectedToJanus) {
            publishers.addAll(publishersList);
        } else {
            currentSession.subscribeToPublisher(publishersList, null);
        }
    }

    @Override
    public void OnPublisherLeft(Integer userID) {

    }

    @Override
    public void OnError(String error) {
        showToast("Connection error, please rejoin if need: " + error);
    }

    @Override
    public void OnSessionClosed(final ConferenceSession session) {
        Log.d(TAG, "Session " + session.getSessionID() + " start stop session");

        if (session.equals(getCurrentSession())) {
            Log.d(TAG, "Stop session");

            if (audioManager != null) {
                audioManager.close();
            }
            releaseCurrentSession();

            closeByWifiStateAllow = true;
            finish();
        }
    }

    //////////////////////////////////////////   end   /////////////////////////////////////////////
    private class ConnectionListener extends AbstractConnectionListener {
        @Override
        public void connectionClosedOnError(Exception e) {
            showNotificationPopUp(R.string.connection_was_lost, true);
            setExpirationReconnectionTime();
        }

        @Override
        public void reconnectionSuccessful() {
            showNotificationPopUp(R.string.connection_was_lost, false);
        }

        @Override
        public void reconnectingIn(int seconds) {
            Log.i(TAG, "reconnectingIn " + seconds);
            if (!callStarted) {
                hangUpAfterLongReconnection();
            }
        }
    }

    public interface OnChangeDynamicToggle {
        void enableDynamicToggle(boolean plugged, boolean wasEarpiece);
    }


    public interface CurrentCallStateCallback {
        void onCallStarted();

        void onCallStopped();

        void onOpponentsListUpdated(ArrayList<QBUser> newUsers);
    }

    private void notifyCallStateListenersCallStarted() {
        for (CurrentCallStateCallback callback : currentCallStateCallbackList) {
            callback.onCallStarted();
        }
    }

    private void notifyCallStateListenersCallStopped() {
        for (CurrentCallStateCallback callback : currentCallStateCallbackList) {
            callback.onCallStopped();
        }
    }

    private void notifyCallStateListenersNeedUpdateOpponentsList(final ArrayList<QBUser> newUsers) {
        for (CurrentCallStateCallback callback : currentCallStateCallbackList) {
            callback.onOpponentsListUpdated(newUsers);
        }
    }

    private class JoinedCallback implements QBEntityCallback<ArrayList<Integer>> {
        Integer userID;

        JoinedCallback(Integer userID) {
            this.userID = userID;
        }

        @Override
        public void onSuccess(ArrayList<Integer> publishers, Bundle params) {
            Log.d(TAG, "onSuccess joinDialog sessionUserID= " + userID + ", publishers= " + publishers);
        }

        @Override
        public void onError(QBResponseException exception) {
            Log.d(TAG, "onError joinDialog exception= " + exception);
        }
    }
}