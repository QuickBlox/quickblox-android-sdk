package com.quickblox.sample.conference.kotlin.domain.repositories.user

import android.os.Bundle
import com.quickblox.core.request.QBPagedRequestBuilder
import com.quickblox.sample.conference.kotlin.data.DataCallBack
import com.quickblox.users.model.QBUser
import java.util.*

/*
 * Created by Injoit in 2021-09-30.
 * Copyright © 2021 Quickblox. All rights reserved.
 */
interface UserRepository {
    fun signInSync(user: QBUser): QBUser
    fun signInAsync(user: QBUser, callback: DataCallBack<QBUser, Exception>)

    fun updateSync(user: QBUser): QBUser
    fun updateAsync(user: QBUser, callback: DataCallBack<QBUser, Exception>)

    fun signUpSync(user: QBUser): QBUser
    fun signUpAsync(user: QBUser, callback: DataCallBack<QBUser, Exception>)

    fun signOutSync(): Pair<Void, Bundle>
    fun signOutAsync(callback: DataCallBack<Unit?, Exception>)

    fun loadSync(requestBuilder: QBPagedRequestBuilder): ArrayList<QBUser>
    fun loadAsync(requestBuilder: QBPagedRequestBuilder, callback: DataCallBack<ArrayList<QBUser>, Exception>)

    fun loadByQuerySync(query: String, requestBuilder: QBPagedRequestBuilder): ArrayList<QBUser>
    fun loadByQueryAsync(query: String, requestBuilder: QBPagedRequestBuilder, callback: DataCallBack<ArrayList<QBUser>, Exception>)

    fun loadByIdsSync(userIds: Collection<Int>): ArrayList<QBUser>
    fun loadByIDsAsync(userIds: Collection<Int>, callback: DataCallBack<ArrayList<QBUser>, Exception>)

    fun loadByIdSync(userId: Int): QBUser
    fun loadByIDAsync(userId: Int, callback: DataCallBack<QBUser, Exception>)
}