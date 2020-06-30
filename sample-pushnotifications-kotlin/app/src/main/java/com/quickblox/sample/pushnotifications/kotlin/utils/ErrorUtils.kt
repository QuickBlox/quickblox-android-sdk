package com.quickblox.sample.pushnotifications.kotlin.utils

import android.view.View
import androidx.annotation.StringRes
import com.google.android.material.snackbar.Snackbar
import com.quickblox.sample.pushnotifications.kotlin.App
import com.quickblox.sample.pushnotifications.kotlin.R

private val NO_CONNECTION_ERROR = App.getInstance().getString(R.string.error_connection)
private val NO_RESPONSE_TIMEOUT = App.getInstance().getString(R.string.error_response_timeout)
private val NO_INTERNET_CONNECTION = App.getInstance().getString(R.string.no_internet_connection)

fun showSnackbar(view: View, @StringRes errorMessage: Int, e: Exception?,
                 @StringRes actionLabel: Int, clickListener: View.OnClickListener?): Snackbar {
    val error = if (e == null) "" else e.message
    val noConnection = NO_CONNECTION_ERROR == error
    val timeout = error!!.startsWith(NO_RESPONSE_TIMEOUT)
    return if (noConnection || timeout) {
        showSnackbar(view, NO_INTERNET_CONNECTION, actionLabel, clickListener)
    } else if (errorMessage == 0) {
        showSnackbar(view, error, actionLabel, clickListener)
    } else if (error == "") {
        showSnackbar(view, errorMessage, NO_INTERNET_CONNECTION, actionLabel, clickListener)
    } else {
        showSnackbar(view, errorMessage, error, actionLabel, clickListener)
    }
}

private fun showSnackbar(view: View, @StringRes errorMessage: Int, error: String,
                         @StringRes actionLabel: Int, clickListener: View.OnClickListener?): Snackbar {
    val errorMessageString = App.getInstance().getString(errorMessage)
    val message = String.format("%s: %s", errorMessageString, error)
    return showSnackbar(view, message, actionLabel, clickListener)
}

private fun showSnackbar(view: View, message: String, @StringRes actionLabel: Int,
                         clickListener: View.OnClickListener?): Snackbar {
    val snackbar = Snackbar.make(view, message.trim { it <= ' ' }, Snackbar.LENGTH_INDEFINITE)
    clickListener?.let {
        snackbar.setAction(actionLabel, clickListener)
    }
    snackbar.show()
    return snackbar
}