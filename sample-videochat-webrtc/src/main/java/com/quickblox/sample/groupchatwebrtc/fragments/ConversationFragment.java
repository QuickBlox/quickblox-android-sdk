package com.quickblox.sample.groupchatwebrtc.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.quickblox.sample.core.utils.Toaster;
import com.quickblox.sample.groupchatwebrtc.R;
import com.quickblox.sample.groupchatwebrtc.activities.CallActivity;
import com.quickblox.sample.groupchatwebrtc.activities.ListUsersActivity;
import com.quickblox.sample.groupchatwebrtc.utils.Consts;
import com.quickblox.sample.groupchatwebrtc.holder.DataHolder;
import com.quickblox.sample.groupchatwebrtc.utils.FragmentLifeCycleHandler;
import com.quickblox.sample.groupchatwebrtc.utils.QBRTCSessionUtils;
import com.quickblox.users.model.QBUser;
import com.quickblox.videochat.webrtc.QBMediaStreamManager;
import com.quickblox.videochat.webrtc.QBRTCSession;
import com.quickblox.videochat.webrtc.QBRTCTypes;
import com.quickblox.videochat.webrtc.callbacks.QBRTCClientVideoTracksCallbacks;
import com.quickblox.videochat.webrtc.callbacks.QBRTCSessionConnectionCallbacks;
import com.quickblox.videochat.webrtc.exception.QBRTCException;
import com.quickblox.videochat.webrtc.view.QBRTCVideoTrack;

import org.webrtc.EglBase;
import org.webrtc.RendererCommon.ScalingType;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoCapturerAndroid;
import org.webrtc.VideoRenderer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;


/**
 * QuickBlox team
 */
