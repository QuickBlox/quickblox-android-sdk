package com.quickblox.sample.videochat.kotlin.fragments

import com.quickblox.sample.videochat.kotlin.activities.CallActivity
import com.quickblox.videochat.webrtc.BaseSession
import com.quickblox.videochat.webrtc.QBRTCSession
import com.quickblox.videochat.webrtc.QBRTCTypes
import com.quickblox.videochat.webrtc.callbacks.QBRTCClientVideoTracksCallbacks
import com.quickblox.videochat.webrtc.callbacks.QBRTCSessionEventsCallback
import com.quickblox.videochat.webrtc.callbacks.QBRTCSessionStateCallback
import com.quickblox.videochat.webrtc.view.QBRTCVideoTrack
import org.jivesoftware.smack.ConnectionListener
import org.webrtc.CameraVideoCapturer


interface ConversationFragmentCallback {

    fun addConnectionListener(connectionCallback: ConnectionListener?)

    fun removeConnectionListener(connectionCallback: ConnectionListener?)

    fun addSessionStateListener(clientConnectionCallbacks: QBRTCSessionStateCallback<*>?)

    fun removeSessionStateListener(clientConnectionCallbacks: QBRTCSessionStateCallback<*>?)

    fun addVideoTrackListener(callback: QBRTCClientVideoTracksCallbacks<QBRTCSession>?)

    fun removeVideoTrackListener(callback: QBRTCClientVideoTracksCallbacks<QBRTCSession>?)

    fun addSessionEventsListener(eventsCallback: QBRTCSessionEventsCallback?)

    fun removeSessionEventsListener(eventsCallback: QBRTCSessionEventsCallback?)

    fun addCurrentCallStateListener(currentCallStateCallback: CallActivity.CurrentCallStateCallback?)

    fun removeCurrentCallStateListener(currentCallStateCallback: CallActivity.CurrentCallStateCallback?)

    fun addOnChangeAudioDeviceListener(onChangeDynamicCallback: CallActivity.OnChangeAudioDevice?)

    fun removeOnChangeAudioDeviceListener(onChangeDynamicCallback: CallActivity.OnChangeAudioDevice?)

    fun onSetAudioEnabled(isAudioEnabled: Boolean)

    fun onSetVideoEnabled(isNeedEnableCam: Boolean)

    fun onSwitchAudio()

    fun onHangUpCurrentSession()

    fun onStartScreenSharing()

    fun onSwitchCamera(cameraSwitchHandler: CameraVideoCapturer.CameraSwitchHandler)

    fun acceptCall(userInfo: Map<String, String>)

    fun startCall(userInfo: Map<String, String>)

    fun currentSessionExist(): Boolean

    fun getOpponents(): List<Int>?

    fun getCallerId(): Int?

    fun getCurrentSessionState(): BaseSession.QBRTCSessionState?

    fun getPeerChannel(userId: Int): QBRTCTypes.QBRTCConnectionState?

    fun isMediaStreamManagerExist(): Boolean

    fun isCallState(): Boolean

    fun getVideoTrackMap(): MutableMap<Int, QBRTCVideoTrack>

    fun getVideoTrack(userId: Int): QBRTCVideoTrack?
}