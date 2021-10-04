package com.quickblox.sample.conference.kotlin.presentation.screens.settings.video

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.quickblox.sample.conference.kotlin.databinding.ActivityVideoSettingsBinding
import com.quickblox.sample.conference.kotlin.presentation.screens.base.BaseActivity
import com.quickblox.sample.conference.kotlin.presentation.screens.login.LoginActivity
import com.quickblox.sample.conference.kotlin.presentation.screens.settings.views.CustomSeekBar
import com.quickblox.sample.conference.kotlin.presentation.screens.settings.views.selectionlist.CustomSelectionView
import dagger.hilt.android.AndroidEntryPoint

/*
 * Created by Injoit in 2021-09-30.
 * Copyright © 2021 Quickblox. All rights reserved.
 */
@AndroidEntryPoint
class VideoSettingsActivity : BaseActivity<VideoSettingsViewModel>(VideoSettingsViewModel::class.java) {
    private lateinit var binding: ActivityVideoSettingsBinding

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, VideoSettingsActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVideoSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel.liveData.observe(this, { result ->
            result?.let { (state, _) ->
                when (state) {
                    ViewState.SHOW_LOGIN_SCREEN -> {
                        LoginActivity.start(this)
                        finish()
                    }
                }
            }
        })
        binding.selectionList.setList(viewModel.getResolutions(), viewModel.callSettings?.getVideoResolution())
        binding.seekbarFrameRate.setSeekBarValue(viewModel.callSettings?.getFps())
        binding.seekbarBitrate.setSeekBarValue(viewModel.callSettings?.getBandwidth())
        binding.selectionList.setCallBack(object : CustomSelectionView.SelectionCallBack {
            override fun changedValue(value: Int?) {
                value?.let {
                    viewModel.callSettings?.setVideoResolution(value)
                }
            }
        })
        binding.seekbarFrameRate.setCallBack(object : CustomSeekBar.SeekBarCAllBack {
            override fun changedValue(value: Int?) {
                value?.let {
                    viewModel.callSettings?.setFps(value)
                }
            }
        })
        binding.seekbarBitrate.setCallBack(object : CustomSeekBar.SeekBarCAllBack {
            override fun changedValue(value: Int?) {
                value?.let {
                    viewModel.callSettings?.setBandwidth(value)
                }
            }
        })

        binding.flBack.setOnClickListener {
            onBackPressed()
        }
    }

    override fun showProgress() {
        // empty
    }

    override fun hideProgress() {
        // empty
    }
}