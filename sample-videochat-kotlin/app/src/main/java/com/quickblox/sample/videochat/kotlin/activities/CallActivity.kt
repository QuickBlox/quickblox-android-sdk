package com.quickblox.sample.videochat.kotlin.activities

import android.annotation.TargetApi
import android.app.Activity
import android.app.KeyguardManager
import android.content.*
import android.net.Uri
import android.os.*
import android.preference.PreferenceManager
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import com.quickblox.core.QBEntityCallbackImpl
import com.quickblox.sample.videochat.kotlin.R
import com.quickblox.sample.videochat.kotlin.db.QbUsersDbManager
import com.quickblox.sample.videochat.kotlin.fragments.*
import com.quickblox.sample.videochat.kotlin.services.CallService
import com.quickblox.sample.videochat.kotlin.services.MIN_OPPONENT_SIZE
import com.quickblox.sample.videochat.kotlin.utils.*
import com.quickblox.users.QBUsers
import com.quickblox.users.model.QBUser
import com.quickblox.videochat.webrtc.*
import com.quickblox.videochat.webrtc.callbacks.QBRTCClientSessionCallbacks
import com.quickblox.videochat.webrtc.callbacks.QBRTCClientVideoTracksCallbacks
import com.quickblox.videochat.webrtc.callbacks.QBRTCSessionEventsCallback
import com.quickblox.videochat.webrtc.callbacks.QBRTCSessionStateCallback
import com.quickblox.videochat.webrtc.view.QBRTCVideoTrack
import org.jivesoftware.smack.AbstractConnectionListener
import org.jivesoftware.smack.ConnectionListener
import org.webrtc.CameraVideoCapturer
import java.util.*
import kotlin.collections.ArrayList

private const val INCOME_CALL_FRAGMENT = "income_call_fragment"
private const val REQUEST_PERMISSION_SETTING = 545

