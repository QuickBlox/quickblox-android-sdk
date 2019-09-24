package com.quickblox.sample.videochat.kotlin.db

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import com.quickblox.core.helper.StringifyArrayList
import com.quickblox.sample.videochat.kotlin.App
import com.quickblox.users.model.QBUser
import java.util.*


object QbUsersDbManager {
    val TAG = QbUsersDbManager::class.java.simpleName

    val allUsers: ArrayList<QBUser>
        get() {
            val allUsers = ArrayList<QBUser>()
            val db = getDb()
            val cursor = db.query(DB_TABLE_NAME, null, null, null,
                    null, null, null)

            if (cursor.moveToFirst()) {
                val userIdColIndex = cursor.getColumnIndex(DB_COLUMN_USER_ID)
                val userLoginColIndex = cursor.getColumnIndex(DB_COLUMN_USER_LOGIN)
                val userPassColIndex = cursor.getColumnIndex(DB_COLUMN_USER_PASSWORD)
                val userFullNameColIndex = cursor.getColumnIndex(DB_COLUMN_USER_FULL_NAME)
                val userTagColIndex = cursor.getColumnIndex(DB_COLUMN_USER_TAG)

                do {
                    val qbUser = QBUser()

                    qbUser.fullName = cursor.getString(userFullNameColIndex)
                    qbUser.login = cursor.getString(userLoginColIndex)
                    qbUser.id = cursor.getInt(userIdColIndex)
                    qbUser.password = cursor.getString(userPassColIndex)

                    val tags = StringifyArrayList<String>()
                    tags.add(cursor.getString(userTagColIndex))
                    qbUser.tags = tags

                    allUsers.add(qbUser)
                } while (cursor.moveToNext())
            }

            cursor.close()
            db.close()
            return allUsers
        }

    fun getUserById(userId: Int?): QBUser? {
        var qbUser: QBUser? = null
        val db = getDb()
        val cursor = db.query(DB_TABLE_NAME, null, null, null,
                null, null, null)

        if (cursor.moveToFirst()) {
            val userIdColIndex = cursor.getColumnIndex(DB_COLUMN_USER_ID)
            val userLoginColIndex = cursor.getColumnIndex(DB_COLUMN_USER_LOGIN)
            val userPassColIndex = cursor.getColumnIndex(DB_COLUMN_USER_PASSWORD)
            val userFullNameColIndex = cursor.getColumnIndex(DB_COLUMN_USER_FULL_NAME)
            val userTagColIndex = cursor.getColumnIndex(DB_COLUMN_USER_TAG)

            do {
                if (cursor.getInt(userIdColIndex) == userId) {
                    qbUser = QBUser()
                    qbUser.fullName = cursor.getString(userFullNameColIndex)
                    qbUser.login = cursor.getString(userLoginColIndex)
                    qbUser.id = cursor.getInt(userIdColIndex)
                    qbUser.password = cursor.getString(userPassColIndex)

                    val tags = StringifyArrayList<String>()

                    tags.add(*cursor.getString(userTagColIndex)
                            .split(",".toRegex())
                            .dropLastWhile { it.isEmpty() }
                            .toTypedArray())

                    qbUser.tags = tags
                    break
                }
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()
        return qbUser
    }

    fun saveAllUsers(allUsers: ArrayList<QBUser>, needRemoveOldData: Boolean) {
        if (needRemoveOldData) {
            clearDB()
        }

        for (qbUser in allUsers) {
            saveUser(qbUser)
        }
        Log.d(TAG, "saveAllUsers")
    }

    fun saveUser(qbUser: QBUser) {
        val cv = ContentValues()

        cv.put(DB_COLUMN_USER_FULL_NAME, qbUser.fullName)
        cv.put(DB_COLUMN_USER_LOGIN, qbUser.login)
        cv.put(DB_COLUMN_USER_ID, qbUser.id)
        cv.put(DB_COLUMN_USER_PASSWORD, qbUser.password)
        cv.put(DB_COLUMN_USER_TAG, qbUser.tags.itemsAsString)

        val db = getDb()
        db.insert(DB_TABLE_NAME, null, cv)
        db.close()
    }

    fun clearDB() {
        val db = getDb()
        db.delete(DB_TABLE_NAME, null, null)
        db.close()
    }

    fun getUsersByIds(usersIds: List<Int>): ArrayList<QBUser> {
        val qbUsers = ArrayList<QBUser>()

        for (userId in usersIds) {
            if (getUserById(userId) != null) {
                val user = getUserById(userId)
                user?.let {
                    qbUsers.add(it)
                }
            }
        }
        return qbUsers
    }

    private fun getDb(): SQLiteDatabase {
        return App.getInstance().getDbHelper().writableDatabase
    }
}