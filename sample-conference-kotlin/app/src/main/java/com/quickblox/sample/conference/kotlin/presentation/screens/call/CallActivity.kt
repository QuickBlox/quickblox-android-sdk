package com.quickblox.sample.conference.kotlin.presentation.screens.call

import android.content.Context
import android.content.Intent
import android.graphics.Insets
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.view.View
import android.view.WindowInsets
import android.view.animation.*
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.core.view.isVisible
import com.quickblox.conference.QBConferenceRole
import com.quickblox.sample.conference.kotlin.R
import com.quickblox.sample.conference.kotlin.data.service.CallService
import com.quickblox.sample.conference.kotlin.databinding.ActivityCallBinding
import com.quickblox.sample.conference.kotlin.domain.call.CallManagerImpl.Companion.CallType.Companion.STREAM
import com.quickblox.sample.conference.kotlin.domain.call.entities.CallEntity
import com.quickblox.sample.conference.kotlin.presentation.screens.base.BaseActivity
import com.quickblox.sample.conference.kotlin.presentation.screens.chat.ChatActivity
import com.quickblox.sample.conference.kotlin.presentation.screens.login.LoginActivity
import com.quickblox.sample.conference.kotlin.presentation.screens.muteparticipants.MuteParticipantsActivity
import com.quickblox.sample.conference.kotlin.presentation.utils.Constants.MEDIA_PROJECT
import com.quickblox.sample.conference.kotlin.presentation.utils.hideWithAnimation
import com.quickblox.sample.conference.kotlin.presentation.utils.setOnClick
import com.quickblox.sample.conference.kotlin.presentation.utils.showWithAnimation
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

private const val WAKEUP_DELAY = 5000L

/*
 * Created by Injoit in 2021-09-30.
 * Copyright © 2021 Quickblox. All rights reserved.
 */
@AndroidEntryPoint
class CallActivity : BaseActivity<CallViewModel>(CallViewModel::class.java) {
    private var controlButtons: ControlsTimer? = null
    private lateinit var binding: ActivityCallBinding

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, CallActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCallBinding.inflate(layoutInflater)
        setContentView(binding.root)
        controlButtons = ControlsTimer()

        if (!CallService.isRunning()) {
            CallService.start(this)
        }
        viewModel.liveData.observe(this, { result ->
            result?.let { (state, data) ->
                when (state) {
                    ViewState.PROGRESS -> {
                        showProgress()
                    }
                    ViewState.ERROR -> {
                        hideProgress()
                        Toast.makeText(baseContext, "$data", Toast.LENGTH_SHORT).show()
                    }
                    ViewState.SHOW_VIDEO_TRACK -> {
                        hideProgress()
                        val metrics = getScreenMetrics()
                        binding.llConversation.updateViews(metrics.second, metrics.first)
                    }
                    ViewState.START_SHARING -> {
                        hideProgress()
                        controlButtons?.stopTimer()
                        setSharingState()
                    }
                    ViewState.STOP_SERVICE -> {
                        hideProgress()
                        viewModel.currentDialog?.dialogId?.let { ChatActivity.start(this@CallActivity, it) }
                        CallService.stop(this)
                        finish()
                    }
                    ViewState.COUNT_PARTICIPANTS -> {
                        val count = data as Int
                        val countString = count.toString()
                        binding.tvMembersCount.text = getString(R.string.online_participants_label, countString)
                    }
                    ViewState.CONVERSATION -> {
                        hideProgress()
                        setConversationState()
                        if (viewModel.isFullScreenState) {
                            binding.llConversation.setCallEntities(viewModel.callEntities)
                        }
                        val metrics = getScreenMetrics()
                        binding.llConversation.updateViews(metrics.second, metrics.first)
                    }
                    ViewState.STREAM -> {
                        hideProgress()
                        setStreamState()
                        if (viewModel.isFullScreenState) {
                            binding.llConversation.setCallEntities(viewModel.callEntities)
                        }
                        val metrics = getScreenMetrics()
                        binding.llConversation.updateViews(metrics.second, metrics.first)
                    }
                    ViewState.ONLINE_PARTICIPANTS -> {
                        val isOnline = data as Boolean
                        if (viewModel.getRole() == QBConferenceRole.LISTENER) {
                            if (isOnline) {
                                binding.ivStreamLabel.setImageResource(R.drawable.live_streaming)
                                binding.tvStreamPlaceHolder.visibility = View.GONE
                            } else {
                                binding.ivStreamLabel.setImageResource(R.drawable.off_line)
                                binding.tvStreamPlaceHolder.visibility = View.VISIBLE
                            }
                        }
                    }
                    ViewState.OPEN_FULL_SCREEN -> {
                        val entities = data as HashSet<CallEntity>
                        hideProgress()
                        binding.llConversation.setCallEntities(entities)
                        val metrics = getScreenMetrics()
                        binding.llConversation.updateViews(metrics.second, metrics.first)
                    }
                    ViewState.BIND_SERVICE -> {
                        val intent = Intent(this, CallService::class.java)
                        viewModel.callServiceConnection?.let {
                            bindService(intent, it, BIND_AUTO_CREATE)
                        }
                        if (viewModel.callEntities.isNotEmpty()) {
                            val metrics = getScreenMetrics()
                            binding.llConversation.updateViews(metrics.second, metrics.first)
                        }
                    }
                    ViewState.UNBIND_SERVICE -> {
                        viewModel.callServiceConnection?.let {
                            unbindService(it)
                        }
                    }
                    ViewState.SHOW_LOGIN_SCREEN -> {
                        CallService.stop(this)
                        LoginActivity.start(this)
                        finish()
                    }
                }
            }
        })
        binding.llConversation.setCallEntities(viewModel.callEntities)
        binding.llConversation.setClickListener(ConversationItemListenerImpl())

