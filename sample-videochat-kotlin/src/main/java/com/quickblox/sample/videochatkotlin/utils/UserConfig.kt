package com.quickblox.sample.videochatkotlin.utils

import android.util.Log
import com.quickblox.sample.core.utils.configs.ConfigParser
import com.quickblox.users.model.QBUser
import org.json.JSONException
import java.io.IOException


/**
 * Created by Roman on 08.04.2018.
 */

fun getAllUsersFromFile(fileName: String): ArrayList<QBUser> {
    val qbUsers = ArrayList<QBUser>()
    var userLogin: String
    var userPassword: String
    var userId: Int
    try {
        val json = ConfigParser().getConfigsAsJson(fileName)
        val keys = json.keys()
        while (keys.hasNext()) {
            val loginField = keys.next()
            val passwordField = keys.next()
            val idField = keys.next()
            userLogin = json.getString(loginField)
            userPassword = json.getString(passwordField)
            userId = json.getInt(idField)
            val qbUser = QBUser(userLogin, userPassword)
            qbUser.id = userId
            qbUsers.add(qbUser)
            Log.d("AMBRA", "userLogin= $userLogin, userPassword= $userPassword, userId= $userId")
        }
    } catch (e: JSONException) {
        e.printStackTrace()
    }

    return qbUsers
}

fun getUserFromFile(fileName: String, userLoginField: String, userPasswordField: String): QBUser? {
    var qbUser: QBUser? = null

    val userLogin: String
    val userPassword: String

    try {
        val configs = ConfigParser().getConfigsAsJson(fileName)
        userLogin = configs.getString(userLoginField)
        userPassword = configs.getString(userPasswordField)
        qbUser = QBUser(userLogin, userPassword)
    } catch (e: IOException) {
        e.printStackTrace()
    } catch (e: JSONException) {
        e.printStackTrace()
    }

    return qbUser
}