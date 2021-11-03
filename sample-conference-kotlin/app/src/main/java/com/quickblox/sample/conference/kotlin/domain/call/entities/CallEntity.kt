package com.quickblox.sample.conference.kotlin.domain.call.entities

import com.quickblox.conference.view.QBConferenceSurfaceView
import com.quickblox.videochat.webrtc.QBRTCAudioTrack
import com.quickblox.videochat.webrtc.view.QBRTCVideoTrack

/*
 * Created by Injoit in 2021-09-30.
 * Copyright © 2021 Quickblox. All rights reserved.
 */
interface CallEntity {
    fun releaseView()
    fun getUserId(): Int?
    fun getUserName(): String?
    fun setVideoTrack(videoTrack: QBRTCVideoTrack?)
    fun setAudioTrack(audioTrack: QBRTCAudioTrack?)
    fun setUserId(userId: Int?)
    fun setUserName(userName: String?)
    fun addViewRender(opponentView: QBConferenceSurfaceView)
    fun isEnableAudioTrack(): Boolean?
    fun setEnabledAudioTrack(enable: Boolean)
    fun isLocalEntity(): Boolean

    class ComparatorImpl : Comparator<CallEntity> {
        override fun compare(first: CallEntity, second: CallEntity): Int {
            if (second.isLocalEntity() != first.isLocalEntity()) {
                return second.isLocalEntity().compareTo(first.isLocalEntity())
            } else if (second != first) {
                return 1
            }
            return 0
        }
    }
}