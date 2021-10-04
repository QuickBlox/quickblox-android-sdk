package com.quickblox.sample.conference.kotlin.presentation.utils

import android.text.Editable
import android.text.TextWatcher

/*
 * Created by Injoit in 2021-09-30.
 * Copyright © 2021 Quickblox. All rights reserved.
 */
open class SimpleTextWatcher : TextWatcher {
    override fun afterTextChanged(editable: Editable?) {
    }

    override fun beforeTextChanged(charSequence: CharSequence?, start: Int, count: Int, after: Int) {
    }

    override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {
    }
}