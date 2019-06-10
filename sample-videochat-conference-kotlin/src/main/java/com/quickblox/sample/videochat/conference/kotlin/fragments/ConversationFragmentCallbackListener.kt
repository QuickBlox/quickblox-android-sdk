package com.quickblox.sample.videochat.conference.kotlin.fragments

import com.quickblox.conference.ConferenceSession
import com.quickblox.sample.videochat.conference.kotlin.activities.CallActivity
import com.quickblox.videochat.webrtc.callbacks.QBRTCSessionStateCallback
import org.webrtc.CameraVideoCapturer


interface ConversationFragmentCallbackListener {

    fun addClientConnectionCallback(clientConnectionCallbacks: QBRTCSessionStateCallback<ConferenceSession>)

    fun removeClientConnectionCallback(clientConnectionCallbacks: QBRTCSessionStateCallback<*>)

    fun addCurrentCallStateCallback(currentCallStateCallback: CallActivity.CurrentCallStateCallback)

    fun removeCurrentCallStateCallback(currentCallStateCallback: CallActivity.CurrentCallStateCallback)

    fun addOnChangeDynamicToggle(onChangeDynamicCallback: CallActivity.OnChangeDynamicToggle)

    fun removeOnChangeDynamicToggle(onChangeDynamicCallback: CallActivity.OnChangeDynamicToggle)

    fun onSetAudioEnabled(isAudioEnabled: Boolean)

    fun onSetVideoEnabled(isNeedEnableCam: Boolean)

    fun onSwitchAudio()

    fun onLeaveCurrentSession()

    fun onSwitchCamera(cameraSwitchHandler: CameraVideoCapturer.CameraSwitchHandler)

    fun onStartJoinConference()
}