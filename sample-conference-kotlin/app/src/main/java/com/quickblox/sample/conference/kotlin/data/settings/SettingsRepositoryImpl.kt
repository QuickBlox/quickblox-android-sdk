package com.quickblox.sample.conference.kotlin.data.settings

import android.content.Context
import androidx.core.content.edit
import com.quickblox.auth.session.QBSettings
import com.quickblox.chat.QBChatService
import com.quickblox.core.LogLevel
import com.quickblox.sample.conference.kotlin.domain.repositories.settings.SettingsRepository
import com.quickblox.sample.conference.kotlin.domain.settings.entities.CallSettingsEntity
import com.quickblox.sample.conference.kotlin.domain.settings.entities.CallSettingsEntityImpl
import com.quickblox.videochat.webrtc.QBRTCConfig
import com.quickblox.videochat.webrtc.QBRTCMediaConfig
import com.quickblox.videochat.webrtc.QBRTCMediaConfig.VideoCodec
import com.quickblox.videochat.webrtc.QBRTCMediaConfig.VideoQuality

private const val CHAT_PORT = 5223
private const val SOCKET_TIMEOUT = 300
private const val KEEP_ALIVE = true
private const val USE_TLS = true
private const val AUTO_JOIN = false
private const val AUTO_MARK_DELIVERED = true
private const val RECONNECTION_ALLOWED = true
private const val ALLOW_LISTEN_NETWORK = true

private const val RESOLUTION = "resolution"
private const val BITRATE = "bitrate"
private const val FRAME_RATE = "frameRate"
private const val AEC = "aec"
private const val AUDIO_PROCESSING = "audiop_rocessing"
private const val OPEN_SLES = "open_sles"
private const val QB_PREF_SETTINGS = "qb_pref_settings"

/*
 * Created by Injoit in 2021-09-30.
 * Copyright © 2021 Quickblox. All rights reserved.
 */
class SettingsRepositoryImpl(context: Context) : SettingsRepository {
    private val sharedPreferences = context.getSharedPreferences(QB_PREF_SETTINGS, Context.MODE_PRIVATE)

    override fun saveCallSettings(callSettings: CallSettingsEntity) {
        sharedPreferences.edit {
            putInt(RESOLUTION, callSettings.getVideoResolution())
            putInt(FRAME_RATE, callSettings.getFps())
            putInt(BITRATE, callSettings.getBandwidth())
            putBoolean(AEC, callSettings.getBuildInAEC())
            putBoolean(AUDIO_PROCESSING, callSettings.getProcessing())
            putBoolean(OPEN_SLES, callSettings.getOpenSLES())
        }
    }

    override fun loadCallSettings(): CallSettingsEntity {
        val resolution = sharedPreferences.getInt(RESOLUTION, 0)
        val bitrate = sharedPreferences.getInt(BITRATE, 0)
        val frameRate = sharedPreferences.getInt(FRAME_RATE, 0)

        val aec = sharedPreferences.getBoolean(AEC, false)
        val audioProcessing = sharedPreferences.getBoolean(AUDIO_PROCESSING, false)
        val openSles = sharedPreferences.getBoolean(OPEN_SLES, false)
        return CallSettingsEntityImpl(resolution, bitrate, frameRate, aec, audioProcessing, openSles)
    }

    override fun applyCallSettings() {
        val settingsModel = loadCallSettings()

        QBRTCMediaConfig.setVideoCodec(VideoCodec.VP8)
        QBRTCMediaConfig.setVideoHWAcceleration(false)
        for (quality in VideoQuality.values()) {
            if (quality.ordinal == settingsModel.getVideoResolution()) {
                QBRTCMediaConfig.setVideoHeight(quality.height)
                QBRTCMediaConfig.setVideoWidth(quality.width)
                break
            }
        }
        QBRTCMediaConfig.setVideoStartBitrate(settingsModel.getBandwidth())
        QBRTCMediaConfig.setVideoFps(settingsModel.getFps())

        QBRTCMediaConfig.setAudioCodec(QBRTCMediaConfig.AudioCodec.OPUS)
        QBRTCMediaConfig.setUseBuildInAEC(!settingsModel.getBuildInAEC())
        QBRTCMediaConfig.setAudioProcessingEnabled(!settingsModel.getProcessing())
        QBRTCMediaConfig.setUseOpenSLES(settingsModel.getOpenSLES())
    }

    override fun applyChatSettings() {
        QBSettings.getInstance().logLevel = LogLevel.DEBUG
        QBChatService.setDebugEnabled(true)
        QBRTCConfig.setDebugEnabled(true)
        QBChatService.setConfigurationBuilder(buildChatConfig())
        QBChatService.setDefaultPacketReplyTimeout(10000)
        QBChatService.getInstance().setUseStreamManagement(true)
    }

    private fun buildChatConfig(): QBChatService.ConfigurationBuilder {
        val configurationBuilder = QBChatService.ConfigurationBuilder()
        configurationBuilder.socketTimeout = SOCKET_TIMEOUT
        configurationBuilder.isUseTls = USE_TLS
        configurationBuilder.isKeepAlive = KEEP_ALIVE
        configurationBuilder.isAutojoinEnabled = AUTO_JOIN
        configurationBuilder.setAutoMarkDelivered(AUTO_MARK_DELIVERED)
        configurationBuilder.isReconnectionAllowed = RECONNECTION_ALLOWED
        configurationBuilder.setAllowListenNetwork(ALLOW_LISTEN_NETWORK)
        configurationBuilder.port = CHAT_PORT
        return configurationBuilder
    }

    override fun clearSettings() {
        sharedPreferences.edit().clear().apply()
    }
}