package com.quickblox.sample.conference.kotlin.presentation.screens.splash

import androidx.annotation.IntDef
import com.quickblox.sample.conference.kotlin.presentation.screens.splash.ViewState.Companion.ERROR
import com.quickblox.sample.conference.kotlin.presentation.screens.splash.ViewState.Companion.SHOW_CALL_SCREEN
import com.quickblox.sample.conference.kotlin.presentation.screens.splash.ViewState.Companion.SHOW_LOGIN_SCREEN
import com.quickblox.sample.conference.kotlin.presentation.screens.splash.ViewState.Companion.SHOW_MAIN_SCREEN

/*
 * Created by Injoit in 2021-09-30.
 * Copyright © 2021 Quickblox. All rights reserved.
 */
@IntDef(SHOW_LOGIN_SCREEN, SHOW_MAIN_SCREEN, ERROR, SHOW_CALL_SCREEN)
annotation class ViewState {
    companion object {
        const val SHOW_LOGIN_SCREEN = 0
        const val SHOW_MAIN_SCREEN = 1
        const val ERROR = 2
        const val SHOW_CALL_SCREEN = 3
    }
}