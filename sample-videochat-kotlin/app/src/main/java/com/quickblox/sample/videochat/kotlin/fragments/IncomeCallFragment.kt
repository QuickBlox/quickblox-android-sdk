package com.quickblox.sample.videochat.kotlin.fragments

import android.app.Activity
import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.SystemClock
import android.os.Vibrator
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.quickblox.chat.QBChatService
import com.quickblox.core.QBEntityCallback
import com.quickblox.core.exception.QBResponseException
import com.quickblox.core.helper.StringifyArrayList
import com.quickblox.core.request.GenericQueryRule
import com.quickblox.core.request.QBPagedRequestBuilder
import com.quickblox.sample.videochat.kotlin.R
import com.quickblox.sample.videochat.kotlin.db.QbUsersDbManager
import com.quickblox.sample.videochat.kotlin.util.loadUsersByPagedRequestBuilder
import com.quickblox.sample.videochat.kotlin.utils.RingtonePlayer
import com.quickblox.sample.videochat.kotlin.utils.WebRtcSessionManager
import com.quickblox.sample.videochat.kotlin.utils.getColorCircleDrawable
import com.quickblox.users.model.QBUser
import com.quickblox.videochat.webrtc.QBRTCSession
import com.quickblox.videochat.webrtc.QBRTCTypes
import java.io.Serializable
import java.util.*
import java.util.concurrent.TimeUnit

private const val PER_PAGE_SIZE_100 = 100
private const val ORDER_RULE = "order"
private const val ORDER_DESC_UPDATED = "desc string updated_at"

class IncomeCallFragment : Fragment(), Serializable, View.OnClickListener {
    private val TAG = IncomeCallFragment::class.java.simpleName
    private val CLICK_DELAY = TimeUnit.SECONDS.toMillis(2)

    //Views
    private lateinit var callTypeTextView: TextView
    private lateinit var rejectButton: ImageButton
    private lateinit var takeButton: ImageButton
    private lateinit var alsoOnCallText: TextView
    private lateinit var progressUserName: ProgressBar
    private lateinit var callerNameTextView: TextView

    private var opponentsIds: List<Int>? = null
    private var vibrator: Vibrator? = null
    private var conferenceType: QBRTCTypes.QBConferenceType? = null
    private var lastClickTime = 0L
    private lateinit var ringtonePlayer: RingtonePlayer
    private lateinit var incomeCallFragmentCallbackListener: IncomeCallFragmentCallbackListener
    private var currentSession: QBRTCSession? = null

    override fun onAttach(activity: Activity?) {
        super.onAttach(activity)
        try {
            incomeCallFragmentCallbackListener = activity as IncomeCallFragmentCallbackListener
        } catch (e: ClassCastException) {
            throw ClassCastException(activity?.toString() + " must implement OnCallEventsController")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        retainInstance = true

        Log.d(TAG, "onCreate() from IncomeCallFragment")
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_income_call, container, false)

        initFields()
        hideToolBar()

        conferenceType?.let {
            initUI(view)
            setDisplayedTypeCall(it)
            initButtonsListener()
        }

        val context = activity as Context
        ringtonePlayer = RingtonePlayer(context)
        return view
    }

    private fun initFields() {
        currentSession = WebRtcSessionManager.getCurrentSession()

        currentSession?.let {
            opponentsIds = it.opponents
            conferenceType = it.conferenceType
            Log.d(TAG, conferenceType.toString() + "From onCreateView()")
        }
    }

    private fun hideToolBar() {
        val toolbar = activity?.findViewById<View>(R.id.toolbar_call)
        toolbar?.visibility = View.GONE
    }

    override fun onStart() {
        super.onStart()
        startCallNotification()
    }

    private fun initButtonsListener() {
        rejectButton.setOnClickListener(this)
        takeButton.setOnClickListener(this)
    }

    private fun initUI(view: View) {
        callTypeTextView = view.findViewById(R.id.call_type)
        val callerAvatarImageView = view.findViewById<ImageView>(R.id.image_caller_avatar)
        callerNameTextView = view.findViewById(R.id.text_caller_name)
        val otherIncUsersTextView = view.findViewById<TextView>(R.id.text_other_inc_users)
        progressUserName = view.findViewById(R.id.progress_bar_opponent_name)
        alsoOnCallText = view.findViewById(R.id.text_also_on_call)
        rejectButton = view.findViewById(R.id.image_button_reject_call)
        takeButton = view.findViewById(R.id.image_button_accept_call)

        currentSession?.let {
            callerAvatarImageView.setBackgroundDrawable(getBackgroundForCallerAvatar(it.callerID))
        }

        val callerUser = QbUsersDbManager.getUserById(currentSession?.callerID)

        if (callerUser != null && !TextUtils.isEmpty(callerUser.fullName)) {
            callerNameTextView.text = callerUser.fullName
        } else {
            callerNameTextView.text = currentSession?.callerID.toString()
            updateUserFromServer()
        }

        otherIncUsersTextView.text = getOtherIncUsersNames()

        setVisibilityAlsoOnCallTextView()
    }

