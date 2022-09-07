package com.quickblox.sample.videochat.kotlin.utils

import com.quickblox.videochat.webrtc.QBRTCConfig
import com.quickblox.videochat.webrtc.QBRTCMediaConfig

fun applyMediaSettings() {
    applyAudioSettings()
    applyVideoSettings()
}

private fun applyAudioSettings() {
    QBRTCMediaConfig.setAudioCodec(QBRTCMediaConfig.AudioCodec.ISAC)
    QBRTCMediaConfig.setUseBuildInAEC(true)
    QBRTCMediaConfig.setAudioProcessingEnabled(true)
    QBRTCMediaConfig.setUseOpenSLES(false)
}

private fun applyVideoSettings() {
    QBRTCMediaConfig.setVideoHWAcceleration(true)
    QBRTCMediaConfig.setVideoWidth(QBRTCMediaConfig.VideoQuality.VGA_VIDEO.width)
    QBRTCMediaConfig.setVideoHeight(QBRTCMediaConfig.VideoQuality.VGA_VIDEO.height)
    QBRTCMediaConfig.setVideoCodec(QBRTCMediaConfig.VideoCodec.VP8)
}

fun applyRTCSettings() {
    QBRTCConfig.setMaxOpponentsCount(MAX_OPPONENTS_COUNT)
    QBRTCConfig.setDebugEnabled(true)

    val ANSWER_TIME_INTERVAL_IN_SECONDS = 30L
    QBRTCConfig.setAnswerTimeInterval(ANSWER_TIME_INTERVAL_IN_SECONDS)

    val DISCONNECT_TIME_IN_SECONDS = 10
    QBRTCConfig.setDisconnectTime(DISCONNECT_TIME_IN_SECONDS)

    val DIALING_TIME_INTERVAL_IN_SECONDS = 5L
    QBRTCConfig.setDialingTimeInterval(DIALING_TIME_INTERVAL_IN_SECONDS)
}