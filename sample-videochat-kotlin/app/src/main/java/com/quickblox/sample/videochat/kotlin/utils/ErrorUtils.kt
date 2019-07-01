package com.quickblox.sample.videochat.kotlin.utils

import android.view.View
import androidx.annotation.StringRes
import com.google.android.material.snackbar.Snackbar
import com.quickblox.sample.videochat.kotlin.App
import com.quickblox.sample.videochat.kotlin.R

private const val EMPTY_STRING = ""
private val NO_CONNECTION_ERROR = App.getInstance().getString(R.string.error_connection)
private val NO_RESPONSE_TIMEOUT = App.getInstance().getString(R.string.error_response_timeout)

private val NO_INTERNET_CONNECTION = App.getInstance().getString(R.string.no_internet_connection)

fun showErrorSnackbar(view: View, @StringRes errorMessage: Int, e: Exception?,
                      @StringRes actionLabel: Int, clickListener: View.OnClickListener): Snackbar {
    val error = if (e == null) {
        EMPTY_STRING
    } else {
        e.message as String
    }
    val noConnection = NO_CONNECTION_ERROR == error
    val timeout = error.startsWith(NO_RESPONSE_TIMEOUT)
    val snackbar: Snackbar
    if (noConnection || timeout) {
        snackbar = showErrorSnackbar(view, NO_INTERNET_CONNECTION, actionLabel, clickListener)
    } else if (errorMessage == 0) {
        snackbar = showErrorSnackbar(view, error, actionLabel, clickListener)
    } else if (error == EMPTY_STRING) {
        snackbar = showErrorSnackbar(view, errorMessage, NO_INTERNET_CONNECTION, actionLabel, clickListener)
    } else {
        snackbar = showErrorSnackbar(view, errorMessage, error, actionLabel, clickListener)
    }
    return snackbar
}

fun showErrorSnackbar(view: View, @StringRes errorMessage: Int, error: String,
                      @StringRes actionLabel: Int, clickListener: View.OnClickListener): Snackbar {
    val errorMessageString = App.getInstance().getString(errorMessage)
    val message = String.format("%s: %s", errorMessageString, error)
    return showErrorSnackbar(view, message, actionLabel, clickListener)
}

fun showErrorSnackbar(view: View, message: String, @StringRes actionLabel: Int,
                      clickListener: View.OnClickListener?): Snackbar {
    val snackbar = Snackbar.make(view, message.trim { it <= ' ' }, Snackbar.LENGTH_INDEFINITE)
    if (clickListener != null) {
        snackbar.setAction(actionLabel, clickListener)
    }
    snackbar.show()
    return snackbar
}