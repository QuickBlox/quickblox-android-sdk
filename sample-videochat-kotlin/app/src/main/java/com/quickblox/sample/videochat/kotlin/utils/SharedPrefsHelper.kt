package com.quickblox.sample.videochat.kotlin.utils

import android.content.Context
import android.content.SharedPreferences
import android.text.TextUtils
import com.quickblox.core.helper.StringifyArrayList
import com.quickblox.sample.videochat.kotlin.App
import com.quickblox.users.model.QBUser


private const val SHARED_PREFS_NAME = "qb"
private const val QB_USER_ID = "qb_user_id"
private const val QB_USER_LOGIN = "qb_user_login"
private const val QB_USER_PASSWORD = "qb_user_password"
private const val QB_USER_FULL_NAME = "qb_user_full_name"
private const val QB_USER_TAGS = "qb_user_tags"

object SharedPrefsHelper {

    private var sharedPreferences: SharedPreferences = App.getInstance().getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE)

    fun delete(key: String) {
        if (sharedPreferences.contains(key)) {
            sharedPreferences.edit().remove(key).apply()
        }
    }

    fun save(key: String, value: Any?) {
        val editor = sharedPreferences.edit()
        when {
            value is Boolean -> editor.putBoolean(key, (value))
            value is Int -> editor.putInt(key, (value))
            value is Float -> editor.putFloat(key, (value))
            value is Long -> editor.putLong(key, (value))
            value is String -> editor.putString(key, value)
            value is Enum<*> -> editor.putString(key, value.toString())
            value != null -> throw RuntimeException("Attempting to save non-supported preference")
        }
        editor.apply()
    }

    fun saveQbUser(qbUser: QBUser) {
        save(QB_USER_ID, qbUser.id)
        save(QB_USER_LOGIN, qbUser.login)
        save(QB_USER_PASSWORD, qbUser.password)
        save(QB_USER_FULL_NAME, qbUser.fullName)
        save(QB_USER_TAGS, qbUser.tags.itemsAsString)
    }

    fun clearAllData() {
        sharedPreferences.edit().clear().apply()
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

    fun getQbUser(): QBUser {
        val id = get<Int>(QB_USER_ID)
        val login = get<String>(QB_USER_LOGIN)
        val password = get<String>(QB_USER_PASSWORD)
        val fullName = get<String>(QB_USER_FULL_NAME)
        val tagsInString = get<String>(QB_USER_TAGS)

        val tags: StringifyArrayList<String> = StringifyArrayList()

        if (!TextUtils.isEmpty(tagsInString)) {
            tags.add(*tagsInString.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray())
        }

        val user = QBUser(login, password)
        user.id = id
        user.fullName = fullName
        user.tags = tags
        return user
    }
}