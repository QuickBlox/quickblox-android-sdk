package com.quickblox.sample.videochat.kotlin.fragments

import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.ToggleButton
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.quickblox.core.helper.StringifyArrayList
import com.quickblox.sample.videochat.kotlin.R
import com.quickblox.sample.videochat.kotlin.activities.CallActivity
import com.quickblox.sample.videochat.kotlin.adapters.AudioCallAdapter
import com.quickblox.sample.videochat.kotlin.adapters.ReconnectingUserModel
import com.quickblox.sample.videochat.kotlin.utils.SharedPrefsHelper
import com.quickblox.sample.videochat.kotlin.utils.getColorCircleDrawable
import com.quickblox.users.model.QBUser
import com.quickblox.videochat.webrtc.QBRTCSession
import com.quickblox.videochat.webrtc.QBRTCTypes.QBRTCReconnectionState
import com.quickblox.videochat.webrtc.QBRTCTypes.QBRTCReconnectionState.*
import com.quickblox.videochat.webrtc.audio.QBAudioManager
import com.quickblox.videochat.webrtc.callbacks.QBRTCSessionEventsCallback


const val SPEAKER_ENABLED = "is_speaker_enabled"

class AudioConversationFragment : BaseConversationFragment(), CallActivity.OnChangeAudioDevice,
    QBRTCSessionEventsCallback {
    private val TAG = AudioConversationFragment::class.simpleName

    private lateinit var audioSwitchToggleButton: ToggleButton
    private lateinit var alsoOnCallText: TextView
    private lateinit var firstOpponentNameTextView: TextView
    private lateinit var otherOpponentsTextView: TextView
    private var adapter: AudioCallAdapter? = null

    override fun onStart() {
        super.onStart()
        conversationFragmentCallback?.addOnChangeAudioDeviceListener(this)
        conversationFragmentCallback?.addSessionEventsListener(this);
    }

    override fun onResume() {
        super.onResume()
        conversationFragmentCallback?.addCallTimeUpdateListener(CallTimeUpdateListenerImpl(TAG))
        conversationFragmentCallback?.addUpdateOpponentsListener(UpdateOpponentsListenerImpl(TAG))
    }

    override fun onPause() {
        super.onPause()
        conversationFragmentCallback?.removeCallTimeUpdateListener(CallTimeUpdateListenerImpl(TAG))
        conversationFragmentCallback?.removeUpdateOpponentsListener(UpdateOpponentsListenerImpl(TAG))
        conversationFragmentCallback?.removeSessionEventsListener(this)
    }

    override fun configureOutgoingScreen() {
        val context: Context = activity as Context
        outgoingOpponentsRelativeLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.white))
        allOpponentsTextView.setTextColor(
            ContextCompat.getColor(
                context,
                R.color.text_color_outgoing_opponents_names_audio_call
            )
        )
        ringingTextView.setTextColor(ContextCompat.getColor(context, R.color.text_color_call_type))
    }

    override fun configureToolbar() {
        toolbar?.visibility = View.VISIBLE
        toolbar?.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
        toolbar?.setTitleTextColor(ContextCompat.getColor(requireContext(), R.color.toolbar_title_color))
        toolbar?.setSubtitleTextColor(ContextCompat.getColor(requireContext(), R.color.toolbar_subtitle_color))
    }

    override fun configureActionBar() {
        actionBar.subtitle = String.format(getString(R.string.subtitle_text_logged_in_as), currentUser.fullName)
    }

    override fun initViews(view: View?) {
        super.initViews(view)
        if (view == null) {
            return
        }
        timerCallText = view.findViewById(R.id.timer_call)

        val firstOpponentAvatarImageView = view.findViewById<ImageView>(R.id.image_caller_avatar)
        firstOpponentAvatarImageView?.setBackgroundDrawable(getColorCircleDrawable(opponents[0].id))

        alsoOnCallText = view.findViewById(R.id.text_also_on_call)
        setVisibilityAlsoOnCallTextView()

        firstOpponentNameTextView = view.findViewById(R.id.text_caller_name)
        val name = opponents[0].fullName ?: opponents[0].login
        firstOpponentNameTextView.text = name

        otherOpponentsTextView = view.findViewById(R.id.text_other_users)
        otherOpponentsTextView.text = getOtherOpponentNames()

        audioSwitchToggleButton = view.findViewById(R.id.toggle_speaker)
        audioSwitchToggleButton.visibility = View.VISIBLE
        audioSwitchToggleButton.isChecked = SharedPrefsHelper.get(SPEAKER_ENABLED, true)
        actionButtonsEnabled(false)

        if (conversationFragmentCallback?.isConnectedCall() == true) {
            startedCall()
        }

        val recyclerView = view.findViewById(R.id.rvUsers) as RecyclerView
        val users = ArrayList<ReconnectingUserModel>()
        for (user in opponents) {
            val state = conversationFragmentCallback!!.getState(user.id)
            if (state != null) {
                when (state) {
                    QB_RTC_RECONNECTION_STATE_RECONNECTING -> users.add(
                        ReconnectingUserModel(user, "Reconnecting")
                    )

                    QB_RTC_RECONNECTION_STATE_RECONNECTED -> users.add(
                        ReconnectingUserModel(user, "Reconnected")
                    )

                    QB_RTC_RECONNECTION_STATE_FAILED -> users.add(
                        ReconnectingUserModel(user, "Reconnection failed")
                    )
                }
            } else {
                users.add(ReconnectingUserModel(user, ""))
            }
        }
        adapter = AudioCallAdapter(context, users)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter
    }

    private fun setVisibilityAlsoOnCallTextView() {
        if (opponents.size < 2) {
            alsoOnCallText.visibility = View.INVISIBLE
        }
    }

    private fun getOtherOpponentNames(): String {
        val otherOpponents = ArrayList<QBUser>()
        otherOpponents.addAll(opponents)
        otherOpponents.removeAt(0)
        return makeStringFromUsersFullNames(otherOpponents)
    }

    private fun makeStringFromUsersFullNames(allUsers: ArrayList<QBUser>): String {
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

    override fun onStop() {
        super.onStop()
        conversationFragmentCallback?.removeOnChangeAudioDeviceListener(this)
    }

    override fun initButtonsListener() {
        super.initButtonsListener()
        audioSwitchToggleButton.setOnCheckedChangeListener { buttonView, isChecked ->
            SharedPrefsHelper.save(SPEAKER_ENABLED, isChecked)
            conversationFragmentCallback?.onSwitchAudio()
        }
    }

    override fun actionButtonsEnabled(inability: Boolean) {
        super.actionButtonsEnabled(inability)
        audioSwitchToggleButton.isActivated = inability
    }

    override fun getFragmentLayout(): Int {
        return R.layout.fragment_audio_conversation
    }

    override fun audioDeviceChanged(newAudioDevice: QBAudioManager.AudioDevice) {
        audioSwitchToggleButton.isChecked = newAudioDevice != QBAudioManager.AudioDevice.SPEAKER_PHONE
    }

    private inner class UpdateOpponentsListenerImpl(val tag: String?) : CallActivity.UpdateOpponentsListener {
        override fun updatedOpponents(updatedOpponents: ArrayList<QBUser>) {
            val name = opponents[0].fullName ?: opponents[0].login
            firstOpponentNameTextView.text = name
            otherOpponentsTextView.text = getOtherOpponentNames()
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

    private inner class CallTimeUpdateListenerImpl(val tag: String?) : CallActivity.CallTimeUpdateListener {
        override fun updatedCallTime(time: String) {
            timerCallText.text = time
        }

        override fun equals(other: Any?): Boolean {
            if (other is CallTimeUpdateListenerImpl) {
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

    override fun onUserNotAnswer(p0: QBRTCSession?, p1: Int?) {
        // empty
    }

    override fun onCallRejectByUser(p0: QBRTCSession?, p1: Int?, p2: MutableMap<String, String>?) {
        // empty
    }

    override fun onCallAcceptByUser(p0: QBRTCSession?, p1: Int?, p2: MutableMap<String, String>?) {
        // empty
    }

    override fun onReceiveHangUpFromUser(p0: QBRTCSession?, p1: Int?, p2: MutableMap<String, String>?) {
        // empty
    }

    override fun onChangeReconnectionState(
        qbRtcSession: QBRTCSession?,
        userId: Int,
        qbrtcReconnectionState: QBRTCReconnectionState,
    ) {
        val user = adapter?.getItemByUserId(userId)
        if (user != null) {
            when (qbrtcReconnectionState) {
                QB_RTC_RECONNECTION_STATE_RECONNECTING -> user.setReconnectingState("Reconnecting")
                QB_RTC_RECONNECTION_STATE_RECONNECTED -> user.setReconnectingState("Reconnected")
                QB_RTC_RECONNECTION_STATE_FAILED -> user.setReconnectingState("Reconnection failed")
            }
            adapter?.notifyDataSetChanged()
        }
    }

    override fun onSessionClosed(p0: QBRTCSession?) {
        // empty
    }
}