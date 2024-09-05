package com.quickblox.sample.conference.kotlin.presentation.screens.main

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.quickblox.chat.model.QBChatDialog
import com.quickblox.sample.conference.kotlin.R
import com.quickblox.sample.conference.kotlin.databinding.ActivityMainBinding
import com.quickblox.sample.conference.kotlin.databinding.PopupMainLayoutBinding
import com.quickblox.sample.conference.kotlin.presentation.screens.appinfo.AppInfoActivity
import com.quickblox.sample.conference.kotlin.presentation.screens.base.BaseActivity
import com.quickblox.sample.conference.kotlin.presentation.screens.chat.ChatActivity
import com.quickblox.sample.conference.kotlin.presentation.screens.createchat.CreateChatActivity
import com.quickblox.sample.conference.kotlin.presentation.screens.login.LoginActivity
import com.quickblox.sample.conference.kotlin.presentation.screens.main.DialogsAdapter.DialogsAdapterStates.Companion.DEFAULT
import com.quickblox.sample.conference.kotlin.presentation.screens.main.ViewState.Companion.DIALOG_UPDATED
import com.quickblox.sample.conference.kotlin.presentation.screens.main.ViewState.Companion.ERROR
import com.quickblox.sample.conference.kotlin.presentation.screens.main.ViewState.Companion.LIST_DIALOGS_UPDATED
import com.quickblox.sample.conference.kotlin.presentation.screens.main.ViewState.Companion.MOVE_TO_FIRST_DIALOG
import com.quickblox.sample.conference.kotlin.presentation.screens.main.ViewState.Companion.PROGRESS
import com.quickblox.sample.conference.kotlin.presentation.screens.main.ViewState.Companion.SHOW_CHAT_SCREEN
import com.quickblox.sample.conference.kotlin.presentation.screens.main.ViewState.Companion.SHOW_DIALOGS
import com.quickblox.sample.conference.kotlin.presentation.screens.main.ViewState.Companion.SHOW_LOGIN_SCREEN
import com.quickblox.sample.conference.kotlin.presentation.screens.main.ViewState.Companion.USER_ERROR
import com.quickblox.sample.conference.kotlin.presentation.screens.settings.audio.AudioSettingsActivity
import com.quickblox.sample.conference.kotlin.presentation.screens.settings.video.VideoSettingsActivity
import com.quickblox.sample.conference.kotlin.presentation.utils.AvatarUtils
import com.quickblox.sample.conference.kotlin.presentation.utils.convertToPx
import com.quickblox.users.model.QBUser
import com.vmadalin.easypermissions.EasyPermissions
import com.vmadalin.easypermissions.dialogs.SettingsDialog
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale

const val POPUP_MAIN_WIDTH = 200

/*
 * Created by Injoit in 2021-09-30.
 * Copyright © 2021 Quickblox. All rights reserved.
 */
@AndroidEntryPoint
class MainActivity : BaseActivity<MainViewModel>(MainViewModel::class.java){
    private lateinit var binding: ActivityMainBinding
    private var dialogsAdapter: DialogsAdapter? = null
    private val onScrollListenerImpl = OnScrollListenerImpl()

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, MainActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initView()
        initAdapter()
        showUser(viewModel.user)
        setAvatarClickListener()
        initScrollListeners()

