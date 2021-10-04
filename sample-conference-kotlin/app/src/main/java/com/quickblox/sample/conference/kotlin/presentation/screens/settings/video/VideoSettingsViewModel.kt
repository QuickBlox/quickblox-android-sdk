package com.quickblox.sample.conference.kotlin.presentation.screens.settings.video

import android.os.Bundle
import com.quickblox.sample.conference.kotlin.R
import com.quickblox.sample.conference.kotlin.domain.DomainCallback
import com.quickblox.sample.conference.kotlin.domain.chat.ChatManager
import com.quickblox.sample.conference.kotlin.domain.settings.SettingsManager
import com.quickblox.sample.conference.kotlin.domain.settings.entities.CallSettingsEntity
import com.quickblox.sample.conference.kotlin.domain.user.UserManager
import com.quickblox.sample.conference.kotlin.presentation.LiveData
import com.quickblox.sample.conference.kotlin.presentation.resources.ResourcesManager
import com.quickblox.sample.conference.kotlin.presentation.screens.base.BaseViewModel
import com.quickblox.users.model.QBUser
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/*
 * Created by Injoit in 2021-09-30.
 * Copyright © 2021 Quickblox. All rights reserved.
 */
@HiltViewModel
class VideoSettingsViewModel @Inject constructor(private val settingsManager: SettingsManager, resourcesManager: ResourcesManager,
                                                 userManager: UserManager, private val chatManager: ChatManager) : BaseViewModel() {
    val liveData = LiveData<Pair<Int, Any?>>()
    var currentUser: QBUser? = null
        private set
    var callSettings: CallSettingsEntity? = null
        private set
    private val resolutions = arrayListOf<Pair<String, Int>>()

    init {
        resolutions.add(Pair(resourcesManager.get().getString(R.string.HD), 0))
        resolutions.add(Pair(resourcesManager.get().getString(R.string.VGA), 1))
        resolutions.add(Pair(resourcesManager.get().getString(R.string.QVGA), 2))
        callSettings = settingsManager.loadCallSettings()
        currentUser = userManager.getCurrentUser()
    }

    override fun onStartView() {
        if (!chatManager.isLoggedInChat()) {
            loginToChat()
        }
    }

    override fun onStopApp() {
        chatManager.destroyChat()
    }

    private fun loginToChat() {
        currentUser?.let {
            chatManager.loginToChat(it, object : DomainCallback<Unit, Exception> {
                override fun onSuccess(result: Unit, bundle: Bundle?) {
                    // empty
                }

                override fun onError(error: Exception) {
                    // empty
                }
            })
        } ?: run {
            liveData.setValue(Pair(ViewState.SHOW_LOGIN_SCREEN, null))
        }
    }


    fun getResolutions(): List<Pair<String, Int>> {
        return resolutions
    }

    override fun onPauseView() {
        callSettings?.let { settingsManager.saveCallSettings(it) }
    }
}