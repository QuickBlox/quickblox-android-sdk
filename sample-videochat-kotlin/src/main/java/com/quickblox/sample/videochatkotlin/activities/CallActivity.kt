package com.quickblox.sample.videochatkotlin.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.util.Log
import com.quickblox.chat.QBChatService
import com.quickblox.sample.core.ui.activity.CoreBaseActivity
import com.quickblox.sample.core.utils.Toaster
import com.quickblox.sample.videochatkotlin.R
import com.quickblox.sample.videochatkotlin.fragments.PreviewFragment
import com.quickblox.sample.videochatkotlin.fragments.VideoConversationFragment
import com.quickblox.sample.videochatkotlin.services.CallService
import com.quickblox.sample.videochatkotlin.utils.*
import com.quickblox.sample.videochatkotlin.utils.StringUtils.createCompositeString
import com.quickblox.users.model.QBUser
import com.quickblox.videochat.webrtc.*
import com.quickblox.videochat.webrtc.callbacks.QBRTCClientSessionCallbacks
import com.quickblox.videochat.webrtc.callbacks.QBRTCSessionEventsCallback
import com.quickblox.videochat.webrtc.callbacks.QBRTCSessionStateCallback
import org.webrtc.CameraVideoCapturer
import java.util.*

/**
 * Created by roman on 4/6/18.
 */
class CallActivity : CoreBaseActivity(), QBRTCClientSessionCallbacks, QBRTCSessionStateCallback<QBRTCSession>, PreviewFragment.CallFragmentCallbackListener,
        VideoConversationFragment.CallFragmentCallbackListener, QBRTCSessionEventsCallback {

    val TAG = CallActivity::class.java.simpleName
    lateinit var systemPermissionHelper: SystemPermissionHelper
    lateinit var opponents: ArrayList<QBUser>
    private var rtcClient: QBRTCClient? = null
    var currentSession: QBRTCSession? = null
    lateinit var audioManager: AppRTCAudioManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate")
        setContentView(R.layout.activity_main)
        initFields()
        initQBRTCClient()
        initActionBar()
        initAudioManager()
        systemPermissionHelper = SystemPermissionHelper(this)
        checkCameraPermissionAndStart()
    }

    private fun initAudioManager() {
        audioManager = AppRTCAudioManager.create(this)

        audioManager.defaultAudioDevice = AppRTCAudioManager.AudioDevice.SPEAKER_PHONE
        Log.d(TAG, "AppRTCAudioManager.AudioDevice.SPEAKER_PHONE")

        audioManager.setOnWiredHeadsetStateListener({ plugged, hasMicrophone ->
            Log.d(TAG, "setOnWiredHeadsetStateListener plugged= $plugged")
        })
    }

    fun initFields() {
        val obj = intent.getSerializableExtra(EXTRA_QB_USERS_LIST)
        if (obj is ArrayList<*>) {
            opponents = obj.filterIsInstance<QBUser>() as ArrayList<QBUser>
        }
    }

    @SuppressLint("InlinedApi")
    fun initActionBar() {
        setActionBarTitle(String.format(QBChatService.getInstance().user.fullName))
        actionBar.setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(this, R.color.black_transparent_50)))
