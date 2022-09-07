package com.quickblox.sample.videochat.kotlin.utils

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import com.quickblox.sample.videochat.kotlin.App

fun showKeyboard(editText: EditText) {
    val imm = App.getInstance().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
}

fun hideKeyboard(view: View) {
    val imm = App.getInstance().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(view.windowToken, 0)
}