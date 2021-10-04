package com.quickblox.sample.conference.kotlin.presentation.screens.muteparticipants

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.quickblox.sample.conference.kotlin.R
import com.quickblox.sample.conference.kotlin.databinding.ActivityMuteParticipantsBinding
import com.quickblox.sample.conference.kotlin.presentation.screens.base.BaseActivity
import com.quickblox.sample.conference.kotlin.presentation.screens.login.LoginActivity
import com.quickblox.sample.conference.kotlin.presentation.screens.muteparticipants.adapter.MuteParticipantsAdapter
import dagger.hilt.android.AndroidEntryPoint

/*
 * Created by Injoit in 2021-09-30.
 * Copyright © 2021 Quickblox. All rights reserved.
 */
@AndroidEntryPoint
class MuteParticipantsActivity : BaseActivity<MuteParticipantsViewModel>(MuteParticipantsViewModel::class.java) {
    private lateinit var binding: ActivityMuteParticipantsBinding
    private var muteParticipantsAdapter: MuteParticipantsAdapter? = null

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, MuteParticipantsActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMuteParticipantsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel.liveData.observe(this, { result ->
            result?.let { (state, data) ->
                when (state) {
                    ViewState.PROGRESS -> {
                        showProgress()
                    }
                    ViewState.UPDATE_LIST -> {
                        muteParticipantsAdapter?.notifyDataSetChanged()
                        fillToolBar()
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
        fillToolBar()
        initAdapter()

        binding.flBack.setOnClickListener {
            onBackPressed()
        }
    }

    private fun fillToolBar() {
        binding.toolbarTitle.text = viewModel.currentDialog?.name
        if (viewModel.getCountParticipants() > 1) {
            binding.tvSubTitle.text = getString(R.string.chat_subtitle, viewModel.currentDialog?.occupants?.size.toString())
        } else {
            binding.tvSubTitle.text = getString(R.string.chat_subtitle_singular)
        }
    }

    private fun initAdapter() {
        muteParticipantsAdapter = MuteParticipantsAdapter(viewModel.callEntities.toList(), viewModel.getCurrentUserId())
        binding.rvUsers.adapter = muteParticipantsAdapter
    }

    override fun showProgress() {
        binding.progressBar.visibility = View.VISIBLE
    }

    override fun hideProgress() {
        binding.progressBar.visibility = View.GONE
    }
}