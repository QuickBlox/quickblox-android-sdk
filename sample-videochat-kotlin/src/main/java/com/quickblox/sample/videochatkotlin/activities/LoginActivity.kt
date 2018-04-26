package com.quickblox.sample.videochatkotlin.activities

import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.support.annotation.StringRes
import android.util.Log
import android.view.KeyEvent
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.quickblox.sample.core.ui.activity.CoreBaseActivity
import com.quickblox.sample.core.utils.Toaster
import com.quickblox.sample.videochatkotlin.R
import com.quickblox.sample.videochatkotlin.services.CallService
import com.quickblox.sample.videochatkotlin.utils.*
import com.quickblox.users.model.QBUser

/**
 * Created by Roman on 09.04.2018.
 */
class LoginActivity : CoreBaseActivity() {

    val TAG = LoginActivity::class.java.simpleName
    lateinit var progressDialog: ProgressDialog
    lateinit var users: ArrayList<QBUser>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        setActionBarTitle(R.string.title_login_activity)
        initFields();
        iniQBUsers()
        initButtons()
    }

    private fun initFields() {
        progressDialog = ProgressDialog(this)
    }

    private fun iniQBUsers() {
        users = getAllUsersFromFile(SAMPLE_CONFIG_FILE_NAME)
    }

    private fun usersCount(): Int {
        return users.size
    }

    private fun initButtons() {
        for (i in 1..usersCount()) {
            val myButton = Button(this)
            myButton.text = String.format(getString(R.string.user), i)
            myButton.setOnClickListener({
                Log.d(TAG, "users.get(i)= $i")
                loginToChat(users.get(i - 1))
//                myButton.isEnabled = false
            })

            val ll: LinearLayout = findViewById(R.id.button_layout)
            val lp = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT)
            ll.addView(myButton, lp)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == EXTRA_LOGIN_RESULT_CODE) {
            hideProgressDialog()
            val isLoginSuccess = data.getBooleanExtra(EXTRA_LOGIN_RESULT, false)
            val errorMessage = data.getStringExtra(EXTRA_LOGIN_ERROR_MESSAGE)

            if (isLoginSuccess) {
//                signInCreatedUser(userForSave, false)
                startCallActivity()
            } else {
                Toaster.longToast(getString(R.string.login_chat_login_error) + errorMessage)
            }
        }
    }

    private fun startCallActivity() {
        val intent = Intent(this, CallActivity::class.java)
        intent.putExtra(EXTRA_QB_USERS_LIST, users)
        startActivity(intent)
    }

    fun loginToChat(user: QBUser) {

        Log.d(TAG, "loginToChat user= " + user)
        showProgressDialog(R.string.dlg_login)

        val intent = Intent(this, CallService::class.java)
        val pendingIntent = createPendingResult(EXTRA_LOGIN_RESULT_CODE, intent, 0)
        intent.putExtra(EXTRA_COMMAND_TO_SERVICE, COMMAND_LOGIN)
        intent.putExtra(EXTRA_QB_USER, user)
        intent.putExtra(EXTRA_PENDING_INTENT, pendingIntent)
        startService(intent)
    }

    internal fun showProgressDialog(@StringRes messageId: Int) {
        progressDialog.setIndeterminate(true)
        progressDialog.setCancelable(false)
        progressDialog.setCanceledOnTouchOutside(false)

        // Disable the back button
        val keyListener = DialogInterface.OnKeyListener { dialog, keyCode, event -> keyCode == KeyEvent.KEYCODE_BACK }
        progressDialog.setOnKeyListener(keyListener)


        progressDialog.setMessage(getString(messageId))

        progressDialog.show()

    }

    fun hideProgressDialog() {
        progressDialog.dismiss()
    }
}