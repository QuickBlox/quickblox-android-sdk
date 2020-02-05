package com.quickblox.sample.videochat.java.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import com.quickblox.chat.QBChatService;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.sample.videochat.java.R;
import com.quickblox.sample.videochat.java.activities.CallActivity;
import com.quickblox.sample.videochat.java.db.QbUsersDbManager;
import com.quickblox.sample.videochat.java.fragments.AudioConversationFragment;
import com.quickblox.sample.videochat.java.fragments.BaseConversationFragment;
import com.quickblox.sample.videochat.java.fragments.VideoConversationFragment;
import com.quickblox.sample.videochat.java.util.NetworkConnectionChecker;
import com.quickblox.sample.videochat.java.utils.Consts;
import com.quickblox.sample.videochat.java.utils.RingtonePlayer;
import com.quickblox.sample.videochat.java.utils.SettingsUtil;
import com.quickblox.sample.videochat.java.utils.SharedPrefsHelper;
import com.quickblox.sample.videochat.java.utils.ToastUtils;
import com.quickblox.sample.videochat.java.utils.WebRtcSessionManager;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;
import com.quickblox.videochat.webrtc.AppRTCAudioManager;
import com.quickblox.videochat.webrtc.BaseSession;
import com.quickblox.videochat.webrtc.QBMediaStreamManager;
import com.quickblox.videochat.webrtc.QBRTCCameraVideoCapturer;
import com.quickblox.videochat.webrtc.QBRTCClient;
import com.quickblox.videochat.webrtc.QBRTCConfig;
import com.quickblox.videochat.webrtc.QBRTCScreenCapturer;
import com.quickblox.videochat.webrtc.QBRTCSession;
import com.quickblox.videochat.webrtc.QBRTCTypes;
import com.quickblox.videochat.webrtc.QBSignalingSpec;
import com.quickblox.videochat.webrtc.callbacks.QBRTCClientSessionCallbacks;
import com.quickblox.videochat.webrtc.callbacks.QBRTCClientVideoTracksCallbacks;
import com.quickblox.videochat.webrtc.callbacks.QBRTCSessionEventsCallback;
import com.quickblox.videochat.webrtc.callbacks.QBRTCSessionStateCallback;
import com.quickblox.videochat.webrtc.callbacks.QBRTCSignalingCallback;
import com.quickblox.videochat.webrtc.exception.QBRTCSignalException;
import com.quickblox.videochat.webrtc.view.QBRTCVideoTrack;

import org.jivesoftware.smack.AbstractConnectionListener;
import org.jivesoftware.smack.ConnectionListener;
import org.webrtc.CameraVideoCapturer;
import org.webrtc.VideoSink;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import static com.quickblox.sample.videochat.java.utils.Consts.MAX_OPPONENTS_COUNT;

public class CallService extends Service {
    private static final String TAG = CallService.class.getSimpleName();

    private static final int SERVICE_ID = 787;
    private static final String CHANNEL_ID = "Quickblox channel";
    private static final String CHANNEL_NAME = "Quickblox background service";

    private HashMap<Integer, QBRTCVideoTrack> videoTrackMap = new HashMap<>();
    private CallServiceBinder callServiceBinder = new CallServiceBinder();
    private NetworkConnectionListener networkConnectionListener;
    private NetworkConnectionChecker networkConnectionChecker;
    private SessionEventsListener sessionEventsListener;
    private SessionStateListener sessionStateListener;
    private ConnectionListenerImpl connectionListener;
    private QBRTCSignalingListener signalingListener;
    private VideoTrackListener videoTrackListener;
    private AppRTCAudioManager appRTCAudioManager;
    private Long expirationReconnectionTime = 0L;
    private CallTimerListener callTimerListener;
    private boolean sharingScreenState = false;
    private boolean isCallState = false;
    private QBRTCSession currentSession;
    private QBRTCClient rtcClient;
    private RingtonePlayer ringtonePlayer;

