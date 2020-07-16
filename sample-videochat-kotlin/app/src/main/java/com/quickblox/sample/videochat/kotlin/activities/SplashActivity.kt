package com.quickblox.sample.videochat.kotlin.activities

import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.quickblox.sample.videochat.kotlin.R
import com.quickblox.sample.videochat.kotlin.services.LoginService
import com.quickblox.sample.videochat.kotlin.utils.SharedPrefsHelper
import com.quickblox.sample.videochat.kotlin.utils.isMiUi
import com.quickblox.sample.videochat.kotlin.utils.longToast

private const val SPLASH_DELAY = 1500

private const val OVERLAY_PERMISSION_CHECKED_KEY = "overlay_checked"
private const val MI_OVERLAY_PERMISSION_CHECKED_KEY = "mi_overlay_checked"
private const val ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE = 1764

class SplashActivity : BaseActivity() {
    private val TAG = SplashActivity::class.java.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        fillVersion()
        supportActionBar?.hide()

        if (checkOverlayPermissions()) {
            runNextScreen()
        }
    }

    private fun runNextScreen() {
        if (SharedPrefsHelper.hasQbUser()) {
            LoginService.start(this, SharedPrefsHelper.getQbUser())
            OpponentsActivity.start(this)
        } else {
            Handler().postDelayed({
                LoginActivity.start(this)
                finish()
            }, SPLASH_DELAY.toLong())
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        Log.e(TAG, "onActivityResult")

        if (requestCode == ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE) {
            if (checkOverlayPermissions()) {
                runNextScreen()
            }
        }
    }

    private fun fillVersion() {
        val appName = getString(R.string.app_name)
        findViewById<TextView>(R.id.text_splash_app_title).text = appName
        val versionName = packageManager.getPackageInfo(packageName, 0).versionName
        findViewById<TextView>(R.id.text_splash_app_version).text = getString(R.string.splash_app_version, versionName)
    }

    private fun checkOverlayPermissions(): Boolean {
        Log.e(TAG, "Checking Permissions")
        val overlayChecked = SharedPrefsHelper.get(OVERLAY_PERMISSION_CHECKED_KEY, false)
        val miOverlayChecked = SharedPrefsHelper.get(MI_OVERLAY_PERMISSION_CHECKED_KEY, false)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this) && !overlayChecked) {
                Log.e(TAG, "Android Overlay Permission NOT Granted")
                buildOverlayPermissionAlertDialog()
                return false
            } else if (isMiUi() && !miOverlayChecked) {
                Log.e(TAG, "Xiaomi Device. Need additional Overlay Permissions")
                buildMIUIOverlayPermissionAlertDialog()
                return false
            }
        }
        Log.e(TAG, "All Overlay Permission Granted")
        SharedPrefsHelper.save(OVERLAY_PERMISSION_CHECKED_KEY, true)
        SharedPrefsHelper.save(MI_OVERLAY_PERMISSION_CHECKED_KEY, true)
        return true
    }

    private fun buildOverlayPermissionAlertDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Overlay Permission Required")
        builder.setIcon(R.drawable.ic_error_outline_gray_24dp)
        builder.setMessage("To receive calls in background - \nPlease Allow overlay permission in Android Settings")
        builder.setCancelable(false)

        builder.setNeutralButton("No") { dialog, which ->
            longToast("You might miss calls while your application in background")
            SharedPrefsHelper.save(OVERLAY_PERMISSION_CHECKED_KEY, true)
        }

        builder.setPositiveButton("Settings") { dialog, which ->
            showAndroidOverlayPermissionsSettings()
        }

        val alertDialog = builder.create()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            alertDialog.create()
            alertDialog.show()
        }
    }

    private fun showAndroidOverlayPermissionsSettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this@SplashActivity)) {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
            intent.data = Uri.parse("package:" + applicationContext.packageName)
            startActivityForResult(intent, ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE)
        } else {
            Log.d(TAG, "Application Already has Overlay Permission")
        }
    }

    fun buildMIUIOverlayPermissionAlertDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Additional Overlay Permission Required")
        builder.setIcon(R.drawable.ic_error_outline_orange_24dp)
        builder.setMessage("Please make sure that all additional permissions granted")
        builder.setCancelable(false)

        builder.setNeutralButton("I'm sure") { dialog, which ->
            SharedPrefsHelper.save(MI_OVERLAY_PERMISSION_CHECKED_KEY, true)
            runNextScreen()
        }

        builder.setPositiveButton("Mi Settings") { dialog, which ->
            showMiUiPermissionsSettings()
        }

        val alertDialog = builder.create()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            alertDialog.create()
            alertDialog.show()
        }
    }

    private fun showMiUiPermissionsSettings() {
        val intent = Intent("miui.intent.action.APP_PERM_EDITOR")
        intent.setClassName(
            "com.miui.securitycenter",
            "com.miui.permcenter.permissions.PermissionsEditorActivity"
        )
        intent.putExtra("extra_pkgname", packageName)
        startActivityForResult(intent, ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE)
    }
}