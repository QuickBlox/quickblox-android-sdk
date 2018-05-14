package com.quickblox.sample.videochatkotlin.fragments

import android.app.Activity
import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.os.Handler
import android.support.annotation.DimenRes
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.util.SparseArray
import android.view.*
import androidx.core.util.forEach
import androidx.core.util.isEmpty
import androidx.core.util.putAll
import com.quickblox.sample.core.utils.Toaster
import com.quickblox.sample.videochatkotlin.R
import com.quickblox.sample.videochatkotlin.adapters.OpponentsCallAdapter
import com.quickblox.sample.videochatkotlin.utils.ChatHelper
import com.quickblox.sample.videochatkotlin.utils.EXTRA_IS_INCOMING_CALL
import com.quickblox.sample.videochatkotlin.utils.EXTRA_QB_USERS_LIST
import com.quickblox.users.model.QBUser
import com.quickblox.videochat.webrtc.AppRTCAudioManager
import com.quickblox.videochat.webrtc.BaseSession
import com.quickblox.videochat.webrtc.QBRTCSession
import com.quickblox.videochat.webrtc.QBRTCTypes
import com.quickblox.videochat.webrtc.callbacks.QBRTCClientVideoTracksCallbacks
import com.quickblox.videochat.webrtc.callbacks.QBRTCSessionStateCallback
import com.quickblox.videochat.webrtc.view.QBRTCSurfaceView
import com.quickblox.videochat.webrtc.view.QBRTCVideoTrack
import kotlinx.android.synthetic.main.fragment_conversation_call.*
import kotlinx.android.synthetic.main.list_item_opponent_from_call.*
import kotlinx.android.synthetic.main.view_action_buttons_conversation_fragment.*
import org.webrtc.CameraVideoCapturer
import org.webrtc.RendererCommon
import org.webrtc.SurfaceViewRenderer
import org.webrtc.VideoRenderer
import java.util.*

/**
 * Created by Roman on 15.04.2018.
 */
class VideoConversationFragment : BaseToolBarFragment(), QBRTCSessionStateCallback<QBRTCSession>, QBRTCClientVideoTracksCallbacks<QBRTCSession> {

    private val TAG = VideoConversationFragment::class.java.simpleName
    private val TRACK_INITIALIZE_DELAY = 500L
    private val spanCount = 2

    private var isIncomingCall: Boolean = false
    lateinit var layoutManager: GridLayoutManager

    private var cameraState = CameraState.DISABLED_FROM_USER

    private var isCurrentCameraFront: Boolean = true
    private var currentSession: QBRTCSession? = null
    private lateinit var eventListener: CallFragmentCallbackListener
    lateinit var opponentsAdapter: OpponentsCallAdapter
    private lateinit var opponents: ArrayList<QBUser>
    private lateinit var opponentViewHolders: SparseArray<OpponentsCallAdapter.ViewHolder>
    private val videoTracks: SparseArray<QBRTCVideoTrack> = SparseArray()
    private var currentUserId: Int = 0
    private var isRemoteShown = false

    override val fragmentLayout: Int
        get() = R.layout.fragment_conversation_call

    private enum class CameraState {
        NONE,
        DISABLED_FROM_USER,
        ENABLED_FROM_USER
    }

    interface CallFragmentCallbackListener {
        fun onHangUpCall()
        fun onSwitchAudio()
        fun onStartScreenSharing()
        fun onSwitchCamera(cameraSwitchHandler: CameraVideoCapturer.CameraSwitchHandler)
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
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate")
        mainHandler = Handler()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        initArguments()
        restoreSession()
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initFields()
    }

    override fun onStart() {
        super.onStart()
        if (currentSession!!.state != BaseSession.QBRTCSessionState.QB_RTC_SESSION_CONNECTED) {
            if (isIncomingCall) {
                currentSession!!.acceptCall(null)
            } else {
                currentSession!!.startCall(null)
            }
        }
        initVideoTrackSListener()
        initSessionListeners()
    }

    private fun restoreSession() {
        Log.d(TAG, "restoreSession ")
        if (currentSession?.state != BaseSession.QBRTCSessionState.QB_RTC_SESSION_CONNECTED) {
            return
        }
        if (!videoTracks.isEmpty()) {
            val videoTracks = SparseArray<QBRTCVideoTrack>()
            videoTracks.putAll(this.videoTracks)
            videoTracks.forEach { userId, videoTrack ->
                if (currentSession!!.getPeerConnection(userId) != null && currentSession!!.getPeerConnection(userId).state != QBRTCTypes.QBRTCConnectionState.QB_RTC_CONNECTION_CLOSED) {
                    mainHandler.post({
                        onConnectedToUser(currentSession!!, userId)
                        onRemoteVideoTrackReceive(currentSession!!, videoTrack, userId)
                    })
                } else {
                    this.videoTracks.remove(userId)
                }
            }
        }
    }

    private fun initArguments() {
        arguments!!.let {
            isIncomingCall = it.getBoolean(EXTRA_IS_INCOMING_CALL)
            val obj = it.get(EXTRA_QB_USERS_LIST)
            if (obj is ArrayList<*>) {
                opponents = obj.filterIsInstance<QBUser>() as ArrayList<QBUser>
            }
        }
        currentUserId = ChatHelper.instance.currentUser.id
        isCurrentCameraFront = true
    }

    private fun initFields() {
        button_hangup_call.setOnClickListener({ hangUp() })
        toggle_switch_camera.setOnCheckedChangeListener { _, _ ->
            switchCamera()
        }
        toggle_switch_camera.visibility = View.VISIBLE
        toggle_camera.setOnCheckedChangeListener { _, isChecked ->
            if (cameraState != CameraState.DISABLED_FROM_USER) {
                toggleCamera(isChecked)
            }
        }
        toggle_camera.visibility = View.VISIBLE
        toggle_speaker.setOnClickListener({ eventListener.onSwitchAudio() })
        toggle_speaker.visibility = View.VISIBLE

        toggle_mute_mic.setOnCheckedChangeListener { _, isChecked -> toggleMic(isChecked) }
        toggle_mute_mic.visibility = View.VISIBLE

        opponentViewHolders = SparseArray(opponents.size)
        initRecyclerView()
    }

    private fun initRecyclerView() {
        recycler_view_opponents.setHasFixedSize(false)
        recycler_view_opponents.addItemDecoration(DividerItemDecoration(context!!, R.dimen.grid_item_divider))
        layoutManager = GridLayoutManager(activity, spanCount)
        layoutManager.reverseLayout = false
        val spanSizeLookup = SpanSizeLookupImpl()
        spanSizeLookup.isSpanIndexCacheEnabled = false
        layoutManager.spanSizeLookup = spanSizeLookup
        recycler_view_opponents.layoutManager = layoutManager

        recycler_view_opponents.itemAnimator = null
        initAdapter()
        recycler_view_opponents.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                var height = recycler_view_opponents.height
                if (height != 0) {
                    if (isRemoteShown) {
                        height /= 2
                    }
                    updateAllCellHeight(height)
                    recycler_view_opponents.viewTreeObserver.removeOnGlobalLayoutListener(this)
                }
            }
        })

    }

    private fun initAdapter() {
        val cellSizeWidth = 0
        val cellSizeHeight = screenHeight()

        val qbUsers = ArrayList<QBUser>()
        opponentsAdapter = OpponentsCallAdapter(context!!, qbUsers, cellSizeWidth, cellSizeHeight)
        recycler_view_opponents.adapter = opponentsAdapter
    }

    private fun hangUp() {
        eventListener.onHangUpCall()
    }

    override fun onLocalVideoTrackReceive(session: QBRTCSession, videoTrack: QBRTCVideoTrack) {
        Log.d(TAG, "onLocalVideoTrackReceive")
        cameraState = CameraState.NONE
        setUserToAdapter(currentUserId)
        mainHandler.postDelayed(Runnable { setViewMultiCall(currentUserId, videoTrack, false) }, TRACK_INITIALIZE_DELAY)
    }

    override fun onRemoteVideoTrackReceive(session: QBRTCSession, videoTrack: QBRTCVideoTrack, userId: Int) {
        Log.d(TAG, "onRemoteVideoTrackReceive")
        updateCellSizeIfNeed()
        setUserToAdapter(userId)
        mainHandler.postDelayed(Runnable { setViewMultiCall(userId, videoTrack, true) }, TRACK_INITIALIZE_DELAY)
    }

    private fun updateCellSizeIfNeed(height: Int = recycler_view_opponents.height / 2) {
        if (!isRemoteShown) {
            isRemoteShown = true

            initCurrentUserCellHeight(height)
            opponentsAdapter.itemHeight = height
        }
    }

    private fun updateAllCellHeight(height: Int) {
        for (user in opponentsAdapter.opponents) {
            val holder = getViewHolderForOpponent(user.id)
            holder?.let { opponentsAdapter.initCellHeight(it, height) }
        }
        opponentsAdapter.itemHeight = height
    }

    private fun initCurrentUserCellHeight(height: Int) {
        val holder = recycler_view_opponents.findViewHolderForAdapterPosition(0)
        if (holder is OpponentsCallAdapter.ViewHolder) {
            opponentsAdapter.initCellHeight(holder, height)
        }
    }

    protected fun setUserToAdapter(userID: Int) {
        val qbUser = getUserById(userID)
        opponentsAdapter.add(qbUser!!)
        recycler_view_opponents.requestLayout()
    }

    private fun getUserById(userID: Int): QBUser? {
        for (qbUser in opponents) {
            if (qbUser.id == userID) {
                return qbUser
            }
        }
        return null
    }

    private fun setViewMultiCall(userId: Int, videoTrack: QBRTCVideoTrack, remoteRenderer: Boolean) {
        Log.d(TAG, "setViewMultiCall userId= $userId")

        val itemHolder = getViewHolderForOpponent(userId)
        if (itemHolder != null) {
            val videoView = itemHolder.opponent_view
            Log.d(TAG, "setViewMultiCall fillVideoView")
            Log.d(TAG, "setViewMultiCall videoView height= " + videoView.height)
            fillVideoView(userId, videoView, videoTrack, remoteRenderer)
        }
    }

    private fun getViewHolderForOpponent(userID: Int): OpponentsCallAdapter.ViewHolder? {
        var holder: OpponentsCallAdapter.ViewHolder? = opponentViewHolders.get(userID)
        if (holder == null) {
            Log.d(TAG, "holder not found in cache")
            holder = findHolder(userID)
            if (holder != null) {
                opponentViewHolders.append(userID, holder)
            }
        }
        return holder
    }

    private fun findHolder(userID: Int): OpponentsCallAdapter.ViewHolder? {
        Log.d(TAG, "findHolder for userID $userID")
        val childCount = recycler_view_opponents.childCount
        Log.d(TAG, "childCount for $childCount")
        for (i in 0 until childCount) {
            Log.d(TAG, "findHolder childCount $childCount , i= $i")
            val childView = recycler_view_opponents[i]
            Log.d(TAG, "childView= $childView")
            val childViewHolder = recycler_view_opponents.getChildViewHolder(childView) as OpponentsCallAdapter.ViewHolder
            Log.d(TAG, "childViewHolder= $childViewHolder")
            if (userID == childViewHolder.userId) {
                return childViewHolder
            }
        }
        return null
    }

    operator fun ViewGroup.get(pos: Int): View = getChildAt(pos)

    private fun fillVideoView(userId: Int, videoView: QBRTCSurfaceView, videoTrack: QBRTCVideoTrack,
                              remoteRenderer: Boolean) {
        videoTrack.removeRenderer(videoTrack.renderer)
        videoTrack.addRenderer(VideoRenderer(videoView))
        videoTracks.put(userId, videoTrack)
        if (!remoteRenderer) {
            updateVideoView(videoView, isCurrentCameraFront)
        }
        Log.d(TAG, (if (remoteRenderer) "remote" else "local") + " Track is rendering")
    }

    private fun updateVideoView(surfaceViewRenderer: SurfaceViewRenderer, mirror: Boolean) {
        updateVideoView(surfaceViewRenderer, mirror, RendererCommon.ScalingType.SCALE_ASPECT_FILL)
    }

    protected fun updateVideoView(surfaceViewRenderer: SurfaceViewRenderer, mirror: Boolean, scalingType: RendererCommon.ScalingType) {
        Log.i(TAG, "updateVideoView mirror:$mirror, scalingType = $scalingType")
        surfaceViewRenderer.setScalingType(scalingType)
        surfaceViewRenderer.setMirror(mirror)
        surfaceViewRenderer.requestLayout()
    }

    private fun initSessionListeners() {
        currentSession!!.addSessionCallbacksListener(this)
    }

    private fun removeSessionListeners() {
        currentSession!!.removeSessionCallbacksListener(this)
    }

    private fun initVideoTrackSListener() {
        currentSession!!.addVideoTrackCallbacksListener(this)

    }

    private fun removeVideoTrackSListener() {
        currentSession!!.removeVideoTrackCallbacksListener(this)
    }

    protected fun releaseOpponentsViews() {
        opponentViewHolders.forEach { _, itemView ->
            itemView.opponent_view.release()
        }
    }

    private fun releaseViewHolders() {
        opponentViewHolders.clear()
    }

    fun initSession(session: QBRTCSession?) {
        currentSession = session
    }

    private fun setStatusForOpponent(userId: Int, status: String) {
        val holder = getViewHolderForOpponent(userId)
        holder?.connection_status?.text = status
    }

    private fun updateNameForOpponent(userId: Int, userName: String) {
        val holder = getViewHolderForOpponent(userId)
        holder?.opponent_name?.text = userName
    }

    private fun screenHeight(): Int {
        val displayMetrics = resources.displayMetrics

        val screenHeightPx = displayMetrics.heightPixels
        Log.d(TAG, "screenWidthPx $screenHeightPx")
        return screenHeightPx
    }

    private fun screenWidth(): Int {
        val displayMetrics = resources.displayMetrics

        val screenWidthPx = displayMetrics.widthPixels
        Log.d(TAG, "screenWidthPx $screenWidthPx")
        return screenWidthPx
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "onDestroyView")
        removeSessionListeners()
        removeVideoTrackSListener()
        releaseOpponentsViews()
        releaseViewHolders()
    }


    ////////////////////////////  QBRTCSessionStateCallbacks ///////////////////
    override fun onDisconnectedFromUser(session: QBRTCSession, userId: Int) {
        setStatusForOpponent(userId, getString(R.string.text_status_disconnected))
    }

    override fun onConnectedToUser(session: QBRTCSession, userId: Int) {
        Log.d(TAG, "onConnectedToUser userId= $userId")
        setStatusForOpponent(userId, getString(R.string.text_status_connected))
    }

    override fun onConnectionClosedForUser(session: QBRTCSession, userId: Int) {
        Log.d(TAG, "onConnectionClosedForUser cleanUpAdapter userId= " + userId)
        setStatusForOpponent(userId, getString(R.string.text_status_closed))
        videoTracks.remove(userId)
        cleanAdapter(userId)
    }

    override fun onStateChanged(session: QBRTCSession, state: BaseSession.QBRTCSessionState) {
    }

    private fun cleanAdapter(userId: Int) {
        val itemHolder = getViewHolderForOpponent(userId)
        if (itemHolder != null) {
            Log.d(TAG, "onConnectionClosedForUser  opponentsAdapter.removeItem")
            opponentsAdapter.removeItem(itemHolder.adapterPosition)
            opponentViewHolders.remove(userId)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.conversation_fragment, menu)
        super.onCreateOptionsMenu(menu, inflater)

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.screen_share -> {
                startScreenSharing()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun startScreenSharing() {
        eventListener.onStartScreenSharing()
    }

    private fun switchCamera() {
        if (cameraState == CameraState.DISABLED_FROM_USER) {
            return
        }
        toggle_camera.isEnabled = false
        eventListener.onSwitchCamera(object : CameraVideoCapturer.CameraSwitchHandler {
            override fun onCameraSwitchDone(b: Boolean) {
                Log.d(TAG, "camera switched, bool = $b")
                isCurrentCameraFront = b
                toggleCameraInternal()
            }

            override fun onCameraSwitchError(s: String) {
                Log.d(TAG, "camera switch error $s")
                Toaster.shortToast(getString(R.string.camera_swicth_failed) + s)
                toggle_camera.isEnabled = true
            }
        })
    }

    private fun toggleMic(isAudioEnabled: Boolean) {
        currentSession!!.mediaStreamManager.localAudioTrack.setEnabled(isAudioEnabled)
    }

    private fun toggleCameraInternal() {
        Log.d(TAG, "Camera was switched!")
        val localView = getViewHolderForOpponent(currentUserId)!!.opponent_view
        updateVideoView(localView, isCurrentCameraFront)
        toggleCamera(true)
    }

    private fun toggleCamera(isNeedEnableCam: Boolean) {
        currentSession?.mediaStreamManager?.localVideoTrack?.setEnabled(isNeedEnableCam)
        if (!toggle_camera.isEnabled) {
            toggle_camera.isEnabled = true
        }
    }

    fun audioDeviceChanged(newAudioDevice: AppRTCAudioManager.AudioDevice) {
        toggle_speaker.isChecked = newAudioDevice != AppRTCAudioManager.AudioDevice.SPEAKER_PHONE
    }

    companion object {

        fun newInstance(incoming: Boolean, opponents: ArrayList<QBUser>) =
                VideoConversationFragment().apply {
                    arguments = Bundle().apply {
                        putBoolean(EXTRA_IS_INCOMING_CALL, incoming)
                        putSerializable(EXTRA_QB_USERS_LIST, opponents)
                    }
                }
    }

    private inner class SpanSizeLookupImpl : GridLayoutManager.SpanSizeLookup() {

        override fun getSpanSize(position: Int): Int {
            val itemCount = opponentsAdapter.itemCount
            if (itemCount == 4) {
                return 1
            }
            if (itemCount == 3) {
                if (position % 3 > 0) {
                    return 1
                }
            }
            return 2
        }
    }

    private inner class DividerItemDecoration(context: Context, @DimenRes dimensionDivider: Int) : RecyclerView.ItemDecoration() {

        private val space: Int = context.resources.getDimensionPixelSize(dimensionDivider)

        override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State?) {
            outRect.set(space, 0, space, space)
        }
    }
}