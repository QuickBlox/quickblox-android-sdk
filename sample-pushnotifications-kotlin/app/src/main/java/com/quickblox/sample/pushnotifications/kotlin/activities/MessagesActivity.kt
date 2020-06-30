package com.quickblox.sample.pushnotifications.kotlin.activities

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ListView
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.quickblox.auth.session.QBSessionManager
import com.quickblox.auth.session.QBSettings
import com.quickblox.core.QBEntityCallback
import com.quickblox.core.exception.QBResponseException
import com.quickblox.core.helper.StringifyArrayList
import com.quickblox.messages.QBPushNotifications
import com.quickblox.messages.model.QBEnvironment
import com.quickblox.messages.model.QBEvent
import com.quickblox.messages.model.QBNotificationType
import com.quickblox.messages.services.QBPushManager
import com.quickblox.messages.services.SubscribeService
import com.quickblox.sample.pushnotifications.kotlin.R
import com.quickblox.sample.pushnotifications.kotlin.utils.*
import com.quickblox.users.QBUsers

private const val SEND_MENU_ITEM: Int = 0
private const val PLAY_SERVICES_REQUEST_CODE = 9000

class MessagesActivity : BaseActivity() {
    private val TAG = javaClass.simpleName

    private lateinit var outgoingMessageEditText: EditText
    private lateinit var adapter: ArrayAdapter<String>
    private var receivedPushes: MutableList<String> = ArrayList()

    companion object {
        fun start(context: Context, message: String?) {
            val intent = Intent(context, MessagesActivity::class.java)
            message?.let {
                intent.putExtra(EXTRA_FCM_MESSAGE, message)
            }
            context.startActivity(intent)
        }
    }

