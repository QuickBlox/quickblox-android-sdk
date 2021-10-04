package com.quickblox.sample.conference.kotlin.presentation.screens.chatinfo

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.quickblox.sample.conference.kotlin.R
import com.quickblox.sample.conference.kotlin.databinding.ActivityChatinfoBinding
import com.quickblox.sample.conference.kotlin.presentation.screens.base.BaseActivity
import com.quickblox.sample.conference.kotlin.presentation.screens.chatinfo.adapter.UsersAdapter
import com.quickblox.sample.conference.kotlin.presentation.screens.login.LoginActivity
import com.quickblox.sample.conference.kotlin.presentation.screens.searchusers.SearchUsersActivity
import dagger.hilt.android.AndroidEntryPoint

private const val EXTRA_DIALOG_ID = "EXTRA_DIALOG_ID"

/*
 * Created by Injoit in 2021-09-30.
 * Copyright © 2021 Quickblox. All rights reserved.
 */
@AndroidEntryPoint
class ChatInfoActivity : BaseActivity<ChatInfoViewModel>(ChatInfoViewModel::class.java) {
    private lateinit var binding: ActivityChatinfoBinding
    private var usersAdapter: UsersAdapter? = null

    companion object {
        fun start(context: Context, dialogId: String) {
            val intent = Intent(context, ChatInfoActivity::class.java)
            intent.putExtra(EXTRA_DIALOG_ID, dialogId)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatinfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initAdapter()
        initClickListeners()

        viewModel.liveData.observe(this, { result ->
            result?.let { (state, data) ->
                when (state) {
                    ViewState.PROGRESS -> {
                        showProgress()
                    }
                    ViewState.SHOW_SEARCH_USER_SCREEN -> {
                        viewModel.currentDialog?.dialogId?.let { SearchUsersActivity.start(this, it) }
                    }
                    ViewState.USERS_UPDATED -> {
                        usersAdapter?.notifyDataSetChanged()
                    }
                    ViewState.USERS_LOADED -> {
                        hideProgress()
                        usersAdapter?.notifyDataSetChanged()
                    }
                    ViewState.DIALOG_LOADED -> {
                        initToolBar()
                        viewModel.loadUsersByIds()
                    }
                    ViewState.ERROR -> {
                        hideProgress()
                        Toast.makeText(baseContext, "$data", Toast.LENGTH_SHORT).show()
                    }
                    ViewState.SHOW_LOGIN_SCREEN -> {
                        LoginActivity.start(this)
                        finish()
                    }
                }
            }
        })
    }

    override fun onResume() {
        super.onResume()
        val dialogId = intent.getStringExtra(EXTRA_DIALOG_ID)
        dialogId?.let {
            viewModel.loadDialogById(it)
        } ?: run {
            Toast.makeText(baseContext, getString(R.string.dialogId_error), Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun initAdapter() {
        usersAdapter = UsersAdapter(viewModel.users, viewModel.getCurrentUserId())
        binding.rvUsers.adapter = usersAdapter
    }

    private fun initToolBar() {
        binding.toolbarTitle.text = viewModel.currentDialog?.name
        if (viewModel.currentDialog?.occupants?.size ?: 1 > 1) {
            binding.tvSubTitle.text = getString(R.string.chat_subtitle, viewModel.currentDialog?.occupants?.size.toString())
        } else {
            binding.tvSubTitle.text = getString(R.string.chat_subtitle_singular)
        }
    }

    private fun initClickListeners() {
        binding.flBack.setOnClickListener {
            onBackPressed()
        }
        binding.ivAddUser.setOnClickListener {
            viewModel.addUser()
        }
    }

    override fun showProgress() {
        binding.progressBar.visibility = View.VISIBLE
    }

    override fun hideProgress() {
        binding.progressBar.visibility = View.GONE
    }
}