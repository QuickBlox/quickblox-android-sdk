package com.quickblox.sample.conference.kotlin.data.user

import android.os.Bundle
import com.quickblox.auth.session.Query
import com.quickblox.core.QBEntityCallback
import com.quickblox.core.exception.QBResponseException
import com.quickblox.core.request.QBPagedRequestBuilder
import com.quickblox.sample.conference.kotlin.data.DataCallBack
import com.quickblox.sample.conference.kotlin.domain.repositories.user.UserRepository
import com.quickblox.users.QBUsers
import com.quickblox.users.model.QBUser
import java.util.*

private const val USERS_PER_PAGE = 100

/*
 * Created by Injoit in 2021-09-30.
 * Copyright © 2021 Quickblox. All rights reserved.
 */
class UserRepositoryImpl : UserRepository {
    @Throws(Exception::class)
    override fun signInSync(user: QBUser): QBUser {
        return QBUsers.signIn(user).perform()
    }

    override fun signInAsync(user: QBUser, callback: DataCallBack<QBUser, Exception>) {
        QBUsers.signIn(user).performAsync(object : QBEntityCallback<QBUser> {
            override fun onSuccess(userFromRest: QBUser, bundle: Bundle) {
                callback.onSuccess(userFromRest, bundle)
            }

            override fun onError(exception: QBResponseException) {
                callback.onError(Exception(exception.httpStatusCode.toString()))
            }
        })
    }

    @Throws(Exception::class)
    override fun updateSync(user: QBUser): QBUser {
        return QBUsers.updateUser(user).perform()
    }

    override fun updateAsync(user: QBUser, callback: DataCallBack<QBUser, Exception>) {
        QBUsers.updateUser(user).performAsync(object : QBEntityCallback<QBUser> {
            override fun onSuccess(user: QBUser, bundle: Bundle) {
                callback.onSuccess(user, bundle)
            }

            override fun onError(exception: QBResponseException) {
                callback.onError(exception)
            }
        })
    }

    @Throws(Exception::class)
    override fun signUpSync(user: QBUser): QBUser {
        return QBUsers.signUp(user).perform()
    }

    override fun signUpAsync(user: QBUser, callback: DataCallBack<QBUser, Exception>) {
        QBUsers.signUp(user).performAsync(object : QBEntityCallback<QBUser> {
            override fun onSuccess(userFromRest: QBUser, bundle: Bundle) {
                callback.onSuccess(userFromRest, bundle)
            }

            override fun onError(exception: QBResponseException) {
                callback.onError(exception)
            }
        })
    }

    @Throws(Exception::class)
    override fun signOutSync(): Pair<Void, Bundle> {
        val performer = QBUsers.signOut() as Query
        return Pair(performer.perform(), performer.bundle)
    }

    override fun signOutAsync(callback: DataCallBack<Unit?, Exception>) {
        QBUsers.signOut().performAsync(object : QBEntityCallback<Void?> {
            override fun onSuccess(aVoid: Void?, bundle: Bundle) {
                callback.onSuccess(Unit, bundle)
            }

            override fun onError(exception: QBResponseException) {
                callback.onError(exception)
            }
        })
    }

    @Throws(Exception::class)
    override fun loadSync(requestBuilder: QBPagedRequestBuilder): ArrayList<QBUser> {
        return QBUsers.getUsers(requestBuilder).perform()
    }

    override fun loadAsync(requestBuilder: QBPagedRequestBuilder, callback: DataCallBack<ArrayList<QBUser>, Exception>) {
        QBUsers.getUsers(requestBuilder).performAsync(object : QBEntityCallback<ArrayList<QBUser>> {
            override fun onSuccess(usersList: ArrayList<QBUser>, params: Bundle) {
                callback.onSuccess(usersList, params)
            }

            override fun onError(exception: QBResponseException) {
                callback.onError(exception)
            }
        })
    }

    @Throws(Exception::class)
    override fun loadByQuerySync(query: String, requestBuilder: QBPagedRequestBuilder): ArrayList<QBUser> {
        return QBUsers.getUsersByFullName(query, requestBuilder).perform()
    }

    override fun loadByQueryAsync(query: String, requestBuilder: QBPagedRequestBuilder, callback: DataCallBack<ArrayList<QBUser>, Exception>) {
        QBUsers.getUsersByFullName(query, requestBuilder).performAsync(object : QBEntityCallback<ArrayList<QBUser>> {
            override fun onSuccess(usersList: ArrayList<QBUser>, params: Bundle) {
                callback.onSuccess(usersList, params)
            }

            override fun onError(exception: QBResponseException) {
                callback.onError(exception)
            }
        })
    }

    @Throws(Exception::class)
    override fun loadByIdsSync(userIds: Collection<Int>): ArrayList<QBUser> {
        val requestBuilder = QBPagedRequestBuilder(USERS_PER_PAGE, 1)
        return QBUsers.getUsersByIDs(userIds, requestBuilder).perform()
    }

    override fun loadByIDsAsync(userIds: Collection<Int>, callback: DataCallBack<ArrayList<QBUser>, Exception>) {
        val requestBuilder = QBPagedRequestBuilder(USERS_PER_PAGE, 1)
        QBUsers.getUsersByIDs(userIds, requestBuilder).performAsync(object : QBEntityCallback<ArrayList<QBUser>> {
            override fun onSuccess(usersList: ArrayList<QBUser>, params: Bundle) {
                callback.onSuccess(usersList, params)
            }

            override fun onError(exception: QBResponseException) {
                callback.onError(exception)
            }
        })
    }

    @Throws(Exception::class)
    override fun loadByIdSync(userId: Int): QBUser {
        return QBUsers.getUser(userId).perform()
    }

    override fun loadByIDAsync(userId: Int, callback: DataCallBack<QBUser, Exception>) {
        QBUsers.getUser(userId).performAsync(object : QBEntityCallback<QBUser> {
            override fun onSuccess(usersList: QBUser, params: Bundle) {
                callback.onSuccess(usersList, params)
            }

            override fun onError(exception: QBResponseException) {
                callback.onError(exception)
            }
        })
    }
}