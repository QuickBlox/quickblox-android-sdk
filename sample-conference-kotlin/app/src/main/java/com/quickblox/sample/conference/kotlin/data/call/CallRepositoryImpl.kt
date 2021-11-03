package com.quickblox.sample.conference.kotlin.data.call

import android.content.Context
import android.os.Handler
import android.os.Looper
import com.quickblox.conference.ConferenceClient
import com.quickblox.conference.ConferenceSession
import com.quickblox.conference.WsException
import com.quickblox.conference.callbacks.ConferenceEntityCallback
import com.quickblox.sample.conference.kotlin.data.DataCallBack
import com.quickblox.sample.conference.kotlin.domain.repositories.call.CallRepository
import com.quickblox.videochat.webrtc.QBRTCTypes

/*
 * Created by Injoit in 2021-09-30.
 * Copyright © 2021 Quickblox. All rights reserved.
 */
class CallRepositoryImpl(private val context: Context) : CallRepository {
    override fun createSession(userId: Int, callback: DataCallBack<ConferenceSession, Exception>) {
        val conferenceType = QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_VIDEO
            ConferenceClient.getInstance(context).createSession(userId, conferenceType, object : ConferenceEntityCallback<ConferenceSession> {
                override fun onSuccess(session: ConferenceSession) {
                    Handler(Looper.getMainLooper()).post {
                        callback.onSuccess(session, null)
                    }
                }

                override fun onError(exception: WsException) {
                    Handler(Looper.getMainLooper()).post {
                        callback.onError(exception)
                    }
                }
            })
    }
}