package com.quickblox.sample.videochatkotlin.fragments

import android.app.Activity
import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.os.Handler
import android.support.annotation.DimenRes
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.util.SparseArray
import android.view.*
import android.widget.ImageButton
import android.widget.ToggleButton
import androidx.core.util.forEach
import com.quickblox.chat.QBChatService
import com.quickblox.sample.core.utils.Toaster
import com.quickblox.sample.videochatkotlin.R
import com.quickblox.sample.videochatkotlin.adapters.OpponentsCallAdapter
import com.quickblox.sample.videochatkotlin.utils.EXTRA_IS_INCOMING_CALL
import com.quickblox.sample.videochatkotlin.utils.EXTRA_QB_USERS_LIST
import com.quickblox.users.model.QBUser
import com.quickblox.videochat.webrtc.AppRTCAudioManager
import com.quickblox.videochat.webrtc.BaseSession
import com.quickblox.videochat.webrtc.QBRTCSession
import com.quickblox.videochat.webrtc.callbacks.QBRTCClientVideoTracksCallbacks
import com.quickblox.videochat.webrtc.callbacks.QBRTCSessionStateCallback
import com.quickblox.videochat.webrtc.view.QBRTCSurfaceView
import com.quickblox.videochat.webrtc.view.QBRTCVideoTrack
import org.webrtc.CameraVideoCapturer
import org.webrtc.RendererCommon
import org.webrtc.SurfaceViewRenderer
import org.webrtc.VideoRenderer
import java.util.*

/**
 * Created by Roman on 15.04.2018.
 */
class VideoConversationFragment : Fragment(), QBRTCSessionStateCallback<QBRTCSession>, QBRTCClientVideoTracksCallbacks<QBRTCSession> {

    private val TAG = VideoConversationFragment::class.java.simpleName
    val spanCount = 2
    lateinit var hangUpCallButton: ImageButton
    lateinit var cameraToggle: ToggleButton
    lateinit var audioSwitchToggle: ToggleButton

    lateinit var mainHandler: Handler
    private var isIncomingCall: Boolean = false
    lateinit var layoutManager: GridLayoutManager

    private var cameraState = CameraState.DISABLED_FROM_USER

    private var isCurrentCameraFront: Boolean = true
    var currentSession: QBRTCSession? = null
    lateinit var localFullScreenVideoView: QBRTCSurfaceView
    lateinit var eventListener: CallFragmentCallbackListener
    lateinit var opponentsAdapter: OpponentsCallAdapter
    lateinit var recyclerView: RecyclerView
    lateinit var opponents: ArrayList<QBUser>
    lateinit var opponentViewHolders: SparseArray<OpponentsCallAdapter.ViewHolder>
    private var currentUserId: Int = 0
    private var isRemoteShown = false

    private enum class CameraState {
        NONE,
        DISABLED_FROM_USER,
        ENABLED_FROM_USER
    }

    interface CallFragmentCallbackListener {
        fun onHangUpCall()
        fun onSetAudioEnabled(isAudioEnabled: Boolean)
        fun onSetVideoEnabled(isNeedEnableCam: Boolean)
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
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_conversation_call, container, false)
        initArguments()
        initFields(view)
        return view
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

    private fun initArguments() {
        if (arguments != null) {
            Log.d(TAG, "arguments != null")
            isIncomingCall = arguments!!.getBoolean(EXTRA_IS_INCOMING_CALL)
            val obj = arguments!!.get(EXTRA_QB_USERS_LIST)
            if (obj is ArrayList<*>) {
                opponents = obj.filterIsInstance<QBUser>() as ArrayList<QBUser>
            }
        }
        currentUserId = QBChatService.getInstance().user.id
    }

    private fun initFields(view: View) {
        localFullScreenVideoView = view.findViewById<View>(R.id.local_video_view) as QBRTCSurfaceView
        hangUpCallButton = view.findViewById(R.id.button_hangup_call)
        hangUpCallButton.setOnClickListener({ hangUp() })
        cameraToggle = view.findViewById(R.id.toggle_camera)
        cameraToggle.setOnCheckedChangeListener { buttonView, isChecked ->
            if (cameraState != CameraState.DISABLED_FROM_USER) {
                toggleCamera(isChecked)
            }
        }
        cameraToggle.visibility = View.VISIBLE
        audioSwitchToggle = view.findViewById(R.id.toggle_speaker)
        audioSwitchToggle.setOnClickListener({ eventListener.onSwitchAudio() })
        audioSwitchToggle.setVisibility(View.VISIBLE)

        recyclerView = view.findViewById(R.id.grid_opponents)

        opponentViewHolders = SparseArray(opponents.size)
        initRecyclerView()
    }

    private fun initRecyclerView() {
        recyclerView.setHasFixedSize(false)
        recyclerView.addItemDecoration(DividerItemDecoration(context!!, R.dimen.grid_item_divider))
        val columnsCount = defineColumnsCount()
        layoutManager = GridLayoutManager(activity, spanCount)
        layoutManager.reverseLayout = false
        val spanSizeLookup = SpanSizeLookupImpl()
        spanSizeLookup.setSpanIndexCacheEnabled(false)
        layoutManager.setSpanSizeLookup(spanSizeLookup)
        recyclerView.setLayoutManager(layoutManager)

        recyclerView.itemAnimator = null
        initAdapter()
        recyclerView.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                var height = recyclerView.height
                if (height != 0) {
                    if (isRemoteShown) {
                        height /= 2
                    }
                    updateAllCellHeight(height)
                    recyclerView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                }
            }
        })

    }

    private fun initAdapter() {
        val cellSizeWidth = 0
        val cellSizeHeight = screenHeight()

        val qbUsers = ArrayList<QBUser>()
        opponentsAdapter = OpponentsCallAdapter(context!!, qbUsers, cellSizeWidth, cellSizeHeight)
        recyclerView.adapter = opponentsAdapter
    }

    private fun defineColumnsCount(): Int {
        if (opponents.size > 2) {
            return 2
        }
        return 1
    }

    fun hangUp() {
        eventListener.onHangUpCall()
    }

    override fun onLocalVideoTrackReceive(session: QBRTCSession, videoTrack: QBRTCVideoTrack) {
        Log.d(TAG, "onLocalVideoTrackReceive")
        cameraState = CameraState.NONE
        setUserToAdapter(currentUserId)
        mainHandler.postDelayed(Runnable { setViewMultiCall(currentUserId, videoTrack) }, 500)
    }

    override fun onRemoteVideoTrackReceive(session: QBRTCSession, videoTrack: QBRTCVideoTrack, userId: Int) {
        Log.d(TAG, "onRemoteVideoTrackReceive")
        updateCellSizeIfNeed()
        setUserToAdapter(userId)
        mainHandler.postDelayed(Runnable { setViewMultiCall(userId, videoTrack) }, 500)
    }

    fun updateCellSizeIfNeed(height: Int = recyclerView.height / 2) {
        if (!isRemoteShown) {
            isRemoteShown = true

            initCurrentUserCellHeight(height)
            opponentsAdapter.itemHeight = height
        }
    }

    fun updateAllCellHeight(height: Int) {
        for (user in opponentsAdapter.opponents) {
            val holder = getViewHolderForOpponent(user.id)
            holder?.let { opponentsAdapter.initCellHeight(it, height) }
        }
        opponentsAdapter.itemHeight = height
    }

    private fun initCurrentUserCellHeight(height: Int) {
        val holder = recyclerView.findViewHolderForAdapterPosition(0)
        if (holder is OpponentsCallAdapter.ViewHolder) {
            opponentsAdapter.initCellHeight(holder, height)
        }
    }

    protected fun setUserToAdapter(userID: Int) {
        val qbUser = getUserById(userID)
        opponentsAdapter.add(qbUser!!)
        recyclerView.requestLayout()
    }

    private fun getUserById(userID: Int): QBUser? {
        for (qbUser in opponents) {
            if (qbUser.getId() == userID) {
                return qbUser
            }
        }
        return null
    }

    fun setViewMultiCall(userId: Int, videoTrack: QBRTCVideoTrack) {
        Log.d(TAG, "setViewMultiCall userId= $userId")

        val itemHolder = getViewHolderForOpponent(userId)
        if (itemHolder != null) {
            val videoView = itemHolder.opponentView
            Log.d(TAG, "setViewMultiCall fillVideoView")
            Log.d(TAG, "setViewMultiCall videoView height= " + videoView.height)
            fillVideoView(userId, videoView, videoTrack, true)
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
        val childCount = recyclerView.childCount
        Log.d(TAG, "childCount for $childCount")
        for (i in 0 until childCount) {
            Log.d(TAG, "findHolder childCount $childCount , i= $i")
            val childView = recyclerView.getChildAt(i)
            Log.d(TAG, "childView= " + childView)
            val childViewHolder = recyclerView.getChildViewHolder(childView) as OpponentsCallAdapter.ViewHolder
            Log.d(TAG, "childViewHolder= " + childViewHolder)
            if (userID == childViewHolder.userId) {
                return childViewHolder
            }
        }
        return null
    }

    private fun fillVideoView(userId: Int, videoView: QBRTCSurfaceView, videoTrack: QBRTCVideoTrack,
                              remoteRenderer: Boolean) {
        videoTrack.removeRenderer(videoTrack.renderer)
        videoTrack.addRenderer(VideoRenderer(videoView))
        if (!remoteRenderer) {
            updateVideoView(videoView, isCurrentCameraFront)
        }
        Log.d(TAG, (if (remoteRenderer) "remote" else "local") + " Track is rendering")
    }

    private fun fillVideoView(videoView: QBRTCSurfaceView, videoTrack: QBRTCVideoTrack, remoteRenderer: Boolean) {
        fillVideoView(0, videoView, videoTrack, remoteRenderer)
    }

    protected fun updateVideoView(surfaceViewRenderer: SurfaceViewRenderer, mirror: Boolean) {
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
            itemView.opponentView.release()
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
        holder?.connectionStatus?.text = status
    }

    private fun updateNameForOpponent(userId: Int, userName: String) {
        val holder = getViewHolderForOpponent(userId)
        holder?.opponentsName?.text = userName
    }

    private fun screenHeight(): Int {
        val displaymetrics = resources.displayMetrics

        val screenHeightPx = displaymetrics.heightPixels
        Log.d(TAG, "screenWidthPx $screenHeightPx")
        return screenHeightPx
    }

    private fun screenWidth(): Int {
        val displaymetrics = resources.displayMetrics

        val screenWidthPx = displaymetrics.widthPixels
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
        cleanAdapter(userId)
    }

    override fun onStateChanged(p0: QBRTCSession?, p1: BaseSession.QBRTCSessionState?) {
    }

    fun cleanAdapter(userId: Int) {
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
        when (item.itemId) {
            R.id.camera_switch -> {
                switchCamera(item)
                return true
            }
            R.id.screen_share -> {
                startScreenSharing()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun startScreenSharing() {
        eventListener.onStartScreenSharing()
    }

    private fun switchCamera(item: MenuItem) {
        if (cameraState == CameraState.DISABLED_FROM_USER) {
            return
        }
        cameraToggle.setEnabled(false)
        eventListener.onSwitchCamera(object : CameraVideoCapturer.CameraSwitchHandler {
            override fun onCameraSwitchDone(b: Boolean) {
                Log.d(TAG, "camera switched, bool = " + b)
                isCurrentCameraFront = b
                updateSwitchCameraIcon(item)
                toggleCamera(true)
            }

            override fun onCameraSwitchError(s: String) {
                Log.d(TAG, "camera switch error " + s)
                Toaster.shortToast(getString(R.string.camera_swicth_failed) + s)
                cameraToggle.setEnabled(true)
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

    private fun toggleCamera(isNeedEnableCam: Boolean) {
        if (currentSession?.mediaStreamManager != null) {
            eventListener.onSetVideoEnabled(isNeedEnableCam)
        }
        if (!cameraToggle.isEnabled) {
            cameraToggle.isEnabled = true
        }
    }

    fun audioDeviceChanged(newAudioDevice: AppRTCAudioManager.AudioDevice) {
        audioSwitchToggle.isChecked = newAudioDevice != AppRTCAudioManager.AudioDevice.SPEAKER_PHONE
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

        private val space: Int

        init {
            this.space = context.resources.getDimensionPixelSize(dimensionDivider)
        }

        override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State?) {
            outRect.set(space, 0, space, space)
        }
    }
}