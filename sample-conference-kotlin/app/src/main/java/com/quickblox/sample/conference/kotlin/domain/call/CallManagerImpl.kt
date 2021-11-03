package com.quickblox.sample.conference.kotlin.domain.call

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
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
import com.quickblox.sample.conference.kotlin.domain.call.entities.*
import com.quickblox.sample.conference.kotlin.domain.call.entities.SessionState.*
import com.quickblox.sample.conference.kotlin.domain.call.entities.SessionStateImpl.*
import com.quickblox.sample.conference.kotlin.domain.repositories.call.CallRepository
import com.quickblox.sample.conference.kotlin.presentation.resources.ResourcesManager
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
private const val SECONDS_13 = 13000L

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
    private var reconnectionListeners = hashSetOf<ReconnectionListener>()
    private val subscribedPublishers = hashSetOf<Int?>()
    private val callEntities = sortedSetOf(CallEntity.ComparatorImpl())
    private var audioManager: AppRTCAudioManager? = null
    private var callType: Int? = null
    private var currentDialog: QBChatDialog? = null
    private val timer = OnlineParticipantsCheckerCountdown(MILLIS_FUTURE, ONLINE_INTERVAL)
    private val sessionState = SessionStateImpl()
    private var backgroundState = false

    companion object {
        @IntDef(CONVERSATION, STREAM)
        annotation class CallType {
            companion object {
                const val CONVERSATION = 0
                const val STREAM = 1
            }
        }
    }

    override fun getSessionState(): SessionState {
        return sessionState
    }

    override fun setBackgroundState(backgroundState: Boolean) {
        this.backgroundState = backgroundState
        if (!isSharing() && backgroundState) {
            saveVideoState()
            setVideoEnabled(false)
        }
    }

    override fun saveVideoState() {
        sessionState.videoEnabled = isVideoEnabled()
    }

    override fun subscribeReconnectionListener(reconnectionListener: ReconnectionListener?) {
        reconnectionListener?.let { reconnectionListeners.add(it) }
    }

    override fun unsubscribeReconnectionListener(reconnectionListener: ReconnectionListener?) {
        reconnectionListeners.remove(reconnectionListener)
    }

    override fun getRole(): QBConferenceRole? {
        return currentSession?.conferenceRole
    }

    override fun getCallType(): Int? {
        return callType
    }

    override fun setDefaultReconnectionState() {
        sessionState.reconnection = ReconnectionState.DEFAULT
    }

    override fun getSession(): ConferenceSession? {
        return currentSession
    }

    override fun getCurrentDialog(): QBChatDialog? {
        return currentDialog
    }

    override fun getCallEntities(): Set<CallEntity> {
        return callEntities
    }

    override fun createSession(currentUserId: Int, dialog: QBChatDialog?, roomId: String?, role: QBConferenceRole?,
                               callType: Int?, callback: DomainCallback<ConferenceSession, Exception>) {
        currentSession?.leave() ?: run {
            sessionState.setDefault()
        }

        callRepository.createSession(currentUserId, object : DataCallBack<ConferenceSession, Exception> {
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
                initAudioManager()

                currentSession?.addSessionCallbacksListener(qbRtcSessionStateListener)
                currentSession?.addConferenceSessionListener(conferenceSessionListener)
                currentSession?.addVideoTrackCallbacksListener(videoTrackListener)
                currentSession?.addAudioTrackCallbacksListener(audioTrackListener)

                joinConference(roomId, role)
                callback.onSuccess(result, null)

                this@CallManagerImpl.callType = callType
                if (this@CallManagerImpl.callType == STREAM && role == QBConferenceRole.PUBLISHER) {
                    timer.start()
                }
            }

            override fun onError(error: Exception) {
                callback.onError(error)
            }
        })
    }

    private fun joinConference(dialogId: String?, role: QBConferenceRole?) {
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

    override fun swapCamera() {
        currentSession?.mediaStreamManager?.videoCapturer?.let {
            val videoCapturer = currentSession?.mediaStreamManager?.videoCapturer as QBRTCCameraVideoCapturer
            videoCapturer.switchCamera(object : CameraVideoCapturer.CameraSwitchHandler {
                override fun onCameraSwitchDone(isSwitch: Boolean) {
                    sessionState.frontCamera = isSwitch
                }

                override fun onCameraSwitchError(exception: String?) {
                    callListeners.forEach { callListener ->
                        callListener.onError(Exception(exception))
                    }
                }
            })
        }
    }

    private fun getOnlineParticipants() {
        currentSession?.getOnlineParticipants(object : ConferenceEntityCallback<Map<Int, Boolean>> {
            override fun onSuccess(integerBooleanMap: Map<Int, Boolean>) {
                if (callListeners.isNotEmpty()) {
                    for (callListener in callListeners) {
                        Handler(Looper.getMainLooper()).post {
                            callListener.setOnlineParticipants(integerBooleanMap.size - 1)
                        }
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
        setVideoEnabled(true)
        sessionState.showedSharing = true
    }

    override fun stopSharing(callback: DomainCallback<Unit?, Exception>) {
        try {
            currentSession?.mediaStreamManager?.videoCapturer = QBRTCCameraVideoCapturer(context, null)
            if (!sessionState.frontCamera) {
                swapCamera()
            }
            sessionState.showedSharing = false
            callback.onSuccess(Unit, null)
        } catch (exception: QBRTCCameraCapturerException) {
            callback.onError(exception)
        }
    }

    override fun isSharing(): Boolean {
        return sessionState.showedSharing
    }

    override fun isAudioEnabled(): Boolean {
        currentSession?.mediaStreamManager?.localAudioTrack?.enabled()?.let {
            return it
        }
        return false
    }

    override fun isVideoEnabled(): Boolean {
        currentSession?.mediaStreamManager?.localVideoTrack?.enabled()?.let {
            return it
        }
        return false
    }

    override fun isFrontCamera(): Boolean {
        return sessionState.frontCamera
    }

    private inner class QBRTCSessionStateListener : QBRTCSessionStateCallback<ConferenceSession> {
        override fun onStateChanged(session: ConferenceSession?, state: BaseSession.QBRTCSessionState) {
            for (callListener in callListeners) {
                callListener.onStateChanged(session, state)
            }
        }

        override fun onConnectedToUser(session: ConferenceSession?, userId: Int?) {
            if (userId == currentSession?.currentUserID &&
                sessionState.reconnection == ReconnectionState.IN_PROGRESS) {
                sessionState.reconnection = ReconnectionState.COMPLETED
                for (reconnectionListener in reconnectionListeners) {
                    Handler(Looper.getMainLooper()).post {
                        reconnectionListener.onChangedState(sessionState.reconnection)
                    }
                }
            }
        }

        override fun onDisconnectedFromUser(session: ConferenceSession?, userId: Int) {
            if (userId == currentSession?.currentUserID &&
                currentSession?.conferenceRole == QBConferenceRole.PUBLISHER) {
                sessionState.reconnection = ReconnectionState.IN_PROGRESS
                sessionState.audioEnabled = isAudioEnabled()
                if (!backgroundState) {
                    sessionState.videoEnabled = isVideoEnabled()
                }
                sessionState.frontCamera = isFrontCamera()

                for (reconnectionListener in reconnectionListeners) {
                    Handler(Looper.getMainLooper()).post {
                        reconnectionListener.onChangedState(sessionState.reconnection)
                    }
                }
            } else if (currentSession?.conferenceRole == QBConferenceRole.LISTENER) {
                sessionState.reconnection = ReconnectionState.IN_PROGRESS
                for (reconnectionListener in reconnectionListeners) {
                    Handler(Looper.getMainLooper()).post {
                        reconnectionListener.onChangedState(sessionState.reconnection)
                    }
                }
                currentSession?.leave()
            }
        }

        override fun onConnectionClosedForUser(session: ConferenceSession?, userId: Int) {
            val callEntity = callEntities.find { it.getUserId() == userId }
            callEntity?.let {
                callEntity.releaseView()
                callEntities.remove(callEntity)
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
            if (session == currentSession && sessionState.reconnection == ReconnectionState.IN_PROGRESS) {
                timer.cancel()
                ReconnectionTimer().start()
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
                        null, null, true
                )

                callEntities.add(entity)
                if (!sessionState.frontCamera) {
                    swapCamera()
                }
                if (backgroundState) {
                    setVideoEnabled(false)
                } else {
                    setVideoEnabled(sessionState.videoEnabled)
                }
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
                callEntity.releaseView()
                callEntity.setVideoTrack(videoTrack)
            }
        }
    }

    private inner class AudioTrackListener : QBRTCClientAudioTracksCallback<ConferenceSession> {
        override fun onLocalAudioTrackReceive(conferenceSession: ConferenceSession, qbrtcAudioTrack: QBRTCAudioTrack) {
            currentSession?.mediaStreamManager?.localAudioTrack?.setEnabled(sessionState.audioEnabled)
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
        audioManager?.stop()
        audioManager = null
        currentSession = null
        videoTrackListener = null
        audioTrackListener = null
        qbRtcSessionStateListener = null
        conferenceSessionListener = null
        sessionState.reconnection = ReconnectionState.DEFAULT
        timer.cancel()
        currentDialog = null
        callType = null
        sessionState.videoEnabled = false
        callEntities.clear()
    }

    private inner class ReconnectionTimer {
        private var timer: Timer? = Timer()
        private var lastDelay: Long = 0
        private var delay: Long = 1000
        private var newDelay: Long = 0

        fun start() {
            if (newDelay >= SECONDS_13) {
                sessionState.reconnection = ReconnectionState.FAILED
                for (reconnectionListener in reconnectionListeners) {
                    Handler(Looper.getMainLooper()).post {
                        reconnectionListener.onChangedState(sessionState.reconnection)
                    }
                }
                return
            }
            newDelay = lastDelay + delay
            lastDelay = delay
            delay = newDelay
            timer?.cancel()
            timer = Timer()
            timer?.schedule(object : TimerTask() {
                override fun run() {
                    currentSession?.currentUserID?.let {
                        createSession(it, currentDialog, currentSession?.dialogID, currentSession?.conferenceRole,
                                callType, object : DomainCallback<ConferenceSession, Exception> {
                            override fun onSuccess(result: ConferenceSession, bundle: Bundle?) {
                                timer?.purge()
                                timer?.cancel()
                                timer = null

                                if (sessionState.showedSharing) {
                                    for (reconnectionListener in reconnectionListeners) {
                                        reconnectionListener.requestPermission()
                                    }
                                }
                                sessionState.reconnection = ReconnectionState.COMPLETED
                                for (reconnectionListener in reconnectionListeners) {
                                    Handler(Looper.getMainLooper()).post {
                                        reconnectionListener.onChangedState(sessionState.reconnection)
                                    }
                                }
                            }

                            override fun onError(error: Exception) {
                                start()
                            }
                        })
                    } ?: run {
                        for (reconnectionListener in reconnectionListeners) {
                            Handler(Looper.getMainLooper()).post {
                                reconnectionListener.onChangedState(sessionState.reconnection)
                            }
                        }
                    }
                }
            }, newDelay)
        }
    }
}