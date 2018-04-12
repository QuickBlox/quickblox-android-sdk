package com.quickblox.sample.videochatkotlin.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.quickblox.sample.core.ui.activity.CoreBaseActivity
import com.quickblox.sample.core.utils.Toaster
import com.quickblox.sample.videochatkotlin.R
import com.quickblox.sample.videochatkotlin.fragments.OutComingFragment
import com.quickblox.sample.videochatkotlin.services.CallService
import com.quickblox.sample.videochatkotlin.utils.*
import com.quickblox.sample.videochatkotlin.utils.StringUtils.createCompositeString

/**
 * Created by roman on 4/6/18.
 */
class CallActivity : CoreBaseActivity() {
    val TAG = CallActivity::class.java.simpleName
    lateinit var systemPermissionHelper: SystemPermissionHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate")
        setContentView(R.layout.activity_main)

        systemPermissionHelper = SystemPermissionHelper(this)
        setActionBarTitle(R.string.title_call_activity)
        checkCameraPermissionAndStart()
    }

    fun checkCameraPermissionAndStart() {
        if (systemPermissionHelper.isAllCameraPermissionGranted()) {
            initSuitableFragment()
        } else {
            systemPermissionHelper.requestPermissionsForCallByType()
        }
    }

    private fun initSuitableFragment() {
        initOutgoingFragment()
    }

    fun initOutgoingFragment() {
        val outComingFragment = OutComingFragment()
        addFragment(supportFragmentManager, R.id.fragment_container, outComingFragment, OutComingFragment::class.java.simpleName)
    }

    fun initIncomingFragment() {

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            SystemPermissionHelper.PERMISSIONS_FOR_CALL_REQUEST -> {
                if (grantResults.isNotEmpty()) {
                    if (!systemPermissionHelper.isAllCameraPermissionGranted()) {
                        Log.d(TAG, "AMBRA showToastDeniedPermissions")
                        showToastDeniedPermissions(permissions, grantResults)
                        startLogout()
                        finish()
                    } else {
                        initOutgoingFragment()
                    }
                }

            }
        }
    }

    private fun showToastDeniedPermissions(permissions: Array<String>, grantResults: IntArray) {
        val deniedPermissions = systemPermissionHelper
                .collectDeniedPermissionsFomResult(permissions, grantResults)

        Toaster.longToast(
                getString(R.string.denied_permission_message, createCompositeString(deniedPermissions)))
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.activity_call, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.menu_logout_user_done -> {
                startLogout()
                startLoginActivity()
                return true
            }

            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun startLogout() {
        val intent = Intent(this, CallService::class.java)
        intent.putExtra(EXTRA_COMMAND_TO_SERVICE, COMMAND_LOGOUT)
        startService(intent)
    }

    private fun startLoginActivity() {
        val intent = Intent(this, LoginActivity::class.java)
        this.startActivity(intent)
        finish()
    }
}