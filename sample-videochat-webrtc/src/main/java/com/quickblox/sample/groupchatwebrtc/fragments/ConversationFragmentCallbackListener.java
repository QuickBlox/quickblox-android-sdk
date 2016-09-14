package com.quickblox.sample.groupchatwebrtc.fragments;

import com.quickblox.sample.groupchatwebrtc.activities.CallActivity;
import com.quickblox.videochat.webrtc.callbacks.QBRTCSessionConnectionCallbacks;

/**
 * Created by tereha on 23.05.16.
 */
public interface ConversationFragmentCallbackListener {

    void addTCClientConnectionCallback(QBRTCSessionConnectionCallbacks clientConnectionCallbacks);
    void removeRTCClientConnectionCallback(QBRTCSessionConnectionCallbacks clientConnectionCallbacks);

    void addRTCSessionUserCallback(CallActivity.QBRTCSessionUserCallback sessionUserCallback);
    void removeRTCSessionUserCallback(CallActivity.QBRTCSessionUserCallback sessionUserCallback);

    void addCurrentCallStateCallback (CallActivity.CurrentCallStateCallback currentCallStateCallback);
    void removeCurrentCallStateCallback (CallActivity.CurrentCallStateCallback currentCallStateCallback);

    void addOnChangeDynamicToggle (CallActivity.OnChangeDynamicToggle onChangeDynamicCallback);
    void removeOnChangeDynamicToggle (CallActivity.OnChangeDynamicToggle onChangeDynamicCallback);

    void onSetAudioEnabled(boolean isAudioEnabled);

    void onSetVideoEnabled(boolean isNeedEnableCam);

    void onSwitchAudio();

    void onHangUpCurrentSession();

}
