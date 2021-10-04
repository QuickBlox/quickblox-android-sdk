package com.quickblox.sample.conference.kotlin.presentation.utils

import android.util.Patterns
import android.widget.EditText
import java.util.regex.Pattern

private const val MAX_LOGIN_LENGTH = 50
private const val MAX_FULL_NAME_LENGTH = 20
private const val MIN_DIALOG_NAME_LENGTH = 3
private const val MAX_DIALOG_NAME_LENGTH = 20
private const val ONE_SPACE = "\\s{2,}"

/*
 * Created by Injoit in 2021-09-30.
 * Copyright © 2021 Quickblox. All rights reserved.
 */
fun EditText.isValidLogin(): Boolean {
    var isCorrect = false
    val patternLogin = Pattern.compile("^[a-zA-Z][a-zA-Z0-9]{2," + (MAX_LOGIN_LENGTH - 1) + "}+$")
    val patternEmail = Patterns.EMAIL_ADDRESS
    val matcherLogin = patternLogin.matcher(this.text.toString().trim { it <= ' ' })
    val matcherEmail = patternEmail.matcher(this.text.toString().trim { it <= ' ' })
    if (matcherLogin.matches() || matcherEmail.matches() && this.text.toString().trim { it <= ' ' }.length < MAX_LOGIN_LENGTH) {
        isCorrect = true
    }

    return isCorrect
}

fun EditText.isValidName(): Boolean {
    var isCorrect = false
    val patternUsername = Pattern.compile("^[a-zA-Z][a-zA-Z 0-9]{2," + (MAX_FULL_NAME_LENGTH - 1) + "}+$")
    val matcherUserName = patternUsername.matcher(this.text.toString().trim { it <= ' ' })
    if (matcherUserName.matches()) {
        isCorrect = true
    }

    return isCorrect
}

fun EditText?.isValidChatName(): Boolean {
    var isCorrect = false
    val string: String = this?.text.toString().trim { it <= ' ' }
    if (string.length in MIN_DIALOG_NAME_LENGTH..MAX_DIALOG_NAME_LENGTH) {
        isCorrect = true
    }

    return isCorrect
}

fun String.oneSpace(): String = this.replace(ONE_SPACE.toRegex(), " ")