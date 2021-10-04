package com.quickblox.sample.conference.kotlin.presentation.screens.createchat

import android.os.Bundle
import androidx.collection.ArraySet
import com.quickblox.chat.model.QBChatDialog
import com.quickblox.sample.conference.kotlin.domain.DomainCallback
import com.quickblox.sample.conference.kotlin.domain.chat.ChatManager
import com.quickblox.sample.conference.kotlin.domain.user.UserManager
import com.quickblox.sample.conference.kotlin.presentation.LiveData
import com.quickblox.sample.conference.kotlin.presentation.screens.base.BaseViewModel
import com.quickblox.sample.conference.kotlin.presentation.screens.createchat.ViewState.Companion.CREATE_CHAT
import com.quickblox.sample.conference.kotlin.presentation.screens.createchat.ViewState.Companion.ERROR
import com.quickblox.sample.conference.kotlin.presentation.screens.createchat.ViewState.Companion.SHOW_CHAT_SCREEN
import com.quickblox.sample.conference.kotlin.presentation.screens.createchat.ViewState.Companion.SHOW_NAME_CHAT_SCREEN
import com.quickblox.sample.conference.kotlin.presentation.screens.createchat.ViewState.Companion.SHOW_NEW_CHAT_SCREEN
import com.quickblox.users.model.QBUser
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/*
 * Created by Injoit in 2021-09-30.
 * Copyright © 2021 Quickblox. All rights reserved.
 */
@HiltViewModel
class CreateChatViewModel @Inject constructor(userManager: UserManager, private val chatManager: ChatManager) : BaseViewModel() {
    val liveData = LiveData<Pair<Int, Any?>>()
    private val selectedUsers = arrayListOf<QBUser>()
    var currentUser: QBUser? = null
        private set

    init {
        liveData.setValue(Pair(SHOW_NEW_CHAT_SCREEN, null))
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

    fun onSelectedUsers(selectedUsers: ArraySet<QBUser>) {
        this.selectedUsers.clear()
        this.selectedUsers.addAll(selectedUsers)
        liveData.setValue(Pair(SHOW_NAME_CHAT_SCREEN, null))
    }

    fun createChat(chatName: String) {
        chatManager.createDialog(selectedUsers, chatName, object : DomainCallback<QBChatDialog, Exception> {
            override fun onSuccess(result: QBChatDialog, bundle: Bundle?) {
                liveData.setValue(Pair(SHOW_CHAT_SCREEN, result))
            }

            override fun onError(error: Exception) {
                liveData.setValue(Pair(ERROR, error.message))
            }
        })
    }

    fun onCreateChat(chatName: String) {
        liveData.setValue(Pair(CREATE_CHAT, chatName))
    }
}