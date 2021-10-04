package com.quickblox.sample.conference.kotlin.presentation.screens.chatinfo

import android.os.Bundle
import com.quickblox.chat.model.QBChatDialog
import com.quickblox.sample.conference.kotlin.domain.DomainCallback
import com.quickblox.sample.conference.kotlin.domain.chat.ChatManager
import com.quickblox.sample.conference.kotlin.domain.repositories.db.DBRepository
import com.quickblox.sample.conference.kotlin.domain.user.UserManager
import com.quickblox.sample.conference.kotlin.presentation.LiveData
import com.quickblox.sample.conference.kotlin.presentation.screens.appinfo.ViewState
import com.quickblox.sample.conference.kotlin.presentation.screens.base.BaseViewModel
import com.quickblox.sample.conference.kotlin.presentation.screens.chatinfo.ViewState.Companion.DIALOG_LOADED
import com.quickblox.sample.conference.kotlin.presentation.screens.chatinfo.ViewState.Companion.ERROR
import com.quickblox.sample.conference.kotlin.presentation.screens.chatinfo.ViewState.Companion.PROGRESS
import com.quickblox.sample.conference.kotlin.presentation.screens.chatinfo.ViewState.Companion.SHOW_SEARCH_USER_SCREEN
import com.quickblox.sample.conference.kotlin.presentation.screens.chatinfo.ViewState.Companion.USERS_LOADED
import com.quickblox.sample.conference.kotlin.presentation.screens.chatinfo.ViewState.Companion.USERS_UPDATED
import com.quickblox.users.model.QBUser
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/*
 * Created by Injoit in 2021-09-30.
 * Copyright © 2021 Quickblox. All rights reserved.
 */
@HiltViewModel
class ChatInfoViewModel @Inject constructor(private val userManager: UserManager, private val chatManager: ChatManager, private val dbRepository: DBRepository) : BaseViewModel() {
    val liveData = LiveData<Pair<Int, Any?>>()
    val users = arrayListOf<QBUser>()
    var currentDialog: QBChatDialog? = null
        private set

    var currentUser: QBUser? = null
        private set

    init {
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
                    liveData.setValue(Pair(ERROR, error.message))
                }
            })
        } ?: run {
            liveData.setValue(Pair(ViewState.SHOW_LOGIN_SCREEN, null))
        }
    }

    fun getCurrentUserId(): Int? {
        return dbRepository.getCurrentUser()?.id
    }

    fun loadUsersByIds() {
        liveData.setValue(Pair(PROGRESS, null))

        currentDialog?.occupants?.let {
            userManager.loadUsersByIds(it, object : DomainCallback<ArrayList<QBUser>, Exception> {
                override fun onSuccess(result: ArrayList<QBUser>, bundle: Bundle?) {
                    users.addAll(result)
                    liveData.setValue(Pair(USERS_LOADED, null))
                }

                override fun onError(error: Exception) {
                    liveData.setValue(Pair(ERROR, error.message))
                }
            })
        }
    }

    fun loadDialogById(dialogId: String) {
        users.clear()
        liveData.setValue(Pair(PROGRESS, null))
        for (dialog in chatManager.getDialogs()) {
            if (dialog.dialogId == dialogId) {
                currentDialog = dialog
                liveData.setValue(Pair(DIALOG_LOADED, null))
                break
            }
        }
    }

    override fun onResumeView() {
        super.onResumeView()
        liveData.setValue(Pair(USERS_UPDATED, null))
    }

    fun addUser() {
        liveData.setValue(Pair(SHOW_SEARCH_USER_SCREEN, null))
    }
}