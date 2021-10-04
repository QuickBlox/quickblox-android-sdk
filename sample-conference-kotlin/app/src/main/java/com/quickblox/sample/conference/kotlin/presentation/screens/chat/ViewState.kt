package com.quickblox.sample.conference.kotlin.presentation.screens.chat

import androidx.annotation.IntDef
import com.quickblox.sample.conference.kotlin.presentation.screens.chat.ViewState.Companion.ERROR
import com.quickblox.sample.conference.kotlin.presentation.screens.chat.ViewState.Companion.ERROR_LOAD_ATTACHMENT
import com.quickblox.sample.conference.kotlin.presentation.screens.chat.ViewState.Companion.ERROR_UPLOAD
import com.quickblox.sample.conference.kotlin.presentation.screens.chat.ViewState.Companion.FILE_DELETED
import com.quickblox.sample.conference.kotlin.presentation.screens.chat.ViewState.Companion.FILE_LOADED
import com.quickblox.sample.conference.kotlin.presentation.screens.chat.ViewState.Companion.FILE_SHOWED
import com.quickblox.sample.conference.kotlin.presentation.screens.chat.ViewState.Companion.LEAVE
import com.quickblox.sample.conference.kotlin.presentation.screens.chat.ViewState.Companion.LOADER_PROGRESS_UPDATED
import com.quickblox.sample.conference.kotlin.presentation.screens.chat.ViewState.Companion.MESSAGES_SHOWED
import com.quickblox.sample.conference.kotlin.presentation.screens.chat.ViewState.Companion.MESSAGES_UPDATED
import com.quickblox.sample.conference.kotlin.presentation.screens.chat.ViewState.Companion.MESSAGE_SENT
import com.quickblox.sample.conference.kotlin.presentation.screens.chat.ViewState.Companion.RECEIVED_MESSAGE
import com.quickblox.sample.conference.kotlin.presentation.screens.chat.ViewState.Companion.PROGRESS
import com.quickblox.sample.conference.kotlin.presentation.screens.chat.ViewState.Companion.SHOW_ATTACHMENT_SCREEN
import com.quickblox.sample.conference.kotlin.presentation.screens.chat.ViewState.Companion.SHOW_CALL_SCREEN
import com.quickblox.sample.conference.kotlin.presentation.screens.chat.ViewState.Companion.SHOW_INFO_SCREEN
import com.quickblox.sample.conference.kotlin.presentation.screens.chat.ViewState.Companion.SHOW_LOGIN_SCREEN
import com.quickblox.sample.conference.kotlin.presentation.screens.chat.ViewState.Companion.UPDATE_TOOLBAR

/*
 * Created by Injoit in 2021-09-30.
 * Copyright © 2021 Quickblox. All rights reserved.
 */
@IntDef(PROGRESS, ERROR, MESSAGES_SHOWED, RECEIVED_MESSAGE, LOADER_PROGRESS_UPDATED, MESSAGE_SENT, SHOW_ATTACHMENT_SCREEN,
        ERROR_UPLOAD, LEAVE, FILE_SHOWED, FILE_DELETED, MESSAGES_UPDATED, SHOW_CALL_SCREEN, UPDATE_TOOLBAR, ERROR_LOAD_ATTACHMENT,
        SHOW_LOGIN_SCREEN, SHOW_INFO_SCREEN, FILE_LOADED)
annotation class ViewState {
    companion object {
        const val PROGRESS = 0
        const val ERROR = 1
        const val MESSAGES_SHOWED = 2
        const val RECEIVED_MESSAGE = 4
        const val LOADER_PROGRESS_UPDATED = 5
        const val MESSAGE_SENT = 6
        const val SHOW_ATTACHMENT_SCREEN = 7
        const val LEAVE = 8
        const val FILE_SHOWED = 9
        const val ERROR_UPLOAD = 10
        const val FILE_DELETED = 11
        const val MESSAGES_UPDATED = 12
        const val SHOW_CALL_SCREEN = 13
        const val UPDATE_TOOLBAR = 14
        const val ERROR_LOAD_ATTACHMENT = 15
        const val SHOW_LOGIN_SCREEN = 16
        const val SHOW_INFO_SCREEN = 17
        const val FILE_LOADED = 18
    }
}