        viewModel.liveData.observe(this) { result ->
            result?.let { (state, data) ->
                when (state) {
                    PROGRESS -> {
                        showProgress()
                    }

                    SHOW_LOGIN_SCREEN -> {
                        LoginActivity.start(this)
                        finish()
                    }

                    ERROR -> {
                        hideProgress()
                        Toast.makeText(baseContext, "$data", Toast.LENGTH_SHORT).show()
                    }

                    MOVE_TO_FIRST_DIALOG -> {
                        dialogsAdapter?.moveToFirst(data as QBChatDialog)
                    }

                    LIST_DIALOGS_UPDATED -> {
                        hideProgress()
                        dialogsAdapter?.notifyDataSetChanged()
                    }

                    DIALOG_UPDATED -> {
                        dialogsAdapter?.notifyItemChanged(data as Int)
                    }

                    USER_ERROR -> {
                        Toast.makeText(baseContext, getString(R.string.user_error), Toast.LENGTH_SHORT).show()
                        viewModel.singOut()
                    }

                    SHOW_DIALOGS -> {
                        hideProgress()
                        dialogsAdapter?.notifyDataSetChanged()
                        if (viewModel.getDialogs().isEmpty()) {
                            binding.rvDialogs.visibility = View.GONE
                            binding.tvPlaceHolder.visibility = View.VISIBLE
                        } else {
                            binding.rvDialogs.visibility = View.VISIBLE
                            binding.tvPlaceHolder.visibility = View.GONE
                        }
                    }

                    SHOW_CHAT_SCREEN -> {
                        val dialog = data as QBChatDialog
                        ChatActivity.start(this@MainActivity, dialog.dialogId)
                    }

                    ViewState.SHOW_CREATE_SCREEN -> {
                        CreateChatActivity.start(this@MainActivity)
                    }
                }
            }
        }
    }

    private fun initScrollListeners() {
        val mLayoutManager = LinearLayoutManager(this)
        binding.rvDialogs.layoutManager = mLayoutManager
        // TODO: 6/10/21  Commented the scroll for pagination
        //binding.rvDialogs.addOnScrollListener(onScrollListenerImpl)
    }

    private fun initAdapter() {
        dialogsAdapter = DialogsAdapter(viewModel.getDialogs(), object : DialogsAdapter.DialogAdapterListener {
            override fun onChanged(state: Int) {
                when (state) {
                    DialogsAdapter.DialogsAdapterStates.SELECT -> {
                        binding.toolbarMain.menu.clear()
                        binding.toolbarMain.inflateMenu(R.menu.menu_delete_dialogs)
                        binding.flBack.visibility = View.VISIBLE
                        binding.rlAvatar.visibility = View.GONE
                        binding.tvSubTitle.visibility = View.VISIBLE
                        binding.toolbarTitle.text = getString(R.string.delete_chats)
                        binding.flBack.setOnClickListener {
                            binding.toolbarMain.inflateMenu(R.menu.menu_main)
                            binding.toolbarMain.menu.clear()
                            binding.toolbarMain.inflateMenu(R.menu.menu_main)
                            dialogsAdapter?.setState(DEFAULT)
                        }
                    }

                    DEFAULT -> {
                        binding.toolbarMain.menu.clear()
                        binding.toolbarMain.inflateMenu(R.menu.menu_main)
                        binding.toolbarTitle.text = getString(R.string.main_toolbar_title)
                        binding.flBack.visibility = View.GONE
                        binding.rlAvatar.visibility = View.VISIBLE
                        binding.tvSubTitle.visibility = View.GONE
                    }
                }
            }

            override fun onSelected(selectedCounter: Int) {
                if (selectedCounter != 0) {
                    binding.toolbarMain.menu.clear()
                    binding.toolbarMain.inflateMenu(R.menu.menu_delete_dialogs)
                } else {
                    binding.toolbarMain.menu.removeItem(R.id.deleteDialogs)
                }
                binding.tvSubTitle.text = if (selectedCounter > 1) {
                    getString(R.string.subtitle_main_chats, selectedCounter.toString())
                } else {
                    getString(R.string.subtitle_main_chat, selectedCounter.toString())
                }
            }

            override fun onDialogClicked(dialog: QBChatDialog) {
                viewModel.onDialogClicked(dialog)
            }
        })
        binding.rvDialogs.layoutManager = LinearLayoutManager(this)
        binding.rvDialogs.itemAnimator = null
        binding.rvDialogs.setHasFixedSize(true)
        binding.rvDialogs.adapter = dialogsAdapter
    }

    private fun initView() {
        binding.toolbarMain.inflateMenu(R.menu.menu_main)
        binding.toolbarMain.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.deleteDialogs -> {
                    openAlertDialog()
                }

                R.id.menuNewChat -> {
                    viewModel.showCreateScreen()
                }
            }
            return@setOnMenuItemClickListener true
        }
        binding.flBack.visibility = View.GONE
        binding.rlAvatar.visibility = View.VISIBLE
        binding.tvSubTitle.visibility = View.GONE
        binding.swipeRefresh.setOnRefreshListener {
            showProgress()
            viewModel.loadDialogs(refresh = true, reJoin = false)
        }
        binding.swipeRefresh.setColorSchemeColors(ContextCompat.getColor(baseContext, R.color.colorPrimary))
    }

    private fun setAvatarClickListener() {
        binding.rlAvatar.setOnClickListener {
            val layoutInflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val bindingPopUp = PopupMainLayoutBinding.inflate(layoutInflater)

            val popupWindow =
                PopupWindow(bindingPopUp.root, POPUP_MAIN_WIDTH.convertToPx(), ViewGroup.LayoutParams.WRAP_CONTENT)
            popupWindow.isOutsideTouchable = true
            popupWindow.showAsDropDown(it)

            bindingPopUp.tvName.text = viewModel.user?.fullName
            bindingPopUp.tvVideoConf.setOnClickListener {
                VideoSettingsActivity.start(this@MainActivity)
                popupWindow.dismiss()
            }
            bindingPopUp.tvAudioConf.setOnClickListener {
                AudioSettingsActivity.start(this@MainActivity)
                popupWindow.dismiss()
            }
            bindingPopUp.tvInfo.setOnClickListener {
                AppInfoActivity.start(this@MainActivity)
                popupWindow.dismiss()
            }
            bindingPopUp.tvLogout.setOnClickListener {
                viewModel.singOut()
                popupWindow.dismiss()
            }
        }
    }

    private fun showUser(user: QBUser?) {
        binding.tvAvatar.text = user?.fullName?.substring(0, 1)?.toUpperCase(Locale.getDefault())
        binding.ivAvatar.setImageDrawable(user?.id?.let {
            AvatarUtils.getDrawableAvatar(baseContext, it)
        })
    }

    override fun showProgress() {
        binding.swipeRefresh.isRefreshing = false
        binding.progressBar.visibility = View.VISIBLE
    }

    override fun hideProgress() {
        onScrollListenerImpl.isLoad = false
        binding.progressBar.visibility = View.GONE
    }

    private fun openAlertDialog() {
        val alertDialogBuilder = AlertDialog.Builder(this, R.style.AlertDialogStyle)
        alertDialogBuilder.setTitle(getString(R.string.delete_dialogs))
        alertDialogBuilder.setMessage(getString(R.string.delete_question))
        alertDialogBuilder.setCancelable(false)
        alertDialogBuilder.setPositiveButton(getString(R.string.delete)) { _, _ ->
            val list = arrayListOf<QBChatDialog>()
            dialogsAdapter?.getSelectedDialogs()?.let { list.addAll(it) }

            dialogsAdapter?.clearSelectedDialogs()
            leaveGroupDialogs(list)
            dialogsAdapter?.setState(DEFAULT)
        }
        alertDialogBuilder.setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
            dialog.dismiss()
        }
        alertDialogBuilder.create()
        alertDialogBuilder.show()
    }

    private fun leaveGroupDialogs(groupDialogsToDelete: ArrayList<QBChatDialog>) {
        viewModel.deleteDialogs(groupDialogsToDelete)
    }

    inner class OnScrollListenerImpl : RecyclerView.OnScrollListener() {
        var isLoad: Boolean = false

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            if (!recyclerView.canScrollVertically(1) || isLoad) {
                isLoad = true
                viewModel.loadDialogs(refresh = false, reJoin = false)
            }
        }
    }
}