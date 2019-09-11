package com.quickblox.sample.videochat.kotlin.util

import com.quickblox.core.QBEntityCallback
import com.quickblox.core.request.QBPagedRequestBuilder
import com.quickblox.users.QBUsers
import com.quickblox.users.model.QBUser
import java.util.*

fun signUp(newQbUser: QBUser, callback: QBEntityCallback<QBUser>) {
    QBUsers.signUp(newQbUser).performAsync(callback)
}

fun signInUser(currentQbUser: QBUser, callback: QBEntityCallback<QBUser>) {
    QBUsers.signIn(currentQbUser).performAsync(callback)
}

fun signOut() {
    QBUsers.signOut().performAsync(null)
}

fun deleteCurrentUser(currentQbUserID: Int, callback: QBEntityCallback<Void>) {
    QBUsers.deleteUser(currentQbUserID).performAsync(callback)
}

fun loadUsersByTag(tag: String, callback: QBEntityCallback<ArrayList<QBUser>>) {
    val requestBuilder = QBPagedRequestBuilder()
    requestBuilder.perPage = 50
    val tags = LinkedList<String>()
    tags.add(tag)
    QBUsers.getUsersByTags(tags, requestBuilder).performAsync(callback)
}

fun loadUsersByPagedRequestBuilder(callback: QBEntityCallback<ArrayList<QBUser>>, requestBuilder: QBPagedRequestBuilder) {
    QBUsers.getUsers(requestBuilder).performAsync(callback)
}

fun loadUsersByIds(usersIDs: Collection<Int>, callback: QBEntityCallback<ArrayList<QBUser>>) {
    QBUsers.getUsersByIDs(usersIDs, null).performAsync(callback)
}