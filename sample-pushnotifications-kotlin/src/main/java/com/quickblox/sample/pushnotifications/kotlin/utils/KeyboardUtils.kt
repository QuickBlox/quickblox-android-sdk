package com.quickblox.sample.pushnotifications.kotlin.utils

import android.content.Context
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import com.quickblox.sample.pushnotifications.kotlin.App

fun showKeyboard(editText: EditText) {
    val imm = App.instanceApp.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
}

fun hideKeyboard(editText: EditText) {
    val imm = App.instanceApp.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(editText.windowToken, 0)
}