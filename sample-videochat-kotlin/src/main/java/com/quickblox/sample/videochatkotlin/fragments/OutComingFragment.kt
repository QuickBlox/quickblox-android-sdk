package com.quickblox.sample.videochatkotlin.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageButton
import com.quickblox.sample.core.utils.Toaster
import com.quickblox.sample.videochatkotlin.R
import com.quickblox.sample.videochatkotlin.utils.EXTRA_QB_USERS_LIST
import com.quickblox.sample.videochatkotlin.utils.MAX_OPPONENTS_COUNT
import com.quickblox.sample.videochatkotlin.utils.getIdsSelectedOpponents
import com.quickblox.sample.videochatkotlin.view.CameraPreview
import com.quickblox.users.model.QBUser
import com.quickblox.videochat.webrtc.QBRTCClient
import com.quickblox.videochat.webrtc.QBRTCTypes
import org.webrtc.ContextUtils.getApplicationContext


class OutComingFragment : Fragment() {
    private val TAG = OutComingFragment::class.java.simpleName

    val cameraFront = 1
    lateinit var cameraPreview: CameraPreview
    lateinit var frameLayout: FrameLayout
    lateinit var startCallButton: ImageButton
    lateinit var hangUpCallButton: ImageButton
    lateinit var opponents: ArrayList<QBUser>

    override fun onCreate(savedInstanceState: Bundle?) {
        retainInstance = true

        Log.d(TAG, "onCreate() from OutComingFragment")
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_outcome_call, container, false)
        frameLayout = view.findViewById(R.id.camera_preview) as FrameLayout
        startCallButton = view.findViewById(R.id.button_start_call)
        startCallButton.setOnClickListener({ startCall() })
        hangUpCallButton = view.findViewById(R.id.button_hangup_call)
        initFields()
        return view
    }

    fun initFields(){
        val obj= arguments.get(EXTRA_QB_USERS_LIST)
        if(obj is ArrayList<*>){
            opponents = obj.filterIsInstance<QBUser>() as ArrayList<QBUser>
        }
        Log.d(TAG,"users= " + opponents)
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

        val qbrtcClient = QBRTCClient.getInstance(getApplicationContext())

        val newQbRtcSession = qbrtcClient.createNewSessionWithOpponents(opponentsList, conferenceType)

        Log.d(TAG, "conferenceType = " + conferenceType)
    }
    override fun onResume() {
        super.onResume()
        startCameraPreview()
    }

    fun startCameraPreview() {
        cameraPreview = CameraPreview(activity, cameraFront)
        frameLayout.addView(cameraPreview)
    }

    fun stopCameraPreview() {
        cameraPreview.stop()
    }

    override fun onPause() {
        super.onPause()
        stopCameraPreview()
    }

    fun onStartCallClick() {

    }
}