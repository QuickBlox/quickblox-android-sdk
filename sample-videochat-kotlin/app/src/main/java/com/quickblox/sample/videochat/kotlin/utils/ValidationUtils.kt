package com.quickblox.sample.videochat.kotlin.utils

import android.content.Context
import android.text.TextUtils
import android.widget.EditText
import com.quickblox.sample.videochat.kotlin.R
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.util.regex.Pattern

private fun isEnteredTextValid(context: Context, editText: EditText, resFieldName: Int, maxLength: Int, checkName: Boolean): Boolean {
    var isCorrect = false
    val p: Pattern
    if (checkName) {
        p = Pattern.compile("^[a-zA-Z][a-zA-Z0-9]{2," + (maxLength - 1) + "}+$")
    } else {
        p = Pattern.compile("^[a-zA-Z][a-zA-Z 0-9]{2," + (maxLength - 1) + "}+$")
    }
    if (editText.text.toString().isNotBlank()) {
        val m = p.matcher(editText.text.toString().trim { it <= ' ' })
        isCorrect = m.matches()
    }
    if (!isCorrect) {
        editText.error = String.format(context.getString(R.string.error_name_must_not_contain_special_characters_from_app),
                context.getString(resFieldName),
                maxLength)
        return false
    } else {
        return true
    }
}

fun isLoginValid(context: Context, editText: EditText): Boolean {
    return isEnteredTextValid(context, editText, R.string.field_name_user_login, MAX_LOGIN_LENGTH, true)
}

fun isFoolNameValid(context: Context, editText: EditText): Boolean {
    return isEnteredTextValid(context, editText, R.string.field_name_user_fullname, MAX_FULLNAME_LENGTH, false)
}

fun isMiUi(): Boolean {
    return !TextUtils.isEmpty(getSystemProperty("ro.miui.ui.version.name")) ||
            !TextUtils.isEmpty(getSystemProperty("ro.miui.ui.version.code"))
}

fun getSystemProperty(propName: String): String? {
    val line: String
    var input: BufferedReader? = null
    try {
        val p = Runtime.getRuntime().exec("getprop $propName")
        input = BufferedReader(InputStreamReader(p.inputStream), 1024)
        line = input.readLine()
        input.close()
    } catch (ex: IOException) {
        return null
    } finally {
        if (input != null) {
            try {
                input.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }
    }
    return line
}