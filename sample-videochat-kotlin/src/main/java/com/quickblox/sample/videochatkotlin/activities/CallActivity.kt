package com.quickblox.sample.videochatkotlin.activities

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.util.Log
import androidx.core.os.postDelayed
import com.quickblox.chat.QBChatService
import com.quickblox.chat.QBWebRTCSignaling
import com.quickblox.sample.core.ui.activity.CoreBaseActivity
import com.quickblox.sample.core.utils.Toaster
import com.quickblox.sample.videochatkotlin.R
import com.quickblox.sample.videochatkotlin.fragments.PreviewCallFragment
import com.quickblox.sample.videochatkotlin.fragments.ScreenShareFragment
import com.quickblox.sample.videochatkotlin.fragments.VideoConversationFragment
import com.quickblox.sample.videochatkotlin.utils.*
import com.quickblox.sample.videochatkotlin.utils.StringUtils.createCompositeString
import com.quickblox.users.model.QBUser
import com.quickblox.videochat.webrtc.*
import com.quickblox.videochat.webrtc.callbacks.QBRTCClientSessionCallbacks
import com.quickblox.videochat.webrtc.callbacks.QBRTCSessionEventsCallback
import org.webrtc.CameraVideoCapturer
import java.util.*

/**
 * Created by roman on 4/6/18.
 */
