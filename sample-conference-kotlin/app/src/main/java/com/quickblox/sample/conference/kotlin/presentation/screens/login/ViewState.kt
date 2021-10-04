package com.quickblox.sample.conference.kotlin.presentation.screens.login

import androidx.annotation.IntDef
import com.quickblox.sample.conference.kotlin.presentation.screens.login.ViewState.Companion.ERROR
import com.quickblox.sample.conference.kotlin.presentation.screens.login.ViewState.Companion.PROGRESS
import com.quickblox.sample.conference.kotlin.presentation.screens.login.ViewState.Companion.SHOW_MAIN_SCREEN

/*
 * Created by Injoit in 2021-09-30.
 * Copyright © 2021 Quickblox. All rights reserved.
 */
@IntDef(PROGRESS, ERROR, SHOW_MAIN_SCREEN)
annotation class ViewState {
    companion object {
        const val PROGRESS = 0
        const val ERROR = 1
        const val SHOW_MAIN_SCREEN = 2
    }
}