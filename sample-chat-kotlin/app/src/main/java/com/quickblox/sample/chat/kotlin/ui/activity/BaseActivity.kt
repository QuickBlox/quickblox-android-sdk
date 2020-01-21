package com.quickblox.sample.chat.kotlin.ui.activity

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.view.KeyEvent
import android.view.MenuItem
import android.view.View
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import com.quickblox.chat.QBChatService
import com.quickblox.core.QBEntityCallback
import com.quickblox.core.exception.QBResponseException
import com.quickblox.sample.chat.kotlin.R
import com.quickblox.sample.chat.kotlin.utils.SharedPrefsHelper
import com.quickblox.sample.chat.kotlin.utils.chat.ChatHelper
import com.quickblox.sample.chat.kotlin.utils.showSnackbar
import com.quickblox.users.model.QBUser

private const val DUMMY_VALUE = "dummy_value"
private const val RESTART_DELAY = 200

abstract class BaseActivity : AppCompatActivity() {

    private val TAG = BaseActivity::class.java.simpleName
    private var progressDialog: ProgressDialog? = null

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        outState.putInt(DUMMY_VALUE, 0)
        super.onSaveInstanceState(outState, outPersistentState)
    }

    override fun onPause() {
        super.onPause()
        hideProgressDialog()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    protected fun showErrorSnackbar(@StringRes resId: Int, e: Exception?, clickListener: View.OnClickListener?) {
        val rootView = window.decorView.findViewById<View>(android.R.id.content)
        rootView?.let {
            showSnackbar(it, resId, e, R.string.dlg_retry, clickListener)
        }
    }

    protected fun showProgressDialog(@StringRes messageId: Int) {
        if (progressDialog == null) {
            progressDialog = ProgressDialog(this)
            progressDialog!!.isIndeterminate = true
            progressDialog!!.setCancelable(false)
            progressDialog!!.setCanceledOnTouchOutside(false)

            // Disable the back button
            val keyListener = DialogInterface.OnKeyListener { dialog,
                                                              keyCode,
                                                              event ->
                keyCode == KeyEvent.KEYCODE_BACK
            }
            progressDialog!!.setOnKeyListener(keyListener)
        }
        progressDialog!!.setMessage(getString(messageId))
        progressDialog!!.show()
    }

    protected fun hideProgressDialog() {
        progressDialog?.let {
            if (it.isShowing) {
                it.dismiss()
            }
        }
    }

    protected fun isProgresDialogShowing(): Boolean {
        if (progressDialog != null && progressDialog?.isShowing != null) {
            return progressDialog!!.isShowing
        } else {
            return false
        }
    }

    fun restartApp(context: Context) {
        // Application needs to restart when user declined some permissions at runtime
        val restartIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        val intent = PendingIntent.getActivity(context, 0, restartIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        val manager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        manager.set(AlarmManager.RTC, System.currentTimeMillis() + RESTART_DELAY, intent)
        System.exit(0)
    }

    override fun onResume() {
        super.onResume()
        val currentUser = ChatHelper.getCurrentUser()
        hideNotifications()
        if (currentUser != null && !QBChatService.getInstance().isLoggedIn) {
            Log.d(TAG, "Resuming with Relogin")
            ChatHelper.login(SharedPrefsHelper.getQbUser()!!, object : QBEntityCallback<QBUser> {
                override fun onSuccess(qbUser: QBUser?, b: Bundle?) {
                    Log.d(TAG, "Relogin Successful")
                    reloginToChat()
                }

                override fun onError(e: QBResponseException?) {
                    Log.d(TAG, e?.message)
                }
            })

        } else {
            Log.d(TAG, "Resuming without Relogin to Chat")
            onResumeFinished()
        }
    }

    private fun reloginToChat() {
        ChatHelper.loginToChat(SharedPrefsHelper.getQbUser()!!, object : QBEntityCallback<Void> {
            override fun onSuccess(aVoid: Void?, bundle: Bundle?) {
                Log.d(TAG, "Relogin to Chat Successful")
                onResumeFinished()
            }

            override fun onError(e: QBResponseException?) {
                Log.d(TAG, "Relogin to Chat Error: " + e?.message)
                onResumeFinished()
            }
        })
    }

    private fun hideNotifications() {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancelAll()
    }

    open fun onResumeFinished() {
        // Need to Override onResumeFinished() method in nested classes if we need to handle returning from background in Activity
    }
}