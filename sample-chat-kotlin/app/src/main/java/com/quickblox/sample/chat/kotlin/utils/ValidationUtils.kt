package com.quickblox.sample.chat.kotlin.utils

import android.content.Context
import android.widget.EditText
import com.quickblox.sample.chat.kotlin.R
import java.util.regex.Pattern

private const val MAX_LOGIN_LENGTH = 15
private const val MAX_FULLNAME_LENGTH = 20

private fun isEnteredTextValid(context: Context, editText: EditText, resFieldName: Int, maxLength: Int, checkName: Boolean): Boolean {
    var isCorrect = false
    val p: Pattern
    if (checkName) {
        p = Pattern.compile("^[a-zA-Z][a-zA-Z 0-9]{2," + (maxLength - 1) + "}+$")
    } else {
        p = Pattern.compile("^[a-zA-Z][a-zA-Z0-9]{2," + (maxLength - 1) + "}+$")
    }
    if (editText.text.toString().isNotBlank()) {
        val m = p.matcher(editText.text.toString().trim { it <= ' ' })
        isCorrect = m.matches()
    }
    if (!isCorrect) {
        editText.error = String.format(context.getString(R.string.error_name_must_not_contain_special_characters_from_app),
                context.getString(resFieldName), maxLength)
        editText.requestFocus()
        return false
    } else {
        return true
    }
}

fun isLoginValid(context: Context, editText: EditText): Boolean {
    return isEnteredTextValid(context, editText, R.string.field_name_user_login, MAX_LOGIN_LENGTH, true)
}

fun isFullNameValid(context: Context, editText: EditText): Boolean {
    return isEnteredTextValid(context, editText, R.string.field_name_user_fullname, MAX_FULLNAME_LENGTH, false)
}