        val activityResultLauncher = registerForActivityResult(StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                result.data?.let { viewModel.startSharing(it) }
                binding.buttons.tbShare.isChecked = false
            } else {
                binding.buttons.tbShare.isChecked = true
                controlButtons?.displayControlsButtons()
            }
        }
        if (viewModel.isShowSharing()) {
            controlButtons?.stopTimer()
            setSharingState()
        } else {
            when (viewModel.getCallType()) {
                STREAM -> setStreamState()
                else -> setConversationState()
            }
        }
        setClickListeners(activityResultLauncher)
    }

    private fun setStreamState() {
        controlButtons?.displayControlsButtons()
        binding.ivStreamLabel.visibility = View.VISIBLE
        binding.tvMembersCount.visibility = View.VISIBLE
        binding.btnMuteParticipants.visibility = View.GONE

        if (viewModel.getRole() == QBConferenceRole.LISTENER) {
            binding.buttons.tbCamEnable.visibility = View.GONE
            binding.buttons.tbMic.visibility = View.GONE
            binding.buttons.tbSwapCam.visibility = View.GONE
            binding.buttons.tbShare.visibility = View.GONE
            binding.tvMembersCount.visibility = View.GONE
        } else {
            binding.vSharing.visibility = View.GONE
            binding.llConversation.visibility = View.VISIBLE
            binding.buttons.tbEndCall.visibility = View.VISIBLE
            binding.buttons.tbCamEnable.visibility = View.VISIBLE
            binding.buttons.tbMic.visibility = View.VISIBLE
            binding.buttons.tbSwapCam.visibility = View.VISIBLE
            setDefaultButtonsState()
        }
    }

    private fun setSharingState() {
        binding.vSharing.visibility = View.VISIBLE
        binding.llConversation.visibility = View.GONE
        binding.buttons.tbEndCall.visibility = View.GONE
        binding.buttons.tbCamEnable.visibility = View.GONE
        binding.buttons.tbMic.visibility = View.GONE
        binding.buttons.tbSwapCam.visibility = View.GONE
        val isVideoEnabled = viewModel.isVideoEnabled()
        if (isVideoEnabled == true) {
            viewModel.setVideoEnabled(true)
        }
        val isShowSharing = viewModel.isShowSharing()
        binding.buttons.tbShare.isChecked = !isShowSharing
    }

    private fun setConversationState() {
        controlButtons?.displayControlsButtons()
        binding.tvTitle.text = viewModel.currentDialog?.name
        binding.vSharing.visibility = View.GONE
        binding.tvMembersCount.visibility = View.GONE
        binding.ivStreamLabel.visibility = View.GONE
        binding.llConversation.visibility = View.VISIBLE
        binding.buttons.tbEndCall.visibility = View.VISIBLE
        binding.buttons.tbCamEnable.visibility = View.VISIBLE
        binding.buttons.tbMic.visibility = View.VISIBLE
        binding.buttons.tbSwapCam.visibility = View.VISIBLE
        setDefaultButtonsState()
    }

    private fun setDefaultButtonsState() {
        val isShowSharing = viewModel.isShowSharing()
        viewModel.getEnableVideoState()?.let { viewModel.setVideoEnabled(it) }
        val isAudioEnabled = viewModel.isAudioEnabled()
        val isFrontCamera = viewModel.isFrontCamera()
        val isVideoEnabled = viewModel.isVideoEnabled()

        isAudioEnabled?.let { binding.buttons.tbMic.isChecked = isAudioEnabled }
        binding.buttons.tbSwapCam.isChecked = isFrontCamera
        binding.buttons.tbShare.isChecked = !isShowSharing

        isVideoEnabled?.let { binding.buttons.tbCamEnable.isChecked = isVideoEnabled }
    }

    private fun getScreenMetrics(): Pair<Int, Int> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowMetrics = windowManager.currentWindowMetrics
            val insets: Insets = windowMetrics.windowInsets.getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())
            Pair(windowMetrics.bounds.width() - insets.left - insets.right, windowMetrics.bounds.height() - insets.top - insets.bottom)
        } else {
            val displayMetrics = DisplayMetrics()
            windowManager.defaultDisplay.getMetrics(displayMetrics)
            Pair(displayMetrics.widthPixels, displayMetrics.heightPixels)
        }
    }

    private fun setClickListeners(activityResultLauncher: ActivityResultLauncher<Intent>) {
        binding.buttons.tbSwapCam.setOnCheckedChangeListener { _, _ ->
            viewModel.swapCamera()
        }
        binding.buttons.tbMic.setOnClickListener {
            val isEnable = viewModel.isAudioEnabled()
            isEnable?.let {
                viewModel.setAudioEnabled(!isEnable)
            }
        }
        binding.buttons.tbShare.setOnClickListener {
            if (viewModel.isShowSharing()) {
                viewModel.stopSharing()
            } else {
                controlButtons?.stopTimer()
                val mMediaProjectionManager = getSystemService(MEDIA_PROJECT) as MediaProjectionManager
                activityResultLauncher.launch(mMediaProjectionManager.createScreenCaptureIntent())
            }
        }
        binding.buttons.tbCamEnable.setOnClickListener {
            val isEnable = viewModel.isVideoEnabled()
            isEnable?.let { viewModel.setVideoEnabled(!isEnable) }
        }
        binding.buttons.tbEndCall.setOnCheckedChangeListener { _, _ ->
            viewModel.leaveSession()
        }
        binding.btnChat.setOnClick {
            viewModel.releaseViews()
            viewModel.currentDialog?.dialogId?.let { it1 -> ChatActivity.start(this@CallActivity, it1) }
        }
        binding.root.setOnClickListener {
            controlButtons?.displayControlsButtons()
        }
        binding.btnMuteParticipants.setOnClickListener {
            viewModel.releaseViews()
            MuteParticipantsActivity.start(this@CallActivity)
        }
    }

    private fun showButtons() {
        binding.rlToolbar.showWithAnimation()
        binding.buttons.root.showWithAnimation()
    }

    private fun hideButtons() {
        binding.rlToolbar.hideWithAnimation()
        binding.buttons.root.hideWithAnimation()
    }

    override fun onBackPressed() {
        // empty
    }

    override fun showProgress() {
        binding.progressBar.visibility = View.VISIBLE
    }

    override fun hideProgress() {
        binding.progressBar.visibility = View.GONE
    }

    private inner class ControlsTimer {
        private var timer: Timer? = null

        fun displayControlsButtons() {
            if (!binding.buttons.root.isVisible) {
                // Some devices call showButtons() on not the main thread.
                Handler(Looper.getMainLooper()).post {
                    showButtons()
                }
            }
            stopTimer()
            timer = Timer()
            timer?.schedule(object : TimerTask() {
                override fun run() {
                    //Some devices call hideButtons() on not the main thread.
                    Handler(Looper.getMainLooper()).post {
                        hideButtons()
                    }
                }
            }, WAKEUP_DELAY)
        }

        fun stopTimer() {
            timer?.cancel()
            timer?.purge()
            timer = null
        }
    }

    private inner class ConversationItemListenerImpl : CustomLinearLayout.ConversationItemListener {
        override fun onItemClick(callEntity: CallEntity) {
            if (viewModel.callEntities.size > 1) {
                if (binding.buttons.root.isVisible) {
                    if (viewModel.isFullScreenState) {
                        viewModel.releaseViews()
                        viewModel.closeFullScreen()
                    } else {
                        viewModel.releaseViews()
                        viewModel.openFullScreen(callEntity)
                    }
                }
            }
            controlButtons?.displayControlsButtons()
        }
    }
}