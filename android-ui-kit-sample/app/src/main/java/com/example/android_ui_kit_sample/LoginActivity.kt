/*
 * Created by Injoit on 17.5.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 *
 */
package com.example.android_ui_kit_sample

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.android_ui_kit_sample.databinding.ActivityLoginBinding
import com.quickblox.android_ui_kit.QuickBloxUiKit
import com.quickblox.android_ui_kit.presentation.screens.dialogs.DialogsActivity
import com.quickblox.android_ui_kit.presentation.theme.DarkUiKitTheme
import com.quickblox.android_ui_kit.presentation.theme.UiKitTheme
import com.quickblox.core.QBEntityCallback
import com.quickblox.core.exception.QBResponseException
import com.quickblox.users.QBUsers
import com.quickblox.users.model.QBUser

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setThemeQBUiKit(DarkUiKitTheme())

        binding.btnLogin.setOnClickListener {
            hideKeyboard(it)

            val user = buildUser()
            QBUsers.signIn(user).performAsync(object : QBEntityCallback<QBUser> {
                override fun onSuccess(user: QBUser?, bundle: Bundle?) {
                    initAndShowQBUiKit()
                }

                override fun onError(exception: QBResponseException) {
                    Toast.makeText(this@LoginActivity, exception.message, Toast.LENGTH_LONG).show()
                }
            })
        }
    }

    private fun setThemeQBUiKit(theme: UiKitTheme) {
        QuickBloxUiKit.setTheme(theme)
    }

    private fun buildUser(): QBUser {
        val user = QBUser()
        user.login = binding.etLogin.text.toString().trim()
        user.password = binding.etPassword.text.toString().trim()
        return user
    }

    private fun initAndShowQBUiKit() {
        QuickBloxUiKit.init(applicationContext)
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