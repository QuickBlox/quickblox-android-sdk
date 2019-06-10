package com.quickblox.sample.videochat.conference.kotlin.fragments

import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ToggleButton
import com.quickblox.conference.ConferenceSession
import com.quickblox.conference.view.QBConferenceSurfaceView
import com.quickblox.sample.videochat.conference.kotlin.R
import com.quickblox.sample.videochat.conference.kotlin.adapter.OpponentsFromCallAdapter
import com.quickblox.sample.videochat.conference.kotlin.utils.shortToast
import com.quickblox.videochat.webrtc.callbacks.QBRTCSessionStateCallback
import com.quickblox.videochat.webrtc.view.QBRTCVideoTrack
import org.webrtc.CameraVideoCapturer
import org.webrtc.SurfaceViewRenderer
import java.io.Serializable


open class VideoConversationFragment : BaseConversationFragment(), Serializable,
        QBRTCSessionStateCallback<ConferenceSession>, OpponentsFromCallAdapter.OnAdapterEventListener {

    private val TAG = VideoConversationFragment::class.java.simpleName

    private enum class CameraState {
        NONE,
        DISABLED_FROM_USER,
        ENABLED_FROM_USER
    }

    private lateinit var cameraToggle: ToggleButton

    private var cameraState = CameraState.DISABLED_FROM_USER
    private var localVideoTrack: QBRTCVideoTrack? = null

    protected var isCurrentCameraFront: Boolean = false

    override fun initViews(view: View) {
        isCurrentCameraFront = true
        cameraToggle = view.findViewById<View>(R.id.toggle_camera) as ToggleButton
        cameraToggle.visibility = View.VISIBLE
        super.initViews(view)
    }

    override fun actionButtonsEnabled(inability: Boolean) {
        super.actionButtonsEnabled(inability)
        cameraToggle.isEnabled = inability
        // inactivate toggle buttons
        cameraToggle.isActivated = inability
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume")
        // If user changed camera state few times and last state was CameraState.ENABLED_FROM_USER
        // than we turn on cam, else we nothing change
        if (cameraState != VideoConversationFragment.CameraState.DISABLED_FROM_USER) {
            cameraToggle.isChecked = true
            toggleCamera(true)
        }
    }

    override fun onPause() {
        // If camera state is CameraState.ENABLED_FROM_USER or CameraState.NONE
        // than we turn off cam
        if (cameraState != CameraState.DISABLED_FROM_USER) {
            toggleCamera(false)
        }
        super.onPause()
    }

    override fun setActionButtonsInvisible() {
        super.setActionButtonsInvisible()
        cameraToggle.visibility = View.INVISIBLE
    }

    override fun initButtonsListener() {
        super.initButtonsListener()

        cameraToggle.setOnCheckedChangeListener { buttonView, isChecked ->
            if (cameraState != CameraState.DISABLED_FROM_USER) {
                toggleCamera(isChecked)
            }
        }
    }

    private fun switchCamera(item: MenuItem) {
        if (cameraState == CameraState.DISABLED_FROM_USER) {
            return
        }
        cameraToggle.isEnabled = false
        conversationFragmentCallbackListener?.onSwitchCamera(object : CameraVideoCapturer.CameraSwitchHandler {
            override fun onCameraSwitchDone(frontCamera: Boolean) {
                Log.d(TAG, "camera switched, bool = $frontCamera")
                isCurrentCameraFront = frontCamera
                updateSwitchCameraIcon(item)
                toggleCameraInternal()
            }

            override fun onCameraSwitchError(message: String) {
                Log.d(TAG, "camera switch error $message")
                shortToast(getString(R.string.camera_swicth_failed) + message)
                cameraToggle.isEnabled = true
            }
        })
    }

    private fun updateSwitchCameraIcon(item: MenuItem) {
        if (isCurrentCameraFront) {
            Log.d(TAG, "CameraFront now!")
            item.setIcon(R.drawable.ic_camera_front)
        } else {
            Log.d(TAG, "CameraRear now!")
            item.setIcon(R.drawable.ic_camera_rear)
        }
    }

    private fun toggleCameraInternal() {
        Log.d(TAG, "Camera was switched!")
        updateVideoView(localVideoView as SurfaceViewRenderer, isCurrentCameraFront)
        toggleCamera(true)
    }

    private fun toggleCamera(isNeedEnableCam: Boolean) {
        if (currentSession != null && currentSession?.mediaStreamManager != null) {
            conversationFragmentCallbackListener?.onSetVideoEnabled(isNeedEnableCam)
        }
        if (!cameraToggle.isEnabled) {
            cameraToggle.isEnabled = true
        }
    }

    override fun fillVideoView(videoView: QBConferenceSurfaceView, videoTrack: QBRTCVideoTrack,
                               remoteRenderer: Boolean) {
        super.fillVideoView(videoView, videoTrack, remoteRenderer)
        if (!remoteRenderer) {
            updateVideoView(videoView, isCurrentCameraFront)
        }
    }

    ////////////////////////////  callbacks from QBRTCClientVideoTracksCallbacks ///////////////////

    override fun onLocalVideoTrackReceive(p0: ConferenceSession?, videoTrack: QBRTCVideoTrack?) {
        Log.d(TAG, "onLocalVideoTrackReceive() run")
        localVideoTrack = videoTrack
        cameraState = CameraState.NONE
        actionButtonsEnabled(true)

        if (localVideoView != null) {
            fillVideoView(localVideoView as QBConferenceSurfaceView, localVideoTrack as QBRTCVideoTrack, false)
        }
    }

    /////////////////////////////////////////    end    ////////////////////////////////////////////

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.camera_switch -> {
                Log.d(TAG, "camera_switch")
                switchCamera(item)
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }
}