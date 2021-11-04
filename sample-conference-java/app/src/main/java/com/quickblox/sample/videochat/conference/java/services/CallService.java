package com.quickblox.sample.videochat.conference.java.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.quickblox.conference.ConferenceClient;
import com.quickblox.conference.ConferenceSession;
import com.quickblox.conference.QBConferenceRole;
import com.quickblox.conference.WsException;
import com.quickblox.conference.callbacks.ConferenceEntityCallback;
import com.quickblox.conference.callbacks.ConferenceSessionCallbacks;
import com.quickblox.sample.videochat.conference.java.App;
import com.quickblox.sample.videochat.conference.java.R;
import com.quickblox.sample.videochat.conference.java.activities.CallActivity;
import com.quickblox.sample.videochat.conference.java.managers.WebRtcSessionManager;
import com.quickblox.sample.videochat.conference.java.utils.Consts;
import com.quickblox.videochat.webrtc.AppRTCAudioManager;
import com.quickblox.videochat.webrtc.BaseSession;
import com.quickblox.videochat.webrtc.QBMediaStreamManager;
import com.quickblox.videochat.webrtc.QBRTCAudioTrack;
import com.quickblox.videochat.webrtc.QBRTCCameraVideoCapturer;
import com.quickblox.videochat.webrtc.QBRTCConfig;
import com.quickblox.videochat.webrtc.QBRTCScreenCapturer;
import com.quickblox.videochat.webrtc.QBRTCTypes;
import com.quickblox.videochat.webrtc.callbacks.QBRTCClientAudioTracksCallback;
import com.quickblox.videochat.webrtc.callbacks.QBRTCClientVideoTracksCallbacks;
import com.quickblox.videochat.webrtc.callbacks.QBRTCSessionStateCallback;
import com.quickblox.videochat.webrtc.view.QBRTCVideoTrack;

import org.webrtc.CameraVideoCapturer;
import org.webrtc.VideoSink;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;


public class CallService extends Service {
    private static final String TAG = CallService.class.getSimpleName();

    private static final int SERVICE_ID = 646;
    private static final long SECONDS_13 = 13000;
    private static final String CHANNEL_ID = "Quickblox Conference Channel";
    private static final String CHANNEL_NAME = "Quickblox Background Conference service";
    private static final String ICE_FAILED_REASON = "ICE failed";

    private final HashMap<Integer, QBRTCVideoTrack> videoTrackMap = new HashMap<>();
    private final CallServiceBinder callServiceBinder = new CallServiceBinder();
    private SessionStateListener sessionStateListener;
    private VideoTrackListener videoTrackListener;
    private AudioTrackListener audioTrackListener;
    private ConferenceSessionListener conferenceSessionListener;
    private final ArrayList<CurrentCallStateCallback> currentCallStateCallbackList = new ArrayList<>();
    private ArrayList<Integer> opponentsIDsList = new ArrayList<>();
    private Map<Integer, Boolean> onlineParticipants = new HashMap<>();
    private OnlineParticipantsChangeListener onlineParticipantsChangeListener;
    private OnlineParticipantsCheckerCountdown onlineParticipantsCheckerCountdown;
    private UsersConnectDisconnectCallback usersConnectDisconnectCallback;
    private AppRTCAudioManager audioManager;
    private boolean sharingScreenState = false;
    private String roomID;
    private String roomTitle;
    private String dialogID;
    private boolean asListenerRole;
    private boolean previousDeviceEarPiece;
    private ConferenceSession currentSession;
    private ConferenceClient conferenceClient;
    private ReconnectionState reconnectionState = ReconnectionState.DEFAULT;
    private final Set<ReconnectionListener> reconnectionListeners = new HashSet<>();

    public static void start(Context context, String roomID, String roomTitle, String dialogID, List<Integer> occupants, boolean listenerRole) {
        Intent intent = new Intent(context, CallService.class);
        intent.putExtra(Consts.EXTRA_ROOM_ID, roomID);
        intent.putExtra(Consts.EXTRA_ROOM_TITLE, roomTitle);
        intent.putExtra(Consts.EXTRA_DIALOG_ID, dialogID);
        intent.putExtra(Consts.EXTRA_DIALOG_OCCUPANTS, (Serializable) occupants);
        intent.putExtra(Consts.EXTRA_AS_LISTENER, listenerRole);

        context.startService(intent);
    }

