package com.quickblox.sample.conference.kotlin.domain.call.entities

import com.quickblox.sample.conference.kotlin.domain.call.entities.SessionState.*

/*
 * Created by Injoit in 2021-10-20.
 * Copyright © 2021 Quickblox. All rights reserved.
 */
data class SessionStateImpl(var audioEnabled: Boolean = true, var videoEnabled: Boolean = false,
                            var showedSharing: Boolean = false, var frontCamera: Boolean = true,
                            var reconnection: ReconnectionState = ReconnectionState.DEFAULT) : SessionState {
    override fun getAudioState(): Boolean {
        return audioEnabled
    }

    override fun getVideoState(): Boolean {
        return videoEnabled
    }

    override fun getSharingState(): Boolean {
        return showedSharing
    }

    override fun getCameraState(): Boolean {
        return frontCamera
    }

    override fun getReconnectionState(): ReconnectionState {
        return reconnection
    }

    override fun setAudioState(audioEnabled: Boolean) {
        this.audioEnabled = audioEnabled
    }

    override fun setVideoState(videoEnabled: Boolean) {
        this.videoEnabled = videoEnabled
    }

    override fun setSharingState(showedSharing: Boolean) {
        this.showedSharing = showedSharing
    }

    override fun setCameraState(frontCamera: Boolean) {
        this.frontCamera = frontCamera
    }

    override fun setReconnectionState(reconnection: ReconnectionState) {
        this.reconnection = reconnection
    }

    override fun setDefault() {
        audioEnabled = true
        videoEnabled = false
        showedSharing = false
        frontCamera = true
        reconnection = ReconnectionState.DEFAULT
    }
}