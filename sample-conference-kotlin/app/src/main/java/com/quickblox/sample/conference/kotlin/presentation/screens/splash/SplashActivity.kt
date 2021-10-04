package com.quickblox.sample.conference.kotlin.presentation.screens.splash

import android.os.Bundle
import android.widget.Toast
import com.quickblox.sample.conference.kotlin.databinding.ActivitySplashBinding
import com.quickblox.sample.conference.kotlin.presentation.screens.base.BaseActivity
import com.quickblox.sample.conference.kotlin.presentation.screens.call.CallActivity
import com.quickblox.sample.conference.kotlin.presentation.screens.login.LoginActivity
import com.quickblox.sample.conference.kotlin.presentation.screens.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint

/*
 * Created by Injoit in 2021-09-30.
 * Copyright © 2021 Quickblox. All rights reserved.
 */
@AndroidEntryPoint
class SplashActivity : BaseActivity<SplashViewModel>(SplashViewModel::class.java) {
    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        fillVersion()

        viewModel.liveData.observe(this, { result ->
            result?.let { (state, data) ->
                when (state) {
                    ViewState.SHOW_LOGIN_SCREEN -> {
                        LoginActivity.start(this)
                        finish()
                    }
                    ViewState.SHOW_CALL_SCREEN -> {
                        CallActivity.start(this)
                        finish()
                    }
                    ViewState.SHOW_MAIN_SCREEN -> {
                        MainActivity.start(this)
                        finish()
                    }
                    ViewState.ERROR -> {
                        Toast.makeText(baseContext, "$data", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    private fun fillVersion() {
        binding.tvVersion.text = packageManager.getPackageInfo(packageName, 0).versionName
    }

    override fun showProgress() {
        // empty
    }

    override fun hideProgress() {
        // empty
    }
}