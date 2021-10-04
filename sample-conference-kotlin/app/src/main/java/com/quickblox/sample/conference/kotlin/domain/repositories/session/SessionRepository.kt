package com.quickblox.sample.conference.kotlin.domain.repositories.session

import com.quickblox.auth.session.QBSessionManager

/*
 * Created by Injoit in 2021-09-30.
 * Copyright © 2021 Quickblox. All rights reserved.
 */
interface SessionRepository {
    fun getCurrentSession(): QBSessionManager
}