package com.quickblox.sample.conference.kotlin.presentation.screens.createchat

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.collection.ArraySet
import com.quickblox.chat.model.QBChatDialog
import com.quickblox.sample.conference.kotlin.databinding.ActivityCreateChatBinding
import com.quickblox.sample.conference.kotlin.presentation.screens.base.BaseActivity
import com.quickblox.sample.conference.kotlin.presentation.screens.chat.ChatActivity
import com.quickblox.sample.conference.kotlin.presentation.screens.createchat.namechat.ChatNameFragment
import com.quickblox.sample.conference.kotlin.presentation.screens.createchat.newchat.NewChatFragment
import com.quickblox.sample.conference.kotlin.presentation.screens.login.LoginActivity
import com.quickblox.users.model.QBUser
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

/*
 * Created by Injoit in 2021-09-30.
 * Copyright © 2021 Quickblox. All rights reserved.
 */
@AndroidEntryPoint
class CreateChatActivity : BaseActivity<CreateChatViewModel>(CreateChatViewModel::class.java) {
    private lateinit var binding: ActivityCreateChatBinding

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, CreateChatActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel.liveData.observe(this, { result ->
            result?.let { (state, data) ->
                when (state) {
                    ViewState.SHOW_NEW_CHAT_SCREEN -> {
                        val fragment = NewChatFragment.newInstance(NewChatListenerImpl())
                        supportFragmentManager.beginTransaction().replace(binding.frameLayout.id, fragment)
                                .addToBackStack(NewChatFragment.TAG).commit()
                    }
                    ViewState.SHOW_NAME_CHAT_SCREEN -> {
                        val fragment = ChatNameFragment.newInstance(CreateDialogListenerImpl())
                        supportFragmentManager.beginTransaction().replace(binding.frameLayout.id, fragment)
                                .addToBackStack(ChatNameFragment.TAG).commit()
                    }
                    ViewState.SHOW_CHAT_SCREEN -> {
                        val dialog = data as QBChatDialog
                        ChatActivity.start(this@CreateChatActivity, dialog.dialogId)
                        finish()
                    }
                    ViewState.CREATE_CHAT -> {
                        showProgress()
                        val chatName = data as String
                        viewModel.createChat(chatName)
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

    override fun showProgress() {
        binding.progressBar.visibility = View.VISIBLE
    }

    override fun hideProgress() {
        binding.progressBar.visibility = View.GONE
    }

    override fun onBackPressed() {
        val count = supportFragmentManager.backStackEntryCount
        if (count == 1) {
            finish()
        } else {
            supportFragmentManager.popBackStack()
        }
    }

    private inner class NewChatListenerImpl : NewChatFragment.NewChatListener {
        override fun onSelectedUsers(selectedUsers: ArraySet<QBUser>) {
            viewModel.onSelectedUsers(selectedUsers)
        }
    }

    private inner class CreateDialogListenerImpl : ChatNameFragment.CreateDialogListener {
        override fun createDialog(chatName: String) {
            viewModel.onCreateChat(chatName)
        }
    }
}