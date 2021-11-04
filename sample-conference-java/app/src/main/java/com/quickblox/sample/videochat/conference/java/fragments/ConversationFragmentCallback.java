package com.quickblox.sample.videochat.conference.java.fragments;

import com.quickblox.conference.ConferenceSession;
import com.quickblox.sample.videochat.conference.java.services.CallService;
import com.quickblox.videochat.webrtc.callbacks.QBRTCSessionStateCallback;
import com.quickblox.videochat.webrtc.view.QBRTCVideoTrack;

import org.webrtc.CameraVideoCapturer;

import java.util.HashMap;


public interface ConversationFragmentCallback {

    void addClientConnectionCallback(QBRTCSessionStateCallback<ConferenceSession> clientConnectionCallbacks);

    void removeClientConnectionCallback(QBRTCSessionStateCallback clientConnectionCallbacks);

    void addCurrentCallStateCallback(CallService.CurrentCallStateCallback currentCallStateCallback);

    void removeCurrentCallStateCallback(CallService.CurrentCallStateCallback currentCallStateCallback);

    void addReconnectionCallback(ReconnectionCallback reconnectionCallback);

    void removeReconnectionCallback(ReconnectionCallback reconnectionCallback);

    void onSetAudioEnabled(boolean isAudioEnabled);

    void onSetVideoEnabled(boolean isNeedEnableCam);

    void onLeaveCurrentSession();

    void onSwitchCamera(CameraVideoCapturer.CameraSwitchHandler cameraSwitchHandler);

    void onStartJoinConference();

    void onStartScreenSharing();

    boolean isScreenSharingState();

    HashMap<Integer, QBRTCVideoTrack> getVideoTrackMap();

    void onReturnToChat();

    void onManageGroup();

    String getDialogID();

    String getRoomID();

    String getRoomTitle();

    boolean isListenerRole();
}