package com.quickblox.sample.conference.kotlin.data.session

import com.quickblox.auth.session.QBSessionManager
import com.quickblox.sample.conference.kotlin.domain.repositories.session.SessionRepository

/*
 * Created by Injoit in 2021-09-30.
 * Copyright © 2021 Quickblox. All rights reserved.
 */
class SessionRepositoryImpl : SessionRepository {
    override fun getCurrentSession(): QBSessionManager {
        return QBSessionManager.getInstance()
    }
}