    public static void stop(Context context) {
        Intent intent = new Intent(context, CallService.class);
        context.stopService(intent);
    }

    public ReconnectionState getReconnectionState() {
        return reconnectionState;
    }

    public void setReconnectionState(ReconnectionState reconnectionState) {
        this.reconnectionState = reconnectionState;
    }

    @Override
    public void onCreate() {
        currentSession = WebRtcSessionManager.getInstance().getCurrentSession();
        initConferenceClient();
        initListeners();
        initAudioManager();
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Notification notification = initNotification();
        startForeground(SERVICE_ID, notification);
        if (intent != null) {
            roomID = intent.getStringExtra(Consts.EXTRA_ROOM_ID);
            roomTitle = intent.getStringExtra(Consts.EXTRA_ROOM_TITLE);
            dialogID = intent.getStringExtra(Consts.EXTRA_DIALOG_ID);
            opponentsIDsList = (ArrayList<Integer>) intent.getSerializableExtra(Consts.EXTRA_DIALOG_OCCUPANTS);
            asListenerRole = intent.getBooleanExtra(Consts.EXTRA_AS_LISTENER, false);

            if (!isListenerRole() && !roomID.equals(dialogID)) {
                onlineParticipantsCheckerCountdown = new OnlineParticipantsCheckerCountdown(Long.MAX_VALUE, 3000);
                onlineParticipantsCheckerCountdown.start();
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (onlineParticipantsCheckerCountdown != null && !isListenerRole()) {
            onlineParticipantsCheckerCountdown.cancel();
        }
        removeVideoTrackRenders();
        releaseAudioManager();

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
        builder.setSmallIcon(R.drawable.ic_logo_vector);

        Bitmap bitmapIcon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_logo_vector);
        builder.setLargeIcon(bitmapIcon);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            builder.setPriority(NotificationManager.IMPORTANCE_LOW);
        } else {
            builder.setPriority(Notification.PRIORITY_LOW);
        }
        builder.setContentIntent(notifyPendingIntent);

        return builder.build();
    }

