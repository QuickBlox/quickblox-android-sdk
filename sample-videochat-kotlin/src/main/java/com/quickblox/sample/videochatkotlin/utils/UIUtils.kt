package com.quickblox.sample.videochatkotlin.utils

import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.support.annotation.StringRes
import android.view.KeyEvent

/**
 * Created by Roman on 29.04.2018.
 */
fun showProgressDialog(context: Context, progressDialog: ProgressDialog, @StringRes messageId: Int) {
    progressDialog.isIndeterminate = true
    progressDialog.setCancelable(false)
    progressDialog.setCanceledOnTouchOutside(false)

    // Disable the back button
    val keyListener = DialogInterface.OnKeyListener { _, keyCode, _ -> keyCode == KeyEvent.KEYCODE_BACK }
    progressDialog.setOnKeyListener(keyListener)

    progressDialog.setMessage(context.getString(messageId))

    progressDialog.show()
}

fun hideProgressDialog(progressDialog: ProgressDialog) {
    progressDialog.dismiss()
}