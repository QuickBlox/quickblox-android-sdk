package com.quickblox.sample.videochatkotlin.services

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import com.quickblox.chat.QBChatService
import com.quickblox.chat.connections.tcp.QBTcpChatConnectionFabric
import com.quickblox.chat.connections.tcp.QBTcpConfigurationBuilder
import com.quickblox.core.QBEntityCallback
import com.quickblox.core.exception.QBResponseException
import com.quickblox.sample.videochatkotlin.utils.*
import com.quickblox.users.model.QBUser
import com.quickblox.videochat.webrtc.QBRTCClient

/**
 * Created by Roman on 09.04.2018.
 */
class CallService : Service() {

    private val TAG = CallService::class.java.simpleName

    lateinit var chatService: QBChatService
    private var rtcClient: QBRTCClient? = null
    private var pendingIntent: PendingIntent? = null
    private var currentCommand: Int = 0
    private var currentUser: QBUser? = null

    override fun onBind(intent: Intent?): IBinder? {
        Log.d(TAG, "Service onBind)")
        return null
    }

    override fun onCreate() {
        super.onCreate()
        createChatService()
        Log.d(TAG, "Service onCreate()")
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.d(TAG, "Service started")
        parseIntentExtras(intent)
        startSuitableActions()
        return Service.START_REDELIVER_INTENT
    }

    private fun createChatService() {
        val configurationBuilder = QBTcpConfigurationBuilder()
        configurationBuilder.socketTimeout = 0
        QBChatService.setConnectionFabric(QBTcpChatConnectionFabric(configurationBuilder))

        QBChatService.setDebugEnabled(true)
        chatService = QBChatService.getInstance()
    }

    private fun parseIntentExtras(intent: Intent?) {
        if (intent != null && intent.extras != null) {
            currentCommand = intent.getIntExtra(EXTRA_COMMAND_TO_SERVICE, COMMAND_NOT_FOUND)
            pendingIntent = intent.getParcelableExtra(EXTRA_PENDING_INTENT)
            currentUser = intent.getSerializableExtra(EXTRA_QB_USER) as QBUser
        }
        Log.d(TAG, "parseIntentExtras currentCommand= " + currentCommand)
        Log.d(TAG, "parseIntentExtras pendingIntent= " + pendingIntent)
        Log.d(TAG, "parseIntentExtras currentUser= " + currentUser)
    }

    private fun startSuitableActions() {
        if (currentCommand == COMMAND_LOGIN) {
            startLoginToChat()
        } else if (currentCommand == COMMAND_LOGOUT) {
            logout()
        }
    }

    private fun startLoginToChat() {
        if (!chatService.isLoggedIn()) {
            loginToChat(currentUser)
        } else {
            sendResultToActivity(true, null)
        }
    }

    private fun loginToChat(qbUser: QBUser?) {
        chatService.login(qbUser, object : QBEntityCallback<Void> {
            override fun onSuccess(void: Void?, bundle: Bundle?) {
                Log.d(TAG, "login onSuccess")
                startActionsOnSuccessLogin()
            }

            override fun onError(e: QBResponseException) {
                Log.d(TAG, "login onError " + e.message)
                sendResultToActivity(false, if (e.message != null)
                    e.message
                else
                    "Login error")
            }
        })
    }

    private fun startActionsOnSuccessLogin() {
        initQBRTCClient()
        sendResultToActivity(true, null)
    }

    private fun initQBRTCClient() {
        rtcClient = QBRTCClient.getInstance(applicationContext)
        // Add signalling manager
//        chatService.videoChatWebRTCSignalingManager.addSignalingManagerListener { qbSignaling, createdLocally ->
//            if (!createdLocally) {
//                rtcClient!!.addSignaling(qbSignaling as QBWebRTCSignaling)
//            }
//        }
//
//        // Configure
//        QBRTCConfig.setDebugEnabled(true)
//
//        // Add service as callback to RTCClient
//        rtcClient!!.addSessionCallbacksListener(WebRtcSessionManager.getInstance(this))
//        rtcClient!!.prepareToProcessCalls()
    }

    private fun sendResultToActivity(isSuccess: Boolean, errorMessage: String?) {
        if (pendingIntent != null) {
            Log.d(TAG, "sendResultToActivity()")
            try {
                val intent = Intent()
                intent.putExtra(EXTRA_LOGIN_RESULT, isSuccess)
                intent.putExtra(EXTRA_LOGIN_ERROR_MESSAGE, errorMessage)

                pendingIntent!!.send(this@CallService, EXTRA_LOGIN_RESULT_CODE, intent)
            } catch (e: PendingIntent.CanceledException) {
                val errorMessageSendingResult = e.message
                Log.d(TAG, errorMessageSendingResult ?: "Error sending result to activity")
            }

        }
    }

    private fun logout() {
        destroyRtcClientAndChat()
    }

    private fun destroyRtcClientAndChat() {
        if (rtcClient != null) {
            rtcClient!!.destroy()
        }
        chatService.logout(object : QBEntityCallback<Void> {
            override fun onSuccess(aVoid: Void, bundle: Bundle) {
                chatService.destroy()
            }

            override fun onError(e: QBResponseException) {
                Log.d(TAG, "logout onError " + e.message)
                chatService.destroy()
            }
        })
        stopSelf()
    }
}
