package com.quickblox.sample.videochatkotlin.utils

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.quickblox.sample.core.utils.configs.ConfigParser
import com.quickblox.users.model.QBUser
import org.json.JSONException

/**
 * Created by Roman on 08.04.2018.
 */

fun getAllUsersFromFile(fileName: String): ArrayList<QBUser> {
    val qbUsers = ArrayList<QBUser>()
    val json = Gson().fromJson(ConfigParser().getConfigsAsJsonString(fileName), JsonElement::class.java)
    try {
        val jsonObject = json.asJsonObject
        val iterator = jsonObject.keySet().iterator()
        while (iterator.hasNext()) {
            val loginField = iterator.next()
            val password = jsonObject.get(loginField).asString
            val qbUser = QBUser(loginField, password)
            qbUsers.add(qbUser)
        }
    } catch (e: JSONException) {
        e.printStackTrace()
    }
    return qbUsers
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