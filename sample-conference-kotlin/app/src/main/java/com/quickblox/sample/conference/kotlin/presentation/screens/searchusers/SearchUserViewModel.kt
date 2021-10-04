package com.quickblox.sample.conference.kotlin.presentation.screens.searchusers

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import androidx.collection.ArraySet
import androidx.collection.arraySetOf
import com.quickblox.chat.model.QBChatDialog
import com.quickblox.core.exception.QBResponseException
import com.quickblox.core.request.GenericQueryRule
import com.quickblox.core.request.QBPagedRequestBuilder
import com.quickblox.sample.conference.kotlin.domain.DomainCallback
import com.quickblox.sample.conference.kotlin.domain.chat.ChatManager
import com.quickblox.sample.conference.kotlin.domain.user.NOT_FOUND_CODE
import com.quickblox.sample.conference.kotlin.domain.user.UserManager
import com.quickblox.sample.conference.kotlin.presentation.LiveData
import com.quickblox.sample.conference.kotlin.presentation.screens.base.BaseViewModel
import com.quickblox.sample.conference.kotlin.presentation.screens.searchusers.ViewState.Companion.DIALOG_LOADED
import com.quickblox.sample.conference.kotlin.presentation.screens.searchusers.ViewState.Companion.ERROR
import com.quickblox.sample.conference.kotlin.presentation.screens.searchusers.ViewState.Companion.MOVE_TO_BACK
import com.quickblox.sample.conference.kotlin.presentation.screens.searchusers.ViewState.Companion.OCCUPANTS_LOADED
import com.quickblox.sample.conference.kotlin.presentation.screens.searchusers.ViewState.Companion.SHOW_USERS
import com.quickblox.users.model.QBUser
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

private const val USERS_PAGE_SIZE = 100
private const val ORDER_VALUE_UPDATED_AT = "desc string updated_at"
private const val ORDER_RULE = "order"
private const val TOTAL_PAGES_BUNDLE_PARAM = "total_pages"
private const val MIN_SEARCH_QUERY_LENGTH = 3
private const val SEARCH_DELAY: Long = 600

/*
 * Created by Injoit in 2021-09-30.
 * Copyright © 2021 Quickblox. All rights reserved.
 */
@HiltViewModel
class SearchUserViewModel @Inject constructor(private val userManager: UserManager, private val chatManager: ChatManager) : BaseViewModel() {
    private var currentDialog: QBChatDialog? = null
    val liveData = LiveData<Pair<Int, Any?>>()
    private var hasMore = true
    val users = arrayListOf<QBUser>()
    private var currentPage = 0
    private var lastSearchQuery = ""
    private var handler = Handler(Looper.getMainLooper())
    val selectedUsers = arraySetOf<QBUser>()
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
                    // empty
                }
            })
        } ?: run {
            liveData.setValue(Pair(ViewState.SHOW_LOGIN_SCREEN, null))
        }
    }

    // TODO: 7/9/21 Need to add filter to search users
    private val occupants = arrayListOf<QBUser>()

    fun loadOccupants() {
        currentDialog?.occupants?.let { occupantsList ->
            userManager.loadUsersByIds(occupantsList, object : DomainCallback<ArrayList<QBUser>, Exception> {
                override fun onSuccess(result: ArrayList<QBUser>, bundle: Bundle?) {
                    occupants.addAll(result)
                    liveData.setValue(Pair(OCCUPANTS_LOADED, null))
                }

                override fun onError(error: Exception) {
                    liveData.setValue(Pair(ERROR, error.message))
                }
            })
        }
    }

    fun loadDialogById(dialogId: String) {
        liveData.setValue(Pair(ViewState.PROGRESS, null))
        for (dialog in chatManager.getDialogs()) {
            if (dialog.dialogId == dialogId) {
                currentDialog = dialog
                liveData.setValue(Pair(DIALOG_LOADED, null))
                break
            }
        }
    }

    fun loadUsers() {
        if (!hasMore) {
            return
        }

        liveData.setValue(Pair(ViewState.PROGRESS, null))
        val rules = ArrayList<GenericQueryRule>()
        rules.add(GenericQueryRule(ORDER_RULE, ORDER_VALUE_UPDATED_AT))

        currentPage += 1

        val requestBuilder = QBPagedRequestBuilder()
        requestBuilder.rules = rules
        requestBuilder.perPage = USERS_PAGE_SIZE
        requestBuilder.page = currentPage

        if (TextUtils.isEmpty(lastSearchQuery)) {
            loadUsersWithoutQuery(requestBuilder)
        } else {
            loadUsersWithQuery(lastSearchQuery, requestBuilder)
        }
    }

    private fun loadUsersWithoutQuery(requestBuilder: QBPagedRequestBuilder) {
        userManager.loadUsers(requestBuilder, object : DomainCallback<ArrayList<QBUser>, Exception> {
            override fun onSuccess(result: ArrayList<QBUser>, bundle: Bundle?) {
                val totalPages = bundle?.getInt(TOTAL_PAGES_BUNDLE_PARAM)
                totalPages?.let {
                    if (currentPage >= it) {
                        hasMore = false
                    }
                }
                result.removeAll(occupants)
                if (currentPage == 1) {
                    users.clear()
                }
                users.addAll(result)
                liveData.setValue(Pair(SHOW_USERS, users))
            }

            override fun onError(error: Exception) {
                liveData.setValue(Pair(ERROR, error.message))
            }
        })
    }

    private fun loadUsersWithQuery(query: String, requestBuilder: QBPagedRequestBuilder) {
        userManager.loadUsersByQuery(query, requestBuilder, object : DomainCallback<ArrayList<QBUser>, Exception> {
            override fun onSuccess(result: ArrayList<QBUser>, bundle: Bundle?) {
                val totalPages = bundle?.getInt(TOTAL_PAGES_BUNDLE_PARAM)
                totalPages?.let {
                    if (currentPage >= it) {
                        hasMore = false
                    }
                }
                result.removeAll(occupants)
                if (currentPage == 1) {
                    users.clear()
                }
                users.addAll(result)
                liveData.setValue(Pair(SHOW_USERS, users))
            }

            override fun onError(error: Exception) {
                if ((error as QBResponseException).httpStatusCode == NOT_FOUND_CODE) {
                    users.clear()
                    liveData.setValue(Pair(SHOW_USERS, users))
                } else {
                    liveData.setValue(Pair(ERROR, error.message))
                }
            }
        })
    }

    fun onQueryTextChange(query: String): Boolean {
        lastSearchQuery = query
        currentPage = 0
        hasMore = true
        if (query.length >= MIN_SEARCH_QUERY_LENGTH) {
            handler.removeCallbacksAndMessages(null)
            handler.postDelayed({
                loadUsers()
            }, SEARCH_DELAY)
        }

        if (query.isEmpty()) {
            loadUsers()
        }
        return true
    }

    fun addUsers(users: ArraySet<QBUser>) {
        liveData.setValue(Pair(ViewState.PROGRESS, null))
        currentDialog?.let { dialog ->
            chatManager.addUsersToDialog(dialog, users, object : DomainCallback<QBChatDialog?, Exception> {
                override fun onSuccess(result: QBChatDialog?, bundle: Bundle?) {
                    if (chatManager.getDialogs().contains(result)) {
                        val index = chatManager.getDialogs().indexOf(result)
                        result?.let { dialog ->
                            chatManager.getDialogs().set(index, dialog)
                        }
                    }

                    val usersIds = arrayListOf<Int>()
                    for (user in users) {
                        usersIds.add(user.id)
                    }
                    currentDialog?.occupants?.toList()?.let {
                        usersIds.addAll(it)
                    }

                    userManager.loadUsersByIds(usersIds, object : DomainCallback<ArrayList<QBUser>, Exception> {
                        override fun onSuccess(result: ArrayList<QBUser>, bundle: Bundle?) {
                            liveData.setValue(Pair(MOVE_TO_BACK, null))
                        }

                        override fun onError(error: Exception) {
                            liveData.setValue(Pair(ERROR, error.message))
                        }
                    })
                }

                override fun onError(error: Exception) {
                    liveData.setValue(Pair(ERROR, error.message))
                }
            })
        }
    }
}