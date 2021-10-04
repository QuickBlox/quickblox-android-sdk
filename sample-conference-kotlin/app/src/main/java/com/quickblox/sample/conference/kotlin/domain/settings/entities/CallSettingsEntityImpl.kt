package com.quickblox.sample.conference.kotlin.domain.settings.entities

/*
 * Created by Injoit in 2021-09-30.
 * Copyright © 2021 Quickblox. All rights reserved.
 */
data class CallSettingsEntityImpl(var resolution: Int = 0, var bitrate: Int = 0, var frameRate: Int = 0,
                                  var aec: Boolean = false, var audioProcessing: Boolean = false, var useOpenSLES: Boolean = false) : CallSettingsEntity {

    override fun getVideoResolution(): Int {
        return resolution
    }

    override fun setVideoResolution(resolution: Int) {
        this.resolution = resolution
    }

    override fun getBandwidth(): Int {
        return bitrate
    }

    override fun setBandwidth(bitrate: Int) {
        this.bitrate = bitrate
    }

    override fun getFps(): Int {
        return frameRate
    }

    override fun setFps(frameRate: Int) {
        this.frameRate = frameRate
    }

    override fun getBuildInAEC(): Boolean {
        return aec
    }

    override fun setBuildInAEC(aec: Boolean) {
        this.aec = aec
    }

    override fun getProcessing(): Boolean {
        return audioProcessing
    }

    override fun setProcessing(audioProcessing: Boolean) {
        this.audioProcessing = audioProcessing
    }

    override fun getOpenSLES(): Boolean {
        return useOpenSLES
    }

    override fun setOpenSLES(useOpenSLES: Boolean) {
        this.useOpenSLES = useOpenSLES
    }
}
