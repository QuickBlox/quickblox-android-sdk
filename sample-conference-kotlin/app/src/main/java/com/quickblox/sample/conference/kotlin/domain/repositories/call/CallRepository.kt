package com.quickblox.sample.conference.kotlin.domain.repositories.call

import com.quickblox.conference.ConferenceSession
import com.quickblox.sample.conference.kotlin.data.DataCallBack

/*
 * Created by Injoit in 2021-09-30.
 * Copyright © 2021 Quickblox. All rights reserved.
 */
interface CallRepository {
    fun createSession(userId: Int, callback: DataCallBack<ConferenceSession, Exception>)
}