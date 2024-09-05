package com.quickblox.sample.conference.kotlin.presentation.screens.splash

import android.Manifest
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.quickblox.sample.conference.kotlin.R
import com.quickblox.sample.conference.kotlin.databinding.ActivitySplashBinding
import com.quickblox.sample.conference.kotlin.presentation.screens.base.BaseActivity
import com.quickblox.sample.conference.kotlin.presentation.screens.call.CallActivity
import com.quickblox.sample.conference.kotlin.presentation.screens.login.LoginActivity
import com.quickblox.sample.conference.kotlin.presentation.screens.main.MainActivity
import com.vmadalin.easypermissions.EasyPermissions
import dagger.hilt.android.AndroidEntryPoint

/*
 * Created by Injoit in 2021-09-30.
 * Copyright © 2021 Quickblox. All rights reserved.
 */

private const val NOTIFICATIONS_PERMISSIONS_CODE = 333
private const val SPLASH_DELAY = 1500L

@AndroidEntryPoint
class SplashActivity : BaseActivity<SplashViewModel>(SplashViewModel::class.java), EasyPermissions.PermissionCallbacks {
    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        fillVersion()

        checkAndRequestNotificationPermission()

        viewModel.liveData.observe(this) { result ->
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
        }
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


    private fun checkAndRequestNotificationPermission() {
        if (EasyPermissions.hasPermissions(this, Manifest.permission.POST_NOTIFICATIONS)) {
            Handler(Looper.getMainLooper()).postDelayed({
                viewModel.run()
            }, SPLASH_DELAY)
        } else {
            requestNotificationPermission()
        }
    }

    private fun requestNotificationPermission() {
        EasyPermissions.requestPermissions(
            host = this,
            rationale = getString(R.string.notification_permission),
            requestCode = NOTIFICATIONS_PERMISSIONS_CODE,
            perms = arrayOf(Manifest.permission.POST_NOTIFICATIONS)
        )
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
        viewModel.run()
    }

    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {
        when (requestCode) {
            NOTIFICATIONS_PERMISSIONS_CODE -> {
                viewModel.run()
            }
        }
    }
}