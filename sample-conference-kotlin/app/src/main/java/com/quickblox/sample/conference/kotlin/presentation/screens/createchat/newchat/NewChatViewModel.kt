package com.quickblox.sample.conference.kotlin.presentation.screens.createchat.newchat

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import androidx.collection.arraySetOf
import androidx.lifecycle.ViewModel
import com.quickblox.core.exception.QBResponseException
import com.quickblox.core.request.GenericQueryRule
import com.quickblox.core.request.QBPagedRequestBuilder
import com.quickblox.sample.conference.kotlin.domain.DomainCallback
import com.quickblox.sample.conference.kotlin.domain.user.NOT_FOUND_CODE
import com.quickblox.sample.conference.kotlin.domain.user.UserManager
import com.quickblox.sample.conference.kotlin.presentation.LiveData
import com.quickblox.sample.conference.kotlin.presentation.screens.createchat.newchat.ViewState.Companion.ERROR
import com.quickblox.sample.conference.kotlin.presentation.screens.createchat.newchat.ViewState.Companion.PROGRESS
import com.quickblox.sample.conference.kotlin.presentation.screens.createchat.newchat.ViewState.Companion.SHOW_USERS
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
class NewChatViewModel @Inject constructor(private val userManager: UserManager) : ViewModel() {
    val liveData = LiveData<Pair<Int, Any?>>()
    private var hasMore = true
    val users = arrayListOf<QBUser>()
    private var currentPage = 0
    private var lastSearchQuery = ""
    private var handler = Handler(Looper.getMainLooper())
    val selectedUsers = arraySetOf<QBUser>()

    init {
        loadUsers()
    }

    fun loadUsers() {
        if (!hasMore) {
            return
        }

        liveData.setValue(Pair(PROGRESS, null))
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
                result.remove(userManager.getCurrentUser())
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
                result.remove(userManager.getCurrentUser())
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
}