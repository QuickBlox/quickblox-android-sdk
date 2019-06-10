package com.quickblox.sample.videochat.conference.kotlin.fragments

import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.widget.TextView
import android.widget.ToggleButton
import com.quickblox.conference.ConferenceSession
import com.quickblox.sample.videochat.conference.kotlin.R
import com.quickblox.sample.videochat.conference.kotlin.activities.CallActivity
import com.quickblox.sample.videochat.conference.kotlin.adapter.OpponentsFromCallAdapter
import com.quickblox.videochat.webrtc.QBRTCAudioTrack
import com.quickblox.videochat.webrtc.callbacks.QBRTCClientAudioTracksCallback
import java.io.Serializable


class AudioConversationFragment : BaseConversationFragment(), Serializable,
        OpponentsFromCallAdapter.OnAdapterEventListener,
        QBRTCClientAudioTracksCallback<ConferenceSession>, CallActivity.OnChangeDynamicToggle {
    private val TAG = AudioConversationFragment::class.simpleName

    private lateinit var localName: TextView
    private lateinit var audioSwitchToggleButton: ToggleButton

    private var headsetPlugged: Boolean = false

    override fun onStart() {
        super.onStart()
        conversationFragmentCallbackListener?.addOnChangeDynamicToggle(this)
    }

    override fun initViews(view: View) {
        localName = view.findViewById<View>(R.id.localName) as TextView
        localName.visibility = View.VISIBLE
        audioSwitchToggleButton = view.findViewById<View>(R.id.toggle_speaker) as ToggleButton
        audioSwitchToggleButton.visibility = View.VISIBLE
        super.initViews(view)
    }

    override fun onLocalAudioTrackReceive(session: ConferenceSession, audioTrack: QBRTCAudioTrack) {
        Log.d(TAG, "onLocalAudioTrackReceive() run")
        setStatusForCurrentUser(getString(R.string.text_status_connected))
        actionButtonsEnabled(true)
    }

    override fun onRemoteAudioTrackReceive(session: ConferenceSession, audioTrack: QBRTCAudioTrack, userID: Int?) {
        Log.d(TAG, "onRemoteAudioTrackReceive() run")
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        val cameraSwitchItem = menu?.findItem(R.id.camera_switch)
        cameraSwitchItem?.isVisible = false
    }

    override fun initButtonsListener() {
        super.initButtonsListener()
        audioSwitchToggleButton.setOnClickListener { conversationFragmentCallbackListener?.onSwitchAudio() }
    }

    override fun actionButtonsEnabled(inability: Boolean) {
        super.actionButtonsEnabled(inability)
        if (!headsetPlugged) {
            audioSwitchToggleButton.isEnabled = inability
        }
        audioSwitchToggleButton.isActivated = inability
    }

    override fun enableDynamicToggle(plugged: Boolean, wasEarpiece: Boolean) {
        headsetPlugged = plugged
        audioSwitchToggleButton.isEnabled = !plugged
        if (plugged) {
            audioSwitchToggleButton.isChecked = true
        } else audioSwitchToggleButton.isChecked = wasEarpiece
    }

    override fun initTrackListeners() {
        super.initTrackListeners()
        initAudioTracksListener()
    }

    override fun removeTrackListeners() {
        removeAudioTracksListener()
    }

    private fun initAudioTracksListener() {
        if (currentSession != null) {
            currentSession?.addAudioTrackCallbacksListener(this)
        }
    }

    private fun removeAudioTracksListener() {
        if (currentSession != null) {
            currentSession?.removeAudioTrackCallbacksListener(this)
        }
    }
}