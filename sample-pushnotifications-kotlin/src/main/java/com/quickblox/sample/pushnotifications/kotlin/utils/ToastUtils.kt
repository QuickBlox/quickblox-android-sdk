package com.quickblox.sample.pushnotifications.kotlin.utils

import android.widget.Toast
import androidx.annotation.IntDef
import androidx.annotation.StringRes
import com.quickblox.sample.pushnotifications.kotlin.App

@IntDef(Toast.LENGTH_LONG, Toast.LENGTH_SHORT)
private annotation class ToastLength

fun shortToast(@StringRes text: Int) {
    shortToast(App.instanceApp.getString(text))
}

fun shortToast(text: String) {
    show(text, Toast.LENGTH_SHORT)
}

fun longToast(@StringRes text: Int) {
    longToast(App.instanceApp.getString(text))
}

fun longToast(text: String) {
    show(text, Toast.LENGTH_LONG)
}

private fun makeToast(text: String, @ToastLength length: Int): Toast {
    return Toast.makeText(App.instanceApp, text, length)
}

private fun show(text: String, @ToastLength length: Int) {
    makeToast(text, length).show()
}