class CallActivity : CoreBaseActivity(), QBRTCClientSessionCallbacks, PreviewCallFragment.CallFragmentCallbackListener,
        VideoConversationFragment.CallFragmentCallbackListener, QBRTCSessionEventsCallback, ScreenShareFragment.OnSharingEvents {

    val TAG = CallActivity::class.java.simpleName
    private lateinit var systemPermissionHelper: SystemPermissionHelper
    private lateinit var opponents: ArrayList<QBUser>
    private var rtcClient: QBRTCClient? = null
    private var currentSession: QBRTCSession? = null
    private var audioManager: AppRTCAudioManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate")
        setContentView(R.layout.activity_main)
        initFields()
        initQBRTCClient()
        systemPermissionHelper = SystemPermissionHelper(this)
        checkCameraPermissionAndStart()
    }

    private fun initAudioManagerIfNeed() {
        if (audioManager == null) {
            audioManager = AppRTCAudioManager.create(this)

            audioManager!!.defaultAudioDevice = AppRTCAudioManager.AudioDevice.SPEAKER_PHONE
            Log.d(TAG, "AppRTCAudioManager.AudioDevice.SPEAKER_PHONE")

            audioManager!!.setOnWiredHeadsetStateListener({ plugged, hasMicrophone ->
                Log.d(TAG, "setOnWiredHeadsetStateListener plugged= $plugged")
            })
        }
    }

    private fun startAudioManager() {
        initAudioManagerIfNeed()
        audioManager!!.start { selectedAudioDevice, availableAudioDevices ->
            Toaster.shortToast("Audio device switched to  " + selectedAudioDevice)
            updateAudioDevice()
        }
    }

    private fun initFields() {
        val obj = intent.getSerializableExtra(EXTRA_QB_USERS_LIST)
        if (obj is ArrayList<*>) {
            opponents = obj.filterIsInstance<QBUser>() as ArrayList<QBUser>
        }
    }

    private fun initQBRTCClient() {
        rtcClient = QBRTCClient.getInstance(this)
        QBChatService.getInstance().videoChatWebRTCSignalingManager?.addSignalingManagerListener { qbSignaling, createdLocally ->
            if (!createdLocally) {
                rtcClient!!.addSignaling(qbSignaling as QBWebRTCSignaling)
            }
        }

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

    private fun checkCameraPermissionAndStart() {
        if (systemPermissionHelper.isAllCameraPermissionGranted()) {
            initPreviewFragment()
        } else {
            systemPermissionHelper.requestPermissionsForCallByType()
        }
    }

    private fun initPreviewFragIfNeed() {
        if (supportFragmentManager.findFragmentByTag(PreviewCallFragment::class.java.simpleName) !is PreviewCallFragment) {
            initPreviewFragment()
        } else {
            initPreviewFragDelayed()
        }
    }

    private fun initPreviewFragDelayed() {
        Handler().postDelayed(CAMERA_RELEASE_DELAY) { popBackStackFragment(supportFragmentManager) }
    }

    private fun initPreviewFragment() {
        val previewFragment = PreviewCallFragment()
        val args = Bundle()
        args.putSerializable(EXTRA_QB_USERS_LIST, opponents)
        previewFragment.arguments = args
        addFragment(supportFragmentManager, R.id.fragment_container, previewFragment, PreviewCallFragment::class.java.simpleName)
    }

    private fun initConversationFragment(incoming: Boolean) {
        val conversationFragment = VideoConversationFragment()
        val args = Bundle()
        args.putBoolean(EXTRA_IS_INCOMING_CALL, incoming)
        args.putSerializable(EXTRA_QB_USERS_LIST, opponents)
        conversationFragment.arguments = args
        addFragmentWithBackStack(supportFragmentManager, R.id.fragment_container, conversationFragment, VideoConversationFragment::class.java.simpleName)
    }

    private fun updatePreviewCallButtons(show: Boolean) {
        val previewFrag = supportFragmentManager.findFragmentByTag(PreviewCallFragment::class.java.simpleName) as PreviewCallFragment?
        Log.d(TAG, "updatePreviewCallButtons")
        if (previewFrag != null) {
            Log.d(TAG, "updateCallButtons")
            previewFrag.updateCallButtons(show)
        }
    }

    private fun updateAudioDevice() {
        val videoFrag = supportFragmentManager.findFragmentByTag(VideoConversationFragment::class.java.simpleName) as VideoConversationFragment?
        Log.d(TAG, "updateAudioDevice")
        if (videoFrag != null) {
            videoFrag.audioDeviceChanged(audioManager!!.selectedAudioDevice)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            SystemPermissionHelper.PERMISSIONS_FOR_CALL_REQUEST -> {
                if (grantResults.isNotEmpty()) {
                    if (!systemPermissionHelper.isAllCameraPermissionGranted()) {
                        Log.d(TAG, "showToastDeniedPermissions")
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.i(TAG, "onActivityResult requestCode=$requestCode, resultCode= $resultCode")
        if (requestCode == QBRTCScreenCapturer.REQUEST_MEDIA_PROJECTION) {
            if (resultCode == Activity.RESULT_OK) {
                startScreenSharing(data!!)
                Log.i(TAG, "Starting screen capture")
            } else {
                Toaster.longToast(
                        getString(R.string.denied_permission_message, "screen"))
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
        ChatHelper.instance.destroy()
    }

    private fun initCurrentSession(session: QBRTCSession) {
        Log.d(TAG, "Init new QBRTCSession addSessionCallbacksListener")
        currentSession = session
    }

    private fun releaseCurrentSession() {
        Log.d(TAG, "Release current session removeSessionCallbacksListener")
        if (currentSession != null) {
            this.currentSession = null
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        rtcClient!!.removeSessionsCallbacksListener(this@CallActivity)
        rtcClient!!.destroy()
    }

    override fun onBackPressed() {
        val fragmentByTag = supportFragmentManager.findFragmentByTag(ScreenShareFragment::class.java.simpleName)
        if (fragmentByTag is ScreenShareFragment) {
            returnToCamera()
            super.onBackPressed()
            Log.i(TAG, "onBackPressed")
        }
    }

    private fun returnToCamera() {
        try {
            currentSession!!.mediaStreamManager.videoCapturer = QBRTCCameraVideoCapturer(this, null)
        } catch (e: QBRTCCameraVideoCapturer.QBRTCCameraCapturerException) {
            Log.i(TAG, "Error: device doesn't have camera")
        }
    }

    private fun startScreenSharing(data: Intent) {
        val screenShareFragment = ScreenShareFragment()
        addFragmentWithBackStack(supportFragmentManager, R.id.fragment_container, screenShareFragment, ScreenShareFragment::class.java.simpleName)
        currentSession!!.mediaStreamManager.videoCapturer = QBRTCScreenCapturer(data, null)
    }

    override fun onStopSharingPreview() {
        onBackPressed()
    }

    private fun hangUpCurrentSession() {
        Log.d(TAG, "hangUpCurrentSession")
        if (currentSession != null) {
            Log.d(TAG, "hangUpCurrentSession currentSession != null")
            currentSession!!.hangUp(HashMap<String, String>())
        }
    }

    override fun onStartCall(session: QBRTCSession) {
        Log.d(TAG, "onStartCall = $session")
        initCurrentSession(session)
        startAudioManager()
        initConversationFragment(false)
    }

    override fun onHangUpCall() {
        hangUpCurrentSession()
    }

    override fun onAcceptCall() {
        Log.d(TAG, "onAcceptCall")
        startAudioManager()
        initConversationFragment(true)
    }

    override fun onRejectCall() {
        if (currentSession != null) {
            currentSession!!.rejectCall(null)
        }
    }

    override fun onLogout() {
        startLogout()
        finish()
    }

    //QBRTCClientSessionCallbacks
    override fun onSessionStartClose(session: QBRTCSession) {
        Log.d(TAG, "onSessionStartClose")
        if (session == currentSession) {
            updatePreviewCallButtons(false)
        }
    }

    override fun onReceiveNewSession(session: QBRTCSession) {
        Log.d(TAG, "onReceiveNewSession")
        if (currentSession == null) {
            currentSession = session
            updatePreviewCallButtons(true)
        } else {
            Log.d(TAG, "Stop new session. Device now is busy")
            session.rejectCall(null)
        }
    }

    override fun onUserNoActions(session: QBRTCSession, userId: Int) {
    }

    //    QBRTCSessionEventsCallback
    override fun onReceiveHangUpFromUser(session: QBRTCSession, userId: Int, userInfo: MutableMap<String, String>?) {
        Log.d(TAG, "onReceiveHangUpFromUser")
        fun getUserNameOrLogin(userId: Int): String {
            opponents.forEach { if (it.id == userId) return it.fullName ?: it.login }
            return ""
        }
        Toaster.shortToast("User " + getUserNameOrLogin(userId) + " " + getString(R.string.text_status_hang_up) + " conversation")
    }

    override fun onCallAcceptByUser(session: QBRTCSession?, p1: Int?, p2: MutableMap<String, String>?) {
        Log.d(TAG, "onCallAcceptByUser")
    }

    override fun onSessionClosed(session: QBRTCSession) {
        Log.d(TAG, "Session " + session.sessionID)

        if (session.equals(currentSession)) {
            Log.d(TAG, "Stop session")
            audioManager?.stop()
            audioManager = null
            releaseCurrentSession()
            initPreviewFragIfNeed()
        }
    }

    override fun onCallRejectByUser(session: QBRTCSession, userId: Int, userInfo: MutableMap<String, String>?) {
        Log.d(TAG, "onCallRejectByUser")
    }

    override fun onUserNotAnswer(session: QBRTCSession, userId: Int) {

    }

    override fun onSwitchAudio() {
        Log.v(TAG, "onSwitchAudio(), SelectedAudioDevice() = " + audioManager!!.selectedAudioDevice)
        if (audioManager!!.selectedAudioDevice != AppRTCAudioManager.AudioDevice.SPEAKER_PHONE) {
            audioManager!!.selectAudioDevice(AppRTCAudioManager.AudioDevice.SPEAKER_PHONE)
        } else {
            if (audioManager!!.audioDevices.contains(AppRTCAudioManager.AudioDevice.BLUETOOTH)) {
                audioManager!!.selectAudioDevice(AppRTCAudioManager.AudioDevice.BLUETOOTH)
            } else if (audioManager!!.audioDevices.contains(AppRTCAudioManager.AudioDevice.WIRED_HEADSET)) {
                audioManager!!.selectAudioDevice(AppRTCAudioManager.AudioDevice.WIRED_HEADSET)
            } else {
                audioManager!!.selectAudioDevice(AppRTCAudioManager.AudioDevice.EARPIECE)
            }
        }
    }

    override fun onStartScreenSharing() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return
        }
        QBRTCScreenCapturer.requestPermissions(this@CallActivity)
    }

    override fun onSwitchCamera(cameraSwitchHandler: CameraVideoCapturer.CameraSwitchHandler) {
        (currentSession!!.mediaStreamManager.videoCapturer as QBRTCCameraVideoCapturer)
                .switchCamera(cameraSwitchHandler)
    }
}