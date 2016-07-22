package com.quickblox.sample.groupchatwebrtc.fragments;

import com.quickblox.videochat.webrtc.QBRTCSession;
import com.quickblox.videochat.webrtc.callbacks.QBRTCClientVideoTracksCallbacks;
import com.quickblox.videochat.webrtc.callbacks.QBRTCSessionConnectionCallbacks;

import org.webrtc.RendererCommon;

import java.util.Map;

public interface SessionController {

    void hangUpCurrentSession(String hangUpReason);

    QBRTCSession getCurrentSession();

    void addVideoTrackCallbacksListener(QBRTCClientVideoTracksCallbacks videoTracksCallbacks);

    void addTCClientConnectionCallback(QBRTCSessionConnectionCallbacks sessionConnectionCallbacks);

    void addRTCSessionUserCallback(QBRTCSessionUserCallback qbrtcSessionUserCallback);

    void removeRTCClientConnectionCallback(QBRTCSessionConnectionCallbacks sessionConnectionCallbacks);

    void removeRTCSessionUserCallback(QBRTCSessionUserCallback sessionUserCallback);

    void addAudioStateCallback(AudioStateCallback audioStateCallback);

    void switchAudio();

    void onUseHeadSet(boolean use);

    void onVideoScalingSwitch(RendererCommon.ScalingType scalingType);
    void onCaptureFormatChange(int width, int height, int framerate);

    interface AudioStateCallback {
        void onWiredHeadsetStateChanged(boolean plugged);
    }

    interface QBRTCSessionUserCallback {
        void onUserNotAnswer(QBRTCSession session, Integer userId);

        void onCallRejectByUser(QBRTCSession session, Integer userId, String userInfo);

        void onCallAcceptByUser(QBRTCSession session, Integer userId, Map<String, String> userInfo);

        void onReceiveHangUpFromUser(QBRTCSession session, Integer userId, String userInfo);
    }
}
