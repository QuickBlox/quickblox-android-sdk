package com.quickblox.sample.videochat.kotlin.utils

import android.text.TextUtils
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.util.regex.Pattern

private fun checkTextByPattern(text: String, pattern: Pattern): Boolean {
    var isCorrect = false

    if (text.isNotBlank()) {
        val matcher = pattern.matcher(text.trim { it <= ' ' })
        isCorrect = matcher.matches()
    }
    return isCorrect
}

fun isLoginValid(login: String): Boolean {
    val pattern = Pattern.compile("^[a-zA-Z][a-zA-Z0-9]{2,49}+$")
    return checkTextByPattern(login, pattern)
}

fun isDisplayNameValid(displayName: String): Boolean {
    val pattern = Pattern.compile("^[a-zA-Z][a-zA-Z 0-9]{2,19}+$")
    return checkTextByPattern(displayName, pattern)
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