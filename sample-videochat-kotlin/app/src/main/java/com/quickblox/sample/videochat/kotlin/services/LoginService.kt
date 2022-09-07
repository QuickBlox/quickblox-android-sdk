package com.quickblox.sample.videochat.kotlin.services

import android.app.ActivityManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.IBinder
import android.text.TextUtils
import android.util.Log
import com.quickblox.chat.QBChatService
import com.quickblox.chat.connections.tcp.QBTcpChatConnectionFabric
import com.quickblox.chat.connections.tcp.QBTcpConfigurationBuilder
import com.quickblox.core.QBEntityCallback
import com.quickblox.core.exception.QBResponseException
import com.quickblox.sample.videochat.kotlin.utils.*
import com.quickblox.users.model.QBUser
import com.quickblox.videochat.webrtc.QBRTCClient
import com.quickblox.videochat.webrtc.QBRTCConfig

private const val EXTRA_COMMAND_TO_SERVICE = "command_for_service"
private const val EXTRA_QB_USER = "qb_user"

private const val COMMAND_NOT_FOUND = 0
private const val COMMAND_LOGIN = 1
private const val COMMAND_LOGOUT = 2
private const val COMMAND_DESTROY_RTC_CLIENT = 3

private const val EXTRA_PENDING_INTENT = "pending_Intent"

class LoginService : Service() {
    private val TAG = LoginService::class.java.simpleName
    private lateinit var chatService: QBChatService
    private lateinit var rtcClient: QBRTCClient
    private var pendingIntent: PendingIntent? = null
    private var currentCommand: Int = 0
    private var currentUser: QBUser? = null

    companion object {
        fun loginToChatAndInitRTCClient(context: Context, qbUser: QBUser, pendingIntent: PendingIntent? = null) {
            val intent = Intent(context, LoginService::class.java)
            intent.putExtra(EXTRA_COMMAND_TO_SERVICE, COMMAND_LOGIN)
            intent.putExtra(EXTRA_QB_USER, qbUser)
            intent.putExtra(EXTRA_PENDING_INTENT, pendingIntent)

            context.startService(intent)
        }

        fun logoutFromChat(context: Context) {
            val intent = Intent(context, LoginService::class.java)
            intent.putExtra(EXTRA_COMMAND_TO_SERVICE, COMMAND_LOGOUT)
            context.startService(intent)
        }

        fun destroyRTCClient(context: Context) {
            val intent = Intent(context, LoginService::class.java)
            intent.putExtra(EXTRA_COMMAND_TO_SERVICE, COMMAND_LOGOUT)
            context.startService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        createChatService()
        Log.d(TAG, "Service onCreate()")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Service started")
        parseIntentExtras(intent)
        startSuitableActions()

        return START_REDELIVER_INTENT
    }

    private fun parseIntentExtras(intent: Intent?) {
        intent?.extras?.let {
            currentCommand = intent.getIntExtra(EXTRA_COMMAND_TO_SERVICE, COMMAND_NOT_FOUND)
            pendingIntent = intent.getParcelableExtra(EXTRA_PENDING_INTENT)
            val currentUser: QBUser? = intent.getSerializableExtra(EXTRA_QB_USER) as QBUser?
            currentUser?.let {
                this.currentUser = currentUser
            }
        }
    }

    private fun startSuitableActions() {
        when (currentCommand) {
            COMMAND_LOGIN -> loginToChatAndInitRTCClient()
            COMMAND_LOGOUT -> logoutFomChat()
            COMMAND_DESTROY_RTC_CLIENT -> destroyRtcClient()
        }
    }

    private fun createChatService() {
        val configurationBuilder = QBTcpConfigurationBuilder()
        configurationBuilder.socketTimeout = 300
        QBChatService.setConnectionFabric(QBTcpChatConnectionFabric(configurationBuilder))
        QBChatService.setDebugEnabled(true)
        QBChatService.setDefaultPacketReplyTimeout(10000)
        chatService = QBChatService.getInstance()
    }

    private fun loginToChatAndInitRTCClient() {
        if (chatService.isLoggedIn) {
            sendResultToActivity(true, null)
        } else {
            currentUser?.let {
                loginToChatAndInitRTCClient(it)
            }
        }
    }

    private fun loginToChatAndInitRTCClient(user: QBUser) {
        chatService.login(user, object : QBEntityCallback<QBUser> {
            override fun onSuccess(user: QBUser?, bundle: Bundle) {
                Log.d(TAG, "login onSuccess")
                initQBRTCClient()
                sendResultToActivity(true, null)
            }

            override fun onError(e: QBResponseException) {
                Log.d(TAG, "login onError " + e.message)
                var errorMessage: String? = e.message
                if (TextUtils.isEmpty(errorMessage)) {
                    errorMessage = "Login error"
                }
                sendResultToActivity(false, errorMessage)
            }
        })
    }

    private fun initQBRTCClient() {
        rtcClient = QBRTCClient.getInstance(applicationContext)
        chatService.videoChatWebRTCSignalingManager?.addSignalingManagerListener { signaling, createdLocally ->
            val needAddSignaling = !createdLocally
            if (needAddSignaling) {
                rtcClient.addSignaling(signaling)
            }
        }

        applyRTCSettings()

        rtcClient.addSessionCallbacksListener(WebRtcSessionManager)
        rtcClient.prepareToProcessCalls()
    }

    private fun sendResultToActivity(isSuccess: Boolean, errorMessage: String?) {
        Log.d(TAG, "sendResultToActivity()")
        try {
            val intent = Intent()
            intent.putExtra(EXTRA_LOGIN_RESULT, isSuccess)
            intent.putExtra(EXTRA_LOGIN_ERROR_MESSAGE, errorMessage)

            pendingIntent?.send(this, EXTRA_LOGIN_RESULT_CODE, intent)
            stopForeground(true)
        } catch (e: PendingIntent.CanceledException) {
            val errorMessageSendingResult = e.message
            Log.d(TAG, errorMessageSendingResult ?: "Error sending result to activity")
        }
    }

    private fun destroyRtcClient() {
        if (::rtcClient.isInitialized) {
            rtcClient.destroy()
        }
        stopSelf()
    }

    private fun logoutFomChat() {
        chatService.logout(object : QBEntityCallback<Void?> {
            override fun onSuccess(aVoid: Void?, bundle: Bundle) {
                chatService.destroy()
            }

            override fun onError(e: QBResponseException) {
                Log.d(TAG, "logout onError " + e.message)
                chatService.destroy()
            }
        })
        stopSelf()
    }

    override fun onBind(intent: Intent): IBinder? {
        Log.d(TAG, "Service onBind)")
        return null
    }

    override fun onTaskRemoved(rootIntent: Intent) {
        Log.d(TAG, "Service onTaskRemoved()")
        super.onTaskRemoved(rootIntent)
        if (isCallServiceNotRunning()) {
            logoutFomChat()
            destroyRtcClient()
        }
    }

    private fun isCallServiceNotRunning(): Boolean {
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        var notRunning = true
        for (service in manager.getRunningServices(Integer.MAX_VALUE)) {
            if (CallService::class.java.name == service.service.className) {
                notRunning = false
            }
        }
        return notRunning
    }
}