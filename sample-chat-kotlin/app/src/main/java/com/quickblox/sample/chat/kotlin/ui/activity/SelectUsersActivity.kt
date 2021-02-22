package com.quickblox.sample.chat.kotlin.ui.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.widget.SearchView
import androidx.core.view.get
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.quickblox.chat.model.QBChatDialog
import com.quickblox.core.QBEntityCallback
import com.quickblox.core.exception.QBResponseException
import com.quickblox.core.request.GenericQueryRule
import com.quickblox.core.request.QBPagedRequestBuilder
import com.quickblox.sample.chat.kotlin.R
import com.quickblox.sample.chat.kotlin.ui.adapter.CheckboxUsersAdapter
import com.quickblox.sample.chat.kotlin.ui.adapter.ScrollViewWithMaxHeight
import com.quickblox.sample.chat.kotlin.utils.chat.CURRENT_PAGE_BUNDLE_PARAM
import com.quickblox.sample.chat.kotlin.utils.chat.ChatHelper
import com.quickblox.sample.chat.kotlin.utils.chat.TOTAL_PAGES_BUNDLE_PARAM
import com.quickblox.sample.chat.kotlin.utils.getColorCircleDrawable
import com.quickblox.sample.chat.kotlin.utils.shortToast
import com.quickblox.users.QBUsers
import com.quickblox.users.model.QBUser
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

private const val EXTRA_QB_DIALOG = "qb_dialog"
const val EXTRA_QB_USERS = "qb_users"
const val MINIMUM_CHAT_OCCUPANTS_SIZE = 1
const val PRIVATE_CHAT_OCCUPANTS_SIZE = 2
const val EXTRA_CHAT_NAME = "chat_name"
const val USERS_PAGE_SIZE = 100
const val MIN_SEARCH_QUERY_LENGTH = 3
const val SEARCH_DELAY = 600L

class SelectUsersActivity : BaseActivity() {
    private val CLICK_DELAY = TimeUnit.SECONDS.toMillis(2)

    private lateinit var usersListView: ListView
    private lateinit var progressBar: ProgressBar
    private lateinit var searchView: SearchView
    private lateinit var menu: Menu
    private lateinit var scrollView: ScrollViewWithMaxHeight
    private lateinit var chipGroup: ChipGroup
    private lateinit var tvNotFound: TextView
    private lateinit var usersAdapter: CheckboxUsersAdapter
    private var existingUsers = HashSet<QBUser>()
    private var lastClickTime = 0L
    private var qbChatDialog: QBChatDialog? = null
    private var chatName: String? = null
    private var currentPage: Int = 0
    private var isLoading: Boolean = false
    private var lastSearchQuery: String = ""
    private var hasNextPage: Boolean = true

    companion object {

        private const val REQUEST_DIALOG_NAME = 135

        fun startForResult(activity: Activity, code: Int, dialog: QBChatDialog?) {
            val intent = Intent(activity, SelectUsersActivity::class.java)
            intent.putExtra(EXTRA_QB_DIALOG, dialog)
            activity.startActivityForResult(intent, code)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_users)

        intent.getSerializableExtra(EXTRA_QB_DIALOG)?.let {
            qbChatDialog = it as QBChatDialog
        }
        initUi()

        if (qbChatDialog != null) {
            updateDialog()
        } else {
            loadUsersFromQB(null)
        }
    }

    private fun initUi() {
        progressBar = findViewById(R.id.progress_select_users)
        usersListView = findViewById(R.id.list_select_users)
        searchView = findViewById(R.id.search)
        scrollView = findViewById(R.id.scroll_view)
        scrollView.setMaxHeight(225)
        chipGroup = findViewById(R.id.chips)
        tvNotFound = findViewById(R.id.tv_no_users_found)
        usersAdapter = CheckboxUsersAdapter(this@SelectUsersActivity, ArrayList())
        usersListView.adapter = usersAdapter

        searchView.setOnQueryTextListener(SearchQueryListener())

        val editingChat = intent.getSerializableExtra(EXTRA_QB_DIALOG) != null
        if (editingChat) {
            supportActionBar?.title = getString(R.string.select_users_edit_chat)
        } else {
            supportActionBar?.title = getString(R.string.select_users_create_chat_title)
            supportActionBar?.subtitle = getString(R.string.select_users_create_chat_subtitle, "0")
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        usersListView.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            usersAdapter.onItemClicked(position, view, parent)
            menu.getItem(0).isVisible = (usersAdapter.selectedUsers.size >= 1)

            var subtitle = ""
            if (usersAdapter.selectedUsers.size != 1) {
                subtitle = getString(R.string.select_users_create_chat_subtitle, usersAdapter.selectedUsers.size.toString())
            } else {
                subtitle = getString(R.string.select_users_create_chat_subtitle_single, "1")
            }
            supportActionBar?.subtitle = subtitle

            chipGroup.removeAllViews()
            for (user in usersAdapter.selectedUsers) {
                val chip = Chip(this)
                chip.text = user.fullName
                chip.chipIcon = getColorCircleDrawable(user.id.hashCode())
                chip.isCloseIconVisible = false
                chip.isCheckable = false
                chip.isClickable = false
                chipGroup.addView(chip as View)
                chipGroup.visibility = View.VISIBLE
                runOnUiThread {
                    scrollView.fullScroll(ScrollView.FOCUS_DOWN)
                }
            }
            if (usersAdapter.selectedUsers.size == 0) {
                chipGroup.visibility = View.GONE
            }
        }

        usersListView.setOnScrollListener(ScrollListener())
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        this.menu = menu
        menuInflater.inflate(R.menu.menu_activity_select_users, menu)
        if (qbChatDialog != null) {
            menu.get(0).title = getString(R.string.menu_done)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (SystemClock.uptimeMillis() - lastClickTime < CLICK_DELAY) {
            return super.onOptionsItemSelected(item)
        }
        lastClickTime = SystemClock.uptimeMillis()

        when (item.itemId) {
            R.id.menu_select_people_action_done -> {
                if (::usersAdapter.isInitialized) {
                    if (usersAdapter.selectedUsers.size < MINIMUM_CHAT_OCCUPANTS_SIZE) {
                        shortToast(R.string.select_users_choose_users)
                    } else {
                        if (qbChatDialog == null && usersAdapter.selectedUsers.size >= PRIVATE_CHAT_OCCUPANTS_SIZE) {
                            NewGroupActivity.startForResult(this@SelectUsersActivity, REQUEST_DIALOG_NAME)
                        } else {
                            passResultToCallerActivity()
                        }
                    }
                }
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (!TextUtils.isEmpty(data?.getSerializableExtra(EXTRA_CHAT_NAME).toString()))
                chatName = data?.getSerializableExtra(EXTRA_CHAT_NAME).toString()
            passResultToCallerActivity()
        }
    }

    private fun passResultToCallerActivity() {
        val intent = Intent()
        intent.putExtra(EXTRA_QB_USERS, usersAdapter.selectedUsers)
        if (!TextUtils.isEmpty(chatName)) {
            intent.putExtra(EXTRA_CHAT_NAME, chatName)
        }
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    private fun updateDialog() {
        showProgressDialog(R.string.dlg_loading)
        val dialogID = qbChatDialog!!.dialogId
        ChatHelper.getDialogById(dialogID, object : QBEntityCallback<QBChatDialog> {
            override fun onSuccess(qbChatDialog: QBChatDialog, bundle: Bundle?) {
                this@SelectUsersActivity.qbChatDialog = qbChatDialog
                loadUsersFromDialog()
            }

            override fun onError(e: QBResponseException) {
                disableProgress()
                showErrorSnackbar(R.string.select_users_get_dialog_error, e, View.OnClickListener { updateDialog() })
            }
        })
    }

    private fun loadUsersFromDialog () {
        ChatHelper.getUsersFromDialog(qbChatDialog!!, object : QBEntityCallback<ArrayList<QBUser>>{
            override fun onSuccess(usersFromDialog: ArrayList<QBUser>?, b: Bundle?) {
                usersFromDialog?.let {
                    existingUsers.addAll(usersFromDialog)
                }
                loadUsersFromQB(null)
            }

            override fun onError(e: QBResponseException?) {
                disableProgress()
                showErrorSnackbar(R.string.select_users_get_users_dialog_error, e, null)
            }
        })
    }

    private fun loadUsersFromQB(query: String?) {
        if (!isProgresDialogShowing()) {
            enableProgress()
        }
        currentPage += 1
        val rules = ArrayList<GenericQueryRule>()
        rules.add(GenericQueryRule(ORDER_RULE, ORDER_VALUE_UPDATED_AT))

        val requestBuilder = QBPagedRequestBuilder()
        requestBuilder.rules = rules
        requestBuilder.perPage = USERS_PAGE_SIZE
        requestBuilder.page = currentPage

        if (TextUtils.isEmpty(query)) {
            loadUsersWithoutQuery(requestBuilder)
        } else {
            loadUsersByQuery(query!!, requestBuilder)
        }
    }

    private fun loadUsersWithoutQuery(requestBuilder: QBPagedRequestBuilder) {
        QBUsers.getUsers(requestBuilder).performAsync(object : QBEntityCallback<ArrayList<QBUser>> {
            override fun onSuccess(usersList: ArrayList<QBUser>, params: Bundle?) {
                tvNotFound.visibility = View.INVISIBLE
                val totalPagesFromParams = params?.get(TOTAL_PAGES_BUNDLE_PARAM) as Int
                if (currentPage >= totalPagesFromParams) {
                    hasNextPage = false
                }

                if (qbChatDialog != null) {
                    existingUsers.forEach {
                        usersList.remove(it)
                    }
                }
                if (currentPage == 1) {
                    usersAdapter.addNewList(usersList)
                } else {
                    usersAdapter.addUsers(usersList)
                }
                disableProgress()
            }

            override fun onError(e: QBResponseException) {
                disableProgress()
                currentPage -= 1
                showErrorSnackbar(R.string.select_users_get_users_error, e, View.OnClickListener { loadUsersWithoutQuery(requestBuilder) })
            }
        })
    }

    private fun loadUsersByQuery(query: String, requestBuilder: QBPagedRequestBuilder) {
        QBUsers.getUsersByFullName(query, requestBuilder).performAsync(object : QBEntityCallback<ArrayList<QBUser>> {
            override fun onSuccess(qbUsers: ArrayList<QBUser>?, params: Bundle?) {
                val totalPagesFromParams = params?.get(TOTAL_PAGES_BUNDLE_PARAM) as Int
                if (currentPage >= totalPagesFromParams) {
                    hasNextPage = false
                }
                if (qbUsers != null) {
                    tvNotFound.visibility = View.INVISIBLE
                    if (qbChatDialog != null) {
                        existingUsers.forEach { user ->
                            qbUsers.remove(user)
                        }
                    }
                    if (currentPage == 1) {
                        usersAdapter.addNewList(qbUsers)
                        usersListView.smoothScrollToPosition(0)
                    } else {
                        usersAdapter.addUsers(qbUsers)
                    }
                } else {
                    usersAdapter.clearList()
                    tvNotFound.visibility = View.VISIBLE
                }
                disableProgress()
            }

            override fun onError(e: QBResponseException?) {
                disableProgress()
                if (e?.httpStatusCode == 404) {
                    usersAdapter.clearList()
                    tvNotFound.visibility = View.VISIBLE
                } else {
                    currentPage -= 1
                    showErrorSnackbar(R.string.select_users_get_users_error, e, View.OnClickListener { loadUsersByQuery(query, requestBuilder) })
                }
            }
        })
    }

    private fun enableProgress() {
        progressBar.visibility = View.VISIBLE
        isLoading = true
    }

    private fun disableProgress() {
        hideProgressDialog()
        progressBar.visibility = View.GONE
        isLoading = false
    }

    private inner class SearchQueryListener : SearchView.OnQueryTextListener {

        private var timer = Timer()

        override fun onQueryTextSubmit(query: String?): Boolean {
            return false
        }

        override fun onQueryTextChange(newText: String?): Boolean {
            if (newText != null) {
                timer.cancel()
                timer = Timer()
                timer.schedule(object : TimerTask() {
                    override fun run() {
                        runOnUiThread {
                            currentPage = 0
                            hasNextPage = true
                            lastSearchQuery = newText
                            if (newText.length >= MIN_SEARCH_QUERY_LENGTH) {
                                loadUsersFromQB(newText)
                            }
                            if (newText.isEmpty()) {
                                loadUsersFromQB(null)
                            }
                        }
                    }
                }, SEARCH_DELAY)
            }
            return false
        }
    }

    private inner class ScrollListener : AbsListView.OnScrollListener {

        override fun onScroll(view: AbsListView?, firstVisibleItem: Int, visibleItemCount: Int, totalItemCount: Int) {
            if (!isLoading && totalItemCount > 0 && (firstVisibleItem + visibleItemCount * 3) >= totalItemCount && hasNextPage) {
                if (TextUtils.isEmpty(lastSearchQuery)) {
                    loadUsersFromQB(null)
                } else {
                    loadUsersFromQB(lastSearchQuery)
                }
            }
        }

        override fun onScrollStateChanged(view: AbsListView?, scrollState: Int) {

        }
    }
}