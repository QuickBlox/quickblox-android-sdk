package com.quickblox.sample.videochat.conference.kotlin.activities

import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.view.KeyEvent
import android.view.View
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.quickblox.sample.videochat.conference.kotlin.R
import com.quickblox.sample.videochat.conference.kotlin.utils.showSnackbar
import kotlinx.android.synthetic.main.list_item_dialog.*


abstract class BaseActivity : AppCompatActivity() {
    private var progressDialog: ProgressDialog? = null

    protected fun showProgressDialog(@StringRes messageId: Int) {
        if (progressDialog == null) {
            progressDialog = ProgressDialog(this)
            progressDialog?.isIndeterminate = true
            progressDialog?.setCancelable(false)
            progressDialog?.setCanceledOnTouchOutside(false)

            // Disable the back button
            val keyListener = DialogInterface.OnKeyListener { dialog, keyCode, event -> keyCode == KeyEvent.KEYCODE_BACK }
            progressDialog?.setOnKeyListener(keyListener)
        }
        progressDialog?.setMessage(getString(messageId))
        progressDialog?.show()
    }

    override fun onPause() {
        super.onPause()
        hideProgressDialog()
    }

    protected fun hideProgressDialog() {
        if (progressDialog != null && progressDialog?.isShowing as Boolean) {
            progressDialog?.dismiss()
        }
    }

    protected fun showErrorSnackbar(@StringRes resId: Int, e: Exception, clickListener: View.OnClickListener?) {
        val rootView = window.decorView.findViewById<View>(android.R.id.content)
        rootView?.let {
            showSnackbar(it, resId, e, R.string.dlg_retry, clickListener)
        }
    }

    protected fun checkPermissions(vararg permissions: String): Boolean {
        for (permission in permissions) {
            if (checkPermission(permission)) {
                return true
            }
        }
        return false
    }

    private fun checkPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_DENIED
    }
}