package com.quickblox.sample.videochatkotlin.utils

import android.os.Bundle
import com.quickblox.auth.session.QBSettings
import com.quickblox.chat.QBChatService
import com.quickblox.core.LogLevel
import com.quickblox.core.QBEntityCallback
import com.quickblox.core.exception.QBResponseException
import com.quickblox.users.QBUsers
import com.quickblox.users.model.QBUser

/**
 * Created by Roman on 28.04.2018.
 */
class ChatHelper private constructor() {

    private val qbChatService: QBChatService
    val currentUser: QBUser
        get() = QBChatService.getInstance().user


    private object Holder {
        val INSTANCE: ChatHelper = ChatHelper()
    }

    companion object {
        val instance: ChatHelper by lazy { Holder.INSTANCE }
    }

    val isLogged: Boolean
        get() = QBChatService.getInstance().isLoggedIn

    init {
        qbChatService = QBChatService.getInstance()
        QBSettings.getInstance().logLevel = LogLevel.DEBUG
        QBChatService.setDebugEnabled(true)
        QBChatService.setConfigurationBuilder(buildChatConfigs())
    }

    private fun buildChatConfigs(): QBChatService.ConfigurationBuilder {
        val configurationBuilder = QBChatService.ConfigurationBuilder()
        configurationBuilder.socketTimeout = 0
        return configurationBuilder
    }


    fun login(user: QBUser, callback: QBEntityCallback<Void>) {
        // Create REST API session on QuickBlox
        QBUsers.signIn(user).performAsync(object : QBEntityCallback<QBUser> {
            override fun onSuccess(qbUser: QBUser, args: Bundle) {
                user.setId(qbUser.id!!)
                loginToChat(user, callback)
            }
            override fun onError(ex: QBResponseException?) {
                callback.onError(ex)
            }
        })
    }

    fun loginToChat(user: QBUser, callback: QBEntityCallback<Void>) {
        if (isLogged) {
            callback.onSuccess(null, null)
            return
        }
        qbChatService.login(user, callback)
    }

    fun destroy() {
        qbChatService.destroy()
    }
}