    private val pushBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            var message = intent.getStringExtra(EXTRA_FCM_MESSAGE)
            if (TextUtils.isEmpty(message)) {
                message = EMPTY_FCM_MESSAGE
            }
            Log.i(TAG, "Receiving event $ACTION_NEW_FCM_EVENT with data: $message")
            retrieveMessage(message)
        }
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_messages)

        val subtitle = defineSubtitleLabel(QBSettings.getInstance().isEnablePushNotification)
        setActionbarSubTitle(subtitle)

        initUI()

        val message = intent.getStringExtra(EXTRA_FCM_MESSAGE)
        message?.let {
            retrieveMessage(message)
        }
        registerReceiver()
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(pushBroadcastReceiver)
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        menu.getItem(SEND_MENU_ITEM).isEnabled = true
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.activity_messages, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.menu_send_message -> {
                item.isEnabled = false
                sendPushMessage()
                return true
            }
            R.id.menu_enable_notification -> {
                QBSettings.getInstance().isEnablePushNotification = true
                setActionbarSubTitle(resources.getString(R.string.subtitle_enabled))
                return true
            }
            R.id.menu_disable_notification -> {
                QBSettings.getInstance().isEnablePushNotification = false
                setActionbarSubTitle(resources.getString(R.string.subtitle_disabled))
                return true
            }
            R.id.menu_appinfo -> {
                AppInfoActivity.start(this)
                return true
            }
            R.id.menu_logout -> {
                unsubscribeFromPushes()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun defineSubtitleLabel(isPushesEnabled: Boolean): String {
        return if (isPushesEnabled) {
            getString(R.string.subtitle_enabled)
        } else {
            getString(R.string.subtitle_disabled)
        }
    }

    private fun setActionbarSubTitle(subTitle: String) {
        supportActionBar?.subtitle = subTitle
    }

    private fun initUI() {
        outgoingMessageEditText = findViewById(R.id.edit_message_out)
        outgoingMessageEditText.addTextChangedListener(MessageTextWatcher())

        val incomingMessagesListView = findViewById<ListView>(R.id.list_messages)
        adapter = ArrayAdapter(this, R.layout.list_item_message, R.id.item_message, receivedPushes)
        incomingMessagesListView.adapter = adapter
        incomingMessagesListView.emptyView = findViewById(R.id.text_empty_messages)
    }

    private fun registerReceiver() {
        checkPlayServicesAvailable()
        LocalBroadcastManager.getInstance(this).registerReceiver(pushBroadcastReceiver,
                IntentFilter(ACTION_NEW_FCM_EVENT))
    }

    private fun checkPlayServicesAvailable() {
        val apiAvailability = GoogleApiAvailability.getInstance()
        val resultCode = apiAvailability.isGooglePlayServicesAvailable(this)
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_REQUEST_CODE).show()
            } else {
                Log.i(TAG, "This device is not supported.")
                finish()
            }
        }
    }

    private fun retrieveMessage(message: String) {
        receivedPushes.add(0, message)
        adapter.notifyDataSetChanged()
    }

    private fun sendPushMessage() {
        val outMessage = outgoingMessageEditText.text.toString().trim { it <= ' ' }
        if (TextUtils.isEmpty(outMessage)) {
            shortToast(R.string.error_field_is_empty)
            invalidateOptionsMenu()
            return
        }

        // Send Push: create QuickBlox Push Notification Event
        val qbEvent = QBEvent()
        qbEvent.notificationType = QBNotificationType.PUSH
        qbEvent.environment = QBEnvironment.DEVELOPMENT
        // Generic push - will be delivered to all platforms (Android, iOS, WP, Blackberry..)
        qbEvent.message = outMessage

        val userIds = StringifyArrayList<Int>()
        userIds.add(QBSessionManager.getInstance().sessionParameters.userId)
        qbEvent.userIds = userIds

        showProgressDialog(R.string.progress_sending)
        hideKeyboard(outgoingMessageEditText)
        QBPushNotifications.createEvent(qbEvent).performAsync(object : QBEntityCallback<QBEvent> {
            override fun onSuccess(qbEvent: QBEvent?, bundle: Bundle?) {
                hideProgressDialog()
                outgoingMessageEditText.text = null
                invalidateOptionsMenu()
            }

            override fun onError(e: QBResponseException?) {
                e?.let {
                    showErrorSnackbar(R.string.sending_error, e, View.OnClickListener {
                        sendPushMessage()
                    })
                }
                hideProgressDialog()
                invalidateOptionsMenu()
            }
        })
    }

    private inner class MessageTextWatcher : TextWatcher {
        override fun afterTextChanged(string: Editable?) {
            string?.let {
                if (it.length >= resources.getInteger(R.integer.push_max_length)) {
                    shortToast(R.string.error_too_long_push)
                }
            }
        }

        override fun beforeTextChanged(string: CharSequence?, start: Int, count: Int, after: Int) {

        }

        override fun onTextChanged(string: CharSequence?, start: Int, before: Int, count: Int) {

        }
    }

    private fun unsubscribeFromPushes() {
        if (QBPushManager.getInstance().isSubscribedToPushes) {
            QBPushManager.getInstance().addListener(object : QBPushManager.QBSubscribeListener {
                override fun onSubscriptionCreated() {

                }

                override fun onSubscriptionError(e: Exception?, i: Int) {

                }

                override fun onSubscriptionDeleted(success: Boolean) {
                    Log.d(TAG, "Subscription Deleted -> Success: $success")
                    QBPushManager.getInstance().removeListener(this)
                    userLogout()
                }
            })
            SubscribeService.unSubscribeFromPushes(this@MessagesActivity)
        }
    }

    private fun userLogout() {
        Log.d(TAG, "SignOut")
        showProgressDialog(R.string.dlg_logout)

        QBUsers.signOut().performAsync(object : QBEntityCallback<Void> {
            override fun onSuccess(aVoid: Void?, bundle: Bundle?) {
                Log.d(TAG, "SignOut Successful")
                SharedPrefsHelper.removeQbUser()
                LoginActivity.start(this@MessagesActivity, null)
                hideProgressDialog()
                finish()
            }

            override fun onError(e: QBResponseException) {
                Log.d(TAG, "Unable to SignOut: " + e.message)
                hideProgressDialog()
                showErrorSnackbar(R.string.error_logout, e, View.OnClickListener {
                    userLogout()
                })
            }
        })
    }
}