    public String getDialogID() {
        return dialogID;
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

    public void setOnlineParticipantsChangeListener(OnlineParticipantsChangeListener onlineParticipantsChangeListener) {
        this.onlineParticipantsChangeListener = onlineParticipantsChangeListener;
    }

    public void subscribeReconnectionListener(ReconnectionListener reconnectionListener) {
        reconnectionListeners.add(reconnectionListener);
    }

    public void unsubscribeReconnectionListener(ReconnectionListener reconnectionListener) {
        reconnectionListeners.remove(reconnectionListener);
    }

    private void initConferenceClient() {
        conferenceClient = ConferenceClient.getInstance(this);
        conferenceClient.setCameraErrorHandler(new CameraEventsListener());
        QBRTCConfig.setDebugEnabled(true);
    }

    private void initListeners() {
        sessionStateListener = new SessionStateListener();
        addSessionStateListener(sessionStateListener);

        videoTrackListener = new VideoTrackListener();
        addVideoTrackListener(videoTrackListener);

        audioTrackListener = new AudioTrackListener();
        addAudioTrackListener(audioTrackListener);

        conferenceSessionListener = new ConferenceSessionListener();
        addConferenceSessionListener(conferenceSessionListener);
    }

    private void initAudioManager() {
        if (audioManager == null) {
            audioManager = AppRTCAudioManager.create(this);
            audioManager.selectAudioDevice(AppRTCAudioManager.AudioDevice.SPEAKER_PHONE);
            previousDeviceEarPiece = false;
            Log.d(TAG, "AppRTCAudioManager.AudioDevice.SPEAKER_PHONE");

            audioManager.setOnWiredHeadsetStateListener((plugged, hasMicrophone) -> {

                if (!plugged) {
                    if (previousDeviceEarPiece) {
                        setAudioDeviceDelayed(AppRTCAudioManager.AudioDevice.EARPIECE);
                    } else {
                        setAudioDeviceDelayed(AppRTCAudioManager.AudioDevice.SPEAKER_PHONE);
                    }
                }
            });
            audioManager.start((audioDevice, set) ->
                    Log.d(TAG, "Audio Device Switched to " + audioDevice)
            );
        }
    }

    private void setAudioDeviceDelayed(final AppRTCAudioManager.AudioDevice audioDevice) {
        new Handler().postDelayed(() -> audioManager.selectAudioDevice(audioDevice), 500);
    }

    private void releaseAudioManager() {
        if (audioManager != null) {
            audioManager.stop();
        }
    }

    public boolean currentSessionExist() {
        return currentSession != null;
    }

    public void leaveCurrentSession() {
        currentSession.leave();
        releaseCurrentSession();
    }

    private void releaseCurrentSession() {
        Log.d(TAG, "Release current session");
        removeSessionStateListener(sessionStateListener);
        removeVideoTrackListener(videoTrackListener);
        removeAudioTrackListener(audioTrackListener);
        removeConferenceSessionListener(conferenceSessionListener);
        CallService.stop(this);
    }

    //Manage Listeners

    private void addSessionStateListener(QBRTCSessionStateCallback callback) {
        if (currentSession != null) {
            currentSession.addSessionCallbacksListener(callback);
        }
    }

    private void removeSessionStateListener(QBRTCSessionStateCallback callback) {
        if (currentSession != null) {
            currentSession.removeSessionCallbacksListener(callback);
        }
    }

    private void addVideoTrackListener(QBRTCClientVideoTracksCallbacks callback) {
        if (currentSession != null) {
            currentSession.addVideoTrackCallbacksListener(callback);
        }
    }

    private void removeVideoTrackListener(QBRTCClientVideoTracksCallbacks callback) {
        if (currentSession != null) {
            currentSession.removeVideoTrackCallbacksListener(callback);
        }
    }

    private void addAudioTrackListener(QBRTCClientAudioTracksCallback callback) {
        if (currentSession != null) {
            currentSession.addAudioTrackCallbacksListener(callback);
        }
    }

    public void removeAudioTrackListener(QBRTCClientAudioTracksCallback callback) {
        if (currentSession != null) {
            currentSession.removeAudioTrackCallbacksListener(callback);
        }
    }

    private void addConferenceSessionListener(ConferenceSessionListener listener) {
        if (currentSession != null) {
            currentSession.addConferenceSessionListener(listener);
        }
    }

    private void removeConferenceSessionListener(ConferenceSessionListener listener) {
        if (currentSession != null) {
            currentSession.removeConferenceSessionListener(listener);
        }
    }

    // Common methods

    public ArrayList<Integer> getOpponentsIDsList() {
        return opponentsIDsList;
    }

    public ArrayList<Integer> getActivePublishers() {
        return new ArrayList<>(currentSession.getActivePublishers());
    }

    public void getOnlineParticipants(ConferenceEntityCallback<Map<Integer, Boolean>> callback) {
        if (currentSession != null) {
            currentSession.getOnlineParticipants(new ConferenceEntityCallback<Map<Integer, Boolean>>() {
                @Override
                public void onSuccess(Map<Integer, Boolean> integerBooleanMap) {
                    onlineParticipants = integerBooleanMap;
                    callback.onSuccess(integerBooleanMap);
                }

                @Override
                public void onError(WsException e) {
                    callback.onError(e);
                }
            });
        }
    }

    public String getRoomID() {
        return roomID;
    }

    public String getRoomTitle() {
        return roomTitle;
    }

    public boolean isListenerRole() {
        return asListenerRole;
    }

    public void setAudioEnabled(boolean enabled) {
        if (currentSession != null && currentSession.getMediaStreamManager() != null
                && currentSession.getMediaStreamManager().getLocalAudioTrack() != null) {
            currentSession.getMediaStreamManager().getLocalAudioTrack().setEnabled(enabled);
        }
    }

    public boolean isAudioEnabledForUser(Integer userID) {
        if (currentSession.getMediaStreamManager() != null) {
            boolean isAudioEnabled = true;
            try {
                isAudioEnabled = currentSession.getMediaStreamManager().getAudioTrack(userID).enabled();
            } catch (Exception e) {
                if (e.getMessage() != null) {
                    Log.d(TAG, e.getMessage());
                }
            }
            return isAudioEnabled;
        } else {
            return false;
        }
    }

    public void setAudioEnabledForUser(Integer userID, boolean isAudioEnabled) {
        currentSession.getMediaStreamManager().getAudioTrack(userID).setEnabled(isAudioEnabled);
    }

    public void setVideoEnabled(boolean videoEnabled) {
        QBMediaStreamManager streamManager = currentSession.getMediaStreamManager();
        QBRTCVideoTrack videoTrack = streamManager.getLocalVideoTrack();
        if (currentSession != null && videoTrack != null) {
            videoTrack.setEnabled(videoEnabled);
        }
    }

    public void switchCamera(CameraVideoCapturer.CameraSwitchHandler cameraSwitchHandler) {
        if (currentSession != null && currentSession.getMediaStreamManager() != null) {
            QBRTCCameraVideoCapturer videoCapturer = (QBRTCCameraVideoCapturer) currentSession.getMediaStreamManager().getVideoCapturer();
            videoCapturer.switchCamera(cameraSwitchHandler);
        }
    }

    public boolean isSharingScreenState() {
        return sharingScreenState;
    }

    public HashMap<Integer, QBRTCVideoTrack> getVideoTrackMap() {
        return videoTrackMap;
    }

    private void addVideoTrack(Integer userId, QBRTCVideoTrack videoTrack) {
        videoTrackMap.put(userId, videoTrack);
    }

    private void removeVideoTrack(int userId) {
        videoTrackMap.remove(userId);
    }

    private void removeVideoTrackRenders() {
        Log.d(TAG, "removeVideoTrackRenders");
        if (!videoTrackMap.isEmpty()) {
            for (Map.Entry<Integer, QBRTCVideoTrack> entry : videoTrackMap.entrySet()) {
                Integer userId = (Integer) entry.getKey();
                QBRTCVideoTrack videoTrack = (QBRTCVideoTrack) entry.getValue();
                Integer currentUserID = currentSession.getCurrentUserID();
                boolean remoteVideoTrack = !userId.equals(currentUserID);
                if (remoteVideoTrack) {
                    VideoSink renderer = videoTrack.getRenderer();
                    if (renderer != null) {
                        videoTrack.removeRenderer(renderer);
                    }
                }
            }
        }
    }

    public void startScreenSharing(Intent data) {
        sharingScreenState = true;
        if (data != null && currentSession != null) {
            currentSession.getMediaStreamManager().setVideoCapturer(new QBRTCScreenCapturer(data, null));
            setVideoEnabled(true);
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

    public void joinConference() {
        QBConferenceRole conferenceRole = asListenerRole ? QBConferenceRole.LISTENER : QBConferenceRole.PUBLISHER;

        currentSession.joinDialog(roomID, conferenceRole, new ConferenceEntityCallback<ArrayList<Integer>>() {
            @Override
            public void onSuccess(ArrayList<Integer> publishers) {
                // empty
            }

            @Override
            public void onError(WsException exception) {
                Log.d(TAG, "onError joinDialog exception= " + exception);
            }
        });
    }

    private void notifyCallStateListenersCallStarted() {
        for (CurrentCallStateCallback callback : currentCallStateCallbackList) {
            callback.onCallStarted();
        }
    }

    public void setUsersConnectDisconnectCallback(UsersConnectDisconnectCallback callback) {
        this.usersConnectDisconnectCallback = callback;
    }

    public void removeUsersConnectDisconnectCallback() {
        this.usersConnectDisconnectCallback = null;
    }

    //////////////////////////////////////////
    //    Call Service Binder
    //////////////////////////////////////////

    public class CallServiceBinder extends Binder {
        public CallService getService() {
            return CallService.this;
        }
    }

    //////////////////////////////////////////
    //    Conference Session Callbacks
    //////////////////////////////////////////

    private class ConferenceSessionListener implements ConferenceSessionCallbacks {
        @Override
        public void onPublishersReceived(ArrayList<Integer> publishersList) {
            currentSession.subscribeToPublisher(publishersList.get(0));
        }

        @Override
        public void onPublisherLeft(Integer userID) {
            Log.d(TAG, "OnPublisherLeft userID" + userID);
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
            Log.d(TAG, "OnError exception= " + exception.getMessage());
            if (exception.getMessage().equals(ICE_FAILED_REASON)) {
                releaseCurrentSession();
            }
        }

        @Override
        public void onSessionClosed(ConferenceSession session) {
            if (session.equals(currentSession) && reconnectionState == ReconnectionState.IN_PROGRESS) {
                new ReconnectionTimer().reconnect();
            }
        }
    }

    private class ReconnectionTimer {
        private Timer timer = new Timer();

        private long lastDelay = 0;
        private long delay = 1000;
        private long newDelay = 0;

        void reconnect() {
            if (newDelay >= SECONDS_13) {
                reconnectionState = ReconnectionState.FAILED;
                for (ReconnectionListener reconnectionListener : reconnectionListeners) {
                    reconnectionListener.onChangedState(reconnectionState);
                }
                leaveCurrentSession();
                return;
            }
            newDelay = lastDelay + delay;
            lastDelay = delay;
            delay = newDelay;

            timer.cancel();
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    ConferenceClient client = ConferenceClient.getInstance(getApplicationContext());
                    QBRTCTypes.QBConferenceType conferenceType = QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_VIDEO;
                    client.createSession(currentSession.getCurrentUserID(), conferenceType, new ConferenceEntityCallback<ConferenceSession>() {
                        @Override
                        public void onSuccess(ConferenceSession conferenceSession) {
                            WebRtcSessionManager.getInstance().setCurrentSession(conferenceSession);
                            currentSession = conferenceSession;
                            initListeners();
                            timer.purge();
                            timer.cancel();
                            timer = null;
                            reconnectionState = ReconnectionState.COMPLETED;
                            for (ReconnectionListener reconnectionListener : reconnectionListeners) {
                                new Handler(Looper.getMainLooper()).post(() -> {
                                    reconnectionListener.onChangedState(reconnectionState);
                                });
                            }
                        }

                        @Override
                        public void onError(WsException exception) {
                            reconnect();
                        }
                    });
                }
            }, newDelay);
        }
    }