//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
    }

    private fun initQBRTCClient() {
        rtcClient = QBRTCClient.getInstance(this)


        // Configure
        //
        QBRTCConfig.setMaxOpponentsCount(MAX_OPPONENTS_COUNT)
        setSettingsForMultiCall(opponents)
        QBRTCConfig.setDebugEnabled(true)


        // Add activity as callback to RTCClient
        rtcClient!!.addSessionCallbacksListener(this)
        // Start mange QBRTCSessions according to VideoCall parser's callbacks
        rtcClient!!.prepareToProcessCalls()
    }

    override fun onAttachFragment(fragment: Fragment) {
        if (fragment is VideoConversationFragment) {
            fragment.initSession(currentSession)
        }
    }

    fun checkCameraPermissionAndStart() {
        if (systemPermissionHelper.isAllCameraPermissionGranted()) {
            initPreviewFragment()
        } else {
            systemPermissionHelper.requestPermissionsForCallByType()
        }
    }

    fun initPreviewFragIfNeed() {
        if (supportFragmentManager.findFragmentByTag(PreviewFragment::class.java.simpleName) !is PreviewFragment) {
            initPreviewFragment()
        }
    }

    fun initPreviewFragment() {
        val previewFragment = PreviewFragment()
        val args = Bundle()
        args.putSerializable(EXTRA_QB_USERS_LIST, opponents)
        previewFragment.arguments = args
        addFragment(supportFragmentManager, R.id.fragment_container, previewFragment, PreviewFragment::class.java.simpleName)
    }

    fun initConversationFragment(incoming: Boolean) {
        val conversationFragment = VideoConversationFragment()
        val args = Bundle()
        args.putBoolean(EXTRA_IS_INCOMING_CALL, incoming)
        args.putSerializable(EXTRA_QB_USERS_LIST, opponents)
        conversationFragment.arguments = args
        addFragment(supportFragmentManager, R.id.fragment_container, conversationFragment, VideoConversationFragment::class.java.simpleName)
    }

    fun initIncomeCall() {
        val previewFrag = supportFragmentManager.findFragmentByTag(PreviewFragment::class.java.simpleName) as PreviewFragment?
        Log.d(TAG, "AMBRA initIncomeCall")
        if (previewFrag != null) {
            Log.d(TAG, "AMBRA updateCallButtons")
            previewFrag.updateCallButtons()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            SystemPermissionHelper.PERMISSIONS_FOR_CALL_REQUEST -> {
                if (grantResults.isNotEmpty()) {
                    if (!systemPermissionHelper.isAllCameraPermissionGranted()) {
                        Log.d(TAG, "AMBRA showToastDeniedPermissions")
                        showToastDeniedPermissions(permissions, grantResults)
                        startLogout()
                        finish()
                    } else {
                        initPreviewFragment()
                    }
                }
            }
        }
    }

    private fun showToastDeniedPermissions(permissions: Array<String>, grantResults: IntArray) {
        val deniedPermissions = systemPermissionHelper
                .collectDeniedPermissionsFomResult(permissions, grantResults)

        Toaster.longToast(
                getString(R.string.denied_permission_message, createCompositeString(deniedPermissions)))
    }

    private fun startLogout() {
        val intent = Intent(this, CallService::class.java)
        intent.putExtra(EXTRA_COMMAND_TO_SERVICE, COMMAND_LOGOUT)
        startService(intent)
    }

    private fun startLoginActivity() {
        val intent = Intent(this, LoginActivity::class.java)
        this.startActivity(intent)
        finish()
    }

    fun initCurrentSession(session: QBRTCSession) {
        Log.d(TAG, "AMBRA1 Init new QBRTCSession addSessionCallbacksListener")
        currentSession = session
        currentSession!!.addSessionCallbacksListener(this@CallActivity)

    }

    fun releaseCurrentSession() {
        Log.d(TAG, "AMBRA1 Release current session removeSessionCallbacksListener")
        if (currentSession != null) {
            currentSession!!.removeSessionCallbacksListener(this@CallActivity)
            this.currentSession = null
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        rtcClient!!.removeSessionsCallbacksListener(this@CallActivity)
    }

    fun hangUpCurrentSession() {
        Log.d(TAG, "AMBRA hangUpCurrentSession")
        if (currentSession != null) {
            Log.d(TAG, "AMBRA hangUpCurrentSession currentSession != null")
            currentSession!!.hangUp(HashMap<String, String>())
        }
    }

    override fun onStartCall(session: QBRTCSession) {
        Log.d(TAG, "AMBRA onStartCall = " + session)
        initCurrentSession(session)
//        initQBRTCClient()
        initConversationFragment(false)
    }

    override fun onHangUpCall() {
        hangUpCurrentSession()
    }

    override fun onAcceptCall() {
        Log.d(TAG, "AMBRA onAcceptCall")
        currentSession!!.acceptCall(null)
        initConversationFragment(true)
    }

    override fun onRejectCall() {
        if (currentSession != null) {
            currentSession!!.rejectCall(null)
        }
    }

    override fun onLogout() {
        startLogout()
        startLoginActivity()
    }

    //QBRTCSessionStateCallback
    override fun onDisconnectedFromUser(p0: QBRTCSession?, p1: Int?) {

    }

    override fun onConnectedToUser(p0: QBRTCSession?, p1: Int?) {
    }

    override fun onConnectionClosedForUser(p0: QBRTCSession?, p1: Int?) {
    }

    override fun onStateChanged(p0: QBRTCSession?, p1: BaseSession.QBRTCSessionState?) {
    }

    //QBRTCClientSessionCallbacks
    override fun onSessionStartClose(session: QBRTCSession) {
        Log.d(TAG, "AMBRA onSessionStartClose")
        currentSession?.removeSessionCallbacksListener(this@CallActivity)
    }

    override fun onReceiveNewSession(session: QBRTCSession?) {
        Log.d(TAG, "AMBRA onReceiveNewSession")
        if (currentSession == null) {
            currentSession = session
            initIncomeCall()
        }
    }

    override fun onUserNoActions(p0: QBRTCSession?, p1: Int?) {
    }

    //    QBRTCSessionEventsCallback
    override fun onReceiveHangUpFromUser(session: QBRTCSession?, p1: Int?, p2: MutableMap<String, String>?) {
        Log.d(TAG, "AMBRA onReceiveHangUpFromUser")
    }

    override fun onCallAcceptByUser(session: QBRTCSession?, p1: Int?, p2: MutableMap<String, String>?) {
        Log.d(TAG, "AMBRA onCallAcceptByUser")
    }

    override fun onSessionClosed(session: QBRTCSession?) {
        Log.d(TAG, "AMBRA Session " + session!!.getSessionID())

        if (session == currentSession) {
            Log.d(TAG, "AMBRA Stop session")
            releaseCurrentSession()
            initPreviewFragIfNeed()
        }
    }

    override fun onCallRejectByUser(p0: QBRTCSession?, p1: Int?, p2: MutableMap<String, String>?) {
        Log.d(TAG, "AMBRA onCallRejectByUser")
    }

    override fun onUserNotAnswer(p0: QBRTCSession?, p1: Int?) {

    }

    override fun onSetAudioEnabled(isAudioEnabled: Boolean) {

    }

    override fun onSetVideoEnabled(isNeedEnableCam: Boolean) {

    }

    override fun onSwitchAudio() {

    }

    override fun onStartScreenSharing() {

    }

    override fun onSwitchCamera(cameraSwitchHandler: CameraVideoCapturer.CameraSwitchHandler) {
        (currentSession!!.mediaStreamManager.videoCapturer as QBRTCCameraVideoCapturer)
                .switchCamera(cameraSwitchHandler)
    }
}