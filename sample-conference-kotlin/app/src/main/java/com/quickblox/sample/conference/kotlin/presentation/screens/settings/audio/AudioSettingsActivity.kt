package com.quickblox.sample.conference.kotlin.presentation.screens.settings.audio

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.quickblox.sample.conference.kotlin.databinding.ActivityAudioSettingsBinding
import com.quickblox.sample.conference.kotlin.presentation.screens.base.BaseActivity
import com.quickblox.sample.conference.kotlin.presentation.screens.login.LoginActivity
import dagger.hilt.android.AndroidEntryPoint

/*
 * Created by Injoit in 2021-09-30.
 * Copyright © 2021 Quickblox. All rights reserved.
 */
@AndroidEntryPoint
class AudioSettingsActivity : BaseActivity<AudioSettingsViewModel>(AudioSettingsViewModel::class.java) {
    private lateinit var binding: ActivityAudioSettingsBinding

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, AudioSettingsActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAudioSettingsBinding.inflate(layoutInflater)
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
        viewModel.callSettings?.getBuildInAEC()?.let { aec ->
            binding.cbAec.isChecked = aec
            binding.cbAec.setOnCheckedChangeListener { _, isChecked ->
                viewModel.callSettings?.setBuildInAEC(isChecked)
            }
        }
        viewModel.callSettings?.getOpenSLES()?.let { openSLES ->
            binding.cbOpenSles.isChecked = openSLES
            binding.cbOpenSles.setOnCheckedChangeListener { _, isChecked ->
                viewModel.callSettings?.setOpenSLES(isChecked)
            }
        }
        viewModel.callSettings?.getProcessing()?.let { audioProcessing ->
            binding.cbProcessing.isChecked = audioProcessing
            binding.cbProcessing.setOnCheckedChangeListener { _, isChecked ->
                viewModel.callSettings?.setProcessing(isChecked)
            }
        }

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