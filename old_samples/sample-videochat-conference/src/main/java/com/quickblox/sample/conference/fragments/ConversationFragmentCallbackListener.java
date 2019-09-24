package com.quickblox.sample.conference.fragments;

import com.quickblox.conference.ConferenceSession;
import com.quickblox.sample.conference.activities.CallActivity;
import com.quickblox.videochat.webrtc.callbacks.QBRTCSessionStateCallback;

import org.webrtc.CameraVideoCapturer;


public interface ConversationFragmentCallbackListener {

    void addClientConnectionCallback(QBRTCSessionStateCallback<ConferenceSession> clientConnectionCallbacks);

    void removeClientConnectionCallback(QBRTCSessionStateCallback clientConnectionCallbacks);

    void addCurrentCallStateCallback(CallActivity.CurrentCallStateCallback currentCallStateCallback);

    void removeCurrentCallStateCallback(CallActivity.CurrentCallStateCallback currentCallStateCallback);

    void addOnChangeDynamicToggle(CallActivity.OnChangeDynamicToggle onChangeDynamicCallback);

    void removeOnChangeDynamicToggle(CallActivity.OnChangeDynamicToggle onChangeDynamicCallback);

    void onSetAudioEnabled(boolean isAudioEnabled);

    void onSetVideoEnabled(boolean isNeedEnableCam);

    void onSwitchAudio();

    void onLeaveCurrentSession();

    void onSwitchCamera(CameraVideoCapturer.CameraSwitchHandler cameraSwitchHandler);

    void onStartJoinConference();
}