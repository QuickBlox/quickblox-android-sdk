package com.quickblox.sample.videochat.kotlin.fragments

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.ToggleButton
import androidx.collection.arrayMapOf
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

abstract class BaseConversationFragment : BaseToolBarFragment() {
    private val TAG = BaseConversationFragment::class.simpleName
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
    private val callStateListener = CallStateListenerImpl(TAG)

    companion object {
        fun newInstance(baseConversationFragment: BaseConversationFragment, isIncomingCall: Boolean): BaseConversationFragment {
            Log.d(TAG, "isIncomingCall =  $isIncomingCall")
            val args = Bundle()
            args.putBoolean(EXTRA_IS_INCOMING_CALL, isIncomingCall)
            baseConversationFragment.arguments = args
            return baseConversationFragment
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            conversationFragmentCallback = context as ConversationFragmentCallback
        } catch (e: ClassCastException) {
            throw ClassCastException(activity?.toString() + " must implement ConversationFragmentCallback")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        conversationFragmentCallback?.addCallStateListener(callStateListener)
        conversationFragmentCallback?.addUpdateOpponentsListener(UpdateOpponentsListenerImpl(TAG))
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
        allOpponentsTextView.text = getNamesFromOpponents(opponents)
    }

    private fun getNamesFromOpponents(allOpponents: ArrayList<QBUser>): String {
        val opponentNames = StringifyArrayList<String>()
        for (opponent in allOpponents) {
            if (opponent.fullName != null) {
                opponentNames.add(opponent.fullName)
            } else if (opponent.id != null) {
                opponentNames.add(opponent.id.toString())
            }
        }
        return opponentNames.itemsAsString.replace(",", ", ")
    }

    protected abstract fun configureOutgoingScreen()

    protected open fun initFields() {
        if (QBChatService.getInstance().user == null) {
            currentUser = SharedPrefsHelper.getCurrentUser()
        } else {
            currentUser = QBChatService.getInstance().user
        }

        arguments?.let {
            isIncomingCall = it.getBoolean(EXTRA_IS_INCOMING_CALL, false)
        }
        initOpponents()
        Log.d(TAG, "opponents: $opponents")
    }

    override fun onStart() {
        super.onStart()
        if (isIncomingCall) {
            conversationFragmentCallback?.acceptCall(HashMap())
        } else {
            val userInfo = arrayMapOf<String, String>()
            userInfo["timestamp"] = System.currentTimeMillis().toString()
            conversationFragmentCallback?.startCall(userInfo)
        }
    }

    override fun onDestroy() {
        conversationFragmentCallback?.removeCallStateListener(callStateListener)
        conversationFragmentCallback?.removeUpdateOpponentsListener(UpdateOpponentsListenerImpl(TAG))
        super.onDestroy()
    }

    protected open fun initViews(view: View?) {
        view?.let {
            micToggleVideoCall = it.findViewById(R.id.toggle_mic)
            micToggleVideoCall.isChecked = SharedPrefsHelper[MIC_ENABLED, true]
            handUpVideoCall = it.findViewById(R.id.button_hangup_call)
            outgoingOpponentsRelativeLayout = it.findViewById(R.id.layout_background_outgoing_screen)
            allOpponentsTextView = it.findViewById(R.id.text_outgoing_opponents_names)
            ringingTextView = it.findViewById(R.id.text_ringing)
        }

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

    private fun initOpponents() {
        val opponentIds = conversationFragmentCallback?.getOpponents()
        opponentIds?.let {
            val usersFromDb = QbUsersDbManager.getUsersByIds(it)
            opponents = checkAndModifyOpponents(usersFromDb, it)
        }

        if (isIncomingCall) {
            var caller = QbUsersDbManager.getUserById(conversationFragmentCallback?.getCallerId())
            if (caller == null) {
                caller = QBUser(conversationFragmentCallback?.getCallerId())
                caller.fullName = conversationFragmentCallback?.getCallerId().toString()
            }

            opponents.add(caller)
            opponents.remove(QBChatService.getInstance().user)
        }
    }

    private fun checkAndModifyOpponents(existedUsers: ArrayList<QBUser>, allIds: List<Int>): ArrayList<QBUser> {
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

    protected fun startedCall() {
        callStateListener.startedCall()
    }

    private inner class CallStateListenerImpl(val tag: String?) : CallActivity.CallStateListener {
        override fun startedCall() {
            hideOutgoingScreen()
            startTimer()
            actionButtonsEnabled(true)
        }

        override fun stoppedCall() {
            isStarted = false
            clearButtonsState()
            actionButtonsEnabled(false)
        }

        override fun equals(other: Any?): Boolean {
            if (other is CallStateListenerImpl) {
                return tag == other.tag
            }
            return false
        }

        override fun hashCode(): Int {
            var hash = 1
            hash = 31 * hash + tag.hashCode()
            return hash
        }
    }

    private inner class UpdateOpponentsListenerImpl(val tag: String?) : CallActivity.UpdateOpponentsListener {
        override fun updatedOpponents(updatedOpponents: ArrayList<QBUser>) {
            initOpponents()
            allOpponentsTextView.text = getNamesFromOpponents(opponents)
        }

        override fun equals(other: Any?): Boolean {
            if (other is UpdateOpponentsListenerImpl) {
                return tag == other.tag
            }
            return false
        }

        override fun hashCode(): Int {
            var hash = 1
            hash = 31 * hash + tag.hashCode()
            return hash
        }
    }
}