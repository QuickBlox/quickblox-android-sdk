package com.quickblox.sample.conference.kotlin.domain.call.entities

interface SessionState {
    fun setDefault()
    fun getReconnectionState(): ReconnectionState
    fun getCameraState(): Boolean
    fun getSharingState(): Boolean
    fun getVideoState(): Boolean
    fun getAudioState(): Boolean
    fun setAudioState(audioEnabled: Boolean)
    fun setVideoState(videoEnabled: Boolean)
    fun setSharingState(showedSharing: Boolean)
    fun setCameraState(frontCamera: Boolean)
    fun setReconnectionState(reconnection: ReconnectionState)

    enum class ReconnectionState { DEFAULT, IN_PROGRESS, COMPLETED, FAILED }
}