    //////////////////////////////////////////
    //    Session State Callback
    //////////////////////////////////////////

    private class SessionStateListener implements QBRTCSessionStateCallback<ConferenceSession> {
        @Override
        public void onStateChanged(ConferenceSession conferenceSession, BaseSession.QBRTCSessionState qbrtcSessionState) {
            // empty
        }

        @Override
        public void onConnectedToUser(ConferenceSession conferenceSession, Integer userID) {
            Log.d(TAG, "onConnectedToUser userID= " + userID + " sessionID= " + conferenceSession.getSessionID());
            notifyCallStateListenersCallStarted();
            if (usersConnectDisconnectCallback != null) {
                usersConnectDisconnectCallback.onUserConnected(userID);
            }
            if (userID.equals(currentSession.getCurrentUserID()) && reconnectionState == ReconnectionState.IN_PROGRESS) {
                reconnectionState = ReconnectionState.COMPLETED;
                for (ReconnectionListener reconnectionListener : reconnectionListeners) {
                    new Handler(Looper.getMainLooper()).post(() -> {
                        reconnectionListener.onChangedState(reconnectionState);
                    });
                }
            }
        }

        @Override
        public void onDisconnectedFromUser(ConferenceSession conferenceSession, Integer userID) {
            if (userID.equals(currentSession.getCurrentUserID()) || conferenceSession.getConferenceRole() == QBConferenceRole.LISTENER) {
                reconnectionState = ReconnectionState.IN_PROGRESS;
                for (ReconnectionListener reconnectionListener : reconnectionListeners) {
                    new Handler(Looper.getMainLooper()).post(() -> {
                        reconnectionListener.onChangedState(reconnectionState);
                    });
                }
                currentSession.leave();
            }
        }

