package com.quickblox.sample.conference.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.quickblox.conference.ConferenceClient;
import com.quickblox.conference.ConferenceSession;
import com.quickblox.conference.QBJanusRole;
import com.quickblox.conference.WsException;
import com.quickblox.conference.WsHangUpException;
import com.quickblox.conference.WsNoResponseException;
import com.quickblox.conference.callbacks.ConferenceSessionCallbacks;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.sample.conference.R;
import com.quickblox.sample.conference.fragments.AudioConversationFragment;
import com.quickblox.sample.conference.fragments.BaseConversationFragment;
import com.quickblox.sample.conference.fragments.ConversationFragmentCallbackListener;
import com.quickblox.sample.conference.fragments.OnCallEventsController;
import com.quickblox.sample.conference.fragments.VideoConversationFragment;
import com.quickblox.sample.conference.util.NetworkConnectionChecker;
import com.quickblox.sample.conference.utils.Consts;
import com.quickblox.sample.conference.utils.FragmentExecuotr;
import com.quickblox.sample.conference.utils.SettingsUtil;
import com.quickblox.sample.conference.utils.WebRtcSessionManager;
import com.quickblox.sample.core.utils.Toaster;
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

    private ConferenceSession currentSession;
    private String hangUpReason;
    private ConferenceClient rtcClient;
    private OnChangeDynamicToggle onChangeDynamicCallback;
    private SharedPreferences sharedPref;
    private LinearLayout connectionView;
    private AppRTCAudioManager audioManager;
    private NetworkConnectionChecker networkConnectionChecker;
    private WebRtcSessionManager sessionManager;
    private boolean isVideoCall;
    private ArrayList<CurrentCallStateCallback> currentCallStateCallbackList = new ArrayList<>();
    private ArrayList<Integer> opponentsIdsList;
    private boolean callStarted;
    private boolean previousDeviceEarPiece;
    private boolean showToastAfterHeadsetPlugged = true;
    private Set<Integer> subscribedPublishers = new CopyOnWriteArraySet<>();
    private volatile boolean connectedToJanus;
    private String dialogID;
    private boolean readyToSubscribe;
    private boolean asListenerRole;


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
//            we have already currentSession == null, so it's no reason to do further initialization
            finish();
            Log.d(TAG, "finish CallActivity");
            return;
        }
        isVideoCall = QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_VIDEO.equals(currentSession.getConferenceType());
        initCurrentSession(currentSession);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        initConferenceClient();
        initAudioManager();
        initWiFiManagerListener();

        connectionView = (LinearLayout) View.inflate(this, R.layout.connection_popup, null);

        startConversationFragment();
    }

    private boolean currentSessionExist() {
        currentSession = sessionManager.getCurrentSession();
        return currentSession != null;
    }

    @Override
    protected View getSnackbarAnchorView() {
        return null;
    }

    private void parseIntentExtras() {
        dialogID = getIntent().getExtras().getString(Consts.EXTRA_DIALOG_ID);
        opponentsIdsList = (ArrayList<Integer>) getIntent().getSerializableExtra(Consts.EXTRA_DIALOG_OCCUPANTS);
        asListenerRole = getIntent().getBooleanExtra(Consts.EXTRA_AS_LISTENER, false);
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

        if (isVideoCall) {
            audioManager.setDefaultAudioDevice(AppRTCAudioManager.AudioDevice.SPEAKER_PHONE);
            Log.d(TAG, "AppRTCAudioManager.AudioDevice.SPEAKER_PHONE");
        } else {
            audioManager.setDefaultAudioDevice(AppRTCAudioManager.AudioDevice.EARPIECE);
            previousDeviceEarPiece = true;
            Log.d(TAG, "AppRTCAudioManager.AudioDevice.EARPIECE");
        }

        audioManager.setOnWiredHeadsetStateListener(new AppRTCAudioManager.OnWiredHeadsetStateListener() {
            @Override
            public void onWiredHeadsetStateChanged(boolean plugged, boolean hasMicrophone) {
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

    private void initConferenceClient() {
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
                if (currentSession != null) {
                    leaveCurrentSession();
                }
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
        SettingsUtil.setSettingsStrategy(opponentsIdsList, sharedPref, CallActivity.this);
        QBRTCConfig.setDebugEnabled(true);
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

        Log.d(TAG, "onConnectedToUser() is started");
    }

    @Override
    public void onStateChanged(ConferenceSession session, BaseSession.QBRTCSessionState state) {
        if (BaseSession.QBRTCSessionState.QB_RTC_SESSION_CONNECTED.equals(state)) {
            connectedToJanus = true;
            Log.d(TAG, "onStateChanged and begin subscribeToAllGotPublisher");
        }
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
        QBJanusRole janusRole = asListenerRole ? QBJanusRole.LISTENER : QBJanusRole.PUBLISHER;
        currentSession.joinDialog(dialogID, janusRole, new JoinedCallback(userID));
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
                showToast(exception.getMessage());
                releaseCurrentSession();
                finish();
            }
        } else {
            showToast((WsNoResponseException.class.isInstance(exception)) ? getString(R.string.packet_failed) : exception.getMessage());
        }
    }

    @Override
    public void onSessionClosed(final ConferenceSession session) {
        Log.d(TAG, "Session " + session.getSessionID() + " start stop session");

        if (session.equals(currentSession)) {
            Log.d(TAG, "Stop session");

            if (audioManager != null) {
                audioManager.close();
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

    private class JoinedCallback implements QBEntityCallback<ArrayList<Integer>> {
        Integer userID;

        JoinedCallback(Integer userID) {
            this.userID = userID;
        }

        @Override
        public void onSuccess(ArrayList<Integer> publishers, Bundle params) {
            Log.d(TAG, "onSuccess joinDialog sessionUserID= " + userID + ", publishers= " + publishers);
            if(asListenerRole){
                connectedToJanus = true;
            }
        }

        @Override
        public void onError(QBResponseException exception) {
            Log.d(TAG, "onError joinDialog exception= " + exception);
            showToast("Join exception: " + exception.getMessage());
            releaseCurrentSession();
            finish();
        }
    }
}