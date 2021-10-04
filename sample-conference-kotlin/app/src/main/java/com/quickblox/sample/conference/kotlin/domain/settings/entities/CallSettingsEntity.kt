package com.quickblox.sample.conference.kotlin.domain.settings.entities

/*
 * Created by Injoit in 2021-09-30.
 * Copyright © 2021 Quickblox. All rights reserved.
 */
interface CallSettingsEntity {
    fun getVideoResolution(): Int
    fun setVideoResolution(resolution: Int)
    fun getBandwidth(): Int
    fun setBandwidth(bitrate: Int)
    fun getFps(): Int
    fun setFps(frameRate: Int)
    fun getBuildInAEC(): Boolean
    fun setBuildInAEC(aec: Boolean)
    fun getProcessing(): Boolean
    fun setProcessing(audioProcessing: Boolean)
    fun getOpenSLES(): Boolean
    fun setOpenSLES(useOpenSLES: Boolean)
}