        @Override
        public void onConnectionClosedForUser(ConferenceSession conferenceSession, Integer userID) {
            Log.d(TAG, "QBRTCSessionStateCallbackImpl onConnectionClosedForUser userID=" + userID);
            removeVideoTrack(userID);
            if (usersConnectDisconnectCallback != null) {
                usersConnectDisconnectCallback.onUserDisconnected(userID);
            }
        }
    }

    //////////////////////////////////////////
    //    Camera Events Handler
    //////////////////////////////////////////

    private class CameraEventsListener implements CameraVideoCapturer.CameraEventsHandler {
        @Override
        public void onCameraError(String s) {
            // empty
        }

        @Override
        public void onCameraDisconnected() {
            // empty
        }

        @Override
        public void onCameraFreezed(String s) {
//            ToastUtils.shortToast(getApplicationContext(), "Camera Freezed");
            // TODO: Need to make switching camera OFF and then Switching it ON
            /*if (currentSession != null) {
                try {
                    currentSession.getMediaStreamManager().getVideoCapturer().stopCapture();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        int videoWidth = QBRTCMediaConfig.VideoQuality.VGA_VIDEO.width;
                        int videoHeight = QBRTCMediaConfig.VideoQuality.VGA_VIDEO.height;

                        if (currentSession != null && currentSession.getMediaStreamManager() != null
                                && currentSession.getMediaStreamManager().getVideoCapturer() != null) {
                            currentSession.getMediaStreamManager().getVideoCapturer().startCapture(videoWidth, videoHeight, 30);
                        }
                    }
                }, 3000);

            }*/
        }

