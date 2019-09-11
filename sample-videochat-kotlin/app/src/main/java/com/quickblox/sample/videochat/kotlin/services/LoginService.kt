package com.quickblox.sample.videochat.kotlin.services

import android.app.ActivityManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import com.quickblox.chat.QBChatService
import com.quickblox.chat.connections.tcp.QBTcpChatConnectionFabric
import com.quickblox.chat.connections.tcp.QBTcpConfigurationBuilder
import com.quickblox.core.QBEntityCallback
import com.quickblox.core.exception.QBResponseException
import com.quickblox.sample.videochat.kotlin.util.ChatPingAlarmManager
import com.quickblox.sample.videochat.kotlin.utils.*
import com.quickblox.users.model.QBUser
import com.quickblox.videochat.webrtc.QBRTCClient
import com.quickblox.videochat.webrtc.QBRTCConfig
import org.jivesoftware.smackx.ping.PingFailedListener


private const val EXTRA_COMMAND_TO_SERVICE = "command_for_service"
private const val EXTRA_QB_USER = "qb_user"

private const val COMMAND_NOT_FOUND = 0
private const val COMMAND_LOGIN = 1
private const val COMMAND_LOGOUT = 2

private const val EXTRA_PENDING_INTENT = "pending_Intent"

class LoginService : Service() {
    private val TAG = LoginService::class.java.simpleName
    private lateinit var chatService: QBChatService
    private lateinit var rtcClient: QBRTCClient
    private var pendingIntent: PendingIntent? = null
    private var currentCommand: Int = 0
    private var currentUser: QBUser? = null

    companion object {
        fun start(context: Context, qbUser: QBUser, pendingIntent: PendingIntent? = null) {
            val intent = Intent(context, LoginService::class.java)
            intent.putExtra(EXTRA_COMMAND_TO_SERVICE, COMMAND_LOGIN)
            intent.putExtra(EXTRA_QB_USER, qbUser)
            intent.putExtra(EXTRA_PENDING_INTENT, pendingIntent)

            context.startService(intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, LoginService::class.java)
            context.stopService(intent)
        }

        fun logout(context: Context) {
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

        return Service.START_REDELIVER_INTENT
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
        if (currentCommand == COMMAND_LOGIN) {
            startLoginToChat()
        } else if (currentCommand == COMMAND_LOGOUT) {
            logout()
        }
    }

    private fun createChatService() {
        val configurationBuilder = QBTcpConfigurationBuilder()
        configurationBuilder.socketTimeout = 0
        QBChatService.setConnectionFabric(QBTcpChatConnectionFabric(configurationBuilder))
        QBChatService.setDebugEnabled(true)
        chatService = QBChatService.getInstance()
    }

    private fun startLoginToChat() {
        if (chatService.isLoggedIn) {
            sendResultToActivity(true, null)
        } else {
            currentUser?.let {
                loginToChat(it)
            }
        }
    }

    private fun loginToChat(qbUser: QBUser) {
        chatService.login(qbUser, object : QBEntityCallback<QBUser> {
            override fun onSuccess(qbUser: QBUser?, bundle: Bundle) {
                Log.d(TAG, "login onSuccess")
                startActionsOnSuccessLogin()
            }

            override fun onError(e: QBResponseException) {
                Log.d(TAG, "login onError " + e.message)
                val errorMessage = if (e.message != null) {
                    e.message
                } else {
                    "Login error"
                }
                sendResultToActivity(false, errorMessage)
            }
        })
    }

    private fun startActionsOnSuccessLogin() {
        initPingListener()
        initQBRTCClient()
        sendResultToActivity(true, null)
    }

    private fun initPingListener() {
        ChatPingAlarmManager.onCreate(this)
        ChatPingAlarmManager.addPingListener(PingFailedListener { Log.d(TAG, "Ping chat server failed") })
    }

    private fun initQBRTCClient() {
        rtcClient = QBRTCClient.getInstance(applicationContext)
        // Add signalling manager
        chatService.videoChatWebRTCSignalingManager?.addSignalingManagerListener { qbSignaling, createdLocally ->
            if (!createdLocally) {
                rtcClient.addSignaling(qbSignaling)
            }
        }

        // Configure
        QBRTCConfig.setDebugEnabled(true)
        configRTCTimers(this)

        // Add service as callback to RTCClient
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

    private fun logout() {
        destroyRtcClientAndChat()
    }

    private fun destroyRtcClientAndChat() {
        if (::rtcClient.isInitialized) {
            rtcClient.destroy()
        }
        ChatPingAlarmManager.onDestroy()
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

    override fun onDestroy() {
        Log.d(TAG, "Service onDestroy()")
        super.onDestroy()
    }

    override fun onBind(intent: Intent): IBinder? {
        Log.d(TAG, "Service onBind)")
        return null
    }

    override fun onTaskRemoved(rootIntent: Intent) {
        Log.d(TAG, "Service onTaskRemoved()")
        super.onTaskRemoved(rootIntent)
        if (!isCallServiceRunning()) {
            logout()
        }
    }

    private fun isCallServiceRunning(): Boolean {
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        var running = false
        for (service in manager.getRunningServices(Integer.MAX_VALUE)) {
            if (CallService::class.java.name == service.service.className) {
                running = true
            }
        }
        return running
    }
}