package com.quickblox.sample.conference.kotlin.domain.session

import com.quickblox.sample.conference.kotlin.domain.repositories.session.SessionRepository

/*
 * Created by Injoit in 2021-09-30.
 * Copyright © 2021 Quickblox. All rights reserved.
 */
class SessionManagerImpl(private val sessionRepository: SessionRepository) : SessionManager {
    override fun isValidSession(): Boolean {
        return sessionRepository.getCurrentSession().isValidActiveSession
    }
}