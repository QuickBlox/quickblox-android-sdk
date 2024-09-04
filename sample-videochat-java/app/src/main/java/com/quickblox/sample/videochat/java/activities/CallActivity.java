package com.quickblox.sample.videochat.java.activities;

import static com.quickblox.sample.videochat.java.services.CallService.ONE_OPPONENT;
import static com.quickblox.videochat.webrtc.BaseSession.QBRTCSessionState.QB_RTC_SESSION_NEW;
import static com.quickblox.videochat.webrtc.BaseSession.QBRTCSessionState.QB_RTC_SESSION_PENDING;

import android.app.Activity;
import android.app.KeyguardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.quickblox.sample.videochat.java.R;
import com.quickblox.sample.videochat.java.db.UsersDbManager;
import com.quickblox.sample.videochat.java.fragments.AudioConversationFragment;
import com.quickblox.sample.videochat.java.fragments.BaseConversationFragment;
import com.quickblox.sample.videochat.java.fragments.ConversationFragmentCallback;
import com.quickblox.sample.videochat.java.fragments.IncomeCallFragment;
import com.quickblox.sample.videochat.java.fragments.IncomeCallFragmentCallbackListener;
import com.quickblox.sample.videochat.java.fragments.ScreenShareFragment;
import com.quickblox.sample.videochat.java.fragments.VideoConversationFragment;
import com.quickblox.sample.videochat.java.services.CallService;
import com.quickblox.sample.videochat.java.utils.Consts;
import com.quickblox.sample.videochat.java.utils.ErrorUtils;
import com.quickblox.sample.videochat.java.utils.FragmentExecuotr;
import com.quickblox.sample.videochat.java.utils.PermissionsChecker;
import com.quickblox.sample.videochat.java.utils.QBEntityCallbackImpl;
import com.quickblox.sample.videochat.java.utils.SettingsManager;
import com.quickblox.sample.videochat.java.utils.SharedPrefsHelper;
import com.quickblox.sample.videochat.java.utils.ToastUtils;
import com.quickblox.sample.videochat.java.utils.UsersUtils;
import com.quickblox.sample.videochat.java.utils.WebRtcSessionManager;
import com.quickblox.users.model.QBUser;
import com.quickblox.videochat.webrtc.BaseSession;
import com.quickblox.videochat.webrtc.QBRTCScreenCapturer;
import com.quickblox.videochat.webrtc.QBRTCSession;
import com.quickblox.videochat.webrtc.QBRTCTypes;
import com.quickblox.videochat.webrtc.audio.QBAudioManager;
import com.quickblox.videochat.webrtc.callbacks.QBRTCClientSessionCallbacks;
import com.quickblox.videochat.webrtc.callbacks.QBRTCClientVideoTracksCallbacks;
import com.quickblox.videochat.webrtc.callbacks.QBRTCSessionEventsCallback;
import com.quickblox.videochat.webrtc.callbacks.QBRTCSessionStateCallback;
import com.quickblox.videochat.webrtc.view.QBRTCVideoTrack;

import org.jivesoftware.smack.AbstractConnectionListener;
import org.jivesoftware.smack.ConnectionListener;
import org.webrtc.CameraVideoCapturer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * QuickBlox team
 */
