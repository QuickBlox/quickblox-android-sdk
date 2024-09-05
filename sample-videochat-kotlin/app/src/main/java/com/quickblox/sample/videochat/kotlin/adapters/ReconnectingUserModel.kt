package com.quickblox.sample.videochat.kotlin.adapters

import com.quickblox.users.model.QBUser


class ReconnectingUserModel(private var user: QBUser, private var reconnectingState: String) {
    fun getUser(): QBUser {
        return user
    }

    fun getReconnectingState(): String {
        return reconnectingState
    }

    fun setUser(user: QBUser) {
        this.user = user
    }

    fun setReconnectingState(reconnectingState: String) {
        this.reconnectingState = reconnectingState
    }
}