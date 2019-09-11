package com.quickblox.sample.chat.kotlin.utils.qb

import android.util.SparseArray
import com.quickblox.users.model.QBUser
import java.util.*

/**
 * Basically in your app you should store userList in database
 * And load userList to memory on demand
 * We're using runtime SpaceArray holder just to simplify app logic
 */
object QbUsersHolder {
    private var qbUserSparseArray: SparseArray<QBUser> = SparseArray()

    fun putUsers(users: List<QBUser>) {
        for (user in users) {
            putUser(user)
        }
    }

    private fun putUser(user: QBUser) {
        qbUserSparseArray.put(user.id, user)
    }

    fun getUsersByIds(usersIds: List<Int>): List<QBUser> {
        val users = ArrayList<QBUser>()
        for (id in usersIds) {
            val user = getUserById(id)
            user?.let {
                users.add(user)
            }
        }
        return users
    }

    fun getUserById(id: Int): QBUser? {
        return qbUserSparseArray.get(id)
    }
}