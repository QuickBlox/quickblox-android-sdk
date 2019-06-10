package com.quickblox.sample.videochat.conference.kotlin.utils

import com.quickblox.conference.ConferenceSession


object WebRtcSessionManager {

    private var currentSession: ConferenceSession? = null

    fun getCurrentSession(): ConferenceSession? {
        return currentSession
    }

    fun setCurrentSession(qbCurrentSession: ConferenceSession) {
        currentSession = qbCurrentSession
    }
}