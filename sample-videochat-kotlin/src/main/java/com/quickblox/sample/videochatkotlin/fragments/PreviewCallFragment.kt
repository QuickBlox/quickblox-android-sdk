package com.quickblox.sample.videochatkotlin.fragments

import android.app.Activity
import android.hardware.Camera
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.TextView
import com.quickblox.sample.core.utils.Toaster
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
import org.webrtc.ContextUtils


class PreviewCallFragment : BaseToolBarFragment() {
    private val TAG = PreviewCallFragment::class.java.simpleName

    private lateinit var cameraPreview: CameraPreview
    private lateinit var frameLayout: FrameLayout
    private lateinit var startCallButton: ImageButton
    private lateinit var hangUpCallButton: ImageButton
    private lateinit var incomeTextView: TextView
    private lateinit var opponents: ArrayList<QBUser>
    private lateinit var eventListener: CallFragmentCallbackListener
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

    override fun onAttach(activity: Activity) {
        super.onAttach(activity)
        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            eventListener = activity as CallFragmentCallbackListener
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)!!
        frameLayout = view.findViewById(R.id.camera_preview)
        startCallButton = view.findViewById(R.id.button_start_call)
        startCallButton.setOnClickListener({ startOrAcceptCall() })
        startCallButtonVisibility(View.VISIBLE)
        hangUpCallButton = view.findViewById(R.id.button_hangup_call)
        hangUpCallButton.setOnClickListener({ rejectCall() })
        hangUpButtonvisibility(View.GONE)
        incomeTextView = view.findViewById(R.id.income_call_type)
        isIncomingCall = false
        return view
    }

    private fun initFields() {
        val obj = arguments!!.get(EXTRA_QB_USERS_LIST)
        if (obj is ArrayList<*>) {
            opponents = obj.filterIsInstance<QBUser>() as ArrayList<QBUser>
            val currentUser = ChatHelper.instance.currentUser
            opponents.remove(currentUser)
        }
        Log.d(TAG, "users= $opponents")
    }

    private fun startOrAcceptCall() {
        if (isIncomingCall) {
            isIncomingCall = false
            incomeTextViewVisibility(View.INVISIBLE)
            startCallButtonVisibility(View.GONE)
            eventListener.onAcceptCall()
        } else {
            startCall()
        }
    }

    private fun rejectCall() {
        eventListener.onRejectCall()
        hangUpButtonvisibility(View.GONE)
        incomeTextViewVisibility(View.INVISIBLE)
    }

    private fun startCall() {
        if (opponents.size > MAX_OPPONENTS_COUNT) {
            Toaster.longToast(String.format(getString(R.string.error_max_opponents_count),
                    MAX_OPPONENTS_COUNT))
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
        frameLayout.addView(cameraPreview)
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
            hangUpButtonvisibility(View.VISIBLE)
            incomeTextViewVisibility(View.VISIBLE)
        } else {
            isIncomingCall = false
            hangUpButtonvisibility(View.GONE)
            incomeTextViewVisibility(View.GONE)
        }
    }

    private fun startCallButtonVisibility(visibility: Int) {
        startCallButton.visibility = visibility
    }

    private fun hangUpButtonvisibility(visibility: Int) {
        hangUpCallButton.visibility = visibility
    }

    private fun incomeTextViewVisibility(visibility: Int) {
        incomeTextView.visibility = visibility
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
}