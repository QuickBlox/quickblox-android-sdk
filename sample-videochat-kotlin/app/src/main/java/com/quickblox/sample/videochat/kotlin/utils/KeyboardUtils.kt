package com.quickblox.sample.videochat.kotlin.utils

import android.content.Context
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import com.quickblox.sample.videochat.kotlin.App

fun showKeyboard(editText: EditText) {
    val imm = App.getInstance().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
}

fun hideKeyboard(editText: EditText) {
    val imm = App.getInstance().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(editText.windowToken, 0)
}