    fun updateUserFromServer() {
        progressUserName.visibility = View.VISIBLE

        val rules = ArrayList<GenericQueryRule>()
        rules.add(GenericQueryRule(ORDER_RULE, ORDER_DESC_UPDATED))
        val requestBuilder = QBPagedRequestBuilder()
        requestBuilder.rules = rules
        requestBuilder.perPage = PER_PAGE_SIZE_100

        loadUsersByPagedRequestBuilder(object : QBEntityCallback<ArrayList<QBUser>> {
            override fun onSuccess(users: ArrayList<QBUser>, params: Bundle?) {
                QbUsersDbManager.saveAllUsers(users, true)
                var callerUser: QBUser? = QbUsersDbManager.getUserById(currentSession?.callerID)
                if (callerUser != null && !TextUtils.isEmpty(callerUser.fullName)) {
                    callerNameTextView.text = callerUser.fullName
                }
                progressUserName.visibility = View.GONE
            }

            override fun onError(e: QBResponseException?) {
                progressUserName.visibility = View.GONE
            }
        }, requestBuilder)
    }

    private fun setVisibilityAlsoOnCallTextView() {
        opponentsIds?.let {
            if (it.size < 2) {
                alsoOnCallText.visibility = View.INVISIBLE
            }
        }
    }

    private fun getBackgroundForCallerAvatar(callerId: Int): Drawable {
        return getColorCircleDrawable(callerId)
    }

    private fun startCallNotification() {
        Log.d(TAG, "startCallNotification()")

        ringtonePlayer.play(false)

        vibrator = activity?.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator?

        val vibrationCycle = longArrayOf(0, 1000, 1000)
        vibrator?.hasVibrator()?.let {
            vibrator?.vibrate(vibrationCycle, 1)
        }
    }

    private fun stopCallNotification() {
        Log.d(TAG, "stopCallNotification()")

        ringtonePlayer.stop()
        vibrator?.cancel()
    }

    private fun getOtherIncUsersNames(): String {
        var result = ""
        opponentsIds?.let {
            val usersFromDb = QbUsersDbManager.getUsersByIds(it)
            val opponents = ArrayList<QBUser>()
            opponents.addAll(getListAllUsersFromIds(usersFromDb, it))

            opponents.remove(QBChatService.getInstance().user)
            Log.d(TAG, "opponentsIds = $opponentsIds")
            result = makeStringFromUsersFullNames(opponents)
        }
        return result
    }

    fun makeStringFromUsersFullNames(allUsers: ArrayList<QBUser>): String {
        val usersNames = StringifyArrayList<String>()

        for (usr in allUsers) {
            if (usr.fullName != null) {
                usersNames.add(usr.fullName)
            } else if (usr.id != null) {
                usersNames.add(usr.id.toString())
            }
        }
        return usersNames.itemsAsString.replace(",", ", ")
    }

    fun getListAllUsersFromIds(existedUsers: ArrayList<QBUser>, allIds: List<Int>): ArrayList<QBUser> {
        val qbUsers = ArrayList<QBUser>()

        for (userId in allIds) {
            val stubUser = createStubUserById(userId)
            if (!existedUsers.contains(stubUser)) {
                qbUsers.add(stubUser)
            }
        }

        qbUsers.addAll(existedUsers)
        return qbUsers
    }

    private fun createStubUserById(userId: Int?): QBUser {
        val stubUser = QBUser(userId)
        stubUser.fullName = userId.toString()
        return stubUser
    }

    private fun setDisplayedTypeCall(conferenceType: QBRTCTypes.QBConferenceType) {
        val isVideoCall = conferenceType == QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_VIDEO

        val callType = if (isVideoCall) {
            R.string.text_incoming_video_call
        } else {
            R.string.text_incoming_audio_call
        }
        callTypeTextView.setText(callType)

        val imageResource = if (isVideoCall) {
            R.drawable.ic_video_white
        } else {
            R.drawable.ic_call
        }
        takeButton.setImageResource(imageResource)
    }

    override fun onStop() {
        stopCallNotification()
        super.onStop()
        Log.d(TAG, "onStop() from IncomeCallFragment")
    }

    override fun onClick(v: View) {
        if (SystemClock.uptimeMillis() - lastClickTime < CLICK_DELAY) {
            return
        }
        lastClickTime = SystemClock.uptimeMillis()

        when (v.id) {
            R.id.image_button_reject_call -> reject()
            R.id.image_button_accept_call -> accept()
            else -> {
            }
        }
    }

    private fun accept() {
        enableButtons(false)
        stopCallNotification()

        incomeCallFragmentCallbackListener.onAcceptCurrentSession()
        Log.d(TAG, "Call is started")
    }

    private fun reject() {
        enableButtons(false)
        stopCallNotification()

        incomeCallFragmentCallbackListener.onRejectCurrentSession()
        Log.d(TAG, "Call is rejected")
    }

    private fun enableButtons(enable: Boolean) {
        takeButton.isEnabled = enable
        rejectButton.isEnabled = enable
    }
}