package com.quickblox.sample.conference.kotlin.presentation.screens.main

import android.os.Bundle
import com.quickblox.chat.model.QBChatDialog
import com.quickblox.sample.conference.kotlin.R
import com.quickblox.sample.conference.kotlin.domain.DomainCallback
import com.quickblox.sample.conference.kotlin.domain.chat.ChatManager
import com.quickblox.sample.conference.kotlin.domain.chat.ConnectionChatListener
import com.quickblox.sample.conference.kotlin.domain.chat.DialogListener
import com.quickblox.sample.conference.kotlin.domain.push.PushManager
import com.quickblox.sample.conference.kotlin.domain.repositories.connection.ConnectionRepository
import com.quickblox.sample.conference.kotlin.domain.repositories.connection.ConnectivityChangedListener
import com.quickblox.sample.conference.kotlin.domain.user.UserManager
import com.quickblox.sample.conference.kotlin.presentation.LiveData
import com.quickblox.sample.conference.kotlin.presentation.resources.ResourcesManager
import com.quickblox.sample.conference.kotlin.presentation.screens.base.BaseViewModel
import com.quickblox.sample.conference.kotlin.presentation.screens.main.ViewState.Companion.ERROR
import com.quickblox.sample.conference.kotlin.presentation.screens.main.ViewState.Companion.LIST_DIALOGS_UPDATED
import com.quickblox.sample.conference.kotlin.presentation.screens.main.ViewState.Companion.MOVE_TO_FIRST_DIALOG
import com.quickblox.sample.conference.kotlin.presentation.screens.main.ViewState.Companion.PROGRESS
import com.quickblox.sample.conference.kotlin.presentation.screens.main.ViewState.Companion.SHOW_CHAT_SCREEN
import com.quickblox.sample.conference.kotlin.presentation.screens.main.ViewState.Companion.SHOW_CREATE_SCREEN
import com.quickblox.sample.conference.kotlin.presentation.screens.main.ViewState.Companion.SHOW_DIALOGS
import com.quickblox.sample.conference.kotlin.presentation.screens.main.ViewState.Companion.SHOW_LOGIN_SCREEN
import com.quickblox.sample.conference.kotlin.presentation.screens.main.ViewState.Companion.USER_ERROR
import com.quickblox.users.model.QBUser
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

const val DIALOGS_LIMIT = 100
const val EXTRA_TOTAL_ENTRIES = "total_entries"
const val EXTRA_SKIP = "skip"

/*
 * Created by Injoit in 2021-09-30.
 * Copyright © 2021 Quickblox. All rights reserved.
 */
@HiltViewModel
class MainViewModel @Inject constructor(private val userManager: UserManager, private val chatManager: ChatManager,
                                        private val resourcesManager: ResourcesManager, private val pushManager: PushManager,
                                        private val connectionRepository: ConnectionRepository) : BaseViewModel() {
    private val TAG: String = MainViewModel::class.java.simpleName
    private var selectedDialogId: String? = null
    private val connectionListener = ConnectionListener(TAG)
    private val dialogListener = DialogListenerImpl(TAG)
    val liveData = LiveData<Pair<Int, Any?>>()
    var user: QBUser? = null
        private set
    private val connectivityChangedListener = ConnectivityChangedListenerImpl(TAG)

    init {
        user = userManager.getCurrentUser()
    }

    fun getDialogs(): ArrayList<QBChatDialog> {
        return chatManager.getDialogs()
    }

    private fun loginToChat(user: QBUser?) {
        if (user == null) {
            liveData.setValue(Pair(USER_ERROR, null))
        } else {
            chatManager.loginToChat(user, object : DomainCallback<Unit, Exception> {
                override fun onSuccess(result: Unit, bundle: Bundle?) {
                    subscribeChatListener()
                    loadDialogs(refresh = true, reJoin = true)
                }

                override fun onError(error: Exception) {
                    liveData.setValue(Pair(ERROR, error.message))
                }
            })
        }
    }

    fun singOut() {
        if (!connectionRepository.isInternetAvailable()) {
            liveData.setValue(Pair(ERROR, resourcesManager.get().getString(R.string.no_internet)))
            return
        }
        liveData.setValue(Pair(PROGRESS, null))
        if (pushManager.isSubscribed()) {
            pushManager.unSubscribe {
                userManager.signOut(object : DomainCallback<Unit?, Exception> {
                    override fun onSuccess(result: Unit?, bundle: Bundle?) {
                        chatManager.clearDialogs()
                        chatManager.unsubscribeDialogListener(dialogListener)
                        chatManager.unSubscribeConnectionChatListener(connectionListener)
                        chatManager.destroyChat()
                        liveData.setValue(Pair(SHOW_LOGIN_SCREEN, null))
                    }

                    override fun onError(error: Exception) {
                        liveData.setValue(Pair(ERROR, error.message))
                    }
                })
            }
        } else {
            userManager.signOut(object : DomainCallback<Unit?, Exception> {
                override fun onSuccess(result: Unit?, bundle: Bundle?) {
                    chatManager.clearDialogs()
                    chatManager.unsubscribeDialogListener(dialogListener)
                    chatManager.unSubscribeConnectionChatListener(connectionListener)
                    chatManager.destroyChat()
                    liveData.setValue(Pair(SHOW_LOGIN_SCREEN, null))
                }

                override fun onError(error: Exception) {
                    liveData.setValue(Pair(ERROR, error.message))
                }
            })
        }
    }

    fun loadDialogs(refresh: Boolean, reJoin: Boolean) {
        liveData.setValue(Pair(PROGRESS, null))
        chatManager.loadDialogs(refresh, reJoin, object : DomainCallback<ArrayList<QBChatDialog>, Exception> {
            override fun onSuccess(result: ArrayList<QBChatDialog>, bundle: Bundle?) {
                liveData.setValue(Pair(SHOW_DIALOGS, chatManager.getDialogs()))
            }

            override fun onError(error: Exception) {
                liveData.setValue(Pair(ERROR, error.message))
            }
        })
    }

    fun deleteDialogs(dialogs: ArrayList<QBChatDialog>) {
        liveData.setValue(Pair(PROGRESS, null))

        user?.let {
            chatManager.deleteDialogs(dialogs, it, object : DomainCallback<List<QBChatDialog>, Exception> {
                override fun onSuccess(notDeletedDialogs: List<QBChatDialog>, bundle: Bundle?) {
                    if (notDeletedDialogs.isNotEmpty()) {
                        liveData.setValue(Pair(ERROR, resourcesManager.get().getString(R.string.not_deleted_dialogs, notDeletedDialogs.size)))
                        return
                    }
                    liveData.setValue(Pair(LIST_DIALOGS_UPDATED, null))
                }

                override fun onError(error: Exception) {
                    // Empty
                }
            })
        }
    }

    private fun subscribeChatListener() {
        chatManager.subscribeDialogListener(dialogListener)
        chatManager.subscribeConnectionChatListener(connectionListener)
    }

    override fun onResumeView() {
        connectionRepository.addListener(connectivityChangedListener)
        if (chatManager.isLoggedInChat()) {
            subscribeChatListener()
            loadDialogs(refresh = true, reJoin = true)
        } else {
            loginToChat(user)
        }
    }

    override fun onStopView() {
        connectionRepository.removeListener(connectivityChangedListener)
        chatManager.unsubscribeDialogListener(dialogListener)
        chatManager.unSubscribeConnectionChatListener(connectionListener)
    }

    override fun onStopApp() {
        chatManager.destroyChat()
    }

    fun onDialogClicked(dialog: QBChatDialog) {
        if (!connectionRepository.isInternetAvailable()) {
            liveData.setValue(Pair(ERROR, resourcesManager.get().getString(R.string.no_internet)))
            return
        }
        selectedDialogId = dialog.dialogId
        liveData.setValue(Pair(SHOW_CHAT_SCREEN, dialog))
    }

    fun showCreateScreen() {
        if (!connectionRepository.isInternetAvailable()) {
            liveData.setValue(Pair(ERROR, Exception(resourcesManager.get().getString(R.string.no_internet))))
            return
        }
        liveData.setValue(Pair(SHOW_CREATE_SCREEN, null))
    }

    private inner class ConnectionListener(val tag: String) : ConnectionChatListener {
        override fun onConnectedChat() {
            liveData.setValue(Pair(ERROR, resourcesManager.get().getString(R.string.connected)))
        }

        override fun onError(exception: Exception) {
            liveData.setValue(Pair(ERROR, exception.message))
        }

        override fun reconnectionFailed(exception: Exception) {
            liveData.setValue(Pair(ERROR, exception.message))
            loginToChat(user)
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) {
                return true
            }
            if (other !is ConnectionListener) {
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

    private inner class DialogListenerImpl(val tag: String) : DialogListener {
        override fun onUpdatedDialog(dialog: QBChatDialog) {
            if (chatManager.getDialogs().isEmpty()) {
                loadDialogs(refresh = true, reJoin = false)
            } else {
                liveData.setValue(Pair(MOVE_TO_FIRST_DIALOG, dialog))
            }
        }

        override fun onError(exception: Exception) {
            liveData.setValue(Pair(ERROR, exception.message))
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) {
                return true
            }
            if (other !is DialogListenerImpl) {
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

    private inner class ConnectivityChangedListenerImpl(val tag: String) : ConnectivityChangedListener {
        override fun onAvailable() {
            if (!chatManager.isLoggedInChat()) {
                liveData.setValue(Pair(ERROR, resourcesManager.get().getString(R.string.internet_restored)))
                loginToChat(user)
            }
        }

        override fun onLost() {
            liveData.setValue(Pair(ERROR, resourcesManager.get().getString(R.string.no_internet)))
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