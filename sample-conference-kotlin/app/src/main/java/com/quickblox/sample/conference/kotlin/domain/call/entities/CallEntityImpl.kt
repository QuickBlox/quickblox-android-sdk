package com.quickblox.sample.conference.kotlin.domain.call.entities

import android.util.Log
import com.quickblox.conference.view.QBConferenceSurfaceView
import com.quickblox.videochat.webrtc.QBRTCAudioTrack
import com.quickblox.videochat.webrtc.view.QBRTCVideoTrack
import org.webrtc.SurfaceViewRenderer

/*
 * Created by Injoit in 2021-09-30.
 * Copyright © 2021 Quickblox. All rights reserved.
 */
class CallEntityImpl(private var participantId: Int?, private var participantName: String?, private var qbVideoTrack: QBRTCVideoTrack?,
                     private var viewRenderer: SurfaceViewRenderer?, private var qbAudioTrack: QBRTCAudioTrack?, private var isLocal: Boolean) : CallEntity {
    override fun releaseView() {
        try {
            this.qbVideoTrack?.removeRenderer(this.qbVideoTrack?.renderer)
            this.viewRenderer?.release()
            this.viewRenderer = null
        } catch (e: Exception) {
            Log.e("Exception", "${e.message}")
        }
    }

    override fun getUserId(): Int? {
        return participantId
    }

    override fun getUserName(): String? {
        return participantName
    }

    override fun setVideoTrack(videoTrack: QBRTCVideoTrack?) {
        this.qbVideoTrack = videoTrack
    }

    override fun setAudioTrack(audioTrack: QBRTCAudioTrack?) {
        this.qbAudioTrack = audioTrack
    }

    override fun setUserId(userId: Int?) {
        this.participantId = userId
    }

    override fun setUserName(userName: String?) {
        this.participantName = userName
    }

    override fun addViewRender(opponentView: QBConferenceSurfaceView) {
        qbVideoTrack?.removeRenderer(this.qbVideoTrack?.renderer)
        this.viewRenderer = opponentView
        qbVideoTrack?.addRenderer(opponentView)
    }

    override fun isEnableAudioTrack(): Boolean? {
        return qbAudioTrack?.enabled()
    }

    override fun setEnabledAudioTrack(enable: Boolean) {
        qbAudioTrack?.setEnabled(enable)
    }

    override fun isLocalEntity(): Boolean {
        return isLocal
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        return if (other is CallEntityImpl) {
            participantId == other.participantId
        } else {
            false
        }
    }

    override fun hashCode(): Int {
        var hash = 1
        hash = 31 * hash + participantId.hashCode()
        return hash
    }
}