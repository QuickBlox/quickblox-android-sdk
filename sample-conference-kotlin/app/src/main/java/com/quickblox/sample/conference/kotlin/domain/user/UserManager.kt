package com.quickblox.sample.conference.kotlin.domain.user

import com.quickblox.core.request.QBPagedRequestBuilder
import com.quickblox.sample.conference.kotlin.domain.DomainCallback
import com.quickblox.users.model.QBUser

interface UserManager {
    fun signIn(login: String, fullName: String, callback: DomainCallback<QBUser, Exception>)
    fun signOut(callback: DomainCallback<Unit?, Exception>)
    fun signUp(login: String, fullName: String, callback: DomainCallback<QBUser?, Exception>)
    fun isSavedUser(): Boolean
    fun getCurrentUser(): QBUser?
    fun loadUsers(requestBuilder: QBPagedRequestBuilder, callback: DomainCallback<ArrayList<QBUser>, Exception>)
    fun loadUsersByQuery(query: String, requestBuilder: QBPagedRequestBuilder, callback: DomainCallback<ArrayList<QBUser>, Exception>)
    fun loadUsersByIds(userIds: Collection<Int>, callback: DomainCallback<ArrayList<QBUser>, Exception>)
    fun loadUserById(userId: Int, callback: DomainCallback<QBUser, Exception>)
    fun clearUser()
}