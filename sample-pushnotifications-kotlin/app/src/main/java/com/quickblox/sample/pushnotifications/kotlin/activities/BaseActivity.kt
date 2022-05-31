package com.quickblox.sample.pushnotifications.kotlin.activities

import android.app.ProgressDialog
import android.content.DialogInterface
import android.view.KeyEvent
import android.view.MenuItem
import android.view.View
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import com.quickblox.sample.pushnotifications.kotlin.R
import com.quickblox.sample.pushnotifications.kotlin.utils.showSnackbar

abstract class BaseActivity : AppCompatActivity() {
    private var progressDialog: ProgressDialog? = null

    protected fun showProgressDialog(@StringRes messageId: Int) {
        if (progressDialog == null) {
            progressDialog = ProgressDialog(this)
            progressDialog?.isIndeterminate = true
            progressDialog?.setCancelable(false)
            progressDialog?.setCanceledOnTouchOutside(false)

            // disable the back button
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
        if (progressDialog?.isShowing == true) {
            progressDialog?.dismiss()
        }
    }

    protected fun showErrorSnackbar(@StringRes resId: Int, e: Exception, clickListener: View.OnClickListener?) {
        val rootView = window.decorView.findViewById<View>(android.R.id.content)
        rootView?.let {
            showSnackbar(it, resId, e, R.string.dlg_retry, clickListener)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}