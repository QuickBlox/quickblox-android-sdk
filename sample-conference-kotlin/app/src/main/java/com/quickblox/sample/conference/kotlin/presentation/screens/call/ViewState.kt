package com.quickblox.sample.conference.kotlin.presentation.screens.call

import androidx.annotation.IntDef
import com.quickblox.sample.conference.kotlin.presentation.screens.call.ViewState.Companion.BIND_SERVICE
import com.quickblox.sample.conference.kotlin.presentation.screens.call.ViewState.Companion.CONVERSATION
import com.quickblox.sample.conference.kotlin.presentation.screens.call.ViewState.Companion.COUNT_PARTICIPANTS
import com.quickblox.sample.conference.kotlin.presentation.screens.call.ViewState.Companion.ERROR
import com.quickblox.sample.conference.kotlin.presentation.screens.call.ViewState.Companion.ONLINE_PARTICIPANTS
import com.quickblox.sample.conference.kotlin.presentation.screens.call.ViewState.Companion.OPEN_FULL_SCREEN
import com.quickblox.sample.conference.kotlin.presentation.screens.call.ViewState.Companion.PROGRESS
import com.quickblox.sample.conference.kotlin.presentation.screens.call.ViewState.Companion.RECONNECTED
import com.quickblox.sample.conference.kotlin.presentation.screens.call.ViewState.Companion.RECONNECTING
import com.quickblox.sample.conference.kotlin.presentation.screens.call.ViewState.Companion.REQUEST_PERMISSION
import com.quickblox.sample.conference.kotlin.presentation.screens.call.ViewState.Companion.SHOW_CHAT_SCREEN
import com.quickblox.sample.conference.kotlin.presentation.screens.call.ViewState.Companion.SHOW_LOGIN_SCREEN
import com.quickblox.sample.conference.kotlin.presentation.screens.call.ViewState.Companion.SHOW_MUTE_PARTICIPANTS_SCREEN
import com.quickblox.sample.conference.kotlin.presentation.screens.call.ViewState.Companion.SHOW_VIDEO_TRACK
import com.quickblox.sample.conference.kotlin.presentation.screens.call.ViewState.Companion.START_SHARING
import com.quickblox.sample.conference.kotlin.presentation.screens.call.ViewState.Companion.STOP_SERVICE
import com.quickblox.sample.conference.kotlin.presentation.screens.call.ViewState.Companion.STREAM
import com.quickblox.sample.conference.kotlin.presentation.screens.call.ViewState.Companion.UNBIND_SERVICE

/*
 * Created by Injoit in 2021-09-30.
 * Copyright © 2021 Quickblox. All rights reserved.
 */
@IntDef(PROGRESS, ERROR, SHOW_VIDEO_TRACK, START_SHARING, STOP_SERVICE, CONVERSATION, OPEN_FULL_SCREEN, BIND_SERVICE,
        UNBIND_SERVICE, STREAM, ONLINE_PARTICIPANTS, COUNT_PARTICIPANTS, SHOW_LOGIN_SCREEN, RECONNECTING, RECONNECTED,
        REQUEST_PERMISSION, SHOW_CHAT_SCREEN, SHOW_MUTE_PARTICIPANTS_SCREEN)
annotation class ViewState {
    companion object {
        const val PROGRESS = 0
        const val ERROR = 1
        const val SHOW_VIDEO_TRACK = 2
        const val START_SHARING = 3
        const val STOP_SERVICE = 5
        const val CONVERSATION = 6
        const val OPEN_FULL_SCREEN = 7
        const val BIND_SERVICE = 8
        const val UNBIND_SERVICE = 9
        const val STREAM = 10
        const val ONLINE_PARTICIPANTS = 11
        const val COUNT_PARTICIPANTS = 12
        const val SHOW_LOGIN_SCREEN = 13
        const val RECONNECTING = 14
        const val RECONNECTED = 15
        const val REQUEST_PERMISSION = 16
        const val SHOW_CHAT_SCREEN = 17
        const val SHOW_MUTE_PARTICIPANTS_SCREEN = 18
    }
}