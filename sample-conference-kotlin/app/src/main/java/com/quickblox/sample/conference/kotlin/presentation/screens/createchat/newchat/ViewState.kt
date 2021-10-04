package com.quickblox.sample.conference.kotlin.presentation.screens.createchat.newchat

import androidx.annotation.IntDef
import com.quickblox.sample.conference.kotlin.presentation.screens.createchat.newchat.ViewState.Companion.ERROR
import com.quickblox.sample.conference.kotlin.presentation.screens.createchat.newchat.ViewState.Companion.PROGRESS
import com.quickblox.sample.conference.kotlin.presentation.screens.createchat.newchat.ViewState.Companion.SHOW_USERS

/*
 * Created by Injoit in 2021-09-30.
 * Copyright © 2021 Quickblox. All rights reserved.
 */
@IntDef(PROGRESS, ERROR, SHOW_USERS)
annotation class ViewState {
    companion object {
        const val PROGRESS = 0
        const val ERROR = 1
        const val SHOW_USERS = 2
    }
}