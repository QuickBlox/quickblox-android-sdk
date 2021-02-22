package com.quickblox.sample.chat.kotlin.utils

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.*

private const val PERMISSIONS_FOR_SAVE_FILE_IMAGE_REQUEST = 1010

class SystemPermissionHelper(private var activity: Activity) {

    fun isSaveImagePermissionGranted(): Boolean {
        return isPermissionGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                && isPermissionGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                && isPermissionGranted(Manifest.permission.CAMERA)
    }

    private fun isPermissionGranted(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED
    }

    fun requestPermissionsForSaveFileImage() {
        val permissions = ArrayList<String>()
        permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        permissions.add(Manifest.permission.CAMERA)
        checkAndRequestPermissions(PERMISSIONS_FOR_SAVE_FILE_IMAGE_REQUEST, permissions)
    }

    private fun checkAndRequestPermissions(requestCode: Int, permissions: ArrayList<String>) {
        if (collectDeniedPermissions(permissions).isNotEmpty()) {
            requestPermissions(requestCode, *collectDeniedPermissions(permissions))
        }
    }

    private fun collectDeniedPermissions(permissions: ArrayList<String>): Array<String> {
        val deniedPermissionsList = ArrayList<String>()
        for (permission in permissions) {
            if (!isPermissionGranted(permission)) {
                deniedPermissionsList.add(permission)
            }
        }

        return deniedPermissionsList.toTypedArray()
    }

    private fun requestPermissions(requestCode: Int, vararg permissions: String) {
        ActivityCompat.requestPermissions(activity, permissions, requestCode)
    }
}