public abstract class ConversationFragment extends Fragment implements SessionController.QBRTCSessionUserCallback,
        QBRTCClientVideoTracksCallbacks, QBRTCSessionConnectionCallbacks, SessionController.AudioStateCallback,
        FragmentLifeCycleHandler.FragmentLifycleListener {

    private static final String TAG = ConversationFragment.class.getSimpleName();

    public static final String CALLER_NAME = "caller_name";
    public static final String SESSION_ID = "sessionID";
    public static final String START_CONVERSATION_REASON = "start_conversation_reason";

    private static final long TOGGLE_CAMERA_DELAY = 1000;

    protected boolean isVideoEnabled = false;
    protected SurfaceViewRenderer localVideoView;
    protected EglBase rootEglBase;
    protected View view;

    protected ArrayList<QBUser> opponents;
    protected SessionController sessionController;
    private int qbConferenceType;
    private int startReason;
    private String sessionID;

    private ToggleButton cameraToggle;
    private ToggleButton switchCameraToggle;
    private ToggleButton dynamicToggleVideoCall;
    private ToggleButton micToggleVideoCall;
    private ImageButton handUpVideoCall;
    private View myCameraOff;
    private TextView incUserName;

    private Map<String, String> userInfo;
    private boolean isAudioEnabled = true;
    private String callerName;
    private boolean isMessageProcessed;

    private CameraState cameraState = CameraState.NONE;
    private boolean isPeerToPeerCall;
    private FragmentLifeCycleHandler mainHandler;
    private SeekBar captureFormatSlider;
    private TextView captureFormatText;
    private OnCallSettingsController videoSettingsController;
    private ImageButton videoScalingButton;
    private ScalingType scalingType = ScalingType.SCALE_ASPECT_FIT;

    public static ConversationFragment newInstance(List<QBUser> opponents, String callerName,
                                                   QBRTCTypes.QBConferenceType qbConferenceType,
                                                   Map<String, String> userInfo,
                                                   CallActivity.StartConversetionReason reason,
                                                   String sesionnId) {
        ConversationFragment fragment = (opponents.size() == 1) ? new OneToOneConversationFragment() :
                new GroupConversationFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(Consts.CONFERENCE_TYPE, qbConferenceType.getValue());
        bundle.putString(CALLER_NAME, callerName);
        bundle.putSerializable(Consts.OPPONENTS, (Serializable) opponents);
        if (userInfo != null) {
            for (String key : userInfo.keySet()) {
                bundle.putString("UserInfo:" + key, userInfo.get(key));
            }
        }
        bundle.putInt(START_CONVERSATION_REASON, reason.ordinal());
        if (sesionnId != null) {
            bundle.putString(SESSION_ID, sesionnId);
        }
        fragment.setArguments(bundle);
        return fragment;
    }

    protected abstract TextView getStatusViewForOpponent(int userId);

    protected abstract void initCustomView(View view);

    protected abstract SurfaceViewRenderer getVideoViewForOpponent(Integer userID);

    protected View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState,
                                int contentId) {
        view = inflater.inflate(R.layout.conversation_fragment, container, false);
        Log.d(TAG, "Fragment. Thread id: " + Thread.currentThread().getId());

        ((CallActivity) getActivity()).initActionBarWithTimer();

        if (getArguments() != null) {
            opponents = (ArrayList<QBUser>) getArguments().getSerializable(Consts.OPPONENTS);
            qbConferenceType = getArguments().getInt(Consts.CONFERENCE_TYPE);
            startReason = getArguments().getInt(CallActivity.START_CONVERSATION_REASON);
            sessionID = getArguments().getString(CallActivity.SESSION_ID);
            callerName = getArguments().getString(CallActivity.CALLER_NAME);

            isPeerToPeerCall = opponents.size() == 1;
            isVideoEnabled = (qbConferenceType ==
                    QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_VIDEO.getValue());
            Log.d(TAG, "CALLER_NAME: " + callerName);
            Log.d(TAG, "opponents: " + opponents.toString());
        }


        // Create video renderers.
        initContentView(view, contentId);
        initViews(view);
        initButtonsListener();
        initSessionListener();
        setUpUiByCallType(qbConferenceType);

        mainHandler = new FragmentLifeCycleHandler(this);
        return view;
    }

    @Override
    public void onAttach(Activity context) {
        super.onAttach(context);
        sessionController = (SessionController) context;
        videoSettingsController = (OnCallSettingsController) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        sessionController = null;
        videoSettingsController = null;
        mainHandler.detach();
    }

    private void initSessionListener() {
        sessionController.addVideoTrackCallbacksListener(this);
    }

    private void setUpUiByCallType(int qbConferenceType) {
        if (!isVideoEnabled) {
            cameraToggle.setVisibility(View.GONE);
            switchCameraToggle.setVisibility(View.INVISIBLE);
        }
    }

    public void actionButtonsEnabled(boolean enability) {

        cameraToggle.setEnabled(enability);
        micToggleVideoCall.setEnabled(enability);
        dynamicToggleVideoCall.setEnabled(enability);

        // inactivate toggle buttons
        cameraToggle.setActivated(enability);
        micToggleVideoCall.setActivated(enability);
        dynamicToggleVideoCall.setActivated(enability);

        switchCameraToggle.setEnabled(enability);
        switchCameraToggle.setActivated(enability);

        if (isVideoEnabled) {
            captureFormatSlider.setEnabled(enability);
            captureFormatSlider.setActivated(enability);
        }
    }


    @Override
    public void onStart() {

        super.onStart();
        QBRTCSession session = sessionController.getCurrentSession();
        if (!isMessageProcessed) {
            if (startReason == CallActivity.StartConversetionReason.INCOME_CALL_FOR_ACCEPTION.ordinal()) {
                session.acceptCall(session.getUserInfo());
            } else {
                session.startCall(session.getUserInfo());
            }
            isMessageProcessed = true;
        }
        sessionController.addTCClientConnectionCallback(this);
        sessionController.addRTCSessionUserCallback(this);
        sessionController.addAudioStateCallback(this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate() from " + TAG);
        super.onCreate(savedInstanceState);
    }

    protected void initContentView(View view, int layoutID) {
        ViewStub stubCompat = (ViewStub) view.findViewById(R.id.main_content);
        stubCompat.setLayoutResource(layoutID);
        stubCompat.inflate();
    }

    private void initViews(View view) {
        localVideoView = (SurfaceViewRenderer) view.findViewById(R.id.localSurfView);

        localVideoView.setZOrderMediaOverlay(true);
        updateVideoView(localVideoView,  false);
        initLocalViewUI(view);

        if (isVideoEnabled) {
            initVideoCallSettings(view);
        }

        cameraToggle = (ToggleButton) view.findViewById(R.id.cameraToggle);
        dynamicToggleVideoCall = (ToggleButton) view.findViewById(R.id.dynamicToggleVideoCall);
        micToggleVideoCall = (ToggleButton) view.findViewById(R.id.micToggleVideoCall);

        handUpVideoCall = (ImageButton) view.findViewById(R.id.handUpVideoCall);
        incUserName = (TextView) view.findViewById(R.id.incUserName);
        incUserName.setText(callerName);
        incUserName.setBackgroundResource(ListUsersActivity.selectBackgrounForOpponent((
                DataHolder.getUserIndexByFullName(callerName)) + 1));
        actionButtonsEnabled(false);
        initCustomView(view);
        initRemoteView();
    }

    private void initVideoCallSettings(View view) {
        view.findViewById(R.id.video_call_settings_view).setVisibility(View.VISIBLE);

        videoScalingButton =
                (ImageButton) view.findViewById(R.id.button_call_scaling_mode);

        captureFormatText =
                (TextView) view.findViewById(R.id.capture_format_text_call);
        captureFormatSlider =
                (SeekBar) view.findViewById(R.id.capture_format_slider_call);

        captureFormatSlider.setOnSeekBarChangeListener(
                new CaptureQualityController(captureFormatText, videoSettingsController));
    }

    protected abstract void initRemoteView();

    protected void updateVideoView(SurfaceViewRenderer surfaceViewRenderer, boolean mirror) {
        updateVideoView(surfaceViewRenderer, mirror, ScalingType.SCALE_ASPECT_FIT);
    }

    protected void updateVideoView(SurfaceViewRenderer surfaceViewRenderer, boolean mirror, ScalingType scalingType) {
        Log.i(TAG, "updateVideoView mirror:" + mirror +", scalintType = "+ scalingType);
        surfaceViewRenderer.setScalingType(scalingType);
        surfaceViewRenderer.setMirror(mirror);
        surfaceViewRenderer.requestLayout();
    }

    @Override
    public void onResume() {
        super.onResume();

        // If user changed camera state few times and last state was CameraState.ENABLED_FROM_USER // Жень, глянь здесь, смысл в том, что мы здесь включаем камеру, если юзер ее не выключал
        // than we turn on cam, else we nothing change
        if (cameraState != CameraState.DISABLED_FROM_USER
                && isVideoEnabled) {
            toggleCamera(true);
        }
    }

    @Override
    public void onPause() {
        // If camera state is CameraState.ENABLED_FROM_USER or CameraState.NONE
        // than we turn off cam
        if (cameraState != CameraState.DISABLED_FROM_USER && isVideoEnabled) {
            toggleCamera(false);
        }

        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        sessionController.removeRTCClientConnectionCallback(this);
        sessionController.removeRTCSessionUserCallback(this);
    }

    @Override
    public void onRemoteVideoTrackReceive(QBRTCSession session, final QBRTCVideoTrack videoTrack,
                                          final Integer userID) {
        Log.d(TAG, "onRemoteVideoTrackReceive for opponent= " + userID);
        SurfaceViewRenderer remoteVideoView = getVideoViewForOpponent(userID);
        if (remoteVideoView != null) {
            fillVideoView(remoteVideoView, videoTrack, true);
        }
    }

    private void initSwitchCameraButton(View view) {
        switchCameraToggle = (ToggleButton) view.findViewById(R.id.switchCameraToggle);
        switchCameraToggle.setVisibility(isVideoEnabled ?
                View.VISIBLE : View.INVISIBLE);
    }

    private void initButtonsListener() {

        cameraToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                cameraState =  isChecked ? CameraState.ENABLED_FROM_USER : CameraState.DISABLED_FROM_USER;
                enableCamera(isChecked);
            }
        });

        dynamicToggleVideoCall.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                sessionController.switchAudio();
            }
        });


        micToggleVideoCall.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                enableAudio(isChecked);
            }
        });

        handUpVideoCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionButtonsEnabled(false);
                Log.d(TAG, "Call is stopped");

                sessionController.hangUpCurrentSession(" because I'm busy");
                handUpVideoCall.setEnabled(false);
                handUpVideoCall.setActivated(false);

            }
        });

        switchCameraToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                QBRTCSession currentSession = sessionController.getCurrentSession();
                if (currentSession == null) {
                    return;
                }
                final QBMediaStreamManager mediaStreamManager = currentSession.getMediaStreamManager();
                if (mediaStreamManager == null) {
                    return;
                }
                mediaStreamManager.switchCameraInput(null);
            }
        });

        if (videoScalingButton != null) {
            videoScalingButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    scalingType = getRandomScalingType();
                    onVideoScalingUpdated(scalingType);
                }
            });
        }

    }

    ScalingType getRandomScalingType(){
        Random random = new Random();
        int nextInt = random.nextInt(3);

        ScalingType[] values = ScalingType.values();
        return values[nextInt];
    }

    protected void onVideoScalingUpdated(ScalingType scalingType ) {
        Log.i(TAG, "onVideoScalingUpdated to " +scalingType);
        Toaster.longToast("View format changed to :"+scalingType);
        updateVideoView(localVideoView, true, scalingType);
    }

    protected void runOnUiThread(Runnable runnable) {
        mainHandler.post(runnable);
    }

    private void toggleCamera(boolean isNeedEnableCam) {
        QBRTCSession currentSession = sessionController.getCurrentSession();
        if (currentSession != null && currentSession.getMediaStreamManager() != null) {
            if (isNeedEnableCam) {
                currentSession.getMediaStreamManager().startVideoSource();
            } else {
                currentSession.getMediaStreamManager().stopVideoSource();
            }
            myCameraOff.setVisibility(isNeedEnableCam ? View.INVISIBLE : View.VISIBLE);
            switchCameraToggle.setVisibility(isNeedEnableCam ? View.VISIBLE : View.INVISIBLE);
        }
    }

    @Override
    public void onWiredHeadsetStateChanged(boolean plugged) {
        dynamicToggleVideoCall.setChecked(plugged);
    }

    private void enableCamera(boolean isNeedEnableCam) {
        QBRTCSession currentSession = sessionController.getCurrentSession();
        if (currentSession != null && currentSession.getMediaStreamManager() != null) {
            currentSession.getMediaStreamManager().setVideoEnabled(isNeedEnableCam);
            myCameraOff.setVisibility(isNeedEnableCam ? View.INVISIBLE : View.VISIBLE);
            switchCameraToggle.setVisibility(isNeedEnableCam ? View.VISIBLE : View.INVISIBLE);
        }
    }

    private void enableAudio(boolean enable) {
        QBRTCSession currentSession = sessionController.getCurrentSession();
        if (currentSession != null) {
            isAudioEnabled = enable;
            currentSession.getMediaStreamManager().setAudioEnabled(enable);
        }
    }

    @Override
    public void onLocalVideoTrackReceive(QBRTCSession qbrtcSession, final QBRTCVideoTrack videoTrack) {
        Log.d(TAG, "onLocalVideoTrackReceive() run");

        if (localVideoView != null) {
            fillVideoView(localVideoView, videoTrack, !isPeerToPeerCall);
        }
    }

    private void initLocalViewUI(View localView) {
        initSwitchCameraButton(localView);
        myCameraOff = localView.findViewById(R.id.cameraOff);
    }

    private void fillVideoView(SurfaceViewRenderer videoView, QBRTCVideoTrack videoTrack, boolean remoteRenderer) {
        videoTrack.addRenderer(new VideoRenderer(videoView));
        Log.d(TAG, (remoteRenderer ? "remote" : "local") + " Track is rendering");
    }

    private void fillVideoView(SurfaceViewRenderer videoView, QBRTCVideoTrack videoTrack) {
        fillVideoView(videoView, videoTrack, true);
    }

    private void setStatusForOpponent(int userId, final String status) {
        final TextView opponentView = getStatusViewForOpponent(userId);
        if (opponentView == null) {
            return;
        }
        opponentView.setText(status);

    }

    @Override
    public void onStartConnectToUser(QBRTCSession qbrtcSession, Integer userId) {
        Log.i(TAG, "onStartConnectToUser" + userId);
        setStatusForOpponent(userId, getString(R.string.checking));
    }

    @Override
    public void onConnectedToUser(QBRTCSession qbrtcSession, final Integer userId) {
        Log.i(TAG, "onConnectedToUser" + userId);
        actionButtonsEnabled(true);
        setStatusForOpponent(userId, getString(R.string.connected));
    }


    @Override
    public void onConnectionClosedForUser(QBRTCSession qbrtcSession, Integer userId) {
        Integer status = QBRTCSessionUtils.getStatusDescriptionReosuurce(qbrtcSession.getPeerChannel(userId).getDisconnectReason());
        if (status == null) {
            status = R.string.closed;
        }
        getVideoViewForOpponent(userId).release();
        setStatusForOpponent(userId, getString(status));
    }

    @Override
    public void onDisconnectedFromUser(QBRTCSession qbrtcSession, Integer integer) {
        setStatusForOpponent(integer, getString(R.string.disconnected));
    }

    @Override
    public void onDisconnectedTimeoutFromUser(QBRTCSession qbrtcSession, Integer integer) {
        setStatusForOpponent(integer, getString(R.string.time_out));
    }

    @Override
    public void onConnectionFailedWithUser(QBRTCSession qbrtcSession, Integer integer) {
        Log.i(TAG, "onConnectionFailedWithUser" + integer);
        setStatusForOpponent(integer, getString(R.string.failed));
    }

    @Override
    public void onError(QBRTCSession qbrtcSession, QBRTCException e) {
        Log.i(TAG, "onError" + e.getLocalizedMessage());
    }

    @Override
    public void onUserNotAnswer(QBRTCSession session, Integer userId) {
        setStatusForOpponent(userId, getString(R.string.noAnswer));
    }

    @Override
    public void onCallRejectByUser(QBRTCSession session, Integer userId, String userInfo) {
        setStatusForOpponent(userId, getString(R.string.rejected));
    }

    @Override
    public void onCallAcceptByUser(QBRTCSession session, Integer userId, Map<String, String> userInfo) {
        setStatusForOpponent(userId, getString(R.string.accepted));
    }

    @Override
    public void onReceiveHangUpFromUser(QBRTCSession session, Integer userId, String userInfo) {
        setStatusForOpponent(userId, getString(R.string.hungUp));
    }

    @Override
    public boolean isFragmentAlive() {
        return isAdded();
    }

    private class AudioStreamReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction().equals(AudioManager.ACTION_HEADSET_PLUG)) {
                Log.d(TAG, "ACTION_HEADSET_PLUG " + intent.getIntExtra("state", -1));
            } else if (intent.getAction().equals(AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED)) {
                Log.d(TAG, "ACTION_SCO_AUDIO_STATE_UPDATED " + intent.getIntExtra("EXTRA_SCO_AUDIO_STATE", -2));
            }

        }
    }

    private enum CameraState {
        NONE,
        DISABLED_FROM_USER,
        ENABLED_FROM_USER
    }

    class CameraSwitchCallback implements VideoCapturerAndroid.CameraSwitchHandler {
        @Override
        public void onCameraSwitchDone(boolean b) {

        }

        @Override
        public void onCameraSwitchError(String s) {

        }

    }

}


