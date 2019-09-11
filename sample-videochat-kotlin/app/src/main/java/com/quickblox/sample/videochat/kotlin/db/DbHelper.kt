package com.quickblox.sample.videochat.kotlin.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

private val DB_NAME = "groupchatwebrtcDB"
val DB_TABLE_NAME = "users"
val DB_COLUMN_ID = "ID"
val DB_COLUMN_USER_FULL_NAME = "userFullName"
val DB_COLUMN_USER_LOGIN = "userLogin"
val DB_COLUMN_USER_ID = "userID"
val DB_COLUMN_USER_PASSWORD = "userPass"
val DB_COLUMN_USER_TAG = "userTag"

class DbHelper(context: Context) : SQLiteOpenHelper(context, DB_NAME, null, 1) {

    private val TAG = DbHelper::class.java.simpleName

    override fun onCreate(db: SQLiteDatabase) {
        Log.d(TAG, "--- onCreate database ---")
        db.execSQL("create table " + DB_TABLE_NAME + " ("
                + DB_COLUMN_ID + " integer primary key autoincrement,"
                + DB_COLUMN_USER_ID + " integer,"
                + DB_COLUMN_USER_LOGIN + " text,"
                + DB_COLUMN_USER_PASSWORD + " text,"
                + DB_COLUMN_USER_FULL_NAME + " text,"
                + DB_COLUMN_USER_TAG + " text"
                + ");")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {

    }
}