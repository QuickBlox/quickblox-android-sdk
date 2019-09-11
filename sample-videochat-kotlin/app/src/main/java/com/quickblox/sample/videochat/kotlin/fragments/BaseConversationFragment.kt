package com.quickblox.sample.videochat.kotlin.fragments

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.ToggleButton
import com.quickblox.chat.QBChatService
import com.quickblox.core.helper.StringifyArrayList
import com.quickblox.sample.videochat.kotlin.R
import com.quickblox.sample.videochat.kotlin.activities.CallActivity
import com.quickblox.sample.videochat.kotlin.db.QbUsersDbManager
import com.quickblox.sample.videochat.kotlin.services.CallService
import com.quickblox.sample.videochat.kotlin.utils.EXTRA_IS_INCOMING_CALL
import com.quickblox.sample.videochat.kotlin.utils.SharedPrefsHelper
import com.quickblox.users.model.QBUser
import com.quickblox.videochat.webrtc.QBRTCTypes
import java.util.*
import kotlin.collections.HashMap

private val TAG = BaseConversationFragment::class.java.simpleName
const val MIC_ENABLED = "is_microphone_enabled"

abstract class BaseConversationFragment : BaseToolBarFragment(), CallActivity.CurrentCallStateCallback {

    private var isIncomingCall: Boolean = false
    protected lateinit var timerCallText: TextView
    protected var conversationFragmentCallback: ConversationFragmentCallback? = null
    protected lateinit var currentUser: QBUser
    protected lateinit var opponents: ArrayList<QBUser>
    private var isStarted: Boolean = false

    private lateinit var micToggleVideoCall: ToggleButton
    private lateinit var handUpVideoCall: ImageButton
    protected lateinit var outgoingOpponentsRelativeLayout: View
    protected lateinit var allOpponentsTextView: TextView
    protected lateinit var ringingTextView: TextView

    companion object {
        fun newInstance(baseConversationFragment: BaseConversationFragment, isIncomingCall: Boolean): BaseConversationFragment {
            Log.d(TAG, "isIncomingCall =  $isIncomingCall")
            val args = Bundle()
            args.putBoolean(EXTRA_IS_INCOMING_CALL, isIncomingCall)
            baseConversationFragment.arguments = args
            return baseConversationFragment
        }
    }

    override fun onAttach(activity: Activity?) {
        super.onAttach(activity)
        try {
            conversationFragmentCallback = context as ConversationFragmentCallback
        } catch (e: ClassCastException) {
            throw ClassCastException(activity?.toString() + " must implement ConversationFragmentCallback")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        conversationFragmentCallback?.addCurrentCallStateListener(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)

        initFields()
        initViews(view)
        initActionBar()
        initButtonsListener()
        prepareAndShowOutgoingScreen()

        return view
    }

    private fun initActionBar() {
        configureToolbar()
        configureActionBar()
    }

    protected abstract fun configureActionBar()

    protected abstract fun configureToolbar()

    private fun prepareAndShowOutgoingScreen() {
        configureOutgoingScreen()
        allOpponentsTextView.text = getUserNamesFromUsersFullNames(opponents)
    }

    private fun getUserNamesFromUsersFullNames(allUsers: ArrayList<QBUser>): String {
        val usersNames = StringifyArrayList<String>()
        for (user in allUsers) {
            if (user.fullName != null) {
                usersNames.add(user.fullName)
            } else if (user.id != null) {
                usersNames.add(user.id.toString())
            }
        }
        return usersNames.itemsAsString.replace(",", ", ")
    }

    protected abstract fun configureOutgoingScreen()

    protected open fun initFields() {
        if (QBChatService.getInstance().user == null) {
            currentUser = SharedPrefsHelper.getQbUser()
        } else {
            currentUser = QBChatService.getInstance().user
        }

        arguments?.let {
            isIncomingCall = it.getBoolean(EXTRA_IS_INCOMING_CALL, false)
        }
        initOpponentsList()
        Log.d(TAG, "opponents: $opponents")
    }

    override fun onStart() {
        super.onStart()
        if (isIncomingCall) {
            conversationFragmentCallback?.acceptCall(HashMap())
        } else {
            conversationFragmentCallback?.startCall(HashMap())
        }
    }

    override fun onDestroy() {
        conversationFragmentCallback?.removeCurrentCallStateListener(this)
        super.onDestroy()
    }

    protected open fun initViews(view: View?) {
        micToggleVideoCall = view?.findViewById<View>(R.id.toggle_mic) as ToggleButton
        micToggleVideoCall.isChecked = SharedPrefsHelper.get(MIC_ENABLED, true)
        handUpVideoCall = view.findViewById<View>(R.id.button_hangup_call) as ImageButton
        outgoingOpponentsRelativeLayout = view.findViewById(R.id.layout_background_outgoing_screen)
        allOpponentsTextView = view.findViewById<View>(R.id.text_outgoing_opponents_names) as TextView
        ringingTextView = view.findViewById<View>(R.id.text_ringing) as TextView

        if (isIncomingCall) {
            hideOutgoingScreen()
        }
    }

    protected open fun initButtonsListener() {
        micToggleVideoCall.setOnCheckedChangeListener { buttonView, isChecked ->
            SharedPrefsHelper.save(MIC_ENABLED, isChecked)
            conversationFragmentCallback?.onSetAudioEnabled(isChecked)
        }

        handUpVideoCall.setOnClickListener {
            actionButtonsEnabled(false)
            handUpVideoCall.isEnabled = false
            handUpVideoCall.isActivated = false
            CallService.stop(activity as Activity)
            conversationFragmentCallback?.onHangUpCurrentSession()
            Log.d(TAG, "Call is stopped")
        }
    }

    private fun clearButtonsState() {
        SharedPrefsHelper.delete(MIC_ENABLED)
        SharedPrefsHelper.delete(SPEAKER_ENABLED)
        SharedPrefsHelper.delete(CAMERA_ENABLED)
    }

    protected open fun actionButtonsEnabled(inability: Boolean) {
        micToggleVideoCall.isEnabled = inability
        // inactivate toggle buttons
        micToggleVideoCall.isActivated = inability
    }

    private fun startTimer() {
        if (!isStarted) {
            timerCallText.visibility = View.VISIBLE
            isStarted = true
        }
    }

    private fun hideOutgoingScreen() {
        outgoingOpponentsRelativeLayout.visibility = View.GONE
    }

    override fun onCallStarted() {
        hideOutgoingScreen()
        startTimer()
        actionButtonsEnabled(true)
    }

    override fun onCallStopped() {
        CallService.stop(activity as Activity)
        isStarted = false
        clearButtonsState()
        actionButtonsEnabled(false)
    }

    override fun onOpponentsListUpdated(newUsers: ArrayList<QBUser>) {
        initOpponentsList()
    }

    private fun initOpponentsList() {
        Log.v("UPDATE_USERS", "super initOpponentsList()")
        val opponnentsIds = conversationFragmentCallback?.getOpponents()
        opponnentsIds?.let {
            val usersFromDb = QbUsersDbManager.getUsersByIds(it)
            opponents = getListAllUsersFromIds(usersFromDb, it)
        }

        var caller = QbUsersDbManager.getUserById(conversationFragmentCallback?.getCallerId())
        if (caller == null) {
            caller = QBUser(conversationFragmentCallback?.getCallerId())
            caller.fullName = conversationFragmentCallback?.getCallerId().toString()
        }

        if (isIncomingCall) {
            opponents.add(caller)
            opponents.remove(QBChatService.getInstance().user)
        }
    }

    private fun getListAllUsersFromIds(existedUsers: ArrayList<QBUser>, allIds: List<Int>): ArrayList<QBUser> {
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

    fun getConnectionState(userId: Int): QBRTCTypes.QBRTCConnectionState? {
        return conversationFragmentCallback?.getPeerChannel(userId)
    }
}