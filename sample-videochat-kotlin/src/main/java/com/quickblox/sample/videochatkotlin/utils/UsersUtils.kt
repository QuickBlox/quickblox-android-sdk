package com.quickblox.sample.videochatkotlin.utils

import com.quickblox.users.model.QBUser
import java.util.ArrayList

fun getListAllUsersFromIds(opponentsIds: List<Int>): ArrayList<QBUser> {
    val qbUsers = ArrayList<QBUser>()

    for (userId in opponentsIds) {
        val stubUser = createStubUserById(userId)
        qbUsers.add(stubUser)
    }

    return qbUsers
}

private fun createStubUserById(userId: Int?): QBUser {
    val stubUser = QBUser(userId)
    stubUser.fullName = userId.toString()
    return stubUser
}