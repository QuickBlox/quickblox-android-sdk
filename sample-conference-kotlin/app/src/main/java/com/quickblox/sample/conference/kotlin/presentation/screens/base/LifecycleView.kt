package com.quickblox.sample.conference.kotlin.presentation.screens.base

/*
 * Created by Injoit in 2021-09-30.
 * Copyright © 2021 Quickblox. All rights reserved.
 */
interface LifecycleView {
    fun onStartView()
    fun onResumeView()
    fun onPauseView()
    fun onStopView()
}