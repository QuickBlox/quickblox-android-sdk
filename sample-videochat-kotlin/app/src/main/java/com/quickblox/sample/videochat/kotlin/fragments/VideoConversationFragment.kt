package com.quickblox.sample.videochat.kotlin.fragments

import android.content.Context
import android.graphics.Color
import android.graphics.Rect
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.util.SparseArray
import android.view.*
import android.widget.*
import androidx.annotation.DimenRes
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.quickblox.sample.videochat.kotlin.R
import com.quickblox.sample.videochat.kotlin.activities.CallActivity
import com.quickblox.sample.videochat.kotlin.adapters.OpponentsFromCallAdapter
import com.quickblox.sample.videochat.kotlin.db.QbUsersDbManager
import com.quickblox.sample.videochat.kotlin.utils.SharedPrefsHelper
import com.quickblox.sample.videochat.kotlin.utils.shortToast
import com.quickblox.users.model.QBUser
import com.quickblox.videochat.webrtc.BaseSession
import com.quickblox.videochat.webrtc.QBRTCSession
import com.quickblox.videochat.webrtc.QBRTCTypes
import com.quickblox.videochat.webrtc.callbacks.QBRTCClientVideoTracksCallbacks
import com.quickblox.videochat.webrtc.callbacks.QBRTCSessionEventsCallback
import com.quickblox.videochat.webrtc.callbacks.QBRTCSessionStateCallback
import com.quickblox.videochat.webrtc.view.QBRTCSurfaceView
import com.quickblox.videochat.webrtc.view.QBRTCVideoTrack
import org.webrtc.CameraVideoCapturer
import org.webrtc.RendererCommon
import org.webrtc.SurfaceViewRenderer
import java.io.Serializable
import java.util.*

const val CAMERA_ENABLED = "is_camera_enabled"
const val IS_CURRENT_CAMERA_FRONT = "is_camera_front"
private const val LOCAL_TRACK_INITIALIZE_DELAY: Long = 800
private const val RECYCLE_VIEW_PADDING = 2
private const val UPDATING_USERS_DELAY: Long = 2000
private const val FULL_SCREEN_CLICK_DELAY: Long = 1000

class VideoConversationFragment : BaseConversationFragment(), Serializable,
    QBRTCClientVideoTracksCallbacks<QBRTCSession>,
    QBRTCSessionStateCallback<QBRTCSession>, QBRTCSessionEventsCallback,
    OpponentsFromCallAdapter.OnAdapterEventListener {
    private val TAG = VideoConversationFragment::class.java.simpleName

    private lateinit var cameraToggle: ToggleButton
    private var parentView: View? = null
    private lateinit var actionVideoButtonsLayout: LinearLayout
    private lateinit var tvFullNameUser: TextView
    private lateinit var recyclerView: RecyclerView
    private var localVideoView: QBRTCSurfaceView? = null
    private var containerLocalVideoView: FrameLayout? = null
    private var remoteFullScreenVideoView: QBRTCSurfaceView? = null
    private lateinit var opponentViewHolders: SparseArray<OpponentsFromCallAdapter.ViewHolder>
    private lateinit var opponentsAdapter: OpponentsFromCallAdapter
    private lateinit var allOpponents: MutableList<QBUser>
    private lateinit var localViewOnClickListener: LocalViewOnClickListener
    private var isPeerToPeerCall: Boolean = false
    private var localVideoTrack: QBRTCVideoTrack? = null
    private var optionsMenu: Menu? = null
    private var isRemoteShown: Boolean = false
    private var amountOpponents: Int = 0
    private var userIdFullScreen: Int = 0
    private var connectionEstablished: Boolean = false
    private var allCallbacksInit: Boolean = false
    private var isCurrentCameraFront: Boolean = false
    private var isLocalVideoFullScreen: Boolean = false

    override fun getFragmentLayout(): Int {
        return R.layout.fragment_video_conversation
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        parentView = super.onCreateView(inflater, container, savedInstanceState)
        return parentView
    }

    override fun onStart() {
        super.onStart()
        if (!allCallbacksInit) {
            addListeners()
            allCallbacksInit = true
        }
    }

    override fun onResume() {
        super.onResume()
        toggleCamera(cameraToggle.isChecked)
        conversationFragmentCallback?.addUpdateOpponentsListener(UpdateOpponentsListenerImpl(TAG))
        conversationFragmentCallback?.addCallTimeUpdateListener(CallTimeUpdateListenerImpl(TAG))
    }

    override fun onPause() {
        // if camera state is CameraState.ENABLED_FROM_USER or CameraState.NONE
        // than we turn off cam
        toggleCamera(false)

        if (connectionEstablished) {
            allCallbacksInit = false
        } else {
            Log.d(TAG, "We are in dialing process yet!")
        }

        releaseViewHolders()
        removeListeners()
        releaseViews()

        super.onPause()
    }

    override fun initViews(view: View?) {
        super.initViews(view)
        Log.i(TAG, "initViews")
        if (view == null) {
            return
        }
        opponentViewHolders = SparseArray(opponents.size)
        isRemoteShown = false
        isCurrentCameraFront = true
        localVideoView = view.findViewById(R.id.local_video_view)
        containerLocalVideoView = view.findViewById(R.id.container_local_video_view)
        initCorrectSizeForLocalView()
        localVideoView?.setZOrderMediaOverlay(true)

        remoteFullScreenVideoView = view.findViewById(R.id.remote_video_view)
        remoteFullScreenVideoView?.setOnClickListener(localViewOnClickListener)

        if (!isPeerToPeerCall) {
            recyclerView = view.findViewById(R.id.grid_opponents)

            recyclerView.addItemDecoration(DividerItemDecoration(requireContext(), R.dimen.grid_item_divider))
            recyclerView.setHasFixedSize(true)
            val columnsCount = defineColumnsCount()
            val layoutManager = LinearLayoutManager(activity, RecyclerView.HORIZONTAL, false)
            recyclerView.layoutManager = layoutManager

            // for correct removing item in adapter
            recyclerView.itemAnimator = null
            recyclerView.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    setGrid(columnsCount)
                    recyclerView.viewTreeObserver?.removeGlobalOnLayoutListener(this)
                }
            })
        }
        tvFullNameUser = view.findViewById(R.id.username_full_view)

        cameraToggle = view.findViewById(R.id.toggle_camera)
        cameraToggle.visibility = View.VISIBLE
        cameraToggle.isChecked = SharedPrefsHelper.get(CAMERA_ENABLED, true)
        toggleCamera(cameraToggle.isChecked)
        actionVideoButtonsLayout = view.findViewById(R.id.element_set_video_buttons)

        isCurrentCameraFront = SharedPrefsHelper.get(IS_CURRENT_CAMERA_FRONT, true)
        if (!isCurrentCameraFront) {
            switchCamera(null)
        }

        actionButtonsEnabled(false)
        restoreSession()
    }

    override fun initFields() {
        super.initFields()
        localViewOnClickListener = LocalViewOnClickListener()
        amountOpponents = opponents.size
        allOpponents = Collections.synchronizedList(ArrayList(opponents.size))
        allOpponents.addAll(opponents)

        timerCallText = requireActivity().findViewById(R.id.timer_call)

        isPeerToPeerCall = opponents.size == 1
    }

    override fun configureOutgoingScreen() {
        outgoingOpponentsRelativeLayout.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.grey_transparent_50))
        allOpponentsTextView.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        ringingTextView.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
    }

    override fun configureActionBar() {
        actionBar.setDisplayShowTitleEnabled(false)
    }

    override fun configureToolbar() {
        toolbar?.visibility = View.VISIBLE
        toolbar?.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.black_transparent_50))
        toolbar?.setTitleTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        toolbar?.setSubtitleTextColor(ContextCompat.getColor(requireContext(), R.color.white))
    }

    private fun setDuringCallActionBar() {
        actionBar.setDisplayShowTitleEnabled(false)
        val user: QBUser? = if (isPeerToPeerCall) {
            opponents[0]
        } else {
            QbUsersDbManager.getUserById(userIdFullScreen)
        }

        user?.let {
            val name = it.fullName ?: it.login
            tvFullNameUser.text = name
            actionButtonsEnabled(true)
        }
    }

    private fun addListeners() {
        conversationFragmentCallback?.addSessionStateListener(this)
        conversationFragmentCallback?.addSessionEventsListener(this)
        conversationFragmentCallback?.addVideoTrackListener(this)
    }

    private fun removeListeners() {
        conversationFragmentCallback?.removeSessionStateListener(this)
        conversationFragmentCallback?.removeSessionEventsListener(this)
        conversationFragmentCallback?.removeVideoTrackListener(this)
        conversationFragmentCallback?.removeCallTimeUpdateListener(CallTimeUpdateListenerImpl(TAG))
        conversationFragmentCallback?.removeUpdateOpponentsListener(UpdateOpponentsListenerImpl(TAG))
    }

    override fun actionButtonsEnabled(inability: Boolean) {
        super.actionButtonsEnabled(inability)
        cameraToggle.isEnabled = inability
        // inactivate toggle buttons
        cameraToggle.isActivated = inability
    }

    private fun restoreSession() {
        Log.d(TAG, "restoreSession ")
        if (conversationFragmentCallback?.isConnectedCall() == false) {
            return
        }
        startedCall()
        val videoTrackMap = conversationFragmentCallback?.getVideoTrackMap() ?: return
        if (videoTrackMap.isNotEmpty()) {
            val entryIterator = videoTrackMap.entries.iterator()
            while (entryIterator.hasNext()) {
                val entry = entryIterator.next()
                Log.d(TAG, "check ability to restoreSession for user:" + entry.key)
                val userId = entry.key
                val videoTrack = entry.value

                if (userId == currentUser.id) {
                    Log.d(TAG, "execute restoreSession for user:$userId")
                    mainHandler?.postDelayed({
                        onLocalVideoTrackReceive(null, videoTrack)
                    }, LOCAL_TRACK_INITIALIZE_DELAY)
                } else if (conversationFragmentCallback?.getPeerChannel(userId) != QBRTCTypes.QBRTCConnectionState.QB_RTC_CONNECTION_CLOSED) {
                    Log.d(TAG, "execute restoreSession for user:$userId")
                    mainHandler?.postDelayed({
                        onConnectedToUser(null, userId)
                        onRemoteVideoTrackReceive(null, videoTrack, userId)
                    }, LOCAL_TRACK_INITIALIZE_DELAY)
                } else {
                    entryIterator.remove()
                }
            }
        }
    }

    private fun initCorrectSizeForLocalView() {
        val params = localVideoView?.layoutParams
        val displaymetrics = resources.displayMetrics

        val screenWidthPx = displaymetrics.widthPixels
        Log.d(TAG, "screenWidthPx $screenWidthPx")

        val width = (screenWidthPx * 0.3).toInt()
        val height = width / 2 * 3
        params?.width = width
        params?.height = height
        localVideoView?.layoutParams = params
    }

    private fun setGrid(columnsCount: Int) {
        val gridWidth = parentView?.measuredWidth
        Log.i(TAG, "onGlobalLayout : gridWidth= $gridWidth; columnsCount= $columnsCount")
        val itemMargin = resources.getDimension(R.dimen.grid_item_divider)

        gridWidth?.let {
            val cellSizeWidth = defineSize(it, columnsCount, itemMargin)
            Log.i(TAG, "onGlobalLayout : cellSize=$cellSizeWidth")
            opponentsAdapter = OpponentsFromCallAdapter(
                requireContext(), this, opponents, cellSizeWidth,
                resources.getDimension(R.dimen.item_height).toInt()
            )
            opponentsAdapter.setAdapterListener(this)
            recyclerView.adapter = opponentsAdapter
        }
    }

    private fun defineSize(measuredWidth: Int, columnsCount: Int, padding: Float): Int {
        return measuredWidth / columnsCount - (padding * 2).toInt() - RECYCLE_VIEW_PADDING
    }

    private fun defineColumnsCount(): Int {
        return opponents.size - 1
    }

    private fun releaseViewHolders() {
        opponentViewHolders.clear()
    }

    private fun releaseViews() {
        if (conversationFragmentCallback?.getCurrentSessionState() != BaseSession.QBRTCSessionState.QB_RTC_SESSION_CLOSED) {
            for (item in (activity as CallActivity).getVideoTrackMap()) {
                val renderer = item.value.renderer
                item.value.removeRenderer(renderer)
            }
        }

        localVideoView?.release()
        remoteFullScreenVideoView?.release()

        remoteFullScreenVideoView = null
        if (!isPeerToPeerCall) {
            releaseOpponentsViews()
        }
    }

    override fun initButtonsListener() {
        super.initButtonsListener()

        cameraToggle.setOnCheckedChangeListener { buttonView, isChecked ->
            SharedPrefsHelper.save(CAMERA_ENABLED, isChecked)
            toggleCamera(isChecked)
        }
    }

    private fun switchCamera(item: MenuItem?) {
        cameraToggle.isEnabled = false
        conversationFragmentCallback?.onSwitchCamera(object : CameraVideoCapturer.CameraSwitchHandler {
            override fun onCameraSwitchDone(b: Boolean) {
                Log.d(TAG, "camera switched, bool = $b")
                isCurrentCameraFront = b
                SharedPrefsHelper.save(IS_CURRENT_CAMERA_FRONT, b)
                if (item != null) {
                    updateSwitchCameraIcon(item)
                } else {
                    optionsMenu?.findItem(R.id.camera_switch)?.setIcon(R.drawable.ic_camera_rear)
                }
                toggleCameraInternal()
            }

            override fun onCameraSwitchError(s: String) {
                Log.d(TAG, "camera switch error $s")
                shortToast(getString(R.string.camera_swicth_failed) + s)
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
        if (remoteFullScreenVideoView == null) {
            return
        }
        val surfaceViewRenderer = if (isLocalVideoFullScreen) {
            remoteFullScreenVideoView
        } else {
            localVideoView
        }
        updateVideoView(surfaceViewRenderer, isCurrentCameraFront)
        toggleCamera(true)
    }

    private fun toggleCamera(isNeedEnableCam: Boolean) {
        if (conversationFragmentCallback?.isMediaStreamManagerExist() == true) {
            conversationFragmentCallback?.onSetVideoEnabled(isNeedEnableCam)
        }
        if (connectionEstablished && !cameraToggle.isEnabled) {
            cameraToggle.isEnabled = true
        }
    }

    override fun onLocalVideoTrackReceive(qbrtcSession: QBRTCSession?, videoTrack: QBRTCVideoTrack) {
        Log.d(TAG, "onLocalVideoTrackReceive() run")
        localVideoTrack = videoTrack
        isLocalVideoFullScreen = true

        localVideoTrack?.let {
            fillVideoView(localVideoView, it, false)
        }

        isLocalVideoFullScreen = false
    }

    override fun onRemoteVideoTrackReceive(session: QBRTCSession?, videoTrack: QBRTCVideoTrack, userID: Int?) {
        Log.d(TAG, "onRemoteVideoTrackReceive for opponent= $userID")
        userID?.let {
            if (isPeerToPeerCall) {
                setDuringCallActionBar()
                remoteFullScreenVideoView?.let {
                    fillVideoView(remoteFullScreenVideoView, videoTrack, true)
                    updateVideoView(remoteFullScreenVideoView, false)
                }
            } else {
                mainHandler?.postDelayed({ setRemoteViewMultiCall(it, videoTrack) }, LOCAL_TRACK_INITIALIZE_DELAY)
            }
        }
    }

    override fun onBindLastViewHolder(holder: OpponentsFromCallAdapter.ViewHolder, position: Int) {
        Log.i(TAG, "onBindLastViewHolder position=$position")
    }

    override fun onItemClick(position: Int) {
        val userId = opponentsAdapter.getItem(position)
        Log.d(TAG, "USer onItemClick= $userId")

        val connectionState = conversationFragmentCallback?.getPeerChannel(userId)
        val videoTrackMap = conversationFragmentCallback?.getVideoTrackMap()
        val isNotExistVideoTrack = videoTrackMap != null && !videoTrackMap.containsKey(userId)
        val isConnectionStateClosed =
            connectionState?.ordinal == QBRTCTypes.QBRTCConnectionState.QB_RTC_CONNECTION_CLOSED.ordinal
        val holder = findHolder(userId)
        if (isNotExistVideoTrack || isConnectionStateClosed || holder == null) {
            return
        }

        replaceUsersInAdapter(position)
        updateViewHolders(position)
        swapUsersFullscreenToPreview(holder, userId)
    }

    private fun replaceUsersInAdapter(position: Int) {
        val opponents = allOpponents
        for (qbUser in opponents) {
            if (qbUser.id == userIdFullScreen) {
                opponentsAdapter.replaceUsers(position, qbUser)
                break
            }
        }
    }

    private fun updateViewHolders(position: Int) {
        val childView = recyclerView.getChildAt(position)
        val childViewHolder = recyclerView.getChildViewHolder(childView) as OpponentsFromCallAdapter.ViewHolder
        opponentViewHolders.put(position, childViewHolder)
    }

    private fun swapUsersFullscreenToPreview(holder: OpponentsFromCallAdapter.ViewHolder, userId: Int) {
        val videoTrack = conversationFragmentCallback?.getVideoTrackMap()?.get(userId)
        val videoTrackFullScreen = conversationFragmentCallback?.getVideoTrackMap()?.get(userIdFullScreen)

        val videoView = holder.getOpponentView()

        videoTrack?.let {
            fillVideoView(userId, remoteFullScreenVideoView, videoTrack);
            val user: QBUser? = QbUsersDbManager.getUserById(userIdFullScreen)
            val name = user?.fullName ?: user?.login
            tvFullNameUser.text = name
        }

        if (videoTrackFullScreen != null) {
            fillVideoView(0, videoView, videoTrackFullScreen)
        } else {
            holder.getOpponentView().setBackgroundColor(Color.BLACK)
            remoteFullScreenVideoView?.setBackgroundColor(Color.TRANSPARENT)
        }
    }

    private fun setRemoteViewMultiCall(userID: Int, videoTrack: QBRTCVideoTrack) {
        Log.d(TAG, "setRemoteViewMultiCall fillVideoView")
        val itemHolder = getViewHolderForOpponent(userID)
        if (itemHolder == null) {
            Log.d(TAG, "itemHolder == null - true")
            return
        }
        val remoteVideoView = itemHolder.getOpponentView()
        remoteVideoView.setZOrderMediaOverlay(true)
        updateVideoView(remoteVideoView, false)

        Log.d(TAG, "onRemoteVideoTrackReceive fillVideoView")
        if (isRemoteShown) {
            Log.d(TAG, "onRemoteVideoTrackReceive User = $userID")
            fillVideoView(remoteVideoView, videoTrack, true)
            showRecyclerView();
        } else {
            isRemoteShown = true
            itemHolder.getOpponentView().release()
            opponentsAdapter.removeItem(itemHolder.adapterPosition)
            remoteFullScreenVideoView?.let {
                fillVideoView(userID, it, videoTrack)
                updateVideoView(remoteFullScreenVideoView, false)
            }
            setDuringCallActionBar()
        }
    }

    private fun showRecyclerView() {
        val params = recyclerView.layoutParams
        params.height = resources.getDimension(R.dimen.item_height).toInt()
        recyclerView.layoutParams = params
        recyclerView.visibility = View.VISIBLE
    }

    private fun getViewHolderForOpponent(userID: Int): OpponentsFromCallAdapter.ViewHolder? {
        var holder: OpponentsFromCallAdapter.ViewHolder? = opponentViewHolders.get(userID)
        if (holder == null) {
            Log.d(TAG, "holder not found in cache")
            holder = findHolder(userID)
            if (holder != null) {
                opponentViewHolders.append(userID, holder)
            }
        }
        return holder
    }

    private fun findHolder(userID: Int?): OpponentsFromCallAdapter.ViewHolder? {
        Log.d(TAG, "findHolder for $userID")
        val childCount = recyclerView.childCount
        for (index in 0 until childCount) {
            val childView = recyclerView.getChildAt(index)
            val childViewHolder = recyclerView.getChildViewHolder(childView) as OpponentsFromCallAdapter.ViewHolder
            if (userID == childViewHolder.getUserId()) {
                return childViewHolder
            }
        }
        return null
    }

    private fun releaseOpponentsViews() {
        val layoutManager = recyclerView.layoutManager
        val childCount = layoutManager?.childCount!!
        Log.d(TAG, " releaseOpponentsViews for  $childCount views")
        for (index in 0 until childCount) {
            val childView = layoutManager.getChildAt(index)
            childView?.let {
                Log.d(TAG, " relese View for  $index, $childView")
                val childViewHolder = recyclerView.getChildViewHolder(childView) as OpponentsFromCallAdapter.ViewHolder
                childViewHolder.getOpponentView().release()
            }
        }
    }

    /**
     * @param userId set userId if it from fullscreen videoTrack
     */
    private fun fillVideoView(videoView: QBRTCSurfaceView?, videoTrack: QBRTCVideoTrack?, remoteRenderer: Boolean) {
        videoTrack?.removeRenderer(videoTrack.renderer)
        videoTrack?.addRenderer(videoView)
        if (!remoteRenderer) {
            updateVideoView(videoView, isCurrentCameraFront)
        }
        Log.d(TAG, (if (remoteRenderer) "remote" else "local") + " Track is rendering")
    }

    private fun updateVideoView(videoView: SurfaceViewRenderer?, mirror: Boolean) {
        val scalingType = RendererCommon.ScalingType.SCALE_ASPECT_FILL
        Log.i(TAG, "updateVideoView mirror:$mirror, scalingType = $scalingType")
        videoView?.setScalingType(scalingType)
        videoView?.setMirror(mirror)
        videoView?.requestLayout()
    }

    /**
     * @param userId set userId if it from fullscreen videoTrack
     */
    private fun fillVideoView(userId: Int, videoView: QBRTCSurfaceView?, videoTrack: QBRTCVideoTrack) {
        if (userId != 0) {
            userIdFullScreen = userId
        }
        fillVideoView(videoView, videoTrack, true)
    }

    private fun setStatusForOpponent(userId: Int?, status: String) {
        if (isPeerToPeerCall) {
            return
        }

        val holder = findHolder(userId) ?: return

        holder.setStatus(status)
    }

    private fun updateNameForOpponent(userId: Int, newUserName: String) {
        if (isPeerToPeerCall) {
            actionBar.subtitle = getString(R.string.opponent, newUserName)
        } else {
            val holder = findHolder(userId)
            if (holder == null) {
                Log.d("UPDATE_USERS", "holder == null")
                return
            }

            Log.d("UPDATE_USERS", "holder != null")
            holder.setUserName(newUserName)
        }
    }

    private fun hideProgressForOpponent(userId: Int) {
        if (isPeerToPeerCall) {
            return
        }
        val holder = getViewHolderForOpponent(userId) ?: return

        holder.getProgressBar().visibility = View.GONE
    }

    private fun setBackgroundOpponentView(userId: Int?) {
        if (userId != userIdFullScreen) {
            val holder = findHolder(userId) ?: return
            holder.getOpponentView().setBackgroundColor(Color.BLACK)
        } else {
            remoteFullScreenVideoView?.setBackgroundColor(Color.BLACK)
        }
    }

    override fun onStateChanged(qbrtcSession: QBRTCSession, qbrtcSessionState: BaseSession.QBRTCSessionState) {
        // empty
    }

    override fun onConnectedToUser(qbrtcSession: QBRTCSession?, userId: Int) {
        connectionEstablished = true
        setStatusForOpponent(userId, getString(R.string.text_status_connected))
        hideProgressForOpponent(userId)
    }

    override fun onConnectionClosedForUser(qbrtcSession: QBRTCSession, userId: Int?) {
        userId?.let {
            setStatusForOpponent(it, getString(R.string.text_status_closed))
            if (!isPeerToPeerCall) {
                Log.d(TAG, "onConnectionClosedForUser videoTrackMap.remove(userId)= $userId")
                setBackgroundOpponentView(it)
                hideProgressForOpponent(userId);
            }
        }
    }

    override fun onDisconnectedFromUser(qbrtcSession: QBRTCSession, integer: Int?) {
        setStatusForOpponent(integer, getString(R.string.text_status_disconnected))
    }

    override fun onUserNotAnswer(session: QBRTCSession, userId: Int) {
        hideProgressForOpponent(userId)
        setStatusForOpponent(userId, getString(R.string.text_status_no_answer))
    }

    override fun onCallRejectByUser(session: QBRTCSession, userId: Int?, userInfo: Map<String, String>?) {
        setStatusForOpponent(userId, getString(R.string.text_status_rejected))
    }

    override fun onCallAcceptByUser(session: QBRTCSession, userId: Int?, userInfo: Map<String, String>?) {
        setStatusForOpponent(userId, getString(R.string.accepted))
    }

    override fun onReceiveHangUpFromUser(session: QBRTCSession, userId: Int?, userInfo: Map<String, String>?) {
        setStatusForOpponent(userId, getString(R.string.text_status_hang_up))
        Log.d(TAG, "onReceiveHangUpFromUser userId= $userId")
        if (!isPeerToPeerCall) {
            if (userId == userIdFullScreen) {
                Log.d(TAG, "setAnotherUserToFullScreen call userId= $userId")
                setAnotherUserToFullScreen()
            }
        }
    }

    override fun onSessionClosed(session: QBRTCSession) {
        // empty
    }

    private fun setAnotherUserToFullScreen() {
        if (opponentsAdapter.opponents.isEmpty()) {
            return
        }
        for (user in opponents) {
            val videoTrack = conversationFragmentCallback?.getVideoTrack(user.id)
            videoTrack?.let { track ->
                val userFullScreen = QbUsersDbManager.getUserById(userIdFullScreen)

                val itemHolder = findHolder(user.id)

                itemHolder?.setUserId(userIdFullScreen)
                itemHolder?.setUserName(userFullScreen?.fullName.toString())
                itemHolder?.setStatus(getString(R.string.text_status_closed))
                itemHolder?.getOpponentView()?.release()
                itemHolder?.adapterPosition?.let { position ->
                    replaceUsersInAdapter(position)
                }
                remoteFullScreenVideoView?.let {
                    fillVideoView(user.id, it, track)
                    val name = user.fullName ?: user.login
                    tvFullNameUser.text = name
                    Log.d(TAG, "fullscreen enabled")
                }
                return
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.conversation_fragment, menu)
        super.onCreateOptionsMenu(menu, inflater)
        optionsMenu = menu
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.camera_switch -> {
                Log.d("Conversation", "camera_switch")
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
        conversationFragmentCallback?.onStartScreenSharing()
    }

    private fun updateAllOpponentsList(newUsers: ArrayList<QBUser>) {
        val indexList = allOpponents.indices
        for (index in indexList) {
            for (updatedUser in newUsers) {
                if (updatedUser == allOpponents[index]) {
                    allOpponents[index] = updatedUser
                }
            }
        }
    }

    private fun runUpdateUsersNames(newUsers: ArrayList<QBUser>) {
        // need delayed for synchronization with recycler parentView initialization
        mainHandler?.postDelayed({
            for (user in newUsers) {
                Log.d(TAG, "runUpdateUsersNames. foreach, user = " + user.fullName)
                updateNameForOpponent(user.id, user.fullName)
            }
        }, UPDATING_USERS_DELAY)
    }

    internal inner class DividerItemDecoration(context: Context, @DimenRes dimensionDivider: Int) :
        RecyclerView.ItemDecoration() {
        private val space: Int = context.resources.getDimensionPixelSize(dimensionDivider)

        override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
            outRect.set(space, space, space, space)
        }
    }

    internal inner class LocalViewOnClickListener : View.OnClickListener {
        private var lastFullScreenClickTime = 0L

        override fun onClick(v: View) {
            if (SystemClock.uptimeMillis() - lastFullScreenClickTime < FULL_SCREEN_CLICK_DELAY) {
                return
            }
            lastFullScreenClickTime = SystemClock.uptimeMillis()

            if (connectionEstablished) {
                setFullScreenOnOff()
            }
        }

        private fun setFullScreenOnOff() {
            if (actionBar.isShowing) {
                hideToolBarAndButtons()
            } else {
                showToolBarAndButtons()
            }
        }

        private fun hideToolBarAndButtons() {
            actionBar.hide()
            containerLocalVideoView?.visibility = View.INVISIBLE
            actionVideoButtonsLayout.visibility = View.GONE
            if (!isPeerToPeerCall) {
                shiftBottomListOpponents()
            }
        }

        private fun showToolBarAndButtons() {
            actionBar.show()
            containerLocalVideoView?.visibility = View.VISIBLE
            actionVideoButtonsLayout.visibility = View.VISIBLE
            if (!isPeerToPeerCall) {
                shiftMarginListOpponents()
            }
        }

        private fun shiftBottomListOpponents() {
            val params = recyclerView.layoutParams as RelativeLayout.LayoutParams
            params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
            params.setMargins(0, 0, 0, 0)

            recyclerView.layoutParams = params
        }

        private fun shiftMarginListOpponents() {
            val params = recyclerView.layoutParams as RelativeLayout.LayoutParams
            params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, 0)
            params.setMargins(0, 0, 0, resources.getDimension(R.dimen.margin_common).toInt())

            recyclerView.layoutParams = params
        }
    }

    private inner class UpdateOpponentsListenerImpl(val tag: String?) : CallActivity.UpdateOpponentsListener {
        override fun updatedOpponents(updatedOpponents: ArrayList<QBUser>) {
            updateAllOpponentsList(updatedOpponents)
            Log.d(TAG, "updateOpponentsList(), opponents = $updatedOpponents")
            runUpdateUsersNames(updatedOpponents)
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
}