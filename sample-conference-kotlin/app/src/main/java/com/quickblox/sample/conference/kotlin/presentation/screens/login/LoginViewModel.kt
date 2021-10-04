package com.quickblox.sample.conference.kotlin.presentation.screens.login

import android.os.Bundle
import com.quickblox.core.exception.QBResponseException
import com.quickblox.sample.conference.kotlin.domain.DomainCallback
import com.quickblox.sample.conference.kotlin.domain.chat.ChatManager
import com.quickblox.sample.conference.kotlin.domain.user.UNAUTHORIZED_CODE
import com.quickblox.sample.conference.kotlin.domain.user.UserManager
import com.quickblox.sample.conference.kotlin.presentation.LiveData
import com.quickblox.sample.conference.kotlin.presentation.screens.base.BaseViewModel
import com.quickblox.sample.conference.kotlin.presentation.screens.login.ViewState.Companion.ERROR
import com.quickblox.users.model.QBUser
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/*
 * Created by Injoit in 2021-09-30.
 * Copyright © 2021 Quickblox. All rights reserved.
 */
@HiltViewModel
class LoginViewModel @Inject constructor(private val userManager: UserManager, private val chatManager: ChatManager) : BaseViewModel() {
    val liveData = LiveData<Pair<Int, Any?>>()

    init {
        userManager.clearUser()
    }

    fun signIn(login: String, fullName: String) {
        liveData.setValue(Pair(ViewState.PROGRESS, null))

        userManager.signIn(login, fullName, object : DomainCallback<QBUser, Exception> {
            override fun onSuccess(result: QBUser, bundle: Bundle?) {
                loginToChat(result)
            }

            override fun onError(error: Exception) {
                if ((error as QBResponseException).httpStatusCode == UNAUTHORIZED_CODE) {
                    signUp(login, fullName)
                } else {
                    liveData.setValue(Pair(ViewState.ERROR, error.message))
                }
            }
        })
    }

    fun signUp(login: String, fullName: String) {
        userManager.signUp(login, fullName, object : DomainCallback<QBUser?, Exception> {
            override fun onSuccess(result: QBUser?, bundle: Bundle?) {
                signIn(login, fullName)
            }

            override fun onError(error: Exception) {
                liveData.setValue(Pair(ViewState.ERROR, error.message))
            }
        })
    }

    private fun loginToChat(user: QBUser) {
        liveData.setValue(Pair(ViewState.PROGRESS, null))
        chatManager.loginToChat(user, object : DomainCallback<Unit, Exception> {
            override fun onSuccess(result: Unit, bundle: Bundle?) {
                liveData.setValue(Pair(ViewState.SHOW_MAIN_SCREEN, null))
            }

            override fun onError(error: Exception) {
                liveData.setValue(Pair(ERROR, error.message))
            }
        })
    }
}