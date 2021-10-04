package com.quickblox.sample.conference.kotlin.presentation.utils

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager

/*
 * Created by Injoit in 2021-09-30.
 * Copyright © 2021 Quickblox. All rights reserved.
 */
class KeyboardUtils {
    companion object {
        fun hideKeyboard(view: View?) {
            view?.post {
                view.context
                view.clearFocus()
                val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(view.windowToken, 0)
            }
        }

        fun showKeyboard(view: View?) {
            view?.post {
                view.requestFocus()
                val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
            }
        }
    }
}