public class CallActivity extends BaseActivity implements IncomeCallFragmentCallbackListener,
        QBRTCSessionStateCallback<QBRTCSession>, QBRTCClientSessionCallbacks,
        ConversationFragmentCallback, ScreenShareFragment.OnSharingEvents {
    private static final String TAG = CallActivity.class.getSimpleName();

    public static final String INCOME_CALL_FRAGMENT = "income_call_fragment";
    public static final int REQUEST_PERMISSION_SETTING = 545;
    public static final int REQUEST_SHARING_MEDIA_PROJECTION = 1060;

    private final ArrayList<CurrentCallStateCallback> currentCallStateCallbackList = new ArrayList<>();
    private final UsersDbManager dbManager = UsersDbManager.getInstance();
    private Handler showIncomingCallWindowTaskHandler;
    private ConnectionListenerImpl connectionListener;
    private ServiceConnection callServiceConnection;
    private Runnable showIncomingCallWindowTask;
    private boolean isInComingCall = false;
    private List<Integer> opponentIds;
    private boolean isVideoCall = false;
    private PermissionsChecker checker;
    private CallService callService;
    private LinearLayout connectionView;

    public static void start(Context context, boolean isIncomingCall) {
        Intent intent = new Intent(context, CallActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(Consts.EXTRA_IS_INCOMING_CALL, isIncomingCall);
        SharedPrefsHelper.getInstance().save(Consts.EXTRA_IS_INCOMING_CALL, isIncomingCall);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        allowOnLockScreen();
        setContentView(R.layout.activity_main);
        checker = new PermissionsChecker(this);
        connectionView = (LinearLayout) View.inflate(CallActivity.this, R.layout.connection_popup, null);

        boolean isVideoCall = isVideoSession();
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.TIRAMISU && isVideoCall && isNewSession()) {
            requestSharingPermissions(this);
            return;
        }

        CallService.start(this);
    }

    private boolean isNewSession() {
        QBRTCSession currentSession = WebRtcSessionManager.getInstance(getApplicationContext()).getCurrentSession();
        if (currentSession == null) {
            return false;
        }

        BaseSession.QBRTCSessionState sessionState = currentSession.getState();
        if (sessionState == null) {
            return false;
        }

        return currentSession.getState().equals(QB_RTC_SESSION_NEW) || currentSession.getState().equals(QB_RTC_SESSION_PENDING);
    }

    private boolean isVideoSession() {
        QBRTCSession currentSession = WebRtcSessionManager.getInstance(getApplicationContext()).getCurrentSession();
        return currentSession != null && currentSession.getConferenceType().equals(QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_VIDEO);
    }

    public void requestSharingPermissions(Activity context) {
        MediaProjectionManager mMediaProjectionManager = (MediaProjectionManager) context.getSystemService(MEDIA_PROJECTION_SERVICE);
        context.startActivityForResult(mMediaProjectionManager.createScreenCaptureIntent(), REQUEST_SHARING_MEDIA_PROJECTION);
    }

    @Override
    protected void onResume() {
        super.onResume();
        bindCallService();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unbindService(callServiceConnection);
        if (callService != null) {
            removeListeners();
        }
    }

    @Override
    public void finish() {
        CallService.stop(this);
        OpponentsActivity.start(this);
        super.finish();
    }

    @Override
    public void onBackPressed() {
        // to prevent returning from Call Fragment
        super.onBackPressed();
    }

    private void allowOnLockScreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
            KeyguardManager keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
            keyguardManager.requestDismissKeyguard(this, null);
        } else {
            this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        }
    }

    private void initScreen() {
        callService.setCallTimerCallback(new CallTimerCallback());
        isVideoCall = callService.isVideoCall();
        opponentIds = callService.getOpponentIds();

        SettingsManager.applySettings();
        addListeners();

        if (getIntent() != null && getIntent().getExtras() != null) {
            isInComingCall = getIntent().getExtras().getBoolean(Consts.EXTRA_IS_INCOMING_CALL, false);
        } else {
            isInComingCall = sharedPrefsHelper.get(Consts.EXTRA_IS_INCOMING_CALL, false);
        }

        if (callService.isCallMode()) {
            checkPermission();
            if (callService.isSharingScreenState()) {
                startScreenSharing(null);
                return;
            }
            addConversationFragment(isInComingCall);
        } else {
            if (!isInComingCall) {
                callService.playRingtone();
            }
            startSuitableFragment(isInComingCall);
        }
    }

    private void addListeners() {
        addSessionEventsListener(this);
        addSessionStateListener(this);

        connectionListener = new ConnectionListenerImpl();
        addConnectionListener(connectionListener);
    }

    private void removeListeners() {
        removeSessionEventsListener(this);
        removeSessionStateListener(this);
        removeConnectionListener(connectionListener);
        callService.removeCallTimerCallback();
    }

    private void bindCallService() {
        callServiceConnection = new CallServiceConnection();
        Intent intent = new Intent(this, CallService.class);
        bindService(intent, callServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i(TAG, "onActivityResult requestCode=" + requestCode + ", resultCode= " + resultCode);
        if (resultCode == Consts.EXTRA_LOGIN_RESULT_CODE) {
            initScreen();
        }
        if (requestCode == QBRTCScreenCapturer.REQUEST_MEDIA_PROJECTION
                && resultCode == Activity.RESULT_OK && data != null) {
            startScreenSharing(data);
            Log.i(TAG, "Starting Screen Capture");
        }

        if (requestCode == REQUEST_SHARING_MEDIA_PROJECTION) {
            if (resultCode == Activity.RESULT_OK) {
                CallService.start(this);
            } else {
                finish();
            }
        }
    }


    private void startScreenSharing(final Intent data) {
        Fragment fragmentByTag = getSupportFragmentManager().findFragmentByTag(ScreenShareFragment.class.getSimpleName());
        if (!(fragmentByTag instanceof ScreenShareFragment)) {
            ScreenShareFragment screenShareFragment = ScreenShareFragment.newInstance();
            FragmentExecuotr.addFragment(getSupportFragmentManager(), R.id.fragment_container, screenShareFragment, ScreenShareFragment.class.getSimpleName());

            if (data != null) {
                callService.startScreenSharing(data);
            }
        }
    }

    private void startSuitableFragment(boolean isInComingCall) {
        QBRTCSession session = WebRtcSessionManager.getInstance(this).getCurrentSession();
        if (session != null) {
            startLoadAbsentUsers();
            if (isInComingCall) {
                initIncomingCallTask();
                addIncomeCallFragment();
                startLoadAbsentUsers();
                checkPermission();
            } else {
                addConversationFragment(false);
                getIntent().removeExtra(Consts.EXTRA_IS_INCOMING_CALL);
                sharedPrefsHelper.save(Consts.EXTRA_IS_INCOMING_CALL, false);
            }
        } else {
            finish();
        }
    }

    private void checkPermission() {
        boolean cam = SharedPrefsHelper.getInstance().get(Consts.PERMISSIONS[0], true);
        boolean mic = SharedPrefsHelper.getInstance().get(Consts.PERMISSIONS[1], true);

        if (isVideoCall && checker.lacksPermissions(Consts.PERMISSIONS)) {
            if (cam) {
                PermissionsActivity.startActivity(this, false, Consts.PERMISSIONS);
            } else {
                View rootView = getWindow().getDecorView().findViewById(android.R.id.content);
                ErrorUtils.showSnackbar(rootView, R.string.error_permission_video, R.string.dlg_allow, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startPermissionSystemSettings();
                    }
                });
            }
        } else if (checker.lacksPermissions(Consts.PERMISSIONS[1])) {
            if (mic) {
                PermissionsActivity.startActivity(this, true, Consts.PERMISSIONS);
            } else {
                View rootView = getWindow().getDecorView().findViewById(android.R.id.content);
                ErrorUtils.showSnackbar(rootView, R.string.error_permission_audio, R.string.dlg_allow, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startPermissionSystemSettings();
                    }
                });
            }
        }
    }

    private void startPermissionSystemSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivityForResult(intent, REQUEST_PERMISSION_SETTING);
    }

    private void startLoadAbsentUsers() {
        ArrayList<QBUser> usersFromDb = dbManager.getAllUsers();
        ArrayList<Integer> allParticipantsOfCall = new ArrayList<>();

        if (opponentIds != null) {
            allParticipantsOfCall.addAll(opponentIds);
        }

        if (isInComingCall) {
            Integer callerID = callService.getCallerId();
            if (callerID != null) {
                allParticipantsOfCall.add(callerID);
            }
        }

        ArrayList<Integer> idsUsersNeedLoad = UsersUtils.getIdsNotLoadedUsers(usersFromDb, allParticipantsOfCall);
        if (!idsUsersNeedLoad.isEmpty()) {
            requestExecutor.loadUsersByIds(idsUsersNeedLoad, new QBEntityCallbackImpl<ArrayList<QBUser>>() {
                @Override
                public void onSuccess(ArrayList<QBUser> users, Bundle params) {
                    dbManager.saveAllUsers(users, false);
                    notifyCallStateListenersNeedUpdateOpponentsList(users);
                }
            });
        }
    }

    private void initIncomingCallTask() {
        showIncomingCallWindowTaskHandler = new Handler(Looper.myLooper());
        showIncomingCallWindowTask = new Runnable() {
            @Override
            public void run() {
                ToastUtils.longToast("Call was stopped by UserNoActions timer");
                callService.clearCallState();
                callService.clearButtonsState();
                WebRtcSessionManager.getInstance(getApplicationContext()).setCurrentSession(null);
                CallService.stop(CallActivity.this);
                finish();
            }
        };
    }

    public void hangUpCurrentSession() {
        callService.stopRingtone();
        if (!callService.hangUpCurrentSession(new HashMap<>())) {
            CallService.stop(this);
            finish();
        }
    }

    private void startIncomeCallTimer(long time) {
        Log.d(TAG, "startIncomeCallTimer");
        if (showIncomingCallWindowTaskHandler != null && showIncomingCallWindowTask != null) {
            showIncomingCallWindowTaskHandler.postAtTime(showIncomingCallWindowTask, SystemClock.uptimeMillis() + time);
        }
    }

    private void stopIncomeCallTimer() {
        Log.d(TAG, "stopIncomeCallTimer");
        if (showIncomingCallWindowTask != null) {
            showIncomingCallWindowTaskHandler.removeCallbacks(showIncomingCallWindowTask);
        }
    }

    private void addIncomeCallFragment() {
        Log.d(TAG, "Adding IncomeCallFragment");
        if (callService.currentSessionExist()) {
            IncomeCallFragment fragment = new IncomeCallFragment();
            FragmentExecuotr.addFragment(getSupportFragmentManager(), R.id.fragment_container, fragment, INCOME_CALL_FRAGMENT);
        } else {
            Log.d(TAG, "SKIP Adding IncomeCallFragment");
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

    private void showConnectionPopUp() {
        runOnUiThread(() -> {
            ((TextView) connectionView.findViewById(R.id.notification)).setText(R.string.connection_was_lost);
            if (connectionView.getParent() == null) {
                ((ViewGroup) CallActivity.this.findViewById(R.id.fragment_container)).addView(connectionView);
            }
        });
    }

    private void hideConnectionPopUp() {
        runOnUiThread(() -> {
            ((ViewGroup) CallActivity.this.findViewById(R.id.fragment_container)).removeView(connectionView);
        });
    }

    private class ConnectionListenerImpl extends AbstractConnectionListener {
        @Override
        public void connectionClosedOnError(Exception e) {
            showConnectionPopUp();
        }

        @Override
        public void reconnectionSuccessful() {
            hideConnectionPopUp();
        }
    }

    @Override
    public void onDisconnectedFromUser(QBRTCSession session, Integer userID) {
        Log.d(TAG, "Disconnected from user: " + userID);
    }

    @Override
    public void onConnectedToUser(QBRTCSession session, final Integer userID) {
        notifyCallStateListenersCallStarted();
        if (isInComingCall) {
            stopIncomeCallTimer();
        }
        Log.d(TAG, "onConnectedToUser() is started");
    }

    @Override
    public void onConnectionClosedForUser(QBRTCSession session, Integer userID) {
        Log.d(TAG, "Connection closed for user: " + userID);
    }

    @Override
    public void onStateChanged(QBRTCSession qbrtcSession, BaseSession.QBRTCSessionState qbrtcSessionState) {

    }

    @Override
    public void onUserNotAnswer(QBRTCSession session, Integer userID) {
        if (callService.isCurrentSession(session)) {
            callService.stopRingtone();
        }
    }

    @Override
    public void onSessionStartClose(final QBRTCSession session) {
        if (callService.isCurrentSession(session)) {
            callService.removeSessionStateListener(this);
            notifyCallStateListenersCallStopped();
        }
    }

    @Override
    public void onReceiveHangUpFromUser(final QBRTCSession session, final Integer userID, Map<String, String> map) {
        if (callService.isCurrentSession(session)) {
            int numberOpponents = session.getOpponents().size();
            if (numberOpponents == ONE_OPPONENT) {
                hangUpCurrentSession();
                Log.d(TAG, "Initiator hung up the call");
            } else {
                if (session.getState() == QB_RTC_SESSION_PENDING && userID.equals(session.getCallerID())) {
                    hangUpCurrentSession();
                }
            }

            QBUser participant = dbManager.getUserById(userID);
            final String participantName = participant != null ? participant.getFullName() : String.valueOf(userID);
            ToastUtils.shortToast("User " + participantName + " " + getString(R.string.text_status_hang_up) + " conversation");
        }
    }

    @Override
    public void onChangeReconnectionState(QBRTCSession qbrtcSession, Integer userId, QBRTCTypes.QBRTCReconnectionState qbrtcReconnectionState) {
        // empty
    }

    @Override
    public void onCallAcceptByUser(QBRTCSession session, Integer userId, Map<String, String> userInfo) {
        if (callService.isCurrentSession(session)) {
            callService.stopRingtone();
        }
    }

    @Override
    public void onReceiveNewSession(final QBRTCSession session) {
        Log.d(TAG, "Session " + session.getSessionID() + " Received");
    }


    @Override
    public void onUserNoActions(QBRTCSession qbrtcSession, Integer integer) {
        startIncomeCallTimer(0);
    }

    @Override
    public void onSessionClosed(final QBRTCSession session) {
        if (callService.isCurrentSession(session)) {
            Log.d(TAG, "Stopping session");
            callService.stopForeground(true);
            finish();
        }
    }

    @Override
    public void onCallRejectByUser(QBRTCSession session, Integer userID, Map<String, String> userInfo) {
        // empty
    }

    @Override
    public void onAcceptCurrentSession() {
        if (callService.currentSessionExist()) {
            addConversationFragment(true);
        } else {
            Log.d(TAG, "SKIP addConversationFragment method");
        }
    }

    @Override
    public void onRejectCurrentSession() {
        callService.rejectCurrentSession(new HashMap<>());
    }

    @Override
    public void addConnectionListener(ConnectionListener connectionCallback) {
        callService.addConnectionListener(connectionCallback);
    }

    @Override
    public void removeConnectionListener(ConnectionListener connectionCallback) {
        callService.removeConnectionListener(connectionCallback);
    }

    @Override
    public void addSessionStateListener(QBRTCSessionStateCallback clientConnectionCallbacks) {
        callService.addSessionStateListener(clientConnectionCallbacks);
    }

    @Override
    public void addSessionEventsListener(QBRTCSessionEventsCallback eventsCallback) {
        callService.addSessionEventsListener(eventsCallback);
    }

    @Override
    public void onSetAudioEnabled(boolean isAudioEnabled) {
        callService.setAudioEnabled(isAudioEnabled);
    }

    @Override
    public void onHangUpCurrentSession() {
        hangUpCurrentSession();
    }

    @Override
    public void onStartScreenSharing() {
        QBRTCScreenCapturer.requestPermissions(this);
    }

    @Override
    public void onSwitchCamera(CameraVideoCapturer.CameraSwitchHandler cameraSwitchHandler) {
        callService.switchCamera(cameraSwitchHandler);
    }

    @Override
    public void onSetVideoEnabled(boolean isNeedEnableCam) {
        callService.setVideoEnabled(isNeedEnableCam);
    }

    @Override
    public void onSwitchAudio() {
        callService.switchAudio();
    }

    @Override
    public void removeSessionStateListener(QBRTCSessionStateCallback clientConnectionCallbacks) {
        callService.removeSessionStateListener(clientConnectionCallbacks);
    }

    @Override
    public void removeSessionEventsListener(QBRTCSessionEventsCallback eventsCallback) {
        callService.removeSessionEventsListener(eventsCallback);
    }

    @Override
    public void addCurrentCallStateListener(CurrentCallStateCallback currentCallStateCallback) {
        if (currentCallStateCallback != null) {
            currentCallStateCallbackList.add(currentCallStateCallback);
        }
    }

    @Override
    public void removeCurrentCallStateListener(CurrentCallStateCallback currentCallStateCallback) {
        currentCallStateCallbackList.remove(currentCallStateCallback);
    }

    @Override
    public void addOnChangeAudioDeviceListener(OnChangeAudioDevice onChangeDynamicCallback) {

    }

    @Override
    public void removeOnChangeAudioDeviceListener(OnChangeAudioDevice onChangeDynamicCallback) {

    }

    @Override
    public void acceptCall(Map<String, String> userInfo) {
        callService.acceptCall(userInfo);
    }

    @Override
    public void startCall(Map<String, String> userInfo) {
        callService.startCall(userInfo);
    }

    @Override
    public boolean isCameraFront() {
        return callService.isCameraFront();
    }

    @Override
    public boolean currentSessionExist() {
        return callService.currentSessionExist();
    }

    @Override
    public List<Integer> getOpponents() {
        return callService.getOpponentIds();
    }

    @Override
    public Integer getCallerId() {
        return callService.getCallerId();
    }

    @Override
    public void addVideoTrackListener(QBRTCClientVideoTracksCallbacks<QBRTCSession> callback) {
        callService.addVideoTrackListener(callback);
    }

    @Override
    public void removeVideoTrackListener(QBRTCClientVideoTracksCallbacks<QBRTCSession> callback) {
        callService.removeVideoTrackListener(callback);
    }

    @Override
    public BaseSession.QBRTCSessionState getCurrentSessionState() {
        return callService.getCurrentSessionState();
    }

    @Override
    public QBRTCTypes.QBRTCConnectionState getPeerChannel(Integer userId) {
        return callService.getPeerChannel(userId);
    }

    @Override
    public boolean isMediaStreamManagerExist() {
        return callService.isMediaStreamManagerExist();
    }

    @Override
    public boolean isCallState() {
        return callService.isCallMode();
    }

    @Override
    public HashMap<Integer, QBRTCVideoTrack> getVideoTrackMap() {
        return callService.getVideoTrackMap();
    }

    @Override
    public QBRTCVideoTrack getVideoTrack(Integer userId) {
        return callService.getVideoTrack(userId);
    }

    @Override
    public QBRTCTypes.QBRTCReconnectionState getState(Integer userId) {
        return callService.getState(userId);
    }

    @Override
    public void onStopPreview() {
        callService.stopScreenSharing();
        addConversationFragment(isInComingCall);
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

    private void notifyCallStateListenersCallTime(String callTime) {
        for (CurrentCallStateCallback callback : currentCallStateCallbackList) {
            callback.onCallTimeUpdate(callTime);
        }
    }

    private class CallServiceConnection implements ServiceConnection {
        @Override
        public void onServiceDisconnected(ComponentName name) {

        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            CallService.CallServiceBinder binder = (CallService.CallServiceBinder) service;
            callService = binder.getService();
            if (callService.currentSessionExist()) {
                initScreen();
            } else {
                finish();
            }
        }
    }

    private class CallTimerCallback implements CallService.CallTimerListener {
        @Override
        public void onCallTimeUpdate(String time) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    notifyCallStateListenersCallTime(time);
                }
            });
        }
    }

    public interface OnChangeAudioDevice {
        void audioDeviceChanged(QBAudioManager.AudioDevice newAudioDevice);
    }

    public interface CurrentCallStateCallback {
        void onCallStarted();

        void onCallStopped();

        void onOpponentsListUpdated(ArrayList<QBUser> newUsers);

        void onCallTimeUpdate(String time);
    }
}