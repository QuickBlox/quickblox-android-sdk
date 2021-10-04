package com.quickblox.sample.conference.kotlin.presentation.screens.muteparticipants

import androidx.annotation.IntDef
import com.quickblox.sample.conference.kotlin.presentation.screens.muteparticipants.ViewState.Companion.ERROR
import com.quickblox.sample.conference.kotlin.presentation.screens.muteparticipants.ViewState.Companion.PROGRESS
import com.quickblox.sample.conference.kotlin.presentation.screens.muteparticipants.ViewState.Companion.SHOW_LOGIN_SCREEN
import com.quickblox.sample.conference.kotlin.presentation.screens.muteparticipants.ViewState.Companion.SHOW_PARTICIPANTS
import com.quickblox.sample.conference.kotlin.presentation.screens.muteparticipants.ViewState.Companion.UPDATE_LIST

/*
 * Created by Injoit in 2021-09-30.
 * Copyright © 2021 Quickblox. All rights reserved.
 */
@IntDef(PROGRESS, ERROR, SHOW_PARTICIPANTS, UPDATE_LIST, SHOW_LOGIN_SCREEN)
annotation class ViewState {
    companion object {
        const val PROGRESS = 0
        const val ERROR = 1
        const val SHOW_PARTICIPANTS = 2
        const val UPDATE_LIST = 3
        const val SHOW_LOGIN_SCREEN = 4
    }
}