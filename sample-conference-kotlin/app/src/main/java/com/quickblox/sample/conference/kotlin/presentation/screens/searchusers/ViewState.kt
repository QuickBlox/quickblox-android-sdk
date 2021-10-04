package com.quickblox.sample.conference.kotlin.presentation.screens.searchusers

import androidx.annotation.IntDef
import com.quickblox.sample.conference.kotlin.presentation.screens.searchusers.ViewState.Companion.DIALOG_LOADED
import com.quickblox.sample.conference.kotlin.presentation.screens.searchusers.ViewState.Companion.ERROR
import com.quickblox.sample.conference.kotlin.presentation.screens.searchusers.ViewState.Companion.MOVE_TO_BACK
import com.quickblox.sample.conference.kotlin.presentation.screens.searchusers.ViewState.Companion.OCCUPANTS_LOADED
import com.quickblox.sample.conference.kotlin.presentation.screens.searchusers.ViewState.Companion.PROGRESS
import com.quickblox.sample.conference.kotlin.presentation.screens.searchusers.ViewState.Companion.SHOW_LOGIN_SCREEN
import com.quickblox.sample.conference.kotlin.presentation.screens.searchusers.ViewState.Companion.SHOW_USERS

/*
 * Created by Injoit in 2021-09-30.
 * Copyright © 2021 Quickblox. All rights reserved.
 */
@IntDef(PROGRESS, ERROR, SHOW_USERS, MOVE_TO_BACK, DIALOG_LOADED, OCCUPANTS_LOADED, SHOW_LOGIN_SCREEN)
annotation class ViewState {
    companion object {
        const val PROGRESS = 0
        const val ERROR = 1
        const val SHOW_USERS = 2
        const val MOVE_TO_BACK = 3
        const val DIALOG_LOADED = 4
        const val OCCUPANTS_LOADED = 5
        const val SHOW_LOGIN_SCREEN = 6
    }
}