        @Override
        public void onCameraOpening(String s) {
            // empty
        }

        @Override
        public void onFirstFrameAvailable() {
            // empty
        }

        @Override
        public void onCameraClosed() {
            // empty
        }
    }

    //////////////////////////////////////////
    //    Video Tracks Callbacks
    //////////////////////////////////////////

    private class VideoTrackListener implements QBRTCClientVideoTracksCallbacks<ConferenceSession> {
        @Override
        public void onLocalVideoTrackReceive(ConferenceSession qbrtcSession, QBRTCVideoTrack videoTrack) {
            Log.d(TAG, "onLocalVideoTrackReceive()");
            if (videoTrack != null) {
                int userID = currentSession.getCurrentUserID();
                removeVideoTrack(userID);
                addVideoTrack(userID, videoTrack);
            }
        }

        @Override
        public void onRemoteVideoTrackReceive(ConferenceSession session, QBRTCVideoTrack videoTrack, Integer userID) {
            Log.d(TAG, "onRemoteVideoTrackReceive for Opponent= " + userID);
            if (videoTrack != null && userID != null) {
                addVideoTrack(userID, videoTrack);
            }
        }
    }

    private class AudioTrackListener implements QBRTCClientAudioTracksCallback<ConferenceSession> {
        @Override
        public void onLocalAudioTrackReceive(ConferenceSession conferenceSession, QBRTCAudioTrack qbrtcAudioTrack) {
            Log.d(TAG, "onLocalAudioTrackReceive()");
            boolean isMicEnabled = ((App) getApplicationContext()).getSharedPrefsHelper().get(Consts.PREF_MIC_ENABLED, true);
            currentSession.getMediaStreamManager().getLocalAudioTrack().setEnabled(isMicEnabled);
        }

        @Override
        public void onRemoteAudioTrackReceive(ConferenceSession conferenceSession, QBRTCAudioTrack qbrtcAudioTrack, Integer userID) {
            Log.d(TAG, "onRemoteAudioTrackReceive for Opponent= " + userID);
        }
    }

    private class OnlineParticipantsCheckerCountdown extends CountDownTimer {

        OnlineParticipantsCheckerCountdown(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            getOnlineParticipants(new ConferenceEntityCallback<Map<Integer, Boolean>>() {
                @Override
                public void onSuccess(Map<Integer, Boolean> integerBooleanMap) {
                    if (onlineParticipantsChangeListener != null) {
                        onlineParticipantsChangeListener.onParticipantsCountChanged(onlineParticipants);
                    }
                    onlineParticipants = integerBooleanMap;
                }

                @Override
                public void onError(WsException exception) {
                    if (exception != null) {
                        Log.d(TAG, "Error Getting Online Participants - " + exception.getMessage());
                    }
                }
            });
        }

        @Override
        public void onFinish() {
            start();
        }
    }

    public interface CurrentCallStateCallback {
        void onCallStarted();
    }

    public interface UsersConnectDisconnectCallback {
        void onUserConnected(Integer userID);

        void onUserDisconnected(Integer userID);
    }

    public interface OnlineParticipantsChangeListener {
        void onParticipantsCountChanged(Map<Integer, Boolean> onlineParticipants);
    }

    public interface ReconnectionListener {
        void onChangedState(ReconnectionState reconnectionState);
    }

    public enum ReconnectionState {
        COMPLETED,
        IN_PROGRESS,
        FAILED,
        DEFAULT
    }
}