class CallActivity : BaseActivity(), IncomeCallFragmentCallbackListener, QBRTCSessionStateCallback<QBRTCSession>,
    QBRTCClientSessionCallbacks, ConversationFragmentCallback, ScreenShareFragment.OnSharingEvents {

    private var TAG = CallActivity::class.java.simpleName

    private val callStateListeners = hashSetOf<CallStateListener>()
    private val updateOpponentsListeners = hashSetOf<UpdateOpponentsListener>()
    private val callTimeUpdateListeners = hashSetOf<CallTimeUpdateListener>()
    private lateinit var showIncomingCallWindowTaskHandler: Handler
    private var connectionListener: ConnectionListenerImpl? = null
    private lateinit var callServiceConnection: ServiceConnection
    private lateinit var showIncomingCallWindowTask: Runnable
    private var opponentIds: List<Int>? = null
    private lateinit var callService: CallService
    private var connectionView: LinearLayout? = null
    private var isInComingCall: Boolean = false
    private var isVideoCall: Boolean = false

    companion object {
        fun start(context: Context, isIncomingCall: Boolean) {
            val intent = Intent(context, CallActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.putExtra(EXTRA_IS_INCOMING_CALL, isIncomingCall)
            SharedPrefsHelper.save(EXTRA_IS_INCOMING_CALL, isIncomingCall)
            context.startActivity(intent)
            CallService.start(context)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            keyguardManager.requestDismissKeyguard(this, null)
        } else {
            this.window.addFlags(
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }
        connectionView = View.inflate(this, R.layout.connection_popup, null) as LinearLayout
    }

    private fun initScreen() {
        callService.setCallTimerCallback(CallTimerCallback())
        isVideoCall = callService.isVideoCall()

        opponentIds = callService.getOpponents()

        applyMediaSettings()

        addListeners()

        isInComingCall = if (intent != null && intent.extras != null) {
            intent?.extras?.getBoolean(EXTRA_IS_INCOMING_CALL) ?: false
        } else {
            SharedPrefsHelper.get(EXTRA_IS_INCOMING_CALL, false)
        }

        if (callService.isConnectedCall()) {
            checkPermission()
            if (callService.isSharingScreenState()) {
                startScreenSharing(null)
                return
            }
            addConversationFragment(isInComingCall)
        } else {
            if (!isInComingCall) {
                callService.playRingtone()
            }
            startSuitableFragment(isInComingCall)
        }
    }

    private fun addListeners() {
        addSessionEventsListener(this)
        addSessionStateListener(this)

        connectionListener = ConnectionListenerImpl()
        addConnectionListener(connectionListener)
    }

    private fun removeListeners() {
        removeSessionEventsListener(this)
        removeSessionStateListener(this)
        removeConnectionListener(connectionListener)

        callService.removeCallTimerCallback()
    }

    private fun bindCallService() {
        callServiceConnection = CallServiceConnection()
        Intent(this, CallService::class.java).also { intent ->
            bindService(intent, callServiceConnection, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.i(TAG, "onActivityResult requestCode=$requestCode, resultCode= $resultCode")
        if (resultCode == EXTRA_LOGIN_RESULT_CODE) {
            data?.let {
                val isLoginSuccess = it.getBooleanExtra(EXTRA_LOGIN_RESULT, false)
                if (isLoginSuccess) {
                    initScreen()
                } else {
                    finish()
                }
            }
        }
        if (requestCode == QBRTCScreenCapturer.REQUEST_MEDIA_PROJECTION && resultCode == Activity.RESULT_OK) {
            data?.let {
                startScreenSharing(it)
                Log.i(TAG, "Starting screen capture")
            }
        }
    }

    private fun startScreenSharing(data: Intent?) {
        val fragmentByTag = supportFragmentManager.findFragmentByTag(ScreenShareFragment::class.simpleName)
        if (fragmentByTag !is ScreenShareFragment) {
            addFragment(
                supportFragmentManager, R.id.fragment_container,
                ScreenShareFragment.newInstance(), ScreenShareFragment::class.java.simpleName
            )
            data?.let {
                callService.startScreenSharing(it)
            }
        }
    }

    private fun startSuitableFragment(isInComingCall: Boolean) {
        val session = WebRtcSessionManager.getCurrentSession()
        if (session != null) {
            loadAbsentUsers()
            if (isInComingCall) {
                initIncomingCallTask()
                addIncomeCallFragment()
                checkPermission()
            } else {
                addConversationFragment(isInComingCall)
                intent.removeExtra(EXTRA_IS_INCOMING_CALL)
                SharedPrefsHelper.save(EXTRA_IS_INCOMING_CALL, false)
            }
        } else {
            finish()
        }
    }

    private fun checkPermission() {
        val cam = SharedPrefsHelper.get(PERMISSIONS[0], true)
        val mic = SharedPrefsHelper.get(PERMISSIONS[1], true)
        Log.d(TAG, "CAMERA => $cam; MICROPHONE => $mic")

        if (isVideoCall && checkPermissions(PERMISSIONS)) {
            if (cam) {
                PermissionsActivity.startForResult(this, false, PERMISSIONS)
            } else {
                val rootView = window.decorView.findViewById<View>(android.R.id.content)
                showErrorSnackbar(
                    rootView,
                    getString(R.string.error_permission_video),
                    R.string.dlg_allow,
                    object : View.OnClickListener {
                        override fun onClick(v: View?) {
                            startPermissionSystemSettings()
                        }
                    })
            }
        } else if (checkPermission(PERMISSIONS[1])) {
            if (mic) {
                PermissionsActivity.startForResult(this, true, PERMISSIONS)
            } else {
                val rootView = window.decorView.findViewById<View>(android.R.id.content)
                showErrorSnackbar(
                    rootView,
                    R.string.error_permission_audio,
                    "Allow Permission",
                    R.string.dlg_allow,
                    object : View.OnClickListener {
                        override fun onClick(v: View?) {
                            startPermissionSystemSettings()
                        }
                    })
            }
        }
    }

    private fun startPermissionSystemSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", packageName, null)
        intent.data = uri
        startActivityForResult(intent, REQUEST_PERMISSION_SETTING)
    }

    private fun loadAbsentUsers() {
        val usersFromDb = QbUsersDbManager.allUsers
        val allParticipantsOfCall = ArrayList<Int>()

        opponentIds?.let {
            allParticipantsOfCall.addAll(it)
        }

        if (isInComingCall) {
            val callerId = callService.getCallerId()
            callerId?.let {
                allParticipantsOfCall.add(it)
            }
        }

        val idsNotLoadedUsers = ArrayList<Int>()

        for (userId in allParticipantsOfCall) {
            val user = QBUser(userId)
            user.fullName = userId.toString()
            if (!usersFromDb.contains(user)) {
                idsNotLoadedUsers.add(userId)
            }
        }
        if (idsNotLoadedUsers.isNotEmpty()) {
            QBUsers.getUsersByIDs(idsNotLoadedUsers, null)
                .performAsync(object : QBEntityCallbackImpl<ArrayList<QBUser>>() {
                    override fun onSuccess(users: ArrayList<QBUser>, params: Bundle) {
                        QbUsersDbManager.saveAllUsers(users, false)
                        notifyOpponentsUpdated(users)
                    }
                })
        }
    }

    private fun initIncomingCallTask() {
        showIncomingCallWindowTaskHandler = Handler(Looper.getMainLooper())
        showIncomingCallWindowTask = Runnable {
            if (callService.currentSessionExist()) {
                longToast("Call was stopped by UserNoActions timer")
                callService.clearCallState()
                callService.clearButtonsState()
                WebRtcSessionManager.setCurrentSession(null)
                CallService.stop(this@CallActivity)
                finish()
            }
        }
    }

    private fun hangUpCurrentSession() {
        callService.stopRingtone()
        if (!callService.hangUpCurrentSession(HashMap())) {
            finish()
        }
    }

    private fun startIncomeCallTimer(time: Long) {
        showIncomingCallWindowTaskHandler.postAtTime(showIncomingCallWindowTask, SystemClock.uptimeMillis() + time)
    }

    private fun stopIncomeCallTimer() {
        Log.d(TAG, "stopIncomeCallTimer")
        showIncomingCallWindowTaskHandler.removeCallbacks(showIncomingCallWindowTask)
    }

    override fun onResume() {
        super.onResume()
        bindCallService()
    }

    override fun onPause() {
        super.onPause()
        unbindService(callServiceConnection)
        if (::callService.isInitialized) {
            removeListeners()
        }
    }

    override fun finish() {
        // fix bug when user returns to call from service and the backstack doesn't have any screens
        OpponentsActivity.start(this)
        CallService.stop(this)
        super.finish()
    }

    override fun onBackPressed() {
        // to prevent returning from Call Fragment
    }

    private fun addIncomeCallFragment() {
        if (callService.currentSessionExist()) {
            val fragment = IncomeCallFragment()
            if (supportFragmentManager.findFragmentByTag(INCOME_CALL_FRAGMENT) == null) {
                addFragment(supportFragmentManager, R.id.fragment_container, fragment, INCOME_CALL_FRAGMENT)
            }
        } else {
            Log.d(TAG, "SKIP addIncomeCallFragment method")
        }
    }

    private fun addConversationFragment(isIncomingCall: Boolean) {
        val baseConversationFragment: BaseConversationFragment = if (isVideoCall) {
            VideoConversationFragment()
        } else {
            AudioConversationFragment()
        }
        val conversationFragment = BaseConversationFragment.newInstance(baseConversationFragment, isIncomingCall)
        addFragment(
            supportFragmentManager,
            R.id.fragment_container,
            conversationFragment,
            conversationFragment.javaClass.simpleName
        )
    }

    private fun showConnectionPopUp() {
        runOnUiThread {
            val fragmentContainer: FrameLayout = findViewById(R.id.fragment_container)
            val connectionNotificationView: TextView? = connectionView?.findViewById(R.id.notification)
            connectionNotificationView?.setText(R.string.connection_was_lost)
            if (connectionView?.parent == null) {
                fragmentContainer.addView(connectionView)
            }
        }
    }

    private fun hideConnectionPopUp() {
        runOnUiThread {
            val fragmentContainer: FrameLayout = findViewById(R.id.fragment_container)
            fragmentContainer.removeView(connectionView)
        }
    }

    private inner class ConnectionListenerImpl : AbstractConnectionListener() {
        override fun connectionClosedOnError(e: Exception?) {
            showConnectionPopUp()
        }

        override fun reconnectionSuccessful() {
            hideConnectionPopUp()
        }
    }

    override fun onDisconnectedFromUser(session: QBRTCSession?, userId: Int?) {
        // empty
    }

    override fun onConnectedToUser(session: QBRTCSession?, userId: Int?) {
        notifyCallStarted()
        if (isInComingCall) {
            stopIncomeCallTimer()
        }
        Log.d(TAG, "onConnectedToUser() is started")
    }

    override fun onConnectionClosedForUser(session: QBRTCSession?, userId: Int?) {
        // empty
    }

    override fun onStateChanged(session: QBRTCSession?, sessiontState: BaseSession.QBRTCSessionState?) {
        // empty
    }

    override fun onUserNotAnswer(session: QBRTCSession?, userId: Int?) {
        if (callService.isCurrentSession(session)) {
            callService.stopRingtone()
        }
    }

    override fun onSessionStartClose(session: QBRTCSession?) {
        if (callService.isCurrentSession(session)) {
            callService.removeSessionStateListener(this)
            notifyCallStopped()
        }
    }

    override fun onReceiveHangUpFromUser(session: QBRTCSession?, userId: Int?, map: MutableMap<String, String>?) {
        if (callService.isCurrentSession(session)) {
            val numberOpponents = session?.opponents?.size
            if (numberOpponents == MIN_OPPONENT_SIZE) {
                hangUpCurrentSession()
            }
            val participant = QbUsersDbManager.getUserById(userId)
            val participantName = if (participant != null) participant.fullName else userId.toString()
            shortToast("User " + participantName + " " + getString(R.string.text_status_hang_up) + " conversation")
        }
    }

    override fun onCallAcceptByUser(session: QBRTCSession?, userId: Int?, map: MutableMap<String, String>?) {
        if (callService.isCurrentSession(session)) {
            callService.stopRingtone()
        }
    }

    override fun onReceiveNewSession(session: QBRTCSession?) {
        // empty
    }

    override fun onUserNoActions(session: QBRTCSession?, userId: Int?) {
        startIncomeCallTimer(0)
    }

    override fun onSessionClosed(session: QBRTCSession?) {
        if (callService.isCurrentSession(session)) {
            callService.stopForeground(true)
            finish()
        }
    }

    override fun onCallRejectByUser(session: QBRTCSession?, userId: Int?, map: MutableMap<String, String>?) {
        // empty
    }

    override fun onAcceptCurrentSession() {
        if (callService.currentSessionExist()) {
            addConversationFragment(true)
        } else {
            Log.d(TAG, "SKIP addConversationFragment method")
        }
    }

    override fun onRejectCurrentSession() {
        callService.rejectCurrentSession(HashMap())
    }

    override fun addConnectionListener(connectionCallback: ConnectionListener?) {
        callService.addConnectionListener(connectionCallback)
    }

    override fun removeConnectionListener(connectionCallback: ConnectionListener?) {
        callService.removeConnectionListener(connectionCallback)
    }

    override fun addSessionStateListener(clientConnectionCallbacks: QBRTCSessionStateCallback<*>?) {
        callService.addSessionStateListener(clientConnectionCallbacks)
    }

    override fun addSessionEventsListener(eventsCallback: QBRTCSessionEventsCallback?) {
        callService.addSessionEventsListener(eventsCallback)
    }

    override fun onSetAudioEnabled(isAudioEnabled: Boolean) {
        callService.setAudioEnabled(isAudioEnabled)
    }

    override fun onHangUpCurrentSession() {
        hangUpCurrentSession()
    }

    @TargetApi(21)
    override fun onStartScreenSharing() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return
        }
        QBRTCScreenCapturer.requestPermissions(this)
    }

    override fun onSwitchCamera(cameraSwitchHandler: CameraVideoCapturer.CameraSwitchHandler) {
        callService.switchCamera(cameraSwitchHandler)
    }

    override fun onSetVideoEnabled(isNeedEnableCam: Boolean) {
        callService.setVideoEnabled(isNeedEnableCam)
    }

    override fun onSwitchAudio() {
        callService.switchAudio()
    }

    override fun removeSessionStateListener(clientConnectionCallbacks: QBRTCSessionStateCallback<*>?) {
        callService.removeSessionStateListener(clientConnectionCallbacks)
    }

    override fun removeSessionEventsListener(eventsCallback: QBRTCSessionEventsCallback?) {
        callService.removeSessionEventsListener(eventsCallback)
    }

    override fun addCallStateListener(callStateListener: CallStateListener) {
        callStateListeners.add(callStateListener)
    }

    override fun removeCallStateListener(callStateListener: CallStateListener) {
        callStateListeners.remove(callStateListener)
    }

    override fun addUpdateOpponentsListener(updateOpponentsListener: UpdateOpponentsListener) {
        updateOpponentsListeners.add(updateOpponentsListener)
    }

    override fun removeUpdateOpponentsListener(updateOpponentsListener: UpdateOpponentsListener) {
        updateOpponentsListeners.remove(updateOpponentsListener)
    }

    override fun addCallTimeUpdateListener(callTimeUpdateListener: CallTimeUpdateListener) {
        callTimeUpdateListeners.add(callTimeUpdateListener)
    }

    override fun removeCallTimeUpdateListener(callTimeUpdateListener: CallTimeUpdateListener) {
        callTimeUpdateListeners.remove(callTimeUpdateListener)
    }

    override fun addOnChangeAudioDeviceListener(onChangeDynamicCallback: OnChangeAudioDevice?) {
        // empty
    }

    override fun removeOnChangeAudioDeviceListener(onChangeDynamicCallback: OnChangeAudioDevice?) {
        // empty
    }

    override fun acceptCall(userInfo: Map<String, String>) {
        callService.acceptCall(userInfo)
    }

    override fun startCall(userInfo: Map<String, String>) {
        callService.startCall(userInfo)
    }

    override fun currentSessionExist(): Boolean {
        return callService.currentSessionExist()
    }

    override fun getOpponents(): List<Int>? {
        return callService.getOpponents()
    }

    override fun getCallerId(): Int? {
        return callService.getCallerId()
    }

    override fun addVideoTrackListener(callback: QBRTCClientVideoTracksCallbacks<QBRTCSession>?) {
        callService.addVideoTrackListener(callback)
    }

    override fun removeVideoTrackListener(callback: QBRTCClientVideoTracksCallbacks<QBRTCSession>?) {
        callService.removeVideoTrackListener(callback)
    }

    override fun getCurrentSessionState(): BaseSession.QBRTCSessionState? {
        return callService.getCurrentSessionState()
    }

    override fun getPeerChannel(userId: Int): QBRTCTypes.QBRTCConnectionState? {
        return callService.getPeerChannel(userId)
    }

    override fun isMediaStreamManagerExist(): Boolean {
        return callService.isMediaStreamManagerExist()
    }

    override fun isConnectedCall(): Boolean {
        return callService.isConnectedCall()
    }

    override fun getVideoTrackMap(): MutableMap<Int, QBRTCVideoTrack> {
        return callService.getVideoTrackMap()
    }

    override fun getVideoTrack(userId: Int): QBRTCVideoTrack? {
        return callService.getVideoTrack(userId)
    }

    override fun onStopPreview() {
        callService.stopScreenSharing()
        addConversationFragment(isInComingCall)
    }

    private fun notifyCallStarted() {
        for (listener in callStateListeners) {
            listener.startedCall()
        }
    }

    private fun notifyCallStopped() {
        for (listener in callStateListeners) {
            listener.stoppedCall()
        }
    }

    private fun notifyOpponentsUpdated(opponents: ArrayList<QBUser>) {
        for (listener in updateOpponentsListeners) {
            listener.updatedOpponents(opponents)
        }
    }

    private fun notifyCallTimeUpdated(callTime: String) {
        for (listener in callTimeUpdateListeners) {
            listener.updatedCallTime(callTime)
        }
    }

    private inner class CallServiceConnection : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {
            // empty
        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as CallService.CallServiceBinder
            callService = binder.getService()
            if (callService.currentSessionExist()) {
                initScreen();
            } else {
                finish()
            }
        }
    }

    private inner class CallTimerCallback : CallService.CallTimerListener {
        override fun onCallTimeUpdate(time: String) {
            runOnUiThread {
                notifyCallTimeUpdated(time)
            }
        }
    }

    interface OnChangeAudioDevice {
        fun audioDeviceChanged(newAudioDevice: AppRTCAudioManager.AudioDevice)
    }

    interface CallStateListener {
        fun startedCall()

        fun stoppedCall()
    }

    interface UpdateOpponentsListener {
        fun updatedOpponents(updatedOpponents: ArrayList<QBUser>)
    }

    interface CallTimeUpdateListener {
        fun updatedCallTime(time: String)
    }
}