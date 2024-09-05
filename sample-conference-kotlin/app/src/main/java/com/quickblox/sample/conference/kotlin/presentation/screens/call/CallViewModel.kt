package com.quickblox.sample.conference.kotlin.presentation.screens.call

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import com.quickblox.chat.model.QBChatDialog
import com.quickblox.conference.ConferenceSession
import com.quickblox.conference.QBConferenceRole
import com.quickblox.sample.conference.kotlin.R
import com.quickblox.sample.conference.kotlin.data.service.CallService
import com.quickblox.sample.conference.kotlin.domain.DomainCallback
import com.quickblox.sample.conference.kotlin.domain.call.CallListener
import com.quickblox.sample.conference.kotlin.domain.call.CallManager
import com.quickblox.sample.conference.kotlin.domain.call.CallManagerImpl.Companion.CallType.Companion.STREAM
import com.quickblox.sample.conference.kotlin.domain.call.ReconnectionListener
import com.quickblox.sample.conference.kotlin.domain.call.entities.CallEntity
import com.quickblox.sample.conference.kotlin.domain.call.entities.SessionState.ReconnectionState
import com.quickblox.sample.conference.kotlin.domain.chat.ChatManager
import com.quickblox.sample.conference.kotlin.domain.repositories.connection.ConnectionRepository
import com.quickblox.sample.conference.kotlin.domain.repositories.connection.ConnectivityChangedListener
import com.quickblox.sample.conference.kotlin.domain.settings.SettingsManager
import com.quickblox.sample.conference.kotlin.domain.user.UserManager
import com.quickblox.sample.conference.kotlin.presentation.LiveData
import com.quickblox.sample.conference.kotlin.presentation.resources.ResourcesManager
import com.quickblox.sample.conference.kotlin.presentation.screens.base.BaseViewModel
import com.quickblox.sample.conference.kotlin.presentation.screens.call.ViewState.Companion.ERROR
import com.quickblox.sample.conference.kotlin.presentation.screens.call.ViewState.Companion.RECONNECTED
import com.quickblox.sample.conference.kotlin.presentation.screens.call.ViewState.Companion.RECONNECTING
import com.quickblox.sample.conference.kotlin.presentation.screens.call.ViewState.Companion.REQUEST_PERMISSION
import com.quickblox.users.model.QBUser
import com.quickblox.videochat.webrtc.BaseSession.QBRTCSessionState
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.SortedSet
import javax.inject.Inject

/*
 * Created by Injoit in 2021-09-30.
 * Copyright © 2021 Quickblox. All rights reserved.
 */
@HiltViewModel
class CallViewModel @Inject constructor(
    private val chatManager: ChatManager, private val userManager: UserManager, val settingsManager: SettingsManager,
    private val callManager: CallManager, private val resourcesManager: ResourcesManager,
    private val connectionRepository: ConnectionRepository,
) : BaseViewModel() {
    private val TAG: String = CallViewModel::class.java.simpleName
    private val callListener = CallListenerImpl(TAG)
    private var currentUser: QBUser? = null
    private val connectivityChangedListener = ConnectivityChangedListenerImpl(TAG)
    private var reconnectionListener: ReconnectionListenerImpl? = null
    val liveData = LiveData<Pair<Int, Any?>>()
    var callEntities: SortedSet<CallEntity>
        private set
    var participants = arrayListOf<QBUser>()
        private set
    var isFullScreenState = false
        private set
    var callServiceConnection: ServiceConnection? = null
        private set
    var currentDialog: QBChatDialog? = null
        private set

    init {
        callEntities = callManager.getCallEntities() as SortedSet<CallEntity>
        currentUser = userManager.getCurrentUser()
        settingsManager.applyCallSettings()
    }

    private fun subscribeListeners() {
        callManager.subscribeCallListener(callListener)
    }

    private fun fillCallEntity(user: QBUser) {
        participants.add(user)
        val callEntity = callEntities.find { it.getUserId() == user.id }
        callEntity?.setUserName(user.fullName)

        if (getCallType() == STREAM && getRole() == QBConferenceRole.LISTENER) {
            liveData.setValue(Pair(ViewState.ONLINE_PARTICIPANTS, true))
        }
        releaseViews()
        if (callEntity?.isLocalEntity() == false) {
            liveData.setValue(Pair(ViewState.SHOW_VIDEO_TRACK, null))
        }
    }

    fun swapCamera() {
        callManager.swapCamera()
    }

    private fun loadDialog() {
        for (dialog in chatManager.getDialogs()) {
            if (dialog.dialogId == callManager.getCurrentDialog()?.dialogId) {
                currentDialog = dialog
                break
            }
        }
    }

    override fun onResumeView() {
        loadDialog()
        reconnectionListener = ReconnectionListenerImpl(TAG)
        connectionRepository.addListener(connectivityChangedListener)
        callManager.subscribeReconnectionListener(reconnectionListener)
        if (!chatManager.isLoggedInChat()) {
            loginToChat()
        }
        subscribeListeners()
        if (CallService.isRunning()) {
            bindCallService()
        }

        val isNotSharing = !isShowSharing()
        if (isNotSharing && callManager.isBackgroundState()) {
            setVideoEnabled(callManager.getSessionState().getVideoState())
            if (callManager.getCallType() == STREAM) {
                liveData.setValue(Pair(ViewState.STREAM, null))
            } else {
                liveData.setValue(Pair(ViewState.CONVERSATION, null))
            }
        }
        callManager.setBackgroundState(false)
        when (callManager.getSessionState().getReconnectionState()) {
            ReconnectionState.FAILED -> {
                liveData.setValue(Pair(ERROR, resourcesManager.get().getString(R.string.reconnection_failed)))
                leaveSession()
            }

            ReconnectionState.COMPLETED -> {
                liveData.setValue(Pair(RECONNECTED, null))

                if (isShowSharing()) {
                    liveData.setValue(Pair(REQUEST_PERMISSION, null))
                }
                callManager.setDefaultReconnectionState()
                callManager.saveVideoState()
                setVideoEnabled(callManager.getSessionState().getVideoState())
            }

            ReconnectionState.IN_PROGRESS -> {
                liveData.setValue(Pair(RECONNECTING, null))
            }

            ReconnectionState.DEFAULT -> {
                // empty
            }
        }
    }

    override fun onPauseView() {
        super.onPauseView()
    }

    override fun onStopView() {
        connectionRepository.removeListener(connectivityChangedListener)
        unbindCallService()
        callManager.setBackgroundState(true)
        releaseViews()
        callManager.unsubscribeCallListener(callListener)
        callManager.unsubscribeReconnectionListener(reconnectionListener)
    }

    private fun loginToChat() {
        currentUser?.let {
            chatManager.loginToChat(it, object : DomainCallback<Unit, Exception> {
                override fun onSuccess(result: Unit, bundle: Bundle?) {
                    // empty
                }

                override fun onError(error: Exception) {
                    liveData.setValue(Pair(ERROR, error.message))
                }
            })
        } ?: run {
            callManager.leaveSession()
            liveData.setValue(Pair(ViewState.SHOW_LOGIN_SCREEN, null))
        }
    }

    override fun onStopApp() {
        chatManager.destroyChat()
    }

    private fun releaseViews() {
        callEntities.forEach { callEntity ->
            callEntity.releaseView()
        }
    }

    fun startSharing(permissionIntent: Intent) {
        callManager.startSharing(permissionIntent)
        saveVideoState()
        releaseViews()
        liveData.setValue(Pair(ViewState.START_SHARING, null))
    }

    fun isShowSharing(): Boolean {
        return callManager.isSharing()
    }

    fun isAudioEnabled(): Boolean {
        return callManager.isAudioEnabled()
    }

    fun isFrontCamera(): Boolean {
        return callManager.isFrontCamera()
    }

    fun stopSharing() {
        callManager.stopSharing(object : DomainCallback<Unit?, Exception> {
            override fun onSuccess(result: Unit?, bundle: Bundle?) {
                releaseViews()
            }

            override fun onError(error: Exception) {
                // empty
            }
        })
    }

    fun leaveSession() {
        liveData.setValue(Pair(ViewState.STOP_SERVICE, null))
        callManager.leaveSession()
    }

    fun setAudioEnabled(enable: Boolean) {
        callManager.setAudioEnabled(enable)
    }

    fun getCallType(): Int? {
        return callManager.getCallType()
    }

    fun getRole(): QBConferenceRole? {
        return callManager.getRole()
    }

    fun isReconnectingState(): Boolean {
        return callManager.getSessionState().getReconnectionState() == ReconnectionState.IN_PROGRESS
    }

    fun setVideoEnabled(enable: Boolean) {
        callManager.setVideoEnabled(enable)
    }

    fun isVideoEnabled(): Boolean {
        return callManager.isVideoEnabled()
    }

    fun saveVideoState() {
        callManager.saveVideoState()
    }

    private fun bindCallService() {
        callServiceConnection = CallServiceConnection()
        liveData.setValue(Pair(ViewState.BIND_SERVICE, null))
    }

    private fun unbindCallService() {
        liveData.setValue(Pair(ViewState.UNBIND_SERVICE, null))
    }

    fun openFullScreen(callEntity: CallEntity) {
        releaseViews()
        isFullScreenState = true
        saveVideoState()
        val callEntities = sortedSetOf(CallEntity.ComparatorImpl())
        callEntities.add(callEntity)
        liveData.setValue(Pair(ViewState.OPEN_FULL_SCREEN, callEntities))
    }

    fun closeFullScreen() {
        releaseViews()
        liveData.setValue(Pair(ViewState.CONVERSATION, null))
        isFullScreenState = false
    }

    fun startMuteParticipantsScreen() {
        releaseViews()
        liveData.setValue(Pair(ViewState.SHOW_MUTE_PARTICIPANTS_SCREEN, null))
    }

    fun startChatScreen() {
        releaseViews()
        liveData.setValue(Pair(ViewState.SHOW_CHAT_SCREEN, null))
    }

    private inner class CallListenerImpl(val tag: String) : CallListener {
        override fun receivedLocalVideoTrack(userId: Int) {
            val callEntity = callEntities.find { it.getUserId() == userId }
            if (callEntity?.getUserName() == null) {
                callEntity?.setUserName(currentUser?.fullName)
            }
            if (callManager.getCallType() == STREAM) {
                liveData.setValue(Pair(ViewState.STREAM, null))
            } else {
                liveData.setValue(Pair(ViewState.CONVERSATION, null))
            }
        }

        override fun leftPublisher(userId: Int) {
            if (isFullScreenState) {
                val callEntity = callEntities.find { it.getUserId() == userId }
                callEntity?.let {
                    callEntities.remove(callEntity)
                }

                closeFullScreen()
            }
            if (getCallType() == STREAM) {
                liveData.setValue(Pair(ViewState.ONLINE_PARTICIPANTS, false))
            }
            releaseViews()
            liveData.setValue(Pair(ViewState.SHOW_VIDEO_TRACK, null))
        }

        override fun receivedRemoteVideoTrack(userId: Int) {
            val user = participants.find { it.id == userId }

            if (user == null) {
                userManager.loadUserById(userId, object : DomainCallback<QBUser, Exception> {
                    override fun onSuccess(result: QBUser, bundle: Bundle?) {
                        fillCallEntity(result)
                    }

                    override fun onError(error: Exception) {
                        liveData.setValue(Pair(ERROR, error.message))
                    }
                })
            } else {
                fillCallEntity(user)
            }
        }

        override fun onStateChanged(session: ConferenceSession?, state: QBRTCSessionState) {
            if (callManager.getCallType() == STREAM && getRole() == QBConferenceRole.LISTENER &&
                state == QBRTCSessionState.QB_RTC_SESSION_CONNECTED) {
                liveData.setValue(Pair(ViewState.ONLINE_PARTICIPANTS, true))
            } else {
                liveData.setValue(Pair(ViewState.ONLINE_PARTICIPANTS, false))
            }
        }

        override fun onError(exception: Exception) {
            liveData.setValue(Pair(ERROR, exception.message))
        }

        override fun releaseSession(exception: Exception?) {
            callServiceConnection = null
            callManager.unsubscribeCallListener(callListener)
        }

        override fun setOnlineParticipants(count: Int) {
            liveData.setValue(Pair(ViewState.COUNT_PARTICIPANTS, count))
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) {
                return true
            }
            return if (other is CallListenerImpl) {
                tag == other.tag
            } else {
                false
            }
        }

        override fun hashCode(): Int {
            var hash = 1
            hash = 31 * hash + tag.hashCode()
            return hash
        }
    }

    private inner class CallServiceConnection : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName) {
            // empty
        }

        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            // empty
        }
    }

    private inner class ConnectivityChangedListenerImpl(val tag: String) : ConnectivityChangedListener {
        override fun onAvailable() {
            liveData.setValue(Pair(ERROR, resourcesManager.get().getString(R.string.internet_restored)))
        }

        override fun onLost() {
            liveData.setValue(Pair(ERROR, resourcesManager.get().getString(R.string.no_internet)))
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) {
                return true
            }
            if (other !is ConnectivityChangedListenerImpl) {
                return false
            }
            return tag == other.tag
        }

        override fun hashCode(): Int {
            var hash = 1
            hash = 31 * hash + tag.hashCode()
            return hash
        }
    }

    private inner class ReconnectionListenerImpl(val tag: String) : ReconnectionListener {
        override fun onChangedState(reconnectionState: ReconnectionState) {
            when (reconnectionState) {
                ReconnectionState.FAILED -> {
                    liveData.setValue(Pair(ERROR, resourcesManager.get().getString(R.string.reconnection_failed)))
                    leaveSession()
                }

                ReconnectionState.COMPLETED -> {
                    setVideoEnabled(callManager.getSessionState().getVideoState())
                    liveData.setValue(Pair(RECONNECTED, null))
                    callManager.setDefaultReconnectionState()
                }
                ReconnectionState.IN_PROGRESS -> {
                    liveData.setValue(Pair(RECONNECTING, null))
                }
                ReconnectionState.DEFAULT -> {
                    // empty
                }
            }
        }

        override fun requestPermission() {
            liveData.setValue(Pair(REQUEST_PERMISSION, null))
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) {
                return true
            }
            if (other !is ReconnectionListenerImpl) {
                return false
            }
            return tag == other.tag
        }

        override fun hashCode(): Int {
            var hash = 1
            hash = 31 * hash + tag.hashCode()
            return hash
        }
    }
}