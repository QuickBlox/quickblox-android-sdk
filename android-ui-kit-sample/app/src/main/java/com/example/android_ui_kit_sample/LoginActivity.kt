/*
 * Created by Injoit on 17.5.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 *
 */
package com.example.android_ui_kit_sample

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.android_ui_kit_sample.databinding.ActivityLoginBinding
import com.quickblox.android_ui_kit.QuickBloxUiKit
import com.quickblox.android_ui_kit.presentation.screens.dialogs.DialogsActivity
import com.quickblox.android_ui_kit.presentation.theme.UiKitTheme
import com.quickblox.core.QBEntityCallback
import com.quickblox.core.exception.QBResponseException
import com.quickblox.users.QBUsers
import com.quickblox.users.model.QBUser

class LoginActivity : AppCompatActivity() {
    private val TAG = LoginActivity::class.java.simpleName

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        setThemeQBUiKit(DarkUiKitTheme())

        loginClickListener()
        signupClickListener()
    }

    override fun onResume() {
        super.onResume()
        signOut()
    }

    private fun setThemeQBUiKit(theme: UiKitTheme) {
        QuickBloxUiKit.setTheme(theme)
    }

    private fun loginClickListener() {
        binding.btnLogin.setOnClickListener {
            hideKeyboard(it)

            binding.progressBar.visibility = View.VISIBLE
            binding.btnLogin.isEnabled = false

            val user = buildUser()
            QBUsers.signIn(user).performAsync(object : QBEntityCallback<QBUser> {
                override fun onSuccess(user: QBUser?, bundle: Bundle?) {
                    binding.progressBar.visibility = View.GONE
                    binding.btnLogin.isEnabled = true
                    initQBUiKit()

                    showQBUiKit()
                }

                override fun onError(exception: QBResponseException) {
                    binding.progressBar.visibility = View.GONE
                    binding.btnLogin.isEnabled = true
                    Toast.makeText(this@LoginActivity, exception.message, Toast.LENGTH_LONG).show()
                }
            })
        }
    }

    private fun signupClickListener() {
        binding.btnSignUp.setOnClickListener {
            SignUpActivity.show(this)
        }
    }

    private fun signOut() {
        QBUsers.signOut().performAsync(object : QBEntityCallback<Void?> {
            override fun onSuccess(aVoid: Void?, bundle: Bundle) {
                Log.d(TAG, "onSuccess: signOut")
            }

            override fun onError(exception: QBResponseException) {
                Log.d(TAG, "onError: signOut")
            }
        })
    }

    private fun buildUser(): QBUser {
        val user = QBUser()
        user.login = binding.etLogin.text.toString().trim()
        user.password = binding.etPassword.text.toString().trim()
        return user
    }

    private fun initQBUiKit() {
        QuickBloxUiKit.init(applicationContext)
    }

    private fun showQBUiKit() {
        DialogsActivity.show(this)
    }

    private fun hideKeyboard(view: View?) {
        view?.post {
            view.context
            view.clearFocus()
            val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }
}