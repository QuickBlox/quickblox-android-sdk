package com.quickblox.sample.videochat.kotlin.activities

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.quickblox.auth.session.QBSettings
import com.quickblox.sample.videochat.kotlin.BuildConfig
import com.quickblox.sample.videochat.kotlin.R

class AppInfoActivity : AppCompatActivity() {
    private lateinit var appVersionTextView: TextView
    private lateinit var sdkVersionTextView: TextView
    private lateinit var appIDTextView: TextView
    private lateinit var authKeyTextView: TextView
    private lateinit var authSecretTextView: TextView
    private lateinit var accountKeyTextView: TextView
    private lateinit var apiDomainTextView: TextView
    private lateinit var chatDomainTextView: TextView
    private lateinit var appQAVersionTextView: TextView

    companion object {
        fun start(context: Context) = context.startActivity(Intent(context, AppInfoActivity::class.java))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_appinfo)
        initUI()
        fillUI()
    }

    private fun initUI() {
        appVersionTextView = findViewById(R.id.text_app_version)
        sdkVersionTextView = findViewById(R.id.text_sdk_version)
        appIDTextView = findViewById(R.id.text_app_id)
        authKeyTextView = findViewById(R.id.text_auth_key)
        authSecretTextView = findViewById(R.id.text_auth_secret)
        accountKeyTextView = findViewById(R.id.text_account_key)
        apiDomainTextView = findViewById(R.id.text_api_domain)
        chatDomainTextView = findViewById(R.id.text_chat_domain)
        appQAVersionTextView = findViewById(R.id.text_qa_version)
    }

    private fun fillUI() {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.appinfo_title)
        appVersionTextView.text = BuildConfig.VERSION_NAME
        sdkVersionTextView.text = com.quickblox.BuildConfig.VERSION_NAME
        appIDTextView.text = QBSettings.getInstance().applicationId
        authKeyTextView.text = QBSettings.getInstance().authorizationKey
        authSecretTextView.text = QBSettings.getInstance().authorizationSecret
        accountKeyTextView.text = QBSettings.getInstance().accountKey
        apiDomainTextView.text = QBSettings.getInstance().serverApiDomain
        chatDomainTextView.text = QBSettings.getInstance().chatEndpoint

        if (BuildConfig.IS_QA) {
            val appVersion = BuildConfig.VERSION_NAME
            val versionQACode = BuildConfig.VERSION_QA_CODE.toString()
            val qaVersion = "$appVersion.$versionQACode"
            val spannable = SpannableString(qaVersion)
            spannable.setSpan(ForegroundColorSpan(Color.RED), appVersion.length + 1,
                    qaVersion.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            appQAVersionTextView.setText(spannable, TextView.BufferType.SPANNABLE)
            appQAVersionTextView.visibility = View.VISIBLE

            findViewById<View>(R.id.text_qa_version_title).visibility = View.VISIBLE
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}