package com.quickblox.sample.conference.kotlin.domain.repositories.db

import com.quickblox.users.model.QBUser

/*
 * Created by Injoit in 2021-09-30.
 * Copyright © 2021 Quickblox. All rights reserved.
 */
interface DBRepository {
    fun saveUser(qbUser: QBUser)
    fun removeUser()
    fun getCurrentUser(): QBUser?
    fun clearAllData()
}