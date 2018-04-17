package com.quickblox.sample.videochatkotlin.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.WindowManager
import com.quickblox.sample.core.ui.activity.CoreBaseActivity
import com.quickblox.sample.core.utils.Toaster
import com.quickblox.sample.videochatkotlin.R
import com.quickblox.sample.videochatkotlin.fragments.OutComingFragment
import com.quickblox.sample.videochatkotlin.fragments.VideoConversationFragment
import com.quickblox.sample.videochatkotlin.services.CallService
import com.quickblox.sample.videochatkotlin.utils.*
import com.quickblox.sample.videochatkotlin.utils.StringUtils.createCompositeString
import com.quickblox.users.model.QBUser
import com.quickblox.videochat.webrtc.BaseSession
import com.quickblox.videochat.webrtc.QBRTCClient
import com.quickblox.videochat.webrtc.QBRTCConfig
import com.quickblox.videochat.webrtc.QBRTCSession
import com.quickblox.videochat.webrtc.callbacks.QBRTCClientSessionCallbacks
import com.quickblox.videochat.webrtc.callbacks.QBRTCSessionEventsCallback
import com.quickblox.videochat.webrtc.callbacks.QBRTCSessionStateCallback
import java.util.HashMap

/**
 * Created by roman on 4/6/18.
 */
class CallActivity : CoreBaseActivity(), QBRTCClientSessionCallbacks, QBRTCSessionStateCallback<QBRTCSession>, OutComingFragment.CallFragmentCallbackListener,
        VideoConversationFragment.CallFragmentCallbackListener, QBRTCSessionEventsCallback {

    val TAG = CallActivity::class.java.simpleName
    lateinit var systemPermissionHelper: SystemPermissionHelper
    lateinit var opponents: ArrayList<QBUser>
    private var rtcClient: QBRTCClient? = null
    var currentSession: QBRTCSession? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate")
        setContentView(R.layout.activity_main)
        initFields()
        initQBRTCClient()
        initActionBar()
        systemPermissionHelper = SystemPermissionHelper(this)
        checkCameraPermissionAndStart()
    }

    fun initFields() {
        val obj = intent.getSerializableExtra(EXTRA_QB_USERS_LIST)
        if (obj is ArrayList<*>) {
            opponents = obj.filterIsInstance<QBUser>() as ArrayList<QBUser>
        }
    }

    @SuppressLint("InlinedApi")
    fun initActionBar() {
        setActionBarTitle(R.string.title_call_activity)
        actionBar.setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(this, R.color.black_transparent_50)))
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
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
            initSuitableFragment()
        } else {
            systemPermissionHelper.requestPermissionsForCallByType()
        }
    }

    private fun initSuitableFragment() {
        initOutgoingFragment()
    }

    fun initOutgoingFragment() {
        val outComingFragment = OutComingFragment()
        val args = Bundle()
        args.putSerializable(EXTRA_QB_USERS_LIST, opponents)
        outComingFragment.arguments = args
        addFragment(supportFragmentManager, R.id.fragment_container, outComingFragment, OutComingFragment::class.java.simpleName)
    }

    fun initConversationFragment(incoming: Boolean) {
        val conversationFragment = VideoConversationFragment()
        val args = Bundle()
        args.putBoolean(EXTRA_IS_INCOMING_CALL, incoming)
        conversationFragment.arguments = args
        addFragment(supportFragmentManager, R.id.fragment_container, conversationFragment, VideoConversationFragment::class.java.simpleName)
    }

    fun initIncomeCall() {
        val outComeFrag = supportFragmentManager.findFragmentByTag(OutComingFragment::class.java.simpleName) as OutComingFragment?
        Log.d(TAG, "AMBRA initIncomeCall")
        if (outComeFrag != null) {
            Log.d(TAG, "AMBRA updateCallButtons")
            outComeFrag.updateCallButtons()
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
                        initOutgoingFragment()
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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.activity_call, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.menu_logout_user_done -> {
                startLogout()
                startLoginActivity()
                return true
            }

            else -> return super.onOptionsItemSelected(item)
        }
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
        currentSession!!.removeSessionCallbacksListener(this@CallActivity)
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
            initOutgoingFragment()
        }
    }

    override fun onCallRejectByUser(p0: QBRTCSession?, p1: Int?, p2: MutableMap<String, String>?) {
        Log.d(TAG, "AMBRA onCallRejectByUser")
    }

    override fun onUserNotAnswer(p0: QBRTCSession?, p1: Int?) {

    }

    interface IncomingCallStateCallback {
        fun onIncomingCallReceive()
    }
}