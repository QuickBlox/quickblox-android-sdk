package com.quickblox.sample.chat.kotlin.utils

import android.content.Context
import android.content.SharedPreferences
import android.text.TextUtils
import com.quickblox.core.helper.StringifyArrayList
import com.quickblox.sample.chat.kotlin.App
import com.quickblox.users.model.QBUser

private const val SHARED_PREFS_NAME = "qb"
private const val QB_USER_ID = "qb_user_id"
private const val QB_USER_LOGIN = "qb_user_login"
private const val QB_USER_PASSWORD = "qb_user_password"
private const val QB_USER_FULL_NAME = "qb_user_full_name"
private const val QB_USER_TAGS = "qb_user_tags"

object SharedPrefsHelper {
    private var sharedPreferences: SharedPreferences =
            App.instance.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE)

    fun save(key: String, value: Any?) {
        val editor = sharedPreferences.edit()
        when {
            value is Boolean -> editor.putBoolean(key, (value as Boolean?)!!)
            value is Int -> editor.putInt(key, (value as Int?)!!)
            value is Float -> editor.putFloat(key, (value as Float?)!!)
            value is Long -> editor.putLong(key, (value as Long?)!!)
            value is String -> editor.putString(key, value as String?)
            value is Enum<*> -> editor.putString(key, value.toString())
            value != null -> throw RuntimeException("Attempting to save non-supported preference")
        }
        editor.apply()
    }

    fun delete(key: String) {
        if (sharedPreferences.contains(key)) {
            getEditor().remove(key).commit()
        }
    }

    private fun getEditor(): SharedPreferences.Editor {
        return sharedPreferences.edit()
    }

    fun saveQbUser(qbUser: QBUser) {
        save(QB_USER_ID, qbUser.id)
        save(QB_USER_LOGIN, qbUser.login)
        save(QB_USER_PASSWORD, qbUser.password)
        save(QB_USER_FULL_NAME, qbUser.fullName)
        save(QB_USER_TAGS, qbUser.tags.itemsAsString)
    }

    fun removeQbUser() {
        delete(QB_USER_ID)
        delete(QB_USER_LOGIN)
        delete(QB_USER_PASSWORD)
        delete(QB_USER_FULL_NAME)
        delete(QB_USER_TAGS)
    }

    fun hasQbUser(): Boolean {
        return has(QB_USER_LOGIN) && has(QB_USER_PASSWORD)
    }

    @Suppress("UNCHECKED_CAST")
    operator fun <T> get(key: String): T {
        return sharedPreferences.all[key] as T
    }

    @Suppress("UNCHECKED_CAST")
    operator fun <T> get(key: String, defValue: T): T {
        val returnValue = sharedPreferences.all[key] as T
        return returnValue ?: defValue
    }

    private fun has(key: String): Boolean {
        return sharedPreferences.contains(key)
    }

    fun getQbUser(): QBUser? {
        if (hasQbUser()) {
            val id = get<Int>(QB_USER_ID)
            val login = get<String>(QB_USER_LOGIN)
            val password = get<String>(QB_USER_PASSWORD)
            val fullName = get<String>(QB_USER_FULL_NAME)
            val tagsInString = get<String>(QB_USER_TAGS)

            var tags: StringifyArrayList<String>? = null

            if (!TextUtils.isEmpty(tagsInString)) {
                tags = StringifyArrayList()
                tags.add(*tagsInString.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray())
            }

            val user = QBUser(login, password)
            user.id = id
            user.fullName = fullName
            user.tags = tags
            return user
        } else {
            return null
        }
    }
}