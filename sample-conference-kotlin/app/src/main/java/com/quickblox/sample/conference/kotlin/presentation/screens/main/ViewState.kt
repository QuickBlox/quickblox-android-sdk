package com.quickblox.sample.conference.kotlin.presentation.screens.main

import androidx.annotation.IntDef
import com.quickblox.sample.conference.kotlin.presentation.screens.main.ViewState.Companion.DIALOG_UPDATED
import com.quickblox.sample.conference.kotlin.presentation.screens.main.ViewState.Companion.ERROR
import com.quickblox.sample.conference.kotlin.presentation.screens.main.ViewState.Companion.LIST_DIALOGS_UPDATED
import com.quickblox.sample.conference.kotlin.presentation.screens.main.ViewState.Companion.MOVE_TO_FIRST_DIALOG
import com.quickblox.sample.conference.kotlin.presentation.screens.main.ViewState.Companion.PROGRESS
import com.quickblox.sample.conference.kotlin.presentation.screens.main.ViewState.Companion.SHOW_CHAT_SCREEN
import com.quickblox.sample.conference.kotlin.presentation.screens.main.ViewState.Companion.SHOW_CREATE_SCREEN
import com.quickblox.sample.conference.kotlin.presentation.screens.main.ViewState.Companion.SHOW_DIALOGS
import com.quickblox.sample.conference.kotlin.presentation.screens.main.ViewState.Companion.SHOW_LOGIN_SCREEN
import com.quickblox.sample.conference.kotlin.presentation.screens.main.ViewState.Companion.USER_ERROR

/*
 * Created by Injoit in 2021-09-30.
 * Copyright © 2021 Quickblox. All rights reserved.
 */
@IntDef(PROGRESS, ERROR, SHOW_DIALOGS, SHOW_LOGIN_SCREEN, DIALOG_UPDATED, SHOW_CHAT_SCREEN, MOVE_TO_FIRST_DIALOG, LIST_DIALOGS_UPDATED,
        USER_ERROR, SHOW_CREATE_SCREEN)
annotation class ViewState {
    companion object {
        const val PROGRESS = 0
        const val ERROR = 1
        const val SHOW_DIALOGS = 2
        const val SHOW_LOGIN_SCREEN = 3
        const val DIALOG_UPDATED = 4
        const val SHOW_CHAT_SCREEN = 5
        const val MOVE_TO_FIRST_DIALOG = 6
        const val LIST_DIALOGS_UPDATED = 7
        const val USER_ERROR = 8
        const val SHOW_CREATE_SCREEN = 9
    }
}