package com.quickblox.sample.conference.kotlin.presentation.screens.createchat

import androidx.annotation.IntDef
import com.quickblox.sample.conference.kotlin.presentation.screens.createchat.ViewState.Companion.CREATE_CHAT
import com.quickblox.sample.conference.kotlin.presentation.screens.createchat.ViewState.Companion.ERROR
import com.quickblox.sample.conference.kotlin.presentation.screens.createchat.ViewState.Companion.SHOW_CHAT_SCREEN
import com.quickblox.sample.conference.kotlin.presentation.screens.createchat.ViewState.Companion.SHOW_LOGIN_SCREEN
import com.quickblox.sample.conference.kotlin.presentation.screens.createchat.ViewState.Companion.SHOW_NAME_CHAT_SCREEN
import com.quickblox.sample.conference.kotlin.presentation.screens.createchat.ViewState.Companion.SHOW_NEW_CHAT_SCREEN

/*
 * Created by Injoit in 2021-09-30.
 * Copyright © 2021 Quickblox. All rights reserved.
 */
@IntDef(SHOW_NEW_CHAT_SCREEN, SHOW_NAME_CHAT_SCREEN, SHOW_CHAT_SCREEN, CREATE_CHAT, ERROR, SHOW_LOGIN_SCREEN)
annotation class ViewState {
    companion object {
        const val SHOW_NEW_CHAT_SCREEN = 0
        const val SHOW_NAME_CHAT_SCREEN = 1
        const val SHOW_CHAT_SCREEN = 2
        const val CREATE_CHAT = 3
        const val ERROR = 4
        const val SHOW_LOGIN_SCREEN = 5
    }
}