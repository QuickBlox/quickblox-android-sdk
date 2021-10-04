package com.quickblox.sample.conference.kotlin.presentation.screens.chatinfo

import androidx.annotation.IntDef
import com.quickblox.sample.conference.kotlin.presentation.screens.chatinfo.ViewState.Companion.DIALOG_LOADED
import com.quickblox.sample.conference.kotlin.presentation.screens.chatinfo.ViewState.Companion.ERROR
import com.quickblox.sample.conference.kotlin.presentation.screens.chatinfo.ViewState.Companion.PROGRESS
import com.quickblox.sample.conference.kotlin.presentation.screens.chatinfo.ViewState.Companion.SHOW_LOGIN_SCREEN
import com.quickblox.sample.conference.kotlin.presentation.screens.chatinfo.ViewState.Companion.SHOW_SEARCH_USER_SCREEN
import com.quickblox.sample.conference.kotlin.presentation.screens.chatinfo.ViewState.Companion.USERS_LOADED
import com.quickblox.sample.conference.kotlin.presentation.screens.chatinfo.ViewState.Companion.USERS_UPDATED

/*
 * Created by Injoit in 2021-09-30.
 * Copyright © 2021 Quickblox. All rights reserved.
 */
@IntDef(PROGRESS, ERROR, SHOW_SEARCH_USER_SCREEN, USERS_LOADED, USERS_UPDATED, DIALOG_LOADED, SHOW_LOGIN_SCREEN)
annotation class ViewState {
    companion object {
        const val PROGRESS = 0
        const val ERROR = 1
        const val SHOW_SEARCH_USER_SCREEN = 2
        const val USERS_UPDATED = 3
        const val USERS_LOADED = 4
        const val DIALOG_LOADED = 5
        const val SHOW_LOGIN_SCREEN = 6
    }
}
