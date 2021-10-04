package com.quickblox.sample.conference.kotlin.presentation.screens.splash

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.quickblox.sample.conference.kotlin.R
import com.quickblox.sample.conference.kotlin.data.service.CallService
import com.quickblox.sample.conference.kotlin.domain.DomainCallback
import com.quickblox.sample.conference.kotlin.domain.chat.ChatManager
import com.quickblox.sample.conference.kotlin.domain.repositories.connection.ConnectionRepository
import com.quickblox.sample.conference.kotlin.domain.repositories.connection.ConnectivityChangedListener
import com.quickblox.sample.conference.kotlin.domain.user.UserManager
import com.quickblox.sample.conference.kotlin.presentation.LiveData
import com.quickblox.sample.conference.kotlin.presentation.resources.ResourcesManager
import com.quickblox.sample.conference.kotlin.presentation.screens.base.BaseViewModel
import com.quickblox.users.model.QBUser
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

private const val SPLASH_DELAY = 1500L

/*
 * Created by Injoit in 2021-09-30.
 * Copyright © 2021 Quickblox. All rights reserved.
 */
@HiltViewModel
class SplashViewModel @Inject constructor(private val userManager: UserManager, private val chatManager: ChatManager,
                                          private val connectionRepository: ConnectionRepository, private val resourcesManager: ResourcesManager) : BaseViewModel() {
    private val TAG: String = SplashViewModel::class.java.simpleName
    val liveData = LiveData<Pair<Int, Any?>>()
    private val connectivityChangedListener = ConnectivityChangedListenerImpl(TAG)

    init {
        Handler(Looper.getMainLooper()).postDelayed({
            run()
        }, SPLASH_DELAY)
    }

    override fun onResumeView() {
        connectionRepository.addListener(connectivityChangedListener)
    }

    override fun onStopView() {
        connectionRepository.removeListener(connectivityChangedListener)
    }

    private fun run() {
        if (CallService.isRunning()) {
            liveData.setValue(Pair(ViewState.SHOW_CALL_SCREEN, null))
            return
        }
        if (chatManager.isLoggedInChat()) {
            liveData.setValue(Pair(ViewState.SHOW_MAIN_SCREEN, null))
        } else {
            if (userManager.getCurrentUser() == null) {
                liveData.setValue(Pair(ViewState.SHOW_LOGIN_SCREEN, null))
            } else {
                loginToChat(userManager.getCurrentUser())
            }
        }
    }

    private fun loginToChat(user: QBUser?) {
        chatManager.loginToChat(user, object : DomainCallback<Unit, Exception> {
            override fun onSuccess(result: Unit, bundle: Bundle?) {
                liveData.setValue(Pair(ViewState.SHOW_MAIN_SCREEN, null))
            }

            override fun onError(error: Exception) {
                liveData.setValue(Pair(ViewState.ERROR, error.message))
            }
        })
    }

    private inner class ConnectivityChangedListenerImpl(val tag: String) : ConnectivityChangedListener {
        override fun onAvailable() {
            run()
        }

        override fun onLost() {
            liveData.setValue(Pair(ViewState.ERROR, resourcesManager.get().getString(R.string.no_internet)))
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) {
                return true
            }
            if (other !is ConnectivityChangedListenerImpl) {
                return false
            }
            return tag == other.tag
        }

        override fun hashCode(): Int {
            var hash = 1
            hash = 31 * hash + tag.hashCode()
            return hash
        }
    }
}