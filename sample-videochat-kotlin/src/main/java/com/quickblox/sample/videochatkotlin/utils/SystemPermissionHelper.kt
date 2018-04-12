package com.quickblox.sample.videochatkotlin.utils

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.util.Log
import java.util.*


class SystemPermissionHelper(val activity: Activity) {
    val TAG = SystemPermissionHelper::class.java.simpleName

    companion object {
        const val PERMISSIONS_FOR_CALL_REQUEST = 15
    }

    fun requestPermissionsForCallByType() {
        checkAndRequestPermissions(PERMISSIONS_FOR_CALL_REQUEST, Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA)
    }

    fun checkAndRequestPermissions(requestCode: Int, vararg permissions: String) {
        if (collectDeniedPermissions(*permissions).size > 0) {
            requestPermissions(requestCode, *collectDeniedPermissions(*permissions))
        }
    }

    private fun collectDeniedPermissions(vararg permissions: String): Array<String> {
        val deniedPermissionsList = ArrayList<String>()
        for (permission in permissions) {
            if (!isPermissionGranted(permission)) {
                deniedPermissionsList.add(permission)
            }
        }

        return deniedPermissionsList.toTypedArray()
    }

    fun requestPermissions(requestCode: Int, vararg permissions: String) {
        Log.v(TAG, "request Permissions for activity " + activity.javaClass.simpleName)
        ActivityCompat.requestPermissions(activity, permissions, requestCode)
    }

    fun isAllCameraPermissionGranted(): Boolean {
        return isPermissionGranted(Manifest.permission.CAMERA)
    }

    fun isPermissionGranted(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(activity.getApplicationContext(), permission) == PackageManager.PERMISSION_GRANTED
    }

    fun collectDeniedPermissionsFomResult(permissions: Array<String>, grantResults: IntArray): ArrayList<String> {
        val deniedPermissions = ArrayList<String>()
        for (i in permissions.indices) {
            if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                deniedPermissions.add(permissions[i])
            }
        }

        return deniedPermissions
    }
}
