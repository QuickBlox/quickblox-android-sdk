package com.quickblox.sample.conference.kotlin.data.db

import android.content.Context
import androidx.core.content.edit
import com.quickblox.sample.conference.kotlin.domain.repositories.db.DBRepository
import com.quickblox.users.model.QBUser

private const val QB_PREF = "qb_pref"
private const val USER_ID = "user_id"
private const val USER_LOGIN = "user_login"
private const val USER_PASSWORD = "user_password"
private const val USER_FULL_NAME = "user_full_name"

/*
 * Created by Injoit in 2021-09-30.
 * Copyright © 2021 Quickblox. All rights reserved.
 */
class DBRepositoryImpl(context: Context) : DBRepository {
    private val sharedPreferences = context.getSharedPreferences(QB_PREF, Context.MODE_PRIVATE)

    override fun saveUser(qbUser: QBUser) {
        sharedPreferences.edit {
            putInt(USER_ID, qbUser.id)
            putString(USER_LOGIN, qbUser.login)
            putString(USER_PASSWORD, qbUser.password)
            putString(USER_FULL_NAME, qbUser.fullName)
        }
    }

    override fun removeUser() {
        sharedPreferences.edit {
            remove(USER_ID).commit()
            remove(USER_LOGIN).commit()
            remove(USER_PASSWORD).commit()
            remove(USER_FULL_NAME).commit()
        }
    }

    override fun getCurrentUser(): QBUser? {
        if (!sharedPreferences.contains(USER_LOGIN)) {
            return null
        }
        val id = sharedPreferences.getInt(USER_ID, 0)
        val login = sharedPreferences.getString(USER_LOGIN, "")
        val password = sharedPreferences.getString(USER_PASSWORD, "")
        val fullName = sharedPreferences.getString(USER_FULL_NAME, "")
        val user = QBUser(login, password)
        user.id = id
        user.fullName = fullName
        return user
    }

    override fun clearAllData() {
        sharedPreferences.edit().clear().apply()
    }
}