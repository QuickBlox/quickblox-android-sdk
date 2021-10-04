package com.quickblox.sample.conference.kotlin.presentation.screens.base

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider

/*
 * Created by Injoit in 2021-09-30.
 * Copyright © 2021 Quickblox. All rights reserved.
 */
abstract class BaseActivity<VM : BaseViewModel>(private val viewModelClass: Class<VM>) : AppCompatActivity() {
    val viewModel by lazy {
        ViewModelProvider(this).get(viewModelClass)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycle.addObserver((viewModel as BaseViewModel).getViewStateObserver())
    }

    abstract fun showProgress()
    abstract fun hideProgress()
}