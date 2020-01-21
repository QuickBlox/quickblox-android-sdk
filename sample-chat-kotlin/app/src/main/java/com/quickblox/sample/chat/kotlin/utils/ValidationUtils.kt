package com.quickblox.sample.chat.kotlin.utils

import android.content.Context
import android.util.Patterns
import android.widget.EditText
import com.quickblox.chat.model.QBAttachment
import com.quickblox.sample.chat.kotlin.R
import java.util.regex.Pattern

private const val MAX_LOGIN_LENGTH = 50
private const val MAX_FULLNAME_LENGTH = 20
private const val MIN_DIALOGNAME_LENGTH = 3
private const val MAX_DIALOGNAME_LENGTH = 20

private fun isEnteredTextValid(context: Context, editText: EditText, resFieldName: Int, maxLength: Int, checkLogin: Boolean): Boolean {
    var isCorrect = false
    val patternLogin = Pattern.compile("^[a-zA-Z][a-zA-Z0-9]{2," + (maxLength - 1) + "}+$")
    val patternEmail =  Patterns.EMAIL_ADDRESS
    val patternUsername = Pattern.compile("^[a-zA-Z][a-zA-Z 0-9]{2," + (maxLength - 1) + "}+$")

    if (checkLogin) {
        val matcherLogin = patternLogin.matcher(editText.text.toString().trim { it <= ' '})
        val matcherEmail = patternEmail.matcher(editText.text.toString().trim { it <= ' '})
        if (matcherLogin.matches() || (matcherEmail.matches() && editText.text.toString().trim().length < MAX_LOGIN_LENGTH)) {
            isCorrect = true
        }
    } else {
        val matcherUsername = patternUsername.matcher(editText.text.toString().trim { it <= ' '})
        if (matcherUsername.matches()) {
            isCorrect = true
        }
    }

    return isCorrect
}

fun isLoginValid(context: Context, editText: EditText): Boolean {
    return isEnteredTextValid(context, editText, R.string.field_name_user_login, MAX_LOGIN_LENGTH, true)
}

fun isFullNameValid(context: Context, editText: EditText): Boolean {
    return isEnteredTextValid(context, editText, R.string.field_name_user_fullname, MAX_FULLNAME_LENGTH, false)
}

fun isDialogNameValid(editText: EditText) : Boolean {
    var isCorrect = false
    val string = editText.text.toString().trim { it <= ' '}
    if (string.length in MIN_DIALOGNAME_LENGTH..MAX_DIALOGNAME_LENGTH) {
        isCorrect = true
    }
    return isCorrect
}

fun isAttachmentValid(attachment: QBAttachment?) : Boolean {
    var result = false
    if (attachment!= null && !attachment.name.isNullOrEmpty() && attachment.type.isNotEmpty()) {
        result = true
    }
    return result
}