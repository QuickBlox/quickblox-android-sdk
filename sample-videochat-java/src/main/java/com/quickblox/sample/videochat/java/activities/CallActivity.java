package com.quickblox.sample.videochat.java.activities;

import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
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
import com.quickblox.sample.videochat.java.R;
import com.quickblox.sample.videochat.java.db.QbUsersDbManager;
import com.quickblox.sample.videochat.java.fragments.AudioConversationFragment;
import com.quickblox.sample.videochat.java.fragments.BaseConversationFragment;
import com.quickblox.sample.videochat.java.fragments.ConversationFragmentCallbackListener;
import com.quickblox.sample.videochat.java.fragments.IncomeCallFragment;
import com.quickblox.sample.videochat.java.fragments.IncomeCallFragmentCallbackListener;
import com.quickblox.sample.videochat.java.fragments.OnCallEventsController;
import com.quickblox.sample.videochat.java.fragments.ScreenShareFragment;
import com.quickblox.sample.videochat.java.fragments.VideoConversationFragment;
import com.quickblox.sample.videochat.java.util.NetworkConnectionChecker;
import com.quickblox.sample.videochat.java.utils.Consts;
import com.quickblox.sample.videochat.java.utils.FragmentExecuotr;
import com.quickblox.sample.videochat.java.utils.PermissionsChecker;
import com.quickblox.sample.videochat.java.utils.QBEntityCallbackImpl;
import com.quickblox.sample.videochat.java.utils.RingtonePlayer;
import com.quickblox.sample.videochat.java.utils.SettingsUtil;
import com.quickblox.sample.videochat.java.utils.ToastUtils;
import com.quickblox.sample.videochat.java.utils.UsersUtils;
import com.quickblox.sample.videochat.java.utils.WebRtcSessionManager;
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
import com.quickblox.videochat.webrtc.callbacks.QBRTCSessionEventsCallback;
import com.quickblox.videochat.webrtc.callbacks.QBRTCSessionStateCallback;
import com.quickblox.videochat.webrtc.callbacks.QBRTCSignalingCallback;
import com.quickblox.videochat.webrtc.exception.QBRTCSignalException;

import org.jivesoftware.smack.AbstractConnectionListener;
import org.webrtc.CameraVideoCapturer;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.fragment.app.Fragment;

/**
 * QuickBlox team
 */
