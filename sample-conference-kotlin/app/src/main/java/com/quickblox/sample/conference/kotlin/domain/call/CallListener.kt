package com.quickblox.sample.conference.kotlin.domain.call

import com.quickblox.conference.ConferenceSession
import com.quickblox.videochat.webrtc.BaseSession

/*
 * Created by Injoit in 2021-09-30.
 * Copyright © 2021 Quickblox. All rights reserved.
 */
interface CallListener {
    fun receivedLocalVideoTrack(userId: Int)
    fun leftPublisher(userId: Int)
    fun receivedRemoteVideoTrack(userId: Int)
    fun onStateChanged(session: ConferenceSession?, state: BaseSession.QBRTCSessionState)
    fun onError(exception: Exception)
    fun releaseSession(exception: Exception?)
    fun setOnlineParticipants(count: Int)
}