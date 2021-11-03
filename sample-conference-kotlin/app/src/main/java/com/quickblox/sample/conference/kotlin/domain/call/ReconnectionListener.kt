package com.quickblox.sample.conference.kotlin.domain.call

import com.quickblox.sample.conference.kotlin.domain.call.entities.SessionState.*

interface ReconnectionListener {
    fun onChangedState(reconnectionState: ReconnectionState)
    fun requestPermission()
}