public class CallActivity extends BaseActivity implements QBRTCClientSessionCallbacks, QBRTCSessionStateCallback<QBRTCSession>, QBRTCSignalingCallback,
        OnCallEventsController, IncomeCallFragmentCallbackListener, ConversationFragmentCallbackListener, NetworkConnectionChecker.OnConnectivityChangedListener,
        ScreenShareFragment.OnSharingEvents {

    private static final String TAG = CallActivity.class.getSimpleName();
    public static final String INCOME_CALL_FRAGMENT = "income_call_fragment";
    private static final long COUNTDOWN_TIME = 10000;
    private static final long COUNTDOWN_INTERVAL = 1000;

    private QBRTCSession currentSession;
    private Runnable showIncomingCallWindowTask;
    private Handler showIncomingCallWindowTaskHandler;
    private boolean isInComingCall;
    private QBRTCClient rtcClient;
    private OnChangeAudioDevice onChangeAudioDeviceCallback;
    private ConnectionListener connectionListener;
    private SharedPreferences sharedPref;
    private RingtonePlayer ringtonePlayer;
    private LinearLayout connectionView;
    private NetworkConnectionChecker networkConnectionChecker;
    private QbUsersDbManager dbManager;
    private ArrayList<CurrentCallStateCallback> currentCallStateCallbackList = new ArrayList<>();
    private List<Integer> opponentsIdsList;
    private boolean callStarted;
    private boolean isVideoCall;
    private long expirationReconnectionTime;
    private PermissionsChecker checker;
    private WeakReference<AppRTCAudioManager> audioManagerWeakRef;

    private AudioManager btAudioManager;
    private CountDown btCountDown;
    private BluetoothBroadcastReceiver btBroadcastReceiver;
    private boolean isBTStarting;

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

        WebRtcSessionManager sessionManager = WebRtcSessionManager.getInstance(this);
        currentSession = sessionManager.getCurrentSession();
        if (currentSession == null) {
            //if currentSession == null so there is no reason to do further initialization
            finish();
            Log.d(TAG, "CallActivity finished since the CurrentSession doesn't exist");
            return;
        }

        initFields();
        initCurrentSession(currentSession);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        btAudioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
        initQBRTCClient();
        initAudioManager();
        startBluetooth();
        initWiFiManagerListener();

        ringtonePlayer = new RingtonePlayer(this, R.raw.beep);
        connectionView = (LinearLayout) View.inflate(this, R.layout.connection_popup, null);
        checker = new PermissionsChecker(getApplicationContext());

        if (!isInComingCall) {
            startAudioManager();
            ringtonePlayer.play(true);
        }
        startSuitableFragment(isInComingCall);
    }

    private void startAudioManager() {
        audioManagerWeakRef.get().start((selectedAudioDevice, availableAudioDevices) -> {
            ToastUtils.shortToast("Audio device switched to  " + selectedAudioDevice);
            if (onChangeAudioDeviceCallback != null) {
                onChangeAudioDeviceCallback.audioDeviceChanged(selectedAudioDevice);
            }
        });
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
            Log.i(TAG, "Error: device doesn't have camera");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        Log.i(TAG, "onActivityResult requestCode=" + requestCode + ", resultCode= " + resultCode);
        if (requestCode == QBRTCScreenCapturer.REQUEST_MEDIA_PROJECTION) {
            if (resultCode == Activity.RESULT_OK) {
                startScreenSharing(data);
                Log.i(TAG, "Starting screen capture");
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
            startPermissionsActivity(!isVideoCall);
        }
    }

    private void startPermissionsActivity(boolean checkOnlyAudio) {
        PermissionsActivity.startActivity(this, checkOnlyAudio, Consts.PERMISSIONS);
    }

    private void startLoadAbsentUsers() {
        ArrayList<QBUser> usersFromDb = dbManager.getAllUsers();
        ArrayList<Integer> allParticipantsOfCall = new ArrayList<>(opponentsIdsList);

        if (isInComingCall) {
            allParticipantsOfCall.add(currentSession.getCallerID());
        }

        ArrayList<Integer> idsUsersNeedLoad = UsersUtils.getIdsNotLoadedUsers(usersFromDb, allParticipantsOfCall);
        if (!idsUsersNeedLoad.isEmpty()) {
            requestExecutor.loadUsersByIds(idsUsersNeedLoad, new QBEntityCallbackImpl<ArrayList<QBUser>>() {
                @Override
                public void onSuccess(ArrayList<QBUser> result, Bundle params) {
                    dbManager.saveAllUsers(result, false);
                    notifyCallStateListenersNeedUpdateOpponentsList(result);
                }
            });
        }
    }

    private void initFields() {
        dbManager = QbUsersDbManager.getInstance(getApplicationContext());
        opponentsIdsList = currentSession.getOpponents();
    }

    private void parseIntentExtras() {
        isInComingCall = getIntent().getExtras().getBoolean(Consts.EXTRA_IS_INCOMING_CALL);
    }

    private void initAudioManager() {
        audioManagerWeakRef = new WeakReference<>(AppRTCAudioManager.create(this));

        isVideoCall = QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_VIDEO.equals(currentSession.getConferenceType());
        if (isVideoCall) {
            audioManagerWeakRef.get().setDefaultAudioDevice(AppRTCAudioManager.AudioDevice.SPEAKER_PHONE);
            Log.d(TAG, "AppRTCAudioManager.AudioDevice.SPEAKER_PHONE");
        } else {
            audioManagerWeakRef.get().setDefaultAudioDevice(AppRTCAudioManager.AudioDevice.EARPIECE);
            audioManagerWeakRef.get().setManageSpeakerPhoneByProximity(SettingsUtil.isManageSpeakerPhoneByProximity(this));
            Log.d(TAG, "AppRTCAudioManager.AudioDevice.EARPIECE");
        }

        audioManagerWeakRef.get().setOnWiredHeadsetStateListener((plugged, hasMicrophone) -> {
            if (callStarted) {
                ToastUtils.shortToast("Headset " + (plugged ? "plugged" : "unplugged"));
            }
        });

        audioManagerWeakRef.get().setBluetoothAudioDeviceStateListener(connected -> {
            if (callStarted) {
                ToastUtils.shortToast("Bluetooth " + (connected ? "connected" : "disconnected"));
            }
        });
    }

    private void initQBRTCClient() {
        rtcClient = QBRTCClient.getInstance(this);
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
        QBRTCConfig.setMaxOpponentsCount(Consts.MAX_OPPONENTS_COUNT);
        SettingsUtil.setSettingsStrategy(opponentsIdsList, sharedPref, CallActivity.this);
        SettingsUtil.configRTCTimers(CallActivity.this);
        QBRTCConfig.setDebugEnabled(true);

        // Add activity as callback to RTCClient
        rtcClient.addSessionCallbacksListener(this);
        // Start manage QBRTCSessions according to VideoCall parser's callbacks
        rtcClient.prepareToProcessCalls();
        connectionListener = new ConnectionListener();
        QBChatService.getInstance().addConnectionListener(connectionListener);
    }

    private void setExpirationReconnectionTime() {
        int reconnectHangUpTimeMillis = SettingsUtil.getPreferenceInt(sharedPref, this, R.string.pref_disconnect_time_interval_key,
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
                    ringtonePlayer.stop();
                    hangUpCurrentSession();
                }
                ToastUtils.longToast("Call was stopped by timer");
            }
        };
    }


    private QBRTCSession getCurrentSession() {
        return currentSession;
    }

    public void rejectCurrentSession() {
        if (getCurrentSession() != null) {
            getCurrentSession().rejectCall(new HashMap<String, String>());
        }
    }

    public void hangUpCurrentSession() {
        ringtonePlayer.stop();
        if (getCurrentSession() != null) {
            getCurrentSession().hangUp(new HashMap<String, String>());
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
        QBChatService.getInstance().removeConnectionListener(connectionListener);
        stopBluetooth();
        audioManagerWeakRef.clear();
    }

    public void initCurrentSession(QBRTCSession session) {
        if (session != null) {
            Log.d(TAG, "Init new QBRTCSession");
            this.currentSession = session;
            this.currentSession.addSessionCallbacksListener(CallActivity.this);
            this.currentSession.addSignalingCallback(CallActivity.this);
        }
    }

    public void releaseCurrentSession() {
        Log.d(TAG, "Release current session");
        if (currentSession != null) {
            this.currentSession.removeSessionCallbacksListener(CallActivity.this);
            this.currentSession.removeSignalingCallback(CallActivity.this);
            rtcClient.removeSessionsCallbacksListener(CallActivity.this);
            this.currentSession = null;
        }
    }

    // ---------------Chat callback methods implementation  ----------------------//
    @Override
    public void onReceiveNewSession(final QBRTCSession session) {
        Log.d(TAG, "Session " + session.getSessionID() + " are income");
        if (getCurrentSession() != null) {
            Log.d(TAG, "Stop new session. Device now is busy");
            session.rejectCall(null);
        }
    }

    @Override
    public void onUserNotAnswer(QBRTCSession session, Integer userID) {
        if (!session.equals(getCurrentSession())) {
            return;
        }
        ringtonePlayer.stop();
    }

    @Override
    public void onUserNoActions(QBRTCSession qbrtcSession, Integer integer) {
        startIncomeCallTimer(0);
    }

    @Override
    public void onCallAcceptByUser(QBRTCSession session, Integer userId, Map<String, String> userInfo) {
        if (!session.equals(getCurrentSession())) {
            return;
        }
        ringtonePlayer.stop();
    }

    @Override
    public void onCallRejectByUser(QBRTCSession session, Integer userID, Map<String, String> userInfo) {
        if (!session.equals(getCurrentSession())) {
            return;
        }
        ringtonePlayer.stop();
    }

    @Override
    public void onConnectionClosedForUser(QBRTCSession session, Integer userID) {
        Log.d(TAG, "Connection closed for user: " + userID);
    }

    @Override
    public void onStateChanged(QBRTCSession qbrtcSession, BaseSession.QBRTCSessionState qbrtcSessionState) {

    }

    @Override
    public void onConnectedToUser(QBRTCSession session, final Integer userID) {
        callStarted = true;
        notifyCallStateListenersCallStarted();
        if (isInComingCall) {
            stopIncomeCallTimer();
        }
        Log.d(TAG, "onConnectedToUser() is started");
    }

    @Override
    public void onSessionClosed(final QBRTCSession session) {

        Log.d(TAG, "Session " + session.getSessionID() + " start stop session");

        if (session.equals(getCurrentSession())) {
            Log.d(TAG, "Stop session");

            if (audioManagerWeakRef.get() != null) {
                audioManagerWeakRef.get().stop();
            }
            releaseCurrentSession();
            finish();
        }
    }

    @Override
    public void onSessionStartClose(final QBRTCSession session) {
        if (session.equals(getCurrentSession())) {
            session.removeSessionCallbacksListener(CallActivity.this);
            notifyCallStateListenersCallStopped();
        }
    }

    @Override
    public void onDisconnectedFromUser(QBRTCSession session, Integer userID) {
        Log.d(TAG, "Disconnected from user: " + userID);
    }

    private void showToast(final int message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ToastUtils.shortToast(message);
            }
        });
    }

    private void showToast(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ToastUtils.shortToast(message);
            }
        });
    }

    @Override
    public void onReceiveHangUpFromUser(final QBRTCSession session, final Integer userID, Map<String, String> map) {
        if (session.equals(getCurrentSession())) {

            if (userID.equals(session.getCallerID())) {
                hangUpCurrentSession();
                Log.d(TAG, "initiator hung up the call");
            }

            QBUser participant = dbManager.getUserById(userID);
            final String participantName = participant != null ? participant.getFullName() : String.valueOf(userID);

            showToast("User " + participantName + " " + getString(R.string.text_status_hang_up) + " conversation");
        }
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
        BaseConversationFragment conversationFragment = BaseConversationFragment.newInstance(
                isVideoCall
                        ? new VideoConversationFragment()
                        : new AudioConversationFragment(),
                isIncomingCall);
        FragmentExecuotr.addFragment(getSupportFragmentManager(), R.id.fragment_container, conversationFragment, conversationFragment.getClass().getSimpleName());
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


    public void onUseHeadSet(boolean use) {
        audioManagerWeakRef.get().setManageHeadsetByDefault(use);
    }

    public void notifyAboutCurrentAudioDevice() {
        onChangeAudioDeviceCallback.audioDeviceChanged(audioManagerWeakRef.get().getSelectedAudioDevice());
    }

    ////////////////////////////// IncomeCallFragmentCallbackListener ////////////////////////////

    @Override
    public void onAcceptCurrentSession() {
        if (currentSession != null) {
            if(audioManagerWeakRef.get() == null) {
                initAudioManager();
            }
            startAudioManager();
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
        Fragment fragmentByTag = getSupportFragmentManager().findFragmentByTag(ScreenShareFragment.TAG);
        if (fragmentByTag instanceof ScreenShareFragment) {
            returnToCamera();
            super.onBackPressed();
        }
    }

    ////////////////////////////// ConversationFragmentCallbackListener ////////////////////////////

    @Override
    public void addTCClientConnectionCallback(QBRTCSessionStateCallback clientConnectionCallbacks) {
        if (currentSession != null) {
            currentSession.addSessionCallbacksListener(clientConnectionCallbacks);
        }
    }

    @Override
    public void addRTCSessionEventsCallback(QBRTCSessionEventsCallback eventsCallback) {
        QBRTCClient.getInstance(this).addSessionCallbacksListener(eventsCallback);
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
    public void onSetVideoEnabled(boolean isNeedEnableCam) {
        setVideoEnabled(isNeedEnableCam);
    }

    @Override
    public void onSwitchAudio() {
        Log.v(TAG, "onSwitchAudio(), SelectedAudioDevice() = " + audioManagerWeakRef.get().getSelectedAudioDevice());

        if (audioManagerWeakRef.get().getSelectedAudioDevice() != AppRTCAudioManager.AudioDevice.SPEAKER_PHONE) {
            audioManagerWeakRef.get().selectAudioDevice(AppRTCAudioManager.AudioDevice.SPEAKER_PHONE);
        } else {
            if (audioManagerWeakRef.get().getAudioDevices().contains(AppRTCAudioManager.AudioDevice.BLUETOOTH)) {
                audioManagerWeakRef.get().selectAudioDevice(AppRTCAudioManager.AudioDevice.BLUETOOTH);
            } else if (audioManagerWeakRef.get().getAudioDevices().contains(AppRTCAudioManager.AudioDevice.WIRED_HEADSET)) {
                audioManagerWeakRef.get().selectAudioDevice(AppRTCAudioManager.AudioDevice.WIRED_HEADSET);
            } else {
                audioManagerWeakRef.get().selectAudioDevice(AppRTCAudioManager.AudioDevice.EARPIECE);
            }
        }
    }

    @Override
    public void removeRTCClientConnectionCallback(QBRTCSessionStateCallback clientConnectionCallbacks) {
        if (currentSession != null) {
            currentSession.removeSessionCallbacksListener(clientConnectionCallbacks);
        }
    }

    @Override
    public void removeRTCSessionEventsCallback(QBRTCSessionEventsCallback eventsCallback) {
        QBRTCClient.getInstance(this).removeSessionsCallbacksListener(eventsCallback);
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
    public void addOnChangeAudioDeviceCallback(OnChangeAudioDevice onChangeDynamicCallback) {
        this.onChangeAudioDeviceCallback = onChangeDynamicCallback;
        notifyAboutCurrentAudioDevice();
    }

    @Override
    public void removeOnChangeAudioDeviceCallback(OnChangeAudioDevice onChangeDynamicCallback) {
        this.onChangeAudioDeviceCallback = null;
    }

    @Override
    public void onStopPreview() {
        onBackPressed();
    }

    private void startBluetooth() {
        Log.d(TAG, "startBluetooth");
        if (btAudioManager.isBluetoothScoAvailableOffCall()) {
            btBroadcastReceiver = new BluetoothBroadcastReceiver();
            registerReceiver(btBroadcastReceiver, new IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED));
            registerReceiver(btBroadcastReceiver, new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED));
            registerReceiver(btBroadcastReceiver, new IntentFilter(AudioManager.ACTION_SCO_AUDIO_STATE_CHANGED));

            // Need to set audio mode to MODE_IN_CALL for call to startBluetoothSco() to succeed.
            btAudioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);

            // CountDown repeatedly tries to start bluetooth Sco audio connection.
            btCountDown = new CountDown(COUNTDOWN_TIME, COUNTDOWN_INTERVAL);
            btCountDown.begin();

            // need for audio sco, see btBroadcastReceiver
            isBTStarting = true;
        }
    }

    private void stopBluetooth() {
        Log.d(TAG, "stopBluetooth");
        btCountDown.stop();

        // Need to stop Sco audio connection here when the app
        // change orientation or close with headset still turns on.
        try {
            unregisterReceiver(btBroadcastReceiver);
        } catch (IllegalArgumentException ignored) {

        }
        btAudioManager.stopBluetoothSco();
        btAudioManager.setMode(AudioManager.MODE_NORMAL);
    }

    private class BluetoothBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (action.equals(BluetoothDevice.ACTION_ACL_CONNECTED)) {
                BluetoothDevice connectedHeadset = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                BluetoothClass bluetoothClass = connectedHeadset.getBluetoothClass();
                Log.d(TAG, connectedHeadset.getName() + "connected");

                if (bluetoothClass != null) {
                    // Check if device is a headset. Besides the 2 below, are there other
                    // device classes also qualified as headset?
                    int deviceClass = bluetoothClass.getDeviceClass();

                    if (deviceClass == BluetoothClass.Device.AUDIO_VIDEO_HANDSFREE
                            || deviceClass == BluetoothClass.Device.AUDIO_VIDEO_WEARABLE_HEADSET) {
                        // start bluetooth Sco audio connection.
                        // Calling startBluetoothSco() always returns fail here,
                        // that's why a count down timer is implemented to call
                        // startBluetoothSco() in the onTick.
                        btAudioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
                        btCountDown.begin();

                        // override this if you want to do other thing when the device is connected.
                        // onHeadsetConnected();
                    }
                }
            } else if (action.equals(BluetoothDevice.ACTION_ACL_DISCONNECTED)) {
                Log.d(TAG, "Headset disconnected");
                btCountDown.stop();
                btAudioManager.setMode(AudioManager.MODE_NORMAL);

                // override this if you want to do other thing when the device is disconnected.
                // onHeadsetDisconnected();
            } else if (action.equals(AudioManager.ACTION_SCO_AUDIO_STATE_CHANGED)) {
                Integer state = intent.getIntExtra(AudioManager.EXTRA_SCO_AUDIO_STATE, AudioManager.SCO_AUDIO_STATE_ERROR);

                // When the device is connected before the application starts,
                // ACTION_ACL_CONNECTED will not be received
                if (state == AudioManager.SCO_AUDIO_STATE_CONNECTED) {
                    Log.d(TAG, "Sco connected");
                    if (isBTStarting) {
                        isBTStarting = false;
                    }
                    btCountDown.stop();

                } else if (state == AudioManager.SCO_AUDIO_STATE_DISCONNECTED) {
                    Log.d(TAG, "Sco disconnected");

                    // Always receive SCO_AUDIO_STATE_DISCONNECTED on call to startBluetooth()
                    // which at that stage we do not want to do anything. Thus the if condition.
                    if (!isBTStarting) {
                        // Need to call stopBluetoothSco(), otherwise startBluetoothSco()
                        // will not be successful.
                        btAudioManager.stopBluetoothSco();
                    }
                }
            }
        }
    }

    private class CountDown extends CountDownTimer {
        private boolean isCountingOn = false;

        CountDown(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        public void begin() {
            isCountingOn = true;
            start();
        }

        public void stop() {
            if (isCountingOn) {
                isCountingOn = false;
                cancel();
            }
        }

        @Override
        public void onTick(long millisUntilFinished) {
            Log.d(TAG, "\nonTick start bluetooth Sco");
            // When this call is successful, this count down timer will be canceled.
            try {
                btAudioManager.startBluetoothSco();
            } catch (Exception ignored) {

            }
        }

        @Override
        public void onFinish() {
            Log.d(TAG, "\nonFinish fail to connect to headset audio");
            // Calls to startBluetoothSco() in onTick are not successful.
            // Should implement something to inform user of this failure
            isCountingOn = false;
            btAudioManager.setMode(AudioManager.MODE_NORMAL);
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

    public interface OnChangeAudioDevice {
        void audioDeviceChanged(AppRTCAudioManager.AudioDevice newAudioDevice);
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
}