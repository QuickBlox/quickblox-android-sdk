/*
 * Created by Injoit on 6.7.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 *
 */

package com.example.android_ui_kit_sample

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.example.android_ui_kit_sample.databinding.ActivitySignupBinding
import com.quickblox.android_ui_kit.QuickBloxUiKit
import com.quickblox.android_ui_kit.presentation.screens.dialogs.DialogsActivity
import com.quickblox.core.QBEntityCallback
import com.quickblox.core.exception.QBResponseException
import com.quickblox.users.QBUsers
import com.quickblox.users.model.QBUser

class SignUpActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignupBinding

    companion object {
        fun show(context: Context) {
            val intent = Intent(context, SignUpActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        configureToolbar()
        onBackPressedClickListener()

        signUpClickListener()
    }

    private fun configureToolbar() {
        setSupportActionBar(binding.toolbar)
        if (supportActionBar != null) {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.setDisplayShowHomeEnabled(true)
            supportActionBar?.title = getString(R.string.signup)
        }
    }

    private fun onBackPressedClickListener() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        })
    }

    private fun signUpClickListener() {
        binding.btnSignUp.setOnClickListener {
            binding.btnSignUp.isEnabled = false

            val user = buildUser()
            signUp(user)
        }
    }

    private fun buildUser(): QBUser {
        val user = QBUser()
        user.login = binding.etLogin.text.toString().trim()
        user.fullName = binding.etDisplayName.text.toString().trim()
        user.password = binding.etPassword.text.toString().trim()
        return user
    }

    private fun signUp(user: QBUser) {
        QBUsers.signUp(user).performAsync(object : QBEntityCallback<QBUser> {
            override fun onSuccess(userFromRest: QBUser, bundle: Bundle) {
                logIn(user)
            }

            override fun onError(exception: QBResponseException) {
                Toast.makeText(this@SignUpActivity, exception.message, Toast.LENGTH_LONG).show()
                binding.btnSignUp.isEnabled = true
            }
        })
    }

    private fun logIn(user: QBUser) {
        QBUsers.signIn(user).performAsync(object : QBEntityCallback<QBUser> {
            override fun onSuccess(user: QBUser?, bundle: Bundle?) {
                binding.btnSignUp.isEnabled = true
                initAndShowQBUiKit()
                finish()
            }

            override fun onError(exception: QBResponseException) {
                binding.btnSignUp.isEnabled = true
                Toast.makeText(this@SignUpActivity, exception.message, Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun initAndShowQBUiKit() {
        QuickBloxUiKit.init(applicationContext)
        DialogsActivity.show(this)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}