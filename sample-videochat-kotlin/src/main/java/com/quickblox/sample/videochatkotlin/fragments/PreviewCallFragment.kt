package com.quickblox.sample.videochatkotlin.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.hardware.Camera
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.quickblox.sample.videochatkotlin.R
import com.quickblox.sample.videochatkotlin.utils.ChatHelper
import com.quickblox.sample.videochatkotlin.utils.EXTRA_QB_USERS_LIST
import com.quickblox.sample.videochatkotlin.utils.MAX_OPPONENTS_COUNT
import com.quickblox.sample.videochatkotlin.utils.getIdsSelectedOpponents
import com.quickblox.sample.videochatkotlin.view.CameraPreview
import com.quickblox.users.model.QBUser
import com.quickblox.videochat.webrtc.QBRTCClient
import com.quickblox.videochat.webrtc.QBRTCSession
import com.quickblox.videochat.webrtc.QBRTCTypes
import kotlinx.android.synthetic.main.fragment_preview.*
import kotlinx.android.synthetic.main.view_action_buttons_conversation_fragment.*
import org.webrtc.ContextUtils


class PreviewCallFragment : BaseToolBarFragment() {
    private val TAG = PreviewCallFragment::class.java.simpleName

    private lateinit var cameraPreview: CameraPreview
    private lateinit var opponents: ArrayList<QBUser>
    private lateinit var eventListener: CallFragmentCallbackListener
    private lateinit var snackBar: Snackbar
    private var isIncomingCall: Boolean = false

    override val fragmentLayout: Int
        get() = R.layout.fragment_preview

    // Container CallActivity must implement this interface
    interface CallFragmentCallbackListener {
        fun onStartCall(session: QBRTCSession)
        fun onAcceptCall()
        fun onRejectCall()
        fun onLogout()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            eventListener = context as CallFragmentCallbackListener
        } catch (e: ClassCastException) {
            throw ClassCastException(activity.toString() + " must implement CallFragmentCallbackListener")
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        retainInstance = true
        Log.d(TAG, "onCreate() from PreviewCallFragment")
        initFields()
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        isIncomingCall = false
        button_start_call.setOnClickListener({ startOrAcceptCall() })
        startCallButtonVisibility(View.VISIBLE)
        button_hangup_call.setOnClickListener({ rejectCall() })
        hangUpButtonVisibility(View.GONE)
        initSnackBar()
    }

    private fun initFields() {
        arguments!!.get(EXTRA_QB_USERS_LIST).let {
            if (it is ArrayList<*>) {
                opponents = it.filterIsInstance<QBUser>() as ArrayList<QBUser>
                val currentUser = ChatHelper.instance.currentUser
                opponents.remove(currentUser)
            }
        }
        Log.d(TAG, "users= $opponents")
    }

    @SuppressLint("NewApi")
    private fun initSnackBar() {
        snackBar = Snackbar.make(snackbar_view, R.string.text_incoming_video_call, Snackbar.LENGTH_INDEFINITE)
        val textView = snackBar.view.findViewById(android.support.design.R.id.snackbar_text) as TextView
        textView.textAlignment = View.TEXT_ALIGNMENT_CENTER
    }

    private fun startOrAcceptCall() {
        if (isIncomingCall) {
            isIncomingCall = false
            snackBar.dismiss()
            startCallButtonVisibility(View.GONE)
            eventListener.onAcceptCall()
        } else {
            startCall()
        }
    }

    private fun rejectCall() {
        eventListener.onRejectCall()
        hangUpButtonVisibility(View.GONE)
        snackBar.dismiss()
    }

    private fun startCall() {
        if (opponents.size > MAX_OPPONENTS_COUNT) {
            Toast.makeText(activity, String.format(getString(R.string.error_max_opponents_count),
                    MAX_OPPONENTS_COUNT), Toast.LENGTH_LONG).show()
            return
        }

        Log.d(TAG, "startCall()")
        val opponentsList = getIdsSelectedOpponents(opponents)
        val conferenceType = QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_VIDEO

        val qbrtcClient = QBRTCClient.getInstance(ContextUtils.getApplicationContext())

        val newQbRtcSession = qbrtcClient.createNewSessionWithOpponents(opponentsList, conferenceType)
        eventListener.onStartCall(newQbRtcSession)
    }

    override fun onResume() {
        super.onResume()
        startCameraPreview()
    }

    private fun startCameraPreview() {
        cameraPreview = CameraPreview(activity!!, Camera.CameraInfo.CAMERA_FACING_FRONT)
        camera_preview.addView(cameraPreview)
    }

    private fun stopCameraPreview() {
        cameraPreview.stop()
    }

    override fun onPause() {
        super.onPause()
        stopCameraPreview()
    }

    fun updateCallButtons(show: Boolean) {
        Log.d(TAG, "updateCallButtons show= $show")
        if (show) {
            isIncomingCall = true
            hangUpButtonVisibility(View.VISIBLE)
            snackBar.show()
        } else {
            isIncomingCall = false
            hangUpButtonVisibility(View.GONE)
            snackBar.dismiss()
        }
    }

    private fun startCallButtonVisibility(visibility: Int) {
        button_start_call.visibility = visibility
    }

    private fun hangUpButtonVisibility(visibility: Int) {
        button_hangup_call?.visibility = visibility
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.activity_call, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_logout_user_done -> {
                eventListener.onLogout()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    companion object {

        fun newInstance(opponents: ArrayList<QBUser>) =
                PreviewCallFragment().apply {
                    arguments = Bundle().apply {
                        putSerializable(EXTRA_QB_USERS_LIST, opponents)
                    }
                }
    }
}