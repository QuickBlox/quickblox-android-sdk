package com.quickblox.sample.videochatkotlin.utils

import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.support.annotation.StringRes
import android.view.KeyEvent
import android.view.View
import com.quickblox.sample.core.utils.ErrorUtils

/**
 * Created by Roman on 29.04.2018.
 */
fun showProgressDialog(context: Context, progressDialog: ProgressDialog, @StringRes messageId: Int) {
    progressDialog.setIndeterminate(true)
    progressDialog.setCancelable(false)
    progressDialog.setCanceledOnTouchOutside(false)

    // Disable the back button
    val keyListener = DialogInterface.OnKeyListener { dialog, keyCode, event -> keyCode == KeyEvent.KEYCODE_BACK }
    progressDialog.setOnKeyListener(keyListener)


    progressDialog.setMessage(context.getString(messageId))

    progressDialog.show()

}

fun hideProgressDialog(progressDialog: ProgressDialog) {
    progressDialog.dismiss()
}

fun showErrorSnackbar(rootView: View?, @StringRes resId: Int, e: Exception,
                                clickListener: View.OnClickListener) {
    if (rootView != null) {
        ErrorUtils.showSnackbar(rootView, resId, e,
                com.quickblox.sample.core.R.string.dlg_retry, clickListener)
    }
}