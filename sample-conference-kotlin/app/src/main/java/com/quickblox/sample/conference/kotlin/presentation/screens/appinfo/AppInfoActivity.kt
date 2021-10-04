package com.quickblox.sample.conference.kotlin.presentation.screens.appinfo

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.quickblox.auth.session.QBSettings
import com.quickblox.conference.ConferenceConfig
import com.quickblox.sample.conference.kotlin.BuildConfig
import com.quickblox.sample.conference.kotlin.databinding.ActivityAppInfoBinding
import com.quickblox.sample.conference.kotlin.presentation.screens.base.BaseActivity
import dagger.hilt.android.AndroidEntryPoint

/*
 * Created by Injoit in 2021-09-30.
 * Copyright © 2021 Quickblox. All rights reserved.
 */
@AndroidEntryPoint
class AppInfoActivity : BaseActivity<AppInfoViewModel>(AppInfoViewModel::class.java) {
    private lateinit var binding: ActivityAppInfoBinding

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, AppInfoActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAppInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.tvAppVersion.text = BuildConfig.VERSION_NAME
        binding.tvSdkVersion.text = com.quickblox.BuildConfig.VERSION_NAME
        binding.tvAppId.text = QBSettings.getInstance().applicationId
        binding.tvAuthKey.text = QBSettings.getInstance().authorizationKey
        binding.tvAuthSecret.text = QBSettings.getInstance().authorizationSecret
        binding.tvAccountKey.text = QBSettings.getInstance().accountKey
        binding.tvApiDomain.text = QBSettings.getInstance().serverApiDomain
        binding.tvChatDomain.text = QBSettings.getInstance().chatEndpoint
        binding.tvJanusServer.text = ConferenceConfig.getUrl()

        val appVersion = BuildConfig.VERSION_NAME
        val versionQACode = BuildConfig.VERSION_QA_CODE
        val qaVersion = "$appVersion.$versionQACode"
        binding.tvQaVersion.text = qaVersion

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