package com.quickblox.sample.conference.kotlin.domain.call

import android.content.Intent
import com.quickblox.chat.model.QBChatDialog
import com.quickblox.conference.ConferenceSession
import com.quickblox.conference.QBConferenceRole
import com.quickblox.sample.conference.kotlin.domain.DomainCallback
import com.quickblox.sample.conference.kotlin.domain.call.entities.CallEntity
import com.quickblox.sample.conference.kotlin.domain.call.entities.SessionState

/*
 * Created by Injoit in 2021-09-30.
 * Copyright © 2021 Quickblox. All rights reserved.
 */
interface CallManager {
    fun getSession(): ConferenceSession?
    fun getCurrentDialog(): QBChatDialog?
    fun getCallEntities(): Set<CallEntity>
    fun createAndJoinSession(currentUserId: Int, dialog: QBChatDialog?, roomId: String?, role: QBConferenceRole?,
                             callType: Int?, callback: DomainCallback<Unit, Exception>)
    fun subscribeCallListener(callListener: CallListener)
    fun unsubscribeCallListener(callListener: CallListener)
    fun getRole(): QBConferenceRole?
    fun getCallType(): Int?
    fun swapCamera()
    fun startSharing(permissionIntent: Intent)
    fun stopSharing(callback: DomainCallback<Unit?, Exception>)
    fun isSharing(): Boolean
    fun isAudioEnabled(): Boolean
    fun isVideoEnabled(): Boolean
    fun isFrontCamera(): Boolean
    fun setVideoEnabled(enable: Boolean)
    fun setAudioEnabled(enable: Boolean)
    fun leaveSession()
    fun saveVideoState()
    fun subscribeReconnectionListener(reconnectionListener: ReconnectionListener?)
    fun unsubscribeReconnectionListener(reconnectionListener: ReconnectionListener?)
    fun setDefaultReconnectionState()
    fun getSessionState(): SessionState
    fun setBackgroundState(backgroundState: Boolean)
    fun isBackgroundState(): Boolean
    fun leaveFromSession(callback: DomainCallback<Unit, Exception>?)
}