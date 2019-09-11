package com.quickblox.sample.videochat.kotlin.activities

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.core.app.ActivityCompat
import com.quickblox.sample.videochat.kotlin.R
import com.quickblox.sample.videochat.kotlin.utils.SharedPrefsHelper
import com.quickblox.sample.videochat.kotlin.utils.longToast

private const val PERMISSION_CODE = 0
private const val EXTRA_PERMISSIONS = "extra_permissions"
private const val CHECK_ONLY_AUDIO = "check_only_audio"

class PermissionsActivity : BaseActivity() {

    companion object {
        fun startForResult(activity: Activity, checkOnlyAudio: Boolean, permissions: Array<String>) {
            val intent = Intent(activity, PermissionsActivity::class.java)
            intent.putExtra(EXTRA_PERMISSIONS, permissions)
            intent.putExtra(CHECK_ONLY_AUDIO, checkOnlyAudio)
            activity.startActivityForResult(intent, PERMISSION_CODE)
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
        if (checkPermission(audioPermission)) {
            requestPermissions(audioPermission)
        } else {
            allPermissionsGranted()
        }
    }

    private fun checkPermissionAudioVideo(permissions: Array<String>) {
        if (checkPermissions(permissions)) {
            requestPermissions(*permissions)
        } else {
            allPermissionsGranted()
        }
    }

    private fun getPermissions(): Array<String> {
        return intent.getStringArrayExtra(EXTRA_PERMISSIONS)
    }

    private fun getCheckOnlyAudio(): Boolean {
        return intent.getBooleanExtra(CHECK_ONLY_AUDIO, false)
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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                for (permission in permissions) {
                    if (!shouldShowRequestPermissionRationale(permission)) {
                        SharedPrefsHelper.save(permission, false)
                    }
                }
            }
            requiresCheck = false
            showDeniedResponse(grantResults)
            finish()
        }
    }

    private fun showDeniedResponse(grantResults: IntArray) {
        if (grantResults.size > 1) {
            for (index in grantResults.indices) {
                if (grantResults[index] != 0) {
                    longToast(getString(R.string.permission_unavailable, PermissionFeatures.values()[index]))
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