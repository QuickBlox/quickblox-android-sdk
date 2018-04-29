package com.quickblox.sample.videochatkotlin.fragments

import android.app.Activity
import android.app.ProgressDialog
import android.os.Bundle
import android.support.annotation.StringRes
import android.util.Log
import android.view.*
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.TextView
import com.quickblox.chat.QBChatService
import com.quickblox.core.QBEntityCallback
import com.quickblox.core.exception.QBResponseException
import com.quickblox.sample.core.utils.Toaster
import com.quickblox.sample.videochatkotlin.R
import com.quickblox.sample.videochatkotlin.utils.*
import com.quickblox.sample.videochatkotlin.view.CameraPreview
import com.quickblox.users.model.QBUser
import com.quickblox.videochat.webrtc.QBRTCClient
import com.quickblox.videochat.webrtc.QBRTCSession
import com.quickblox.videochat.webrtc.QBRTCTypes
import org.webrtc.ContextUtils


class PreviewCallFragment : BaseToolBarFragment() {
    private val TAG = PreviewCallFragment::class.java.simpleName

    val cameraFront = 1
    lateinit var cameraPreview: CameraPreview
    lateinit var frameLayout: FrameLayout
    lateinit var startCallButton: ImageButton
    lateinit var hangUpCallButton: ImageButton
    lateinit var incomeTextView: TextView
    lateinit var opponents: ArrayList<QBUser>
    lateinit var eventListener: CallFragmentCallbackListener
    private var progressDialog: ProgressDialog? = null
    var isIncomingCall: Boolean = false

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
        loadUsers()
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
        return view
    }

    fun initFields() {
        val obj = arguments!!.get(EXTRA_QB_USERS_LIST)
        if (obj is ArrayList<*>) {
            opponents = obj.filterIsInstance<QBUser>() as ArrayList<QBUser>
            val currentUser = QBChatService.getInstance().user
            opponents.remove(currentUser)
        }
        Log.d(TAG, "users= " + opponents)
    }

    fun loadUsers() {
        showProgress(R.string.dlg_loading_opponents)
        val logins = ArrayList<String>()
        opponents.forEach { logins.add(it.login) }
        loadUsersByLogins(logins, object : QBEntityCallback<ArrayList<QBUser>> {
            override fun onSuccess(users: ArrayList<QBUser>, p1: Bundle?) {
                hideProgress()
                opponents = users
            }

            override fun onError(responseException: QBResponseException?) {
                hideProgress()
                showErrorSnackbar(view, R.string.loading_users_error, responseException!!, View.OnClickListener { loadUsers() })
            }
        })

    }

    fun startOrAcceptCall() {
        if (isIncomingCall) {
            isIncomingCall = false
            incomeTextViewVisibility(View.INVISIBLE)
            startCallButtonVisibility(View.GONE)
            eventListener.onAcceptCall()
        } else {
            startCall()
        }
    }

    fun rejectCall() {
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

    fun startCameraPreview() {
        cameraPreview = CameraPreview(activity!!, cameraFront)
        frameLayout.addView(cameraPreview)
    }

    fun stopCameraPreview() {
        cameraPreview.stop()
    }

    override fun onPause() {
        super.onPause()
        stopCameraPreview()
    }

    fun updateCallButtons() {
        Log.d(TAG, "updateCallButtons")
        isIncomingCall = true
        hangUpButtonvisibility(View.VISIBLE)
        incomeTextViewVisibility(View.VISIBLE)
    }

    fun startCallButtonVisibility(visibility: Int) {
        startCallButton.visibility = visibility
    }

    fun hangUpButtonvisibility(visibility: Int) {
        hangUpCallButton.visibility = visibility
    }

    fun incomeTextViewVisibility(visibility: Int) {
        incomeTextView.visibility = visibility
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.activity_call, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.menu_logout_user_done -> {
                eventListener.onLogout()
                return true
            }

            else -> return super.onOptionsItemSelected(item)
        }
    }

    fun showProgress(@StringRes messageId: Int) {
        if (progressDialog == null) {
            progressDialog = ProgressDialog(context)
        }
        showProgressDialog(context!!, progressDialog!!, messageId)
    }

    fun hideProgress() {
        if (progressDialog != null) {
            hideProgressDialog(progressDialog!!)
        }
    }
}