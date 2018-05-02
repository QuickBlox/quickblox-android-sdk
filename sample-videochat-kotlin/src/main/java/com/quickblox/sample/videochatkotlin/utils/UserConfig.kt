package com.quickblox.sample.videochatkotlin.utils

import android.util.Log
import com.quickblox.sample.core.utils.configs.ConfigParser
import com.quickblox.users.model.QBUser
import org.json.JSONException
import org.json.JSONObject


/**
 * Created by Roman on 08.04.2018.
 */

fun getAllUsersFromFile(fileName: String): ArrayList<QBUser> {
    val qbUsers = ArrayList<QBUser>()
    try {
        val json = ConfigParser().getConfigsAsJson(fileName)
        val keys = json.keys()
        while (keys.hasNext()) {
            val loginField = keys.next()
            val passwordField = keys.next()
            val qbUser = getUser(json, loginField, passwordField)
            qbUsers.add(qbUser!!)
        }
    } catch (e: JSONException) {
        e.printStackTrace()
    }

    return qbUsers
}

private fun getUser(json: JSONObject, loginField: String, passwordField: String): QBUser? {
    return QBUser(json.getString(loginField), json.getString(passwordField))
}

fun getIdsSelectedOpponents(selectedUsers: Collection<QBUser>): java.util.ArrayList<Int> {
    val opponentsIds = java.util.ArrayList<Int>()
    if (!selectedUsers.isEmpty()) {
        for (qbUser in selectedUsers) {
            opponentsIds.add(qbUser.id)
        }
    }

    return opponentsIds
}