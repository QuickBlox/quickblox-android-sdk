package com.quickblox.sample.videochat.conference.kotlin.activities

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.core.app.ActivityCompat
import com.quickblox.sample.videochat.conference.kotlin.R
import com.quickblox.sample.videochat.conference.kotlin.utils.longToast


class PermissionsActivity : BaseActivity() {

    companion object {
        private const val PERMISSION_CODE = 777
        private const val EXTRA_PERMISSIONS = "extra_permissions"
        private const val EXTRA_CHECK_ONLY_AUDIO = "extra_check_only_audio"

        fun startForResult(activity: Activity, code: Int, checkOnlyAudio: Boolean, permissions: Array<String>) {
            val intent = Intent(activity, PermissionsActivity::class.java)
            intent.putExtra(EXTRA_PERMISSIONS, permissions)
            intent.putExtra(EXTRA_CHECK_ONLY_AUDIO, checkOnlyAudio)
            activity.startActivityForResult(intent, code)
        }
    }

    private enum class PermissionFeatures {
        CAMERA,
        MICROPHONE
    }

    private var requiresCheck: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (intent == null || !intent.hasExtra(EXTRA_PERMISSIONS)) {
            throw RuntimeException("This Activity needs to be launched using the static startActivityForResult() method.")
        }
        setContentView(R.layout.activity_permissions)
        supportActionBar?.hide()
        requiresCheck = true
    }

    override fun onResume() {
        super.onResume()
        if (requiresCheck) {
            checkPermissions()
        } else {
            requiresCheck = true
        }
    }

    private fun checkPermissions() {
        val permissions = getPermissions()
        val checkOnlyAudio = getCheckOnlyAudio()

        if (checkOnlyAudio) {
            checkPermissionAudio(permissions[1])
        } else {
            checkPermissionAudioVideo(permissions)
        }
    }

    private fun checkPermissionAudio(audioPermission: String) {
        if (checkPermissions(audioPermission)) {
            requestPermissions(audioPermission)
        } else {
            allPermissionsGranted()
        }
    }

    private fun checkPermissionAudioVideo(permissions: Array<String>) {
        if (checkPermissions(*permissions)) {
            requestPermissions(*permissions)
        } else {
            allPermissionsGranted()
        }
    }

    private fun getPermissions(): Array<String> {
        return intent.getStringArrayExtra(EXTRA_PERMISSIONS)
    }

    private fun getCheckOnlyAudio(): Boolean {
        return intent.getBooleanExtra(EXTRA_CHECK_ONLY_AUDIO, false)
    }

    private fun requestPermissions(vararg permissions: String) {
        ActivityCompat.requestPermissions(this, permissions, PERMISSION_CODE)
    }

    private fun allPermissionsGranted() {
        setResult(Activity.RESULT_OK)
        finish()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == PERMISSION_CODE && hasAllPermissionsGranted(grantResults)) {
            requiresCheck = true
            allPermissionsGranted()
        } else {
            requiresCheck = false
            showDeniedResponse(grantResults)
            finish()
        }
    }

    private fun showDeniedResponse(grantResults: IntArray) {
        if (grantResults.size > 1) {
            for (i in grantResults.indices) {
                if (grantResults[i] != 0) {
                    longToast(getString(R.string.permission_unavailable, PermissionFeatures.values()[i]))
                }
            }
        } else {
            longToast(getString(R.string.permission_unavailable, PermissionFeatures.MICROPHONE))
        }
    }

    private fun hasAllPermissionsGranted(grantResults: IntArray): Boolean {
        for (grantResult in grantResults) {
            if (grantResult == PackageManager.PERMISSION_DENIED) {
                return false
            }
        }
        return true
    }
}