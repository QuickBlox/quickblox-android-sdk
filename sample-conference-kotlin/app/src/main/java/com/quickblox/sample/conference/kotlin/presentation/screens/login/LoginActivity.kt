package com.quickblox.sample.conference.kotlin.presentation.screens.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.view.View
import android.widget.Toast
import com.quickblox.sample.conference.kotlin.R
import com.quickblox.sample.conference.kotlin.databinding.ActivityLoginBinding
import com.quickblox.sample.conference.kotlin.presentation.screens.appinfo.AppInfoActivity
import com.quickblox.sample.conference.kotlin.presentation.screens.base.BaseActivity
import com.quickblox.sample.conference.kotlin.presentation.screens.main.MainActivity
import com.quickblox.sample.conference.kotlin.presentation.utils.*
import dagger.hilt.android.AndroidEntryPoint

/*
 * Created by Injoit in 2021-09-30.
 * Copyright © 2021 Quickblox. All rights reserved.
 */
@AndroidEntryPoint
class LoginActivity : BaseActivity<LoginViewModel>(LoginViewModel::class.java) {
    private lateinit var binding: ActivityLoginBinding

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbarLogin.inflateMenu(R.menu.menu_login)
        binding.toolbarLogin.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.appInfo -> {
                    AppInfoActivity.start(this@LoginActivity)
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
                        Toast.makeText(baseContext, "$data", Toast.LENGTH_SHORT).show()
                    }
                    ViewState.SHOW_MAIN_SCREEN -> {
                        hideProgress()
                        MainActivity.start(this@LoginActivity)
                        finish()
                    }
                }
            }
        })
        initListeners()
    }

    private fun initListeners() {
        binding.etLogin.addTextChangedListener(object : SimpleTextWatcher() {
            override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {
                super.onTextChanged(charSequence, start, before, count)
                val validString = charSequence.toString().oneSpace()

                if (charSequence.toString() != validString) {
                    binding.etLogin.setText(validString)
                    binding.etLogin.setSelection(start)
                }
            }

            override fun afterTextChanged(editable: Editable?) {
                if (binding.etLogin.isValidLogin()) {
                    binding.tvLoginHint.visibility = View.GONE
                } else {
                    binding.tvLoginHint.visibility = View.VISIBLE
                }
                validationField()
            }
        })

        binding.etName.addTextChangedListener(object : SimpleTextWatcher() {
            override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {
                super.onTextChanged(charSequence, start, before, count)
                val validString = charSequence.toString().oneSpace()

                if (charSequence.toString() != validString) {
                    binding.etName.setText(validString)
                    binding.etName.setSelection(start)
                }
            }

            override fun afterTextChanged(editable: Editable?) {
                if (binding.etName.isValidName()) {
                    binding.tvNameHint.visibility = View.GONE
                } else {
                    binding.tvNameHint.visibility = View.VISIBLE
                }
                validationField()
            }
        })

        binding.btnLogin.setOnClickListener {
            KeyboardUtils.hideKeyboard(binding.btnLogin)
            viewModel.signIn(
                    binding.etLogin.text.toString().trim(),
                    binding.etName.text.toString().trim()
            )
        }
    }

    private fun validationField() {
        binding.btnLogin.isEnabled = binding.etLogin.isValidLogin() && binding.etName.isValidName()
    }

    override fun showProgress() {
        binding.progressBar.visibility = View.VISIBLE
    }

    override fun hideProgress() {
        binding.progressBar.visibility = View.GONE
    }
}