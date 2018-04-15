package com.quickblox.sample.videochatkotlin.utils

import com.quickblox.users.model.QBUser
import com.quickblox.videochat.webrtc.QBRTCMediaConfig

/**
 * Created by Roman on 15.04.2018.
 */
fun setSettingsForMultiCall(users: ArrayList<QBUser>) {
    if (users.size == 1) {
        QBRTCMediaConfig.setVideoWidth(QBRTCMediaConfig.VideoQuality.HD_VIDEO.width)
        QBRTCMediaConfig.setVideoHeight(QBRTCMediaConfig.VideoQuality.HD_VIDEO.height)
    } else {
        //set to minimum settings
        QBRTCMediaConfig.setVideoWidth(QBRTCMediaConfig.VideoQuality.QBGA_VIDEO.width)
        QBRTCMediaConfig.setVideoHeight(QBRTCMediaConfig.VideoQuality.QBGA_VIDEO.height)
        QBRTCMediaConfig.setVideoHWAcceleration(false)
        QBRTCMediaConfig.setVideoCodec(null)
    }
}