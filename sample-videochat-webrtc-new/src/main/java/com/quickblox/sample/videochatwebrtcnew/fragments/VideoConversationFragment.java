package com.quickblox.sample.videochatwebrtcnew.fragments;

import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ToggleButton;

import com.quickblox.sample.videochatwebrtcnew.R;
import com.quickblox.sample.videochatwebrtcnew.SessionManager;
import com.quickblox.videochat.webrtc.QBRTCClient;
import com.quickblox.videochat.webrtc.QBRTCSession;
import com.quickblox.videochat.webrtc.QBRTCTypes;
import com.quickblox.videochat.webrtc.callbacks.QBRTCClientVideoTracksCallbacks;
import com.quickblox.videochat.webrtc.view.QBGLVideoView;
import com.quickblox.videochat.webrtc.view.QBRTCVideoTrack;
import com.quickblox.videochat.webrtc.view.VideoCallBacks;

import org.webrtc.VideoRenderer;

/**
 * Created by tereha on 15.07.15.
 */
public class VideoConversationFragment extends BaseConversationFragment implements View.OnClickListener, QBRTCClientVideoTracksCallbacks {


    private static final String TAG = VideoConversationFragment.class.getSimpleName();
    private QBGLVideoView localVideoView;
    private QBGLVideoView remoteVideoView;
    private ToggleButton cameraToggle;
    private ToggleButton switchCameraToggle;
    private ImageView imgMyCameraOff;
    private CameraState cameraState = CameraState.NONE;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        QBRTCClient.getInstance().addVideoTrackCallbacksListener(this);
    }

    @Override
    protected int getContentView() {
        return R.layout.fragment_conversation_base;
    }

    @Override
    public void onResume() {
        Log.d(TAG, "onResume()");
        super.onResume();
        // If user changed camera state few times and last state was CameraState.ENABLED_FROM_USER
        // than we turn on cam, else we nothing change
        if (cameraState != CameraState.DISABLED_FROM_USER
                && qbConferenceType == QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_VIDEO) {
            toggleCamera(true);
        }
    }

    @Override
    public void onPause() {
        Log.d(TAG, "onPause()");
        // If camera state is CameraState.ENABLED_FROM_USER or CameraState.NONE
        // than we turn off cam
        if(cameraState != CameraState.DISABLED_FROM_USER) {
            toggleCamera(false);
        }

        super.onPause();
    }

    @Override
    protected void initViews(View view){
        super.initViews(view);

        localVideoView = (QBGLVideoView) view.findViewById(R.id.localVideoVidew);
        localVideoView.setVisibility(View.VISIBLE);

        remoteVideoView = (QBGLVideoView) view.findViewById(R.id.remoteVideoView);
        remoteVideoView.setVisibility(View.VISIBLE);

        cameraToggle = (ToggleButton) view.findViewById(R.id.cameraToggle);
        cameraToggle.setVisibility(View.VISIBLE);
        cameraToggle.setOnClickListener(this);

        switchCameraToggle = (ToggleButton) view.findViewById(R.id.switchCameraToggle);
        switchCameraToggle.setVisibility(View.VISIBLE);
        switchCameraToggle.setOnClickListener(this);

        imgMyCameraOff = (ImageView) view.findViewById(R.id.imgMyCameraOff);

        actionButtonsEnabled(false);
    }

    @Override
    public void actionButtonsEnabled(boolean enability) {
        super.actionButtonsEnabled(enability);

        cameraToggle.setEnabled(enability);
        switchCameraToggle.setEnabled(enability);
        imgMyCameraOff.setEnabled(enability);

        // inactivate toggle buttons
        cameraToggle.setActivated(enability);
        switchCameraToggle.setActivated(enability);
        imgMyCameraOff.setActivated(enability);
    }

    private void toggleCamera(boolean isNeedEnableCam) {
        // TODO temporary insertion will be removed when GLVideoView will be fixed
        DisplayMetrics displaymetrics = new DisplayMetrics();
        displaymetrics.setToDefaults();

        ViewGroup.LayoutParams layoutParams = imgMyCameraOff.getLayoutParams();

        layoutParams.height = localVideoView.getHeight();
        layoutParams.width = localVideoView.getWidth();

        imgMyCameraOff.setLayoutParams(layoutParams);

        Log.d(TAG, "Width is: " + imgMyCameraOff.getLayoutParams().width + " height is:" + imgMyCameraOff.getLayoutParams().height);
        // TODO end

        if (SessionManager.getCurrentSession() != null) {
            SessionManager.getCurrentSession().setVideoEnabled(isNeedEnableCam);
            cameraToggle.setChecked(isNeedEnableCam);

            if (isNeedEnableCam) {
                Log.d(TAG, "Camera is on!");
                switchCameraToggle.setVisibility(View.VISIBLE);
                imgMyCameraOff.setVisibility(View.INVISIBLE);
            } else {
                Log.d(TAG, "Camera is off!");
                switchCameraToggle.setVisibility(View.INVISIBLE);
                imgMyCameraOff.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onLocalVideoTrackReceive(QBRTCSession qbrtcSession, QBRTCVideoTrack qbrtcVideoTrack) {
        Log.d(TAG, "localVideoView is " + localVideoView);
        if (localVideoView != null) {
            qbrtcVideoTrack.addRenderer(new VideoRenderer(new VideoCallBacks(localVideoView, QBGLVideoView.Endpoint.LOCAL)));
            localVideoView.setVideoTrack(qbrtcVideoTrack, QBGLVideoView.Endpoint.LOCAL);
            Log.d(TAG, "onLocalVideoTrackReceive() is rune");
        }
    }

    @Override
    public void onRemoteVideoTrackReceive(QBRTCSession qbrtcSession, QBRTCVideoTrack qbrtcVideoTrack, Integer integer) {
        Log.d(TAG, "remoteVideoView is " + remoteVideoView);
        if (remoteVideoView != null) {
            VideoRenderer remoteRenderer = new VideoRenderer(new VideoCallBacks(remoteVideoView, QBGLVideoView.Endpoint.REMOTE));
            qbrtcVideoTrack.addRenderer(remoteRenderer);
            remoteVideoView.setVideoTrack(qbrtcVideoTrack, QBGLVideoView.Endpoint.REMOTE);
            Log.d(TAG, "onRemoteVideoTrackReceive() is rune");
        }
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.switchCameraToggle:
                if (SessionManager.getCurrentSession() != null) {
                    SessionManager.getCurrentSession().switchCapturePosition(new Runnable() {
                        @Override
                        public void run() {
                        }
                    });
                }
                break;
            case R.id.cameraToggle:
                if (SessionManager.getCurrentSession() != null) {
                    if (cameraState != CameraState.DISABLED_FROM_USER) {
                        toggleCamera(false);
                        cameraState = CameraState.DISABLED_FROM_USER;
                    } else {
                        toggleCamera(true);
                        cameraState = CameraState.ENABLED_FROM_USER;
                    }
                }
                break;
            default:
                break;
        }
    }

    private enum CameraState {
        NONE,
        DISABLED_FROM_USER,
        ENABLED_FROM_USER
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        QBRTCClient.getInstance().removeVideoTrackCallbacksListener(this);
    }
}
