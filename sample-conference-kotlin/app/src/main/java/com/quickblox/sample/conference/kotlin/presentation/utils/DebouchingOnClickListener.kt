package com.quickblox.sample.conference.kotlin.presentation.utils

import android.view.View

private const val CLICK_INTERVAL = 1000L

/*
 * Created by Injoit in 2021-09-30.
 * Copyright © 2021 Quickblox. All rights reserved.
 */
class DebouchingOnClickListener(private val doClick: ((View) -> Unit)) : View.OnClickListener {
    companion object {
        @JvmStatic
        var enabled = true
        private val ENABLE_AGAIN = Runnable { enabled = true }
    }

    override fun onClick(v: View) {
        if (enabled) {
            enabled = false
            v.postDelayed(ENABLE_AGAIN, CLICK_INTERVAL)
            doClick(v)
        }
    }
}