    private CallTimerTask callTimerTask = new CallTimerTask();
    private Timer callTimer = new Timer();
    private Long callTime;

    public static void start(Context context) {
        Intent intent = new Intent(context, CallService.class);
        context.startService(intent);
    }

    public static void stop(Context context) {
        Intent intent = new Intent(context, CallService.class);
        context.stopService(intent);
    }

    @Override
    public void onCreate() {
        currentSession = WebRtcSessionManager.getInstance(getApplicationContext()).getCurrentSession();
        clearButtonsState();
        initNetworkChecker();
        initRTCClient();
        initListeners();
        initAudioManager();
        ringtonePlayer = new RingtonePlayer(this, R.raw.beep);
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Notification notification = initNotification();
        startForeground(SERVICE_ID, notification);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        networkConnectionChecker.unregisterListener(networkConnectionListener);
        removeConnectionListener(connectionListener);
        removeVideoTrackRenders();

        releaseCurrentSession();
        releaseAudioManager();

        stopCallTimer();
        clearButtonsState();
        clearCallState();
        stopForeground(true);
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.cancelAll();
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return callServiceBinder;
    }

    private Notification initNotification() {
        Intent notifyIntent = new Intent(this, CallActivity.class);
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent notifyPendingIntent = PendingIntent.getActivity(this, 0,
                notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        String notificationTitle = getString(R.string.notification_title);
        String notificationText = getString(R.string.notification_text, "");

        String callTime = getCallTime();
        if (!TextUtils.isEmpty(callTime)) {
            notificationText = getString(R.string.notification_text, callTime);
        }

        NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle();
        bigTextStyle.setBigContentTitle(notificationTitle);
        bigTextStyle.bigText(notificationText);

        String channelID = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ?
                createNotificationChannel(CHANNEL_ID, CHANNEL_NAME)
                : getString(R.string.app_name);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelID);
        builder.setStyle(bigTextStyle);
        builder.setContentTitle(notificationTitle);
        builder.setContentText(notificationText);
        builder.setWhen(System.currentTimeMillis());
        builder.setSmallIcon(R.mipmap.ic_launcher);

        Bitmap bitmapIcon = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
        builder.setLargeIcon(bitmapIcon);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            builder.setPriority(NotificationManager.IMPORTANCE_LOW);
        } else {
            builder.setPriority(Notification.PRIORITY_LOW);
        }
        builder.setContentIntent(notifyPendingIntent);

        return builder.build();
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private String createNotificationChannel(String channelID, String channelName) {
        NotificationChannel channel = new NotificationChannel(channelID, channelName, NotificationManager.IMPORTANCE_LOW);
        channel.setLightColor(getColor(R.color.green));
        channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.createNotificationChannel(channel);
        }
        return channelID;
    }

    private String getCallTime() {
        String time = "";
        if (callTime != null) {
            String format = String.format("%%0%dd", 2);
            Long elapsedTime = callTime / 1000;
            String seconds = String.format(format, elapsedTime % 60);
            String minutes = String.format(format, elapsedTime % 3600 / 60);
            String hours = String.format(format, elapsedTime / 3600);

            time = minutes + ":" + seconds;
            if (!TextUtils.isEmpty(hours) && hours != "00") {
                time = hours + ":" + minutes + ":" + seconds;
            }
        }
        return time;
    }

    public void playRingtone() {
        ringtonePlayer.play(true);
    }

    public void stopRingtone() {
        ringtonePlayer.stop();
    }

    private void initNetworkChecker() {
        networkConnectionChecker = new NetworkConnectionChecker(getApplication());
        networkConnectionListener = new NetworkConnectionListener();
        networkConnectionChecker.registerListener(networkConnectionListener);
    }

    private void initRTCClient() {
        rtcClient = QBRTCClient.getInstance(this);
        rtcClient.setCameraErrorHandler(new CameraEventsListener());

        QBRTCConfig.setMaxOpponentsCount(MAX_OPPONENTS_COUNT);
        QBRTCConfig.setDebugEnabled(true);

        SettingsUtil.configRTCTimers(this);

        rtcClient.prepareToProcessCalls();
    }

    private void initListeners() {
        sessionStateListener = new SessionStateListener();
        addSessionStateListener(sessionStateListener);

        signalingListener = new QBRTCSignalingListener();
        addSignalingListener(signalingListener);

        videoTrackListener = new VideoTrackListener();
        addVideoTrackListener(videoTrackListener);

        connectionListener = new ConnectionListenerImpl();
        addConnectionListener(connectionListener);

        sessionEventsListener = new SessionEventsListener();
        addSessionEventsListener(sessionEventsListener);
    }

    public void initAudioManager() {
        appRTCAudioManager = AppRTCAudioManager.create(this);

        appRTCAudioManager.setOnWiredHeadsetStateListener(new AppRTCAudioManager.OnWiredHeadsetStateListener() {
            @Override
            public void onWiredHeadsetStateChanged(boolean plugged, boolean hasMicrophone) {
                ToastUtils.shortToast("Headset " + (plugged ? "Plugged" : "Unplugged"));
            }
        });

        appRTCAudioManager.setBluetoothAudioDeviceStateListener(new AppRTCAudioManager.BluetoothAudioDeviceStateListener() {
            @Override
            public void onStateChanged(boolean connected) {
                ToastUtils.shortToast("Bluetooth " + (connected ? "Connected" : "Disconnected"));
            }
        });

        appRTCAudioManager.start(new AppRTCAudioManager.AudioManagerEvents() {
            @Override
            public void onAudioDeviceChanged(AppRTCAudioManager.AudioDevice audioDevice, Set<AppRTCAudioManager.AudioDevice> set) {
                ToastUtils.shortToast("Audio Device Switched to " + audioDevice);
            }
        });

        if (currentSessionExist() && currentSession.getConferenceType() == QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_AUDIO) {
            appRTCAudioManager.selectAudioDevice(AppRTCAudioManager.AudioDevice.EARPIECE);
        }
    }

    public void releaseAudioManager() {
        appRTCAudioManager.stop();
    }

    public boolean currentSessionExist() {
        return currentSession != null;
    }

    private void releaseCurrentSession() {
        Log.d(TAG, "Release current session");
        removeSessionStateListener(sessionStateListener);
        removeSignalingListener(signalingListener);
        removeSessionEventsListener(sessionEventsListener);
        removeVideoTrackListener(videoTrackListener);
        currentSession = null;
    }

    //Listeners
    public void addConnectionListener(ConnectionListener connectionListener) {
        QBChatService.getInstance().addConnectionListener(connectionListener);
    }

    public void removeConnectionListener(ConnectionListener connectionListener) {
        QBChatService.getInstance().removeConnectionListener(connectionListener);
    }

    public void addSessionStateListener(QBRTCSessionStateCallback callback) {
        if (currentSession != null) {
            currentSession.addSessionCallbacksListener(callback);
        }
    }

    public void removeSessionStateListener(QBRTCSessionStateCallback callback) {
        if (currentSession != null) {
            currentSession.removeSessionCallbacksListener(callback);
        }
    }

    public void addVideoTrackListener(QBRTCClientVideoTracksCallbacks callback) {
        if (currentSession != null) {
            currentSession.addVideoTrackCallbacksListener(callback);
        }
    }

    public void removeVideoTrackListener(QBRTCClientVideoTracksCallbacks callback) {
        if (currentSession != null) {
            currentSession.removeVideoTrackCallbacksListener(callback);
        }
    }

    public void addSignalingListener(QBRTCSignalingCallback callback) {
        if (currentSession != null) {
            currentSession.addSignalingCallback(callback);
        }
    }

    public void removeSignalingListener(QBRTCSignalingCallback callback) {
        if (currentSession != null) {
            currentSession.removeSignalingCallback(callback);
        }
    }

    public void addSessionEventsListener(QBRTCSessionEventsCallback callback) {
        rtcClient.addSessionCallbacksListener(callback);
    }

    public void removeSessionEventsListener(QBRTCSessionEventsCallback callback) {
        rtcClient.removeSessionsCallbacksListener(callback);
    }

    // Common methods
    public void acceptCall(Map<String, String> userInfo) {
        if (currentSession != null) {
            currentSession.acceptCall(userInfo);
        }
    }

    public void startCall(Map<String, String> userInfo) {
        if (currentSession != null) {
            currentSession.startCall(userInfo);
        }
    }

    public void rejectCurrentSession(Map<String, String> userInfo) {
        if (currentSession != null) {
            currentSession.rejectCall(userInfo);
        }
    }

    public boolean hangUpCurrentSession(Map<String, String> userInfo) {
        stopRingtone();
        boolean result = false;
        if (currentSession != null) {
            currentSession.hangUp(userInfo);
            result = true;
        }
        return result;
    }

    public void setAudioEnabled(boolean enabled) {
        if (currentSession != null) {
            currentSession.getMediaStreamManager().getLocalAudioTrack().setEnabled(enabled);
        }
    }

    public void startScreenSharing(Intent data) {
        sharingScreenState = true;
        if (currentSession != null) {
            currentSession.getMediaStreamManager().setVideoCapturer(new QBRTCScreenCapturer(data, null));
        }
    }

    public void stopScreenSharing() {
        sharingScreenState = false;
        if (currentSession != null) {
            try {
                currentSession.getMediaStreamManager().setVideoCapturer(new QBRTCCameraVideoCapturer(this, null));
            } catch (QBRTCCameraVideoCapturer.QBRTCCameraCapturerException e) {
                Log.i(TAG, "Error: device doesn't have camera");
            }
        }
    }

    public Integer getCallerId() {
        if (currentSession != null) {
            return currentSession.getCallerID();
        } else {
            return null;
        }
    }

    public List<Integer> getOpponents() {
        if (currentSession != null) {
            return currentSession.getOpponents();
        } else {
            return null;
        }
    }

    public boolean isVideoCall() {
        return QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_VIDEO == currentSession.getConferenceType();
    }

    public void setVideoEnabled(boolean videoEnabled) {
        QBMediaStreamManager streamManager = currentSession.getMediaStreamManager();
        QBRTCVideoTrack videoTrack = streamManager.getLocalVideoTrack();
        if (currentSession != null && videoTrack != null) {
            videoTrack.setEnabled(videoEnabled);
        }
    }

    public BaseSession.QBRTCSessionState getCurrentSessionState() {
        return currentSession != null ? currentSession.getState() : null;
    }

    public boolean isMediaStreamManagerExist() {
        return currentSession != null && currentSession.getMediaStreamManager() != null;
    }

    public QBRTCTypes.QBRTCConnectionState getPeerChannel(Integer userID) {
        QBRTCTypes.QBRTCConnectionState result = null;
        if (currentSession != null && currentSession.getPeerChannel(userID) != null) {
            result = currentSession.getPeerChannel(userID).getState();
        }
        return result;
    }

    public boolean isCurrentSession(QBRTCSession session) {
        boolean isCurrentSession = false;
        if (session != null && currentSession != null) {
            isCurrentSession = currentSession.getSessionID().equals(session.getSessionID());
        }
        return isCurrentSession;
    }

    public void switchCamera(CameraVideoCapturer.CameraSwitchHandler cameraSwitchHandler) {
        if (currentSession != null && currentSession.getMediaStreamManager() != null) {
            QBRTCCameraVideoCapturer videoCapturer = (QBRTCCameraVideoCapturer) currentSession.getMediaStreamManager().getVideoCapturer();
            videoCapturer.switchCamera(cameraSwitchHandler);
        }
    }

    public void switchAudio() {
        Log.v(TAG, "onSwitchAudio(), SelectedAudioDevice() = " + appRTCAudioManager.getSelectedAudioDevice());
        if (appRTCAudioManager.getSelectedAudioDevice() != AppRTCAudioManager.AudioDevice.SPEAKER_PHONE) {
            appRTCAudioManager.selectAudioDevice(AppRTCAudioManager.AudioDevice.SPEAKER_PHONE);
        } else {
            if (appRTCAudioManager.getAudioDevices().contains(AppRTCAudioManager.AudioDevice.BLUETOOTH)) {
                appRTCAudioManager.selectAudioDevice(AppRTCAudioManager.AudioDevice.BLUETOOTH);
            } else if (appRTCAudioManager.getAudioDevices().contains(AppRTCAudioManager.AudioDevice.WIRED_HEADSET)) {
                appRTCAudioManager.selectAudioDevice(AppRTCAudioManager.AudioDevice.WIRED_HEADSET);
            } else {
                appRTCAudioManager.selectAudioDevice(AppRTCAudioManager.AudioDevice.EARPIECE);
            }
        }
    }

    public boolean isSharingScreenState() {
        return sharingScreenState;
    }

    public boolean isCallMode() {
        return isCallState;
    }

    public HashMap<Integer, QBRTCVideoTrack> getVideoTrackMap() {
        return videoTrackMap;
    }

    private void addVideoTrack(Integer userId, QBRTCVideoTrack videoTrack) {
        videoTrackMap.put(userId, videoTrack);
    }

    public QBRTCVideoTrack getVideoTrack(Integer userId) {
        return videoTrackMap.get(userId);
    }

    private void removeVideoTrack(int userId) {
        videoTrackMap.remove(userId);
    }

    private void removeVideoTrackRenders() {
        Log.d(TAG, "removeVideoTrackRenders");
        if (!videoTrackMap.isEmpty()) {
            Iterator<Map.Entry<Integer, QBRTCVideoTrack>> entryIterator = videoTrackMap.entrySet().iterator();
            while (entryIterator.hasNext()) {
                Map.Entry entry = entryIterator.next();
                Integer userId = (Integer) entry.getKey();
                QBRTCVideoTrack videoTrack = (QBRTCVideoTrack) entry.getValue();
                QBUser qbUser = QBChatService.getInstance().getUser();
                boolean remoteVideoTrack = !userId.equals(qbUser.getId());
                if (remoteVideoTrack) {
                    VideoSink renderer = videoTrack.getRenderer();
                    if (renderer != null) {
                        videoTrack.removeRenderer(renderer);
                    }
                }
            }
        }
    }

    public void setCallTimerCallback(CallTimerListener callback) {
        callTimerListener = callback;
    }

    public void removeCallTimerCallback() {
        callTimerListener = null;
    }

    private void startCallTimer() {
        if (callTime == null) {
            callTime = 1000L;
        }
        if (!callTimerTask.isRunning()) {
            callTimer.scheduleAtFixedRate(callTimerTask, 0, 1000L);
        }
    }

    private void stopCallTimer() {
        callTimerListener = null;

        callTimer.cancel();
        callTimer.purge();
    }

    public void clearButtonsState() {
        SharedPrefsHelper.getInstance().delete(BaseConversationFragment.MIC_ENABLED);
        SharedPrefsHelper.getInstance().delete(AudioConversationFragment.SPEAKER_ENABLED);
        SharedPrefsHelper.getInstance().delete(VideoConversationFragment.CAMERA_ENABLED);
        SharedPrefsHelper.getInstance().delete(VideoConversationFragment.IS_CURRENT_CAMERA_FRONT);
    }

    public void clearCallState() {
        SharedPrefsHelper.getInstance().delete(Consts.EXTRA_IS_INCOMING_CALL);
    }

    private class CallTimerTask extends TimerTask {
        private boolean isRunning = false;

        @Override
        public void run() {
            isRunning = true;
            callTime = callTime + 1000L;
            Notification notification = initNotification();
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                notificationManager.notify(SERVICE_ID, notification);
            }

            if (callTimerListener != null) {
                String callTime = getCallTime();
                if (!TextUtils.isEmpty(callTime)) {
                    callTimerListener.onCallTimeUpdate(callTime);
                }
            }
        }

        public boolean isRunning() {
            return isRunning;
        }
    }

    public class CallServiceBinder extends Binder {
        public CallService getService() {
            return CallService.this;
        }
    }

    private class ConnectionListenerImpl extends AbstractConnectionListener {
        @Override
        public void connectionClosedOnError(Exception e) {
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            int reconnectHangUpTimeMillis = SettingsUtil.getPreferenceInt(sharedPref,
                    getApplicationContext(), R.string.pref_disconnect_time_interval_key,
                    R.string.pref_disconnect_time_interval_default_value) * 1000;
            expirationReconnectionTime = System.currentTimeMillis() + reconnectHangUpTimeMillis;
        }

        @Override
        public void reconnectionSuccessful() {

        }

        @Override
        public void reconnectingIn(int seconds) {
            Log.i(TAG, "reconnectingIn " + seconds);
            if (!isCallState && expirationReconnectionTime < System.currentTimeMillis()) {
                hangUpCurrentSession(new HashMap<>());
            }
        }
    }

    private class SessionEventsListener implements QBRTCClientSessionCallbacks {
        @Override
        public void onUserNotAnswer(QBRTCSession qbrtcSession, Integer integer) {

        }

        @Override
        public void onSessionStartClose(QBRTCSession session) {
            if (session == WebRtcSessionManager.getInstance(getApplicationContext()).getCurrentSession()) {
                CallService.stop(getApplicationContext());
            }
        }

        @Override
        public void onReceiveHangUpFromUser(QBRTCSession session, Integer userID, Map<String, String> map) {
            stopRingtone();
            if (session == WebRtcSessionManager.getInstance(getApplicationContext()).getCurrentSession()) {
                Log.d(TAG, "Initiator HangUp the Call");
                if (userID.equals(session.getCallerID()) && currentSession != null) {
                    currentSession.hangUp(new HashMap<>());
                }

                QBUser participant = QbUsersDbManager.getInstance(getApplicationContext()).getUserById(userID);
                String participantName = participant != null ? participant.getFullName() : userID.toString();
                ToastUtils.shortToast("User " + participantName + " " + getString(R.string.text_status_hang_up) + " conversation");
            }
        }

        @Override
        public void onCallAcceptByUser(QBRTCSession session, Integer userID, Map<String, String> map) {
            stopRingtone();
            if (session != WebRtcSessionManager.getInstance(getApplicationContext()).getCurrentSession()) {
                return;
            }
        }

        @Override
        public void onReceiveNewSession(QBRTCSession session) {
            Log.d(TAG, "Session " + session.getSessionID() + " are Income");
            if (WebRtcSessionManager.getInstance(getApplicationContext()).getCurrentSession() != null) {
                session.rejectCall(null);
            }
        }

        @Override
        public void onUserNoActions(QBRTCSession qbrtcSession, Integer integer) {
            ToastUtils.longToast("Call was stopped by UserNoActions timer");
            clearCallState();
            clearButtonsState();
            WebRtcSessionManager.getInstance(getApplicationContext()).setCurrentSession(null);
            CallService.stop(CallService.this);
        }

        @Override
        public void onSessionClosed(QBRTCSession session) {
            stopRingtone();
            Log.d(TAG, "Session " + session.getSessionID() + " onSessionClosed()");
            if (session == currentSession) {
                Log.d(TAG, "Stopping Session");
                CallService.stop(CallService.this);
            }
        }

        @Override
        public void onCallRejectByUser(QBRTCSession session, Integer integer, Map<String, String> map) {
            stopRingtone();
            if (session == WebRtcSessionManager.getInstance(getApplicationContext()).getCurrentSession()) {
                return;
            }
        }
    }

    private class SessionStateListener implements QBRTCSessionStateCallback<QBRTCSession> {
        @Override
        public void onStateChanged(QBRTCSession qbrtcSession, BaseSession.QBRTCSessionState qbrtcSessionState) {

        }

        @Override
        public void onConnectedToUser(QBRTCSession qbrtcSession, Integer integer) {
            stopRingtone();
            isCallState = true;
            Log.d(TAG, "onConnectedToUser() is started");
            startCallTimer();
        }

        @Override
        public void onDisconnectedFromUser(QBRTCSession qbrtcSession, Integer userID) {
            Log.d(TAG, "Disconnected from user: " + userID);
        }

        @Override
        public void onConnectionClosedForUser(QBRTCSession qbrtcSession, Integer userID) {
            if (userID != null) {
                Log.d(TAG, "Connection closed for user: " + userID);
                ToastUtils.shortToast("The user: " + userID + " has left the call");
                removeVideoTrack(userID);
            }
        }
    }

    private class QBRTCSignalingListener implements QBRTCSignalingCallback {
        @Override
        public void onSuccessSendingPacket(QBSignalingSpec.QBSignalCMD qbSignalCMD, Integer integer) {

        }

        @Override
        public void onErrorSendingPacket(QBSignalingSpec.QBSignalCMD qbSignalCMD, Integer integer, QBRTCSignalException e) {
            ToastUtils.shortToast(R.string.dlg_signal_error);
        }
    }

    private class NetworkConnectionListener implements NetworkConnectionChecker.OnConnectivityChangedListener {

        @Override
        public void connectivityChanged(boolean availableNow) {
            ToastUtils.shortToast("Internet Connection " + (availableNow ? "Available" : "Unavailable"));
        }
    }

    private class CameraEventsListener implements CameraVideoCapturer.CameraEventsHandler {
        @Override
        public void onCameraError(String s) {
            ToastUtils.shortToast("Camera Error: " + s);
        }

        @Override
        public void onCameraDisconnected() {
            ToastUtils.shortToast("Camera Disconnected");
        }

        @Override
        public void onCameraFreezed(String s) {
            ToastUtils.shortToast("Camera Freezed");
            hangUpCurrentSession(new HashMap<>());
        }

        @Override
        public void onCameraOpening(String s) {
            ToastUtils.shortToast("Camera Opening");
        }

        @Override
        public void onFirstFrameAvailable() {
            ToastUtils.shortToast("Camera onFirstFrameAvailable");
        }

        @Override
        public void onCameraClosed() {
            ToastUtils.shortToast("Camera Closed");
        }
    }

    private class VideoTrackListener implements QBRTCClientVideoTracksCallbacks<QBRTCSession> {
        @Override
        public void onLocalVideoTrackReceive(QBRTCSession session, QBRTCVideoTrack videoTrack) {
            if (videoTrack != null) {
                int userID = QBChatService.getInstance().getUser().getId();
                removeVideoTrack(userID);
                addVideoTrack(userID, videoTrack);
            }
            Log.d(TAG, "onLocalVideoTrackReceive() run");
        }

        @Override
        public void onRemoteVideoTrackReceive(QBRTCSession session, QBRTCVideoTrack videoTrack, Integer userID) {
            if (videoTrack != null && userID != null) {
                addVideoTrack(userID, videoTrack);
            }
            Log.d(TAG, "onRemoteVideoTrackReceive for Opponent= " + userID);
        }
    }

    public interface CallTimerListener {
        void onCallTimeUpdate(String time);
    }
}