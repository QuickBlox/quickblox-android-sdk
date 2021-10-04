package com.quickblox.sample.conference.kotlin.presentation.screens.muteparticipants

import android.os.Bundle
import com.quickblox.chat.model.QBChatDialog
import com.quickblox.conference.ConferenceSession
import com.quickblox.sample.conference.kotlin.domain.DomainCallback
import com.quickblox.sample.conference.kotlin.domain.call.CallListener
import com.quickblox.sample.conference.kotlin.domain.call.CallManager
import com.quickblox.sample.conference.kotlin.domain.call.entities.CallEntity
import com.quickblox.sample.conference.kotlin.domain.chat.ChatManager
import com.quickblox.sample.conference.kotlin.domain.repositories.db.DBRepository
import com.quickblox.sample.conference.kotlin.domain.user.UserManager
import com.quickblox.sample.conference.kotlin.presentation.LiveData
import com.quickblox.sample.conference.kotlin.presentation.screens.base.BaseViewModel
import com.quickblox.users.model.QBUser
import com.quickblox.videochat.webrtc.BaseSession
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/*
 * Created by Injoit in 2021-09-30.
 * Copyright © 2021 Quickblox. All rights reserved.
 */
@HiltViewModel
class MuteParticipantsViewModel @Inject constructor(private val dbRepository: DBRepository, private val userManager: UserManager,
                                                    private val callManager: CallManager, private val chatManager: ChatManager) : BaseViewModel() {
    private val TAG: String = MuteParticipantsViewModel::class.java.simpleName
    val liveData = LiveData<Pair<Int, Any?>>()
    private val callListener = CallListenerImpl(TAG)
    var callEntities: HashSet<CallEntity>
        private set
    var currentDialog: QBChatDialog? = null
        private set

    var currentUser: QBUser? = null
        private set

    init {
        callEntities = callManager.getCallEntities()
        currentDialog = callManager.getCurrentDialog()
        currentUser = userManager.getCurrentUser()
    }

    override fun onStartView() {
        if (!chatManager.isLoggedInChat()) {
            loginToChat()
        }
    }

    override fun onStopApp() {
        chatManager.destroyChat()
    }

    private fun loginToChat() {
        currentUser?.let {
            chatManager.loginToChat(it, object : DomainCallback<Unit, Exception> {
                override fun onSuccess(result: Unit, bundle: Bundle?) {
                    // empty
                }

                override fun onError(error: Exception) {
                    // empty
                }
            })
        } ?: run {
            liveData.setValue(Pair(ViewState.SHOW_LOGIN_SCREEN, null))
        }
    }

    fun getCurrentUserId(): Int? {
        return dbRepository.getCurrentUser()?.id
    }

    fun getCountParticipants(): Int {
        return callEntities.size
    }

    override fun onResumeView() {
        callManager.subscribeCallListener(callListener)
    }

    override fun onPauseView() {
        callManager.unsubscribeCallListener(callListener)
    }

    private inner class CallListenerImpl(val tag: String) : CallListener {
        override fun receivedLocalVideoTrack(userId: Int) {
            // empty
        }

        override fun leftPublisher(userId: Int) {
            liveData.setValue(Pair(ViewState.UPDATE_LIST, null))
        }

        override fun receivedRemoteVideoTrack(userId: Int) {
            userManager.loadUserById(userId, object : DomainCallback<QBUser, Exception> {
                override fun onSuccess(result: QBUser, bundle: Bundle?) {
                    val callEntity = callEntities.find { it.getUserId() == result.id }
                    callEntity?.setUserName(result.fullName)
                    liveData.setValue(Pair(ViewState.UPDATE_LIST, null))
                }

                override fun onError(error: Exception) {
                    liveData.setValue(Pair(com.quickblox.sample.conference.kotlin.presentation.screens.call.ViewState.ERROR, error.message))
                }
            })
        }

        override fun onStateChanged(session: ConferenceSession?, state: BaseSession.QBRTCSessionState) {
            // empty
        }

        override fun onError(exception: Exception) {
            liveData.setValue(Pair(ViewState.ERROR, exception.message))
        }

        override fun releaseSession(exception: Exception?) {
            // empty
        }

        override fun setOnlineParticipants(count: Int) {
            // empty
        }

        override fun onClosedSession() {
            // empty
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
}