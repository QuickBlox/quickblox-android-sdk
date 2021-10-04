package com.quickblox.sample.conference.kotlin.presentation.screens.base

import androidx.lifecycle.*

/*
 * Created by Injoit in 2021-09-30.
 * Copyright © 2021 Quickblox. All rights reserved.
 */
abstract class BaseViewModel : ViewModel(), LifecycleView, LifecycleApp {
    init {
        ProcessLifecycleOwner.get().lifecycle.addObserver(object : LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                if (event == Lifecycle.Event.ON_STOP) {
                    onStopApp()
                }
            }
        })
    }

    fun getViewStateObserver(): LifecycleEventObserver {
        return LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> onStartView()
                Lifecycle.Event.ON_RESUME -> onResumeView()
                Lifecycle.Event.ON_PAUSE -> onPauseView()
                Lifecycle.Event.ON_STOP -> onStopView()
                else -> {
                    // empty
                }
            }
        }
    }

    override fun onStopApp() {}
    override fun onStartView() {}
    override fun onResumeView() {}
    override fun onPauseView() {}
    override fun onStopView() {}
}