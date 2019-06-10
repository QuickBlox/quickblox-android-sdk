package com.quickblox.sample.videochat.conference.java.activities;

import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;

import com.quickblox.conference.ConferenceClient;
import com.quickblox.conference.ConferenceSession;
import com.quickblox.conference.QBConferenceRole;
import com.quickblox.conference.WsException;
import com.quickblox.conference.WsHangUpException;
import com.quickblox.conference.WsNoResponseException;
import com.quickblox.conference.callbacks.ConferenceEntityCallback;
import com.quickblox.conference.callbacks.ConferenceSessionCallbacks;
import com.quickblox.sample.videochat.conference.java.R;
import com.quickblox.sample.videochat.conference.java.fragments.AudioConversationFragment;
import com.quickblox.sample.videochat.conference.java.fragments.BaseConversationFragment;
import com.quickblox.sample.videochat.conference.java.fragments.ConversationFragmentCallbackListener;
import com.quickblox.sample.videochat.conference.java.fragments.OnCallEventsController;
import com.quickblox.sample.videochat.conference.java.fragments.VideoConversationFragment;
import com.quickblox.sample.videochat.conference.java.util.NetworkConnectionChecker;
import com.quickblox.sample.videochat.conference.java.utils.Consts;
import com.quickblox.sample.videochat.conference.java.utils.FragmentExecuotr;
import com.quickblox.sample.videochat.conference.java.utils.SettingsUtil;
import com.quickblox.sample.videochat.conference.java.utils.ToastUtils;
import com.quickblox.sample.videochat.conference.java.utils.WebRtcSessionManager;
import com.quickblox.videochat.webrtc.AppRTCAudioManager;
import com.quickblox.videochat.webrtc.BaseSession;
import com.quickblox.videochat.webrtc.QBRTCCameraVideoCapturer;
import com.quickblox.videochat.webrtc.QBRTCConfig;
import com.quickblox.videochat.webrtc.QBRTCTypes;
import com.quickblox.videochat.webrtc.callbacks.QBRTCSessionStateCallback;

import org.webrtc.CameraVideoCapturer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * QuickBlox team
 */
public class CallActivity extends BaseActivity implements QBRTCSessionStateCallback<ConferenceSession>, ConferenceSessionCallbacks,
        OnCallEventsController, ConversationFragmentCallbackListener, NetworkConnectionChecker.OnConnectivityChangedListener {

    private static final String TAG = CallActivity.class.getSimpleName();
    private static final String ICE_FAILED_REASON = "ICE failed";
    private static final long COUNTDOWN_TIME = 10000;
    private static final long COUNTDOWN_INTERVAL = 1000;

    private ConferenceSession currentSession;
    private ConferenceClient rtcClient;
    private OnChangeDynamicToggle onChangeDynamicCallback;
    private SharedPreferences sharedPref;
    private AppRTCAudioManager audioManager;
    private NetworkConnectionChecker networkConnectionChecker;
    private WebRtcSessionManager sessionManager;
    private boolean isVideoCall;
    private ArrayList<CurrentCallStateCallback> currentCallStateCallbackList = new ArrayList<>();
    private ArrayList<Integer> opponentsIdsList;
    private boolean callStarted;
    private boolean previousDeviceEarPiece;
    private Set<Integer> subscribedPublishers = new CopyOnWriteArraySet<>();
    private volatile boolean connectedToJanus;
    private String dialogID;
    private boolean readyToSubscribe;
    private boolean asListenerRole;

    private AudioManager btAudioManager;

    private CountDown btCountDown;
    private BluetoothBroadcastReceiver btBroadcastReceiver;
    private boolean isBTStarting;


    public static void start(Context context, String dialogID, List<Integer> occupants, boolean listenerRole) {
        Intent intent = new Intent(context, CallActivity.class);
        intent.putExtra(Consts.EXTRA_DIALOG_ID, dialogID);
        intent.putExtra(Consts.EXTRA_DIALOG_OCCUPANTS, (Serializable) occupants);
        intent.putExtra(Consts.EXTRA_AS_LISTENER, listenerRole);

        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        parseIntentExtras();
        sessionManager = WebRtcSessionManager.getInstance(this);
        if (!currentSessionExist()) {
            //we have already currentSession == null, so it's no reason to do further initialization
            finish();
            Log.d(TAG, "finish CallActivity");
            return;
        }
        initCurrentSession(currentSession);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        btAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        initConferenceClient();
        initAudioManager();
        startBluetooth();
        initWiFiManagerListener();

        startConversationFragment();
    }

    private boolean currentSessionExist() {
        currentSession = sessionManager.getCurrentSession();
        return currentSession != null;
    }

    private void parseIntentExtras() {
        dialogID = getIntent().getExtras().getString(Consts.EXTRA_DIALOG_ID);
        opponentsIdsList = (ArrayList<Integer>) getIntent().getSerializableExtra(Consts.EXTRA_DIALOG_OCCUPANTS);
        asListenerRole = getIntent().getBooleanExtra(Consts.EXTRA_AS_LISTENER, false);
    }

    private void initAudioManager() {
        audioManager = AppRTCAudioManager.create(this);
        isVideoCall = QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_VIDEO.equals(currentSession.getConferenceType());
        if (isVideoCall) {
            audioManager.setDefaultAudioDevice(AppRTCAudioManager.AudioDevice.SPEAKER_PHONE);
            previousDeviceEarPiece = false;
            Log.d(TAG, "AppRTCAudioManager.AudioDevice.SPEAKER_PHONE");
        } else {
            audioManager.setDefaultAudioDevice(AppRTCAudioManager.AudioDevice.EARPIECE);
            audioManager.setManageSpeakerPhoneByProximity(false);
            previousDeviceEarPiece = true;
            Log.d(TAG, "AppRTCAudioManager.AudioDevice.EARPIECE");
        }

        audioManager.setOnWiredHeadsetStateListener(new AppRTCAudioManager.OnWiredHeadsetStateListener() {
            @Override
            public void onWiredHeadsetStateChanged(boolean plugged, boolean hasMicrophone) {
                if (callStarted) {
                    ToastUtils.shortToast("Headset " + (hasMicrophone ? "with microphone" : "without microphone") + (plugged ? "plugged" : "unplugged"));
                }
                if (onChangeDynamicCallback != null) {
                    if (!plugged) {
                        if (previousDeviceEarPiece) {
                            setAudioDeviceDelayed(AppRTCAudioManager.AudioDevice.EARPIECE);
                        } else {
                            setAudioDeviceDelayed(AppRTCAudioManager.AudioDevice.SPEAKER_PHONE);
                        }
                    }
                    if (onChangeDynamicCallback != null) {
                        onChangeDynamicCallback.enableDynamicToggle(plugged, previousDeviceEarPiece);
                    }
                }
            }
        });

        audioManager.setBluetoothAudioDeviceStateListener(new AppRTCAudioManager.BluetoothAudioDeviceStateListener() {
            @Override
            public void onStateChanged(boolean connected) {
                if (callStarted) {
                    ToastUtils.shortToast("Bluetooth " + (connected ? "connected" : "disconnected"));
                }
            }
        });
    }

    private void setAudioDeviceDelayed(final AppRTCAudioManager.AudioDevice audioDevice) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                audioManager.selectAudioDevice(audioDevice);
            }
        }, 500);
    }

    private void initConferenceClient() {
        rtcClient = ConferenceClient.getInstance(this);

        rtcClient.setCameraErrorHandler(new CameraVideoCapturer.CameraEventsHandler() {
            @Override
            public void onCameraError(final String s) {
                ToastUtils.shortToast("Camera error: " + s);
            }

            @Override
            public void onCameraDisconnected() {
                ToastUtils.shortToast("Camera onCameraDisconnected: ");
            }

            @Override
            public void onCameraFreezed(String s) {
                ToastUtils.shortToast("Camera freezed: " + s);
                if (currentSession != null) {
                    leaveCurrentSession();
                }
            }

            @Override
            public void onCameraOpening(String s) {
                ToastUtils.shortToast("Camera aOpening: " + s);
            }

            @Override
            public void onFirstFrameAvailable() {
                ToastUtils.shortToast("onFirstFrameAvailable: ");
            }

            @Override
            public void onCameraClosed() {
            }
        });

        // Configure
        SettingsUtil.setSettingsStrategy(opponentsIdsList, sharedPref, CallActivity.this);
        QBRTCConfig.setDebugEnabled(true);
    }

    @Override
    public void connectivityChanged(boolean availableNow) {
        if (callStarted) {
            ToastUtils.shortToast("Internet connection " + (availableNow ? "available" : " unavailable"));
        }
    }

    private void initWiFiManagerListener() {
        networkConnectionChecker = new NetworkConnectionChecker(getApplication());
    }

    public void leaveCurrentSession() {
        currentSession.leave();
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

    @Override
    protected void onResume() {
        super.onResume();
        readyToSubscribe = true;
        subscribeToPublishersIfNeed();
        networkConnectionChecker.registerListener(this);
    }

    private void subscribeToPublishersIfNeed() {
        Set<Integer> notSubscribedPublishers = new CopyOnWriteArraySet<>(currentSession.getActivePublishers());
        notSubscribedPublishers.removeAll(subscribedPublishers);
        if (!notSubscribedPublishers.isEmpty()) {
            subscribeToPublishers(new ArrayList<>(notSubscribedPublishers));
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        readyToSubscribe = false;
        networkConnectionChecker.unregisterListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopBluetooth();
    }

    public void initCurrentSession(ConferenceSession session) {
        if (session != null) {
            Log.d(TAG, "Init new ConferenceSession");
            this.currentSession = session;
            this.currentSession.addSessionCallbacksListener(CallActivity.this);
            this.currentSession.addConferenceSessionListener(CallActivity.this);
        }
    }

    public void releaseCurrentSession() {
        Log.d(TAG, "Release current session");
        if (currentSession != null) {
            leaveCurrentSession();
            this.currentSession.removeSessionCallbacksListener(CallActivity.this);
            this.currentSession.removeConferenceSessionListener(CallActivity.this);
            this.currentSession = null;
        }
    }

    // ---------------Chat callback methods implementation  ----------------------//

    @Override
    public void onConnectionClosedForUser(ConferenceSession session, Integer userID) {
        Log.d(TAG, "QBRTCSessionStateCallbackImpl onConnectionClosedForUser userID=" + userID);
    }

    @Override
    public void onConnectedToUser(ConferenceSession session, final Integer userID) {
        Log.d(TAG, "onConnectedToUser userID= " + userID + " sessionID= " + session.getSessionID());
        callStarted = true;
        notifyCallStateListenersCallStarted();

        Log.d(TAG, "onConnectedToUser() is started");
    }

    @Override
    public void onStateChanged(ConferenceSession session, BaseSession.QBRTCSessionState state) {
        if (BaseSession.QBRTCSessionState.QB_RTC_SESSION_CONNECTED.equals(state)) {
            connectedToJanus = true;
            Log.d(TAG, "onStateChanged and begin subscribeToPublishersIfNeed");
            subscribeToPublishersIfNeed();
        }
    }

    @Override
    public void onDisconnectedFromUser(ConferenceSession session, Integer userID) {
        Log.d(TAG, "QBRTCSessionStateCallbackImpl onDisconnectedFromUser userID=" + userID);
    }

    private void startConversationFragment() {
        Bundle bundle = new Bundle();
        bundle.putIntegerArrayList(Consts.EXTRA_DIALOG_OCCUPANTS, opponentsIdsList);
        bundle.putBoolean(Consts.EXTRA_AS_LISTENER, asListenerRole);
        BaseConversationFragment conversationFragment = BaseConversationFragment.newInstance(
                isVideoCall
                        ? new VideoConversationFragment()
                        : new AudioConversationFragment());
        conversationFragment.setArguments(bundle);
        FragmentExecuotr.addFragment(getSupportFragmentManager(), R.id.fragment_container, conversationFragment, conversationFragment.getClass().getSimpleName());
    }


    public void onUseHeadSet(boolean use) {
        audioManager.setManageHeadsetByDefault(use);
    }

    @Override
    public void onBackPressed() {
    }

    ////////////////////////////// ConversationFragmentCallbackListener ////////////////////////////

    @Override
    public void addClientConnectionCallback(QBRTCSessionStateCallback clientConnectionCallbacks) {
        if (currentSession != null) {
            currentSession.addSessionCallbacksListener(clientConnectionCallbacks);
        }
    }

    @Override
    public void onSetAudioEnabled(boolean isAudioEnabled) {
        setAudioEnabled(isAudioEnabled);
    }

    @Override
    public void onLeaveCurrentSession() {
        leaveCurrentSession();
    }

    @Override
    public void onSwitchCamera(CameraVideoCapturer.CameraSwitchHandler cameraSwitchHandler) {
        ((QBRTCCameraVideoCapturer) (currentSession.getMediaStreamManager().getVideoCapturer()))
                .switchCamera(cameraSwitchHandler);
    }

    @Override
    public void onStartJoinConference() {
        int userID = currentSession.getCurrentUserID();
        QBConferenceRole conferenceRole = asListenerRole ? QBConferenceRole.LISTENER : QBConferenceRole.PUBLISHER;
        currentSession.joinDialog(dialogID, conferenceRole, new JoinedCallback(userID));
    }

    @Override
    public void onSetVideoEnabled(boolean isNeedEnableCam) {
        setVideoEnabled(isNeedEnableCam);
    }

    @Override
    public void onSwitchAudio() {
        Log.v(TAG, "onSwitchAudio(), SelectedAudioDevice() = " + audioManager.getSelectedAudioDevice());

        if (audioManager.getSelectedAudioDevice() != AppRTCAudioManager.AudioDevice.SPEAKER_PHONE) {
            audioManager.selectAudioDevice(AppRTCAudioManager.AudioDevice.SPEAKER_PHONE);
        } else {
            if (audioManager.getAudioDevices().contains(AppRTCAudioManager.AudioDevice.BLUETOOTH)) {
                audioManager.selectAudioDevice(AppRTCAudioManager.AudioDevice.BLUETOOTH);
            } else if (audioManager.getAudioDevices().contains(AppRTCAudioManager.AudioDevice.WIRED_HEADSET)) {
                audioManager.selectAudioDevice(AppRTCAudioManager.AudioDevice.WIRED_HEADSET);
            } else {
                audioManager.selectAudioDevice(AppRTCAudioManager.AudioDevice.EARPIECE);
            }
        }
    }

    @Override
    public void removeClientConnectionCallback(QBRTCSessionStateCallback clientConnectionCallbacks) {
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
    }

    @Override
    public void removeOnChangeDynamicToggle(OnChangeDynamicToggle onChangeDynamicCallback) {
        this.onChangeDynamicCallback = null;
    }

    ////////////////////////////// ConferenceSessionCallbacks ////////////////////////////

    private void subscribeToPublishers(ArrayList<Integer> publishersList) {
        subscribedPublishers.addAll(currentSession.getActivePublishers());
        for (Integer publisher : publishersList) {
            currentSession.subscribeToPublisher(publisher);
        }
    }

    @Override
    public void onPublishersReceived(ArrayList<Integer> publishersList) {
        Log.d(TAG, "OnPublishersReceived connectedToJanus " + connectedToJanus + ", readyToSubscribe= " + readyToSubscribe);
        if (connectedToJanus && readyToSubscribe) {
            subscribedPublishers.addAll(publishersList);
            subscribeToPublishers(publishersList);
        }
    }

    @Override
    public void onPublisherLeft(Integer userID) {
        Log.d(TAG, "OnPublisherLeft userID" + userID);
        subscribedPublishers.remove(userID);
    }

    @Override
    public void onMediaReceived(String type, boolean success) {
        Log.d(TAG, "OnMediaReceived type " + type + ", success" + success);
    }

    @Override
    public void onSlowLinkReceived(boolean uplink, int nacks) {
        Log.d(TAG, "OnSlowLinkReceived uplink " + uplink + ", nacks" + nacks);
    }

    @Override
    public void onError(WsException exception) {
        Log.d(TAG, "OnError getClass= " + exception.getClass());
        if (WsHangUpException.class.isInstance(exception)) {
            Log.d(TAG, "OnError exception= " + exception.getMessage());
            if (exception.getMessage().equals(ICE_FAILED_REASON)) {
                ToastUtils.shortToast(exception.getMessage());
                releaseCurrentSession();
                finish();
            }
        } else {
            ToastUtils.shortToast((WsNoResponseException.class.isInstance(exception)) ? getString(R.string.packet_failed) : exception.getMessage());
        }
    }

    @Override
    public void onSessionClosed(final ConferenceSession session) {
        Log.d(TAG, "Session " + session.getSessionID() + " start stop session");

        if (session.equals(currentSession)) {
            Log.d(TAG, "Stop session");

            if (audioManager != null) {
                audioManager.stop();
            }
            releaseCurrentSession();
            finish();
        }
    }

    //////////////////////////////////////////   end   /////////////////////////////////////////////

    public interface OnChangeDynamicToggle {
        void enableDynamicToggle(boolean plugged, boolean wasEarpiece);
    }


    public interface CurrentCallStateCallback {
        void onCallStarted();
    }

    private void notifyCallStateListenersCallStarted() {
        for (CurrentCallStateCallback callback : currentCallStateCallbackList) {
            callback.onCallStarted();
        }
    }

    private void startBluetooth() {
        Log.d(TAG, "startBluetooth");
        if (btAudioManager.isBluetoothScoAvailableOffCall()) {
            btBroadcastReceiver = new BluetoothBroadcastReceiver();
            registerReceiver(btBroadcastReceiver, new IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED));
            registerReceiver(btBroadcastReceiver, new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED));
            registerReceiver(btBroadcastReceiver, new IntentFilter(AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED));

            // Need to set audio mode to MODE_IN_COMMUNICATION for call to startBluetoothSco() to succeed.
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
                    }
                }
            } else if (action.equals(BluetoothDevice.ACTION_ACL_DISCONNECTED)) {
                Log.d(TAG, "Headset disconnected");
                btCountDown.stop();
                btAudioManager.setMode(AudioManager.MODE_NORMAL);
            } else if (action.equals(AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED)) {
                Integer state = intent.getIntExtra(AudioManager.EXTRA_SCO_AUDIO_STATE, AudioManager.SCO_AUDIO_STATE_ERROR);

                if (state == AudioManager.SCO_AUDIO_STATE_CONNECTED) {
                    Log.d(TAG, "Sco connected");
                    if (isBTStarting) {
                        isBTStarting = false;
                    }
                    btCountDown.stop();

                    // When the device is connected before the application starts,
                    // ACTION_ACL_CONNECTED will not be received
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

        void begin() {
            isCountingOn = true;
            start();
        }

        void stop() {
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

    private class JoinedCallback implements ConferenceEntityCallback<ArrayList<Integer>> {
        Integer userID;

        JoinedCallback(Integer userID) {
            this.userID = userID;
        }

        @Override
        public void onSuccess(ArrayList<Integer> publishers) {
            Log.d(TAG, "onSuccess joinDialog sessionUserID= " + userID + ", publishers= " + publishers);
            if (rtcClient.isAutoSubscribeAfterJoin()) {
                subscribedPublishers.addAll(publishers);
            }
            if (asListenerRole) {
                connectedToJanus = true;
            }
        }

        @Override
        public void onError(WsException exception) {
            Log.d(TAG, "onError joinDialog exception= " + exception);
            ToastUtils.shortToast("Join exception: " + exception.getMessage());
            releaseCurrentSession();
            finish();
        }
    }
}