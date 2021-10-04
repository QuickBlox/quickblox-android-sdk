package com.quickblox.sample.conference.kotlin.domain.call

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import androidx.annotation.IntDef
import com.quickblox.chat.model.QBChatDialog
import com.quickblox.conference.ConferenceSession
import com.quickblox.conference.QBConferenceRole
import com.quickblox.conference.WsException
import com.quickblox.conference.callbacks.ConferenceEntityCallback
import com.quickblox.conference.callbacks.ConferenceSessionCallbacks
import com.quickblox.sample.conference.kotlin.R
import com.quickblox.sample.conference.kotlin.data.DataCallBack
import com.quickblox.sample.conference.kotlin.domain.DomainCallback
import com.quickblox.sample.conference.kotlin.domain.call.CallManagerImpl.Companion.CallType.Companion.CONVERSATION
import com.quickblox.sample.conference.kotlin.domain.call.CallManagerImpl.Companion.CallType.Companion.STREAM
import com.quickblox.sample.conference.kotlin.domain.call.entities.CallEntity
import com.quickblox.sample.conference.kotlin.domain.call.entities.CallEntityImpl
import com.quickblox.sample.conference.kotlin.domain.repositories.call.CallRepository
import com.quickblox.sample.conference.kotlin.presentation.resources.ResourcesManager
import com.quickblox.sample.conference.kotlin.presentation.utils.Constants.FACING_FRONT
import com.quickblox.users.model.QBUser
import com.quickblox.videochat.webrtc.*
import com.quickblox.videochat.webrtc.AppRTCAudioManager.*
import com.quickblox.videochat.webrtc.QBRTCCameraVideoCapturer.QBRTCCameraCapturerException
import com.quickblox.videochat.webrtc.callbacks.QBRTCClientAudioTracksCallback
import com.quickblox.videochat.webrtc.callbacks.QBRTCClientVideoTracksCallbacks
import com.quickblox.videochat.webrtc.callbacks.QBRTCSessionStateCallback
import com.quickblox.videochat.webrtc.view.QBRTCVideoTrack
import org.webrtc.CameraVideoCapturer
import java.util.*

private const val MAX_CONFERENCE_OPPONENTS_ALLOWED = 12
private const val ICE_FAILED_REASON = "ICE failed"
private const val ONLINE_INTERVAL = 3000L
private const val MILLIS_FUTURE = 7000L

/*
 * Created by Injoit in 2021-09-30.
 * Copyright © 2021 Quickblox. All rights reserved.
 */
class CallManagerImpl(private val context: Context, private val resourcesManager: ResourcesManager,
                      private val callRepository: CallRepository) : CallManager {
    private var currentSession: ConferenceSession? = null
    private var qbRtcSessionStateListener: QBRTCSessionStateCallback<ConferenceSession>? = null
    private var conferenceSessionListener: ConferenceSessionCallbacks? = null
    private var videoTrackListener: QBRTCClientVideoTracksCallbacks<ConferenceSession>? = null
    private var audioTrackListener: QBRTCClientAudioTracksCallback<ConferenceSession>? = null
    private var callListeners = hashSetOf<CallListener>()
    private val subscribedPublishers = hashSetOf<Int?>()
    private val callEntities = linkedSetOf<CallEntity>()
    private var audioManager: AppRTCAudioManager? = null
    private var role: QBConferenceRole? = null
    private var callType: Int? = null
    private var isEnableVideoState: Boolean? = false
    private var currentDialog: QBChatDialog? = null
    private val timer = OnlineParticipantsCheckerCountdown(MILLIS_FUTURE, ONLINE_INTERVAL)

    companion object {
        @IntDef(CONVERSATION, STREAM)
        annotation class CallType {
            companion object {
                const val CONVERSATION = 0
                const val STREAM = 1
            }
        }
    }

    init {
        initAudioManager()
    }

    override fun getEnableVideoState(): Boolean? {
        return isEnableVideoState
    }

    override fun setEnableVideoState(enableState: Boolean?) {
        this.isEnableVideoState = enableState
    }

    override fun getRole(): QBConferenceRole? {
        return role
    }

    override fun getCallType(): Int? {
        return callType
    }

    override fun getSession(): ConferenceSession? {
        return currentSession
    }

    override fun getCurrentDialog(): QBChatDialog? {
        return currentDialog
    }

    override fun getCallEntities(): LinkedHashSet<CallEntity> {
        return callEntities
    }

    override fun createSession(currentUser: QBUser, dialog: QBChatDialog, roomId: String, role: QBConferenceRole, callType: Int, callback: DomainCallback<ConferenceSession, Exception>) {
        currentSession?.let {
            releaseSession(null)
        }

        callRepository.createSession(currentUser.id, object : DataCallBack<ConferenceSession, Exception> {
            override fun onSuccess(result: ConferenceSession, bundle: Bundle?) {
                if (result.activePublishers.size >= MAX_CONFERENCE_OPPONENTS_ALLOWED) {
                    result.leave()
                    callback.onError(Exception(resourcesManager.get().getString(R.string.full_room)))
                    return
                }
                currentDialog = dialog
                currentSession = result
                videoTrackListener = VideoTrackListener()
                audioTrackListener = AudioTrackListener()
                qbRtcSessionStateListener = QBRTCSessionStateListener()
                conferenceSessionListener = ConferenceSessionListener()

                currentSession?.addSessionCallbacksListener(qbRtcSessionStateListener)
                currentSession?.addConferenceSessionListener(conferenceSessionListener)
                currentSession?.addVideoTrackCallbacksListener(videoTrackListener)
                currentSession?.addAudioTrackCallbacksListener(audioTrackListener)

                this@CallManagerImpl.role = role
                this@CallManagerImpl.callType = callType
                if (this@CallManagerImpl.callType == STREAM && role == QBConferenceRole.PUBLISHER) {
                    timer.start()
                }
                joinConference(roomId, role)
                callback.onSuccess(result, null)
            }

            override fun onError(error: Exception) {
                callback.onError(error)
            }
        })
    }

    private fun joinConference(dialogId: String?, role: QBConferenceRole) {
        currentSession?.joinDialog(dialogId, role, object : ConferenceEntityCallback<ArrayList<Int?>> {
            override fun onSuccess(publishers: ArrayList<Int?>?) {
                // empty
            }

            override fun onError(exception: WsException) {
                releaseSession(exception)
            }
        })
    }

    private fun initAudioManager() {
        audioManager = create(context)
        audioManager?.selectAudioDevice(AudioDevice.SPEAKER_PHONE)
        audioManager?.start { _: AudioDevice, _: Set<AudioDevice?>? -> }
    }

    override fun subscribeCallListener(callListener: CallListener) {
        callListeners.add(callListener)
    }

    override fun unsubscribeCallListener(callListener: CallListener) {
        callListeners.remove(callListener)
    }

    override fun swapCamera(callback: DomainCallback<Boolean?, Exception>) {
        currentSession?.mediaStreamManager?.videoCapturer?.let {
            val videoCapturer = currentSession?.mediaStreamManager?.videoCapturer as QBRTCCameraVideoCapturer
            videoCapturer.switchCamera(object : CameraVideoCapturer.CameraSwitchHandler {
                override fun onCameraSwitchDone(isSwitch: Boolean) {
                    callback.onSuccess(isSwitch, null)
                }

                override fun onCameraSwitchError(exception: String?) {
                    callback.onError(Exception(exception))
                }
            })
        }
    }

    private fun getOnlineParticipants() {
        currentSession?.getOnlineParticipants(object : ConferenceEntityCallback<Map<Int, Boolean>> {
            override fun onSuccess(integerBooleanMap: Map<Int, Boolean>) {
                if (callListeners.isNotEmpty()) {
                    for (callListener in callListeners) {
                        callListener.setOnlineParticipants(integerBooleanMap.size - 1)
                    }
                }
            }

            override fun onError(e: WsException) {
                // empty
            }
        })
    }

    override fun setAudioEnabled(enable: Boolean) {
        currentSession?.mediaStreamManager?.localAudioTrack?.setEnabled(enable)
    }

    override fun setVideoEnabled(enable: Boolean) {
        currentSession?.mediaStreamManager?.localVideoTrack?.setEnabled(enable)
    }

    override fun leaveSession() {
        releaseSession(null)
    }

    override fun startSharing(permissionIntent: Intent) {
        QBRTCMediaConfig.setVideoWidth(QBRTCMediaConfig.VideoQuality.HD_VIDEO.width)
        QBRTCMediaConfig.setVideoHeight(QBRTCMediaConfig.VideoQuality.HD_VIDEO.height)
        currentSession?.mediaStreamManager?.videoCapturer = QBRTCScreenCapturer(permissionIntent, null)
    }

    override fun stopSharing(callback: DomainCallback<Unit?, Exception>) {
        try {
            currentSession?.mediaStreamManager?.videoCapturer = QBRTCCameraVideoCapturer(context, null)
            callback.onSuccess(Unit, null)
        } catch (exception: QBRTCCameraCapturerException) {
            callback.onError(exception)
        }
    }

    override fun isSharing(): Boolean {
        return currentSession?.mediaStreamManager?.videoCapturer is QBRTCScreenCapturer
    }

    override fun isAudioEnabled(): Boolean? {
        return currentSession?.mediaStreamManager?.localAudioTrack?.enabled()
    }

    override fun isVideoEnabled(): Boolean? {
        return currentSession?.mediaStreamManager?.localVideoTrack?.enabled()
    }

    override fun isFrontCamera(): Boolean {
        if (currentSession?.mediaStreamManager?.videoCapturer is QBRTCCameraVideoCapturer) {
            val videoCapturer = currentSession?.mediaStreamManager?.videoCapturer as QBRTCCameraVideoCapturer
            val splitName = videoCapturer.cameraName.split(", ")
            return splitName.contains(FACING_FRONT)
        }
        return true
    }

    private inner class QBRTCSessionStateListener : QBRTCSessionStateCallback<ConferenceSession> {
        override fun onStateChanged(session: ConferenceSession?, state: BaseSession.QBRTCSessionState) {
            for (callListener in callListeners) {
                callListener.onStateChanged(session, state)
            }
        }

        override fun onConnectedToUser(session: ConferenceSession?, userId: Int?) {
            // empty
        }

        override fun onDisconnectedFromUser(session: ConferenceSession?, userId: Int) {
            // empty
        }

        override fun onConnectionClosedForUser(session: ConferenceSession?, userId: Int) {
            val callEntity = callEntities.find { it.getUserId() == userId }
            callEntity?.releaseView()
            callEntities.removeIf {
                it == callEntity
            }
            for (callListener in callListeners) {
                callListener.leftPublisher(userId)
            }
        }
    }

    private inner class ConferenceSessionListener : ConferenceSessionCallbacks {
        override fun onPublishersReceived(publishersList: ArrayList<Int>) {
            subscribePublishers(publishersList)
        }

        override fun onPublisherLeft(userId: Int?) {
            subscribedPublishers.remove(userId)
        }

        override fun onMediaReceived(p0: String?, p1: Boolean) {
            //empty
        }

        override fun onSlowLinkReceived(p0: Boolean, p1: Int) {
            //empty
        }

        override fun onError(exception: WsException?) {
            if (exception?.message == ICE_FAILED_REASON) {
                releaseSession(exception)
            }
        }

        override fun onSessionClosed(session: ConferenceSession?) {
            for (callListener in callListeners) {
                callListener.onClosedSession()
            }
            if (session == currentSession) {
                audioManager?.stop()
                releaseSession(null)
            }
        }
    }

    private fun subscribePublishers(publishersList: ArrayList<Int>) {
        publishersList.let {
            for (publisher in publishersList) {
                currentSession?.subscribeToPublisher(publisher)
            }
            subscribedPublishers.addAll(publishersList)
        }
    }

    private inner class VideoTrackListener : QBRTCClientVideoTracksCallbacks<ConferenceSession> {
        override fun onLocalVideoTrackReceive(qbrtcSession: ConferenceSession?, videoTrack: QBRTCVideoTrack?) {
            val callEntity = callEntities.find { it.getUserId() == qbrtcSession?.currentUserID }

            if (callEntity == null) {
                val entity = CallEntityImpl(currentSession?.currentUserID, null, videoTrack,
                        null, null, true)
                callEntities.add(entity)
                setVideoEnabled(false)
                for (callListener in callListeners) {
                    currentSession?.currentUserID?.let { userId -> callListener.receivedLocalVideoTrack(userId) }
                }
                return
            }

            if (!isSharing()) {
                callEntity.releaseView()
                callEntity.setVideoTrack(videoTrack)

                for (callListener in callListeners) {
                    currentSession?.currentUserID?.let { userId -> callListener.receivedLocalVideoTrack(userId) }
                }
            }
        }

        override fun onRemoteVideoTrackReceive(session: ConferenceSession?, videoTrack: QBRTCVideoTrack, userId: Int) {
            val callEntity = callEntities.find { it.getUserId() == userId }
            if (callEntity == null) {
                callEntities.add(CallEntityImpl(userId, null, videoTrack, null, null, false))
            } else {
                callEntity.setVideoTrack(videoTrack)
            }
        }
    }

    private inner class AudioTrackListener : QBRTCClientAudioTracksCallback<ConferenceSession> {
        override fun onLocalAudioTrackReceive(conferenceSession: ConferenceSession, qbrtcAudioTrack: QBRTCAudioTrack) {
            currentSession?.mediaStreamManager?.localAudioTrack?.setEnabled(true)
        }

        override fun onRemoteAudioTrackReceive(conferenceSession: ConferenceSession?, qbrtcAudioTrack: QBRTCAudioTrack, userId: Int) {
            val callEntity = callEntities.find { it.getUserId() == userId }
            callEntity?.setAudioTrack(qbrtcAudioTrack)

            for (callListener in callListeners) {
                callListener.receivedRemoteVideoTrack(userId)
            }
        }
    }

    private inner class OnlineParticipantsCheckerCountdown constructor(millisInFuture: Long, countDownInterval: Long) : CountDownTimer(millisInFuture, countDownInterval) {
        override fun onTick(millisUntilFinished: Long) {
            getOnlineParticipants()
        }

        override fun onFinish() {
            start()
        }
    }

    private fun releaseSession(exception: Exception?) {
        callListeners.forEach { callListener ->
            callListener.releaseSession(exception)
        }

        callEntities.forEach {
            it.releaseView()
        }
        currentSession?.removeSessionCallbacksListener(qbRtcSessionStateListener)
        currentSession?.removeConferenceSessionListener(conferenceSessionListener)
        currentSession?.removeVideoTrackCallbacksListener(videoTrackListener)
        currentSession?.removeAudioTrackCallbacksListener(audioTrackListener)
        currentSession?.leave()

        currentSession = null
        videoTrackListener = null
        audioTrackListener = null
        qbRtcSessionStateListener = null
        conferenceSessionListener = null
        role = null
        timer.cancel()
        currentDialog = null
        callType = null
        isEnableVideoState = false

        callEntities.clear()
    }
}