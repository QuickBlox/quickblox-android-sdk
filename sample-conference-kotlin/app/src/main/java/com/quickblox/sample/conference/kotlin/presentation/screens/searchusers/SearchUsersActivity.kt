package com.quickblox.sample.conference.kotlin.presentation.screens.searchusers

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.quickblox.sample.conference.kotlin.R
import com.quickblox.sample.conference.kotlin.databinding.ActivitySearchUsersBinding
import com.quickblox.sample.conference.kotlin.presentation.screens.base.BaseActivity
import com.quickblox.sample.conference.kotlin.presentation.screens.createchat.newchat.UsersSearchAdapter
import com.quickblox.sample.conference.kotlin.presentation.screens.login.LoginActivity
import dagger.hilt.android.AndroidEntryPoint

private const val EXTRA_DIALOG_ID = "EXTRA_DIALOG_ID"

/*
 * Created by Injoit in 2021-09-30.
 * Copyright © 2021 Quickblox. All rights reserved.
 */
@AndroidEntryPoint
class SearchUsersActivity : BaseActivity<SearchUserViewModel>(SearchUserViewModel::class.java) {
    private lateinit var binding: ActivitySearchUsersBinding
    private var usersSearchAdapter: UsersSearchAdapter? = null
    private val onScrollListenerImpl = OnScrollListenerImpl()

    companion object {
        fun start(context: Context, dialogId: String) {
            val intent = Intent(context, SearchUsersActivity::class.java)
            intent.putExtra(EXTRA_DIALOG_ID, dialogId)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchUsersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val dialogId = intent.getStringExtra(EXTRA_DIALOG_ID)
        dialogId?.let {
            viewModel.loadDialogById(it)
        } ?: run {
            Toast.makeText(baseContext, getString(R.string.dialogId_error), Toast.LENGTH_SHORT).show()
            finish()
        }

        initToolbar()
        initAdapter()
        initScrollListeners()

        binding.toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.done -> {
                    viewModel.addUsers(viewModel.selectedUsers)
                }
            }
            return@setOnMenuItemClickListener true
        }
        viewModel.liveData.observe(this, { result ->
            result?.let { (state, data) ->
                when (state) {
                    ViewState.PROGRESS -> {
                        showProgress()
                    }
                    ViewState.ERROR -> {
                        hideProgress()
                        Toast.makeText(this, "$data", Toast.LENGTH_SHORT).show()
                    }
                    ViewState.DIALOG_LOADED -> {
                        viewModel.loadOccupants()
                    }
                    ViewState.OCCUPANTS_LOADED -> {
                        viewModel.loadUsers()
                    }
                    ViewState.SHOW_USERS -> {
                        hideProgress()

                        if (viewModel.users.isEmpty()) {
                            binding.tvPlaceHolder.visibility = View.VISIBLE
                            binding.rvUsers.visibility = View.GONE
                        } else {
                            binding.tvPlaceHolder.visibility = View.GONE
                            binding.rvUsers.visibility = View.VISIBLE
                        }
                        usersSearchAdapter?.notifyDataSetChanged()
                    }
                    ViewState.MOVE_TO_BACK -> {
                        hideProgress()
                        onBackPressed()
                    }
                    ViewState.SHOW_LOGIN_SCREEN -> {
                        LoginActivity.start(this)
                        finish()
                    }
                }
            }
        })
    }

    private fun initToolbar() {
        binding.toolbar.inflateMenu(R.menu.menu_searche_users)
        binding.toolbar.menu?.getItem(0)?.isVisible = false
        binding.flBack.setOnClickListener {
            onBackPressed()
        }
        binding.toolbarTitle.setText(R.string.add_members)
        binding.searchView.setOnQueryTextListener(SearchQueryListener())
    }

    private fun changeToolbar() {
        val selectedCounter = viewModel.selectedUsers.size
        binding.toolbar.menu?.getItem(0)?.isVisible = selectedCounter > 0

        if (selectedCounter == 0) {
            binding.tvSubTitle.visibility = View.GONE
            return
        } else {
            binding.tvSubTitle.visibility = View.VISIBLE
        }

        binding.tvSubTitle.text = if (selectedCounter > 1) {
            getString(R.string.subtitle_new_chat_users, selectedCounter.toString())
        } else {
            getString(R.string.subtitle_new_chat_user, selectedCounter.toString())
        }
    }

    private fun initAdapter() {
        usersSearchAdapter = UsersSearchAdapter(viewModel.users, object : UsersSearchAdapter.UsersAdapterListener {
            override fun onSelected() {
                changeToolbar()
            }
        }, viewModel.selectedUsers)
        binding.rvUsers.adapter = usersSearchAdapter
    }

    private fun initScrollListeners() {
        val mLayoutManager = LinearLayoutManager(this)
        binding.rvUsers.layoutManager = mLayoutManager
        binding.rvUsers.addOnScrollListener(onScrollListenerImpl)
    }

    override fun showProgress() {
        binding.progressBar.visibility = View.VISIBLE
    }

    override fun hideProgress() {
        onScrollListenerImpl.isLoad = false
        binding.progressBar.visibility = View.GONE
    }

    private inner class SearchQueryListener : SearchView.OnQueryTextListener {
        override fun onQueryTextSubmit(query: String): Boolean {
            return false
        }

        override fun onQueryTextChange(newText: String): Boolean {
            viewModel.onQueryTextChange(newText)
            return false
        }
    }

    inner class OnScrollListenerImpl : RecyclerView.OnScrollListener() {
        var isLoad: Boolean = false

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            if (!recyclerView.canScrollVertically(1) || isLoad) {
                isLoad = true
                viewModel.loadUsers()
            }
        }
    }
}