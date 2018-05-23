package com.quickblox.sample.videochatkotlin.utils

import android.content.Context
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.quickblox.users.model.QBUser
import org.json.JSONException
import java.io.BufferedReader
import java.io.IOException

/**
 * Created by Roman on 08.04.2018.
 */

fun getAllUsersFromFile(fileName: String, context: Context): ArrayList<QBUser> {
    val qbUsers = ArrayList<QBUser>()
    val json = Gson().fromJson(getConfigsAsJsonString(fileName, context), JsonElement::class.java)
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

fun getIdsSelectedOpponents(selectedUsers: Collection<QBUser>): ArrayList<Int> {
    val opponentsIds = ArrayList<Int>()
    if (!selectedUsers.isEmpty()) {
        for (qbUser in selectedUsers) {
            opponentsIds.add(qbUser.id)
        }
    }
    return opponentsIds
}

@Throws(IOException::class)
fun getConfigsAsJsonString(fileName: String, context: Context): String {
    return getJsonAsString(fileName, context)
}

@Throws(IOException::class)
fun getJsonAsString(filename: String, context: Context): String {
    val manager = context.assets
    val jsonInputStream = manager.open(filename)
    val allText = jsonInputStream.bufferedReader().use(BufferedReader::readText)

    jsonInputStream.close()

    return allText
}