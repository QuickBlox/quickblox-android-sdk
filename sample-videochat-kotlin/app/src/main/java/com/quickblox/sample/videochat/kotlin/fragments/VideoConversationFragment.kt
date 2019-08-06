package com.quickblox.sample.videochat.kotlin.fragments

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.Rect
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.util.SparseArray
import android.view.*
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.ToggleButton
import androidx.annotation.DimenRes
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.quickblox.sample.videochat.kotlin.R
import com.quickblox.sample.videochat.kotlin.adapters.OpponentsFromCallAdapter
import com.quickblox.sample.videochat.kotlin.services.CallService
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

    //Views
    private lateinit var cameraToggle: ToggleButton
    private var parentView: View? = null
    private lateinit var actionVideoButtonsLayout: LinearLayout
    private lateinit var connectionStatusLocal: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var localVideoView: QBRTCSurfaceView
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
    private var userIDFullScreen: Int = 0
    private var connectionEstablished: Boolean = false
    private var allCallbacksInit: Boolean = false
    private var isCurrentCameraFront: Boolean = false
    private var isLocalVideoFullScreen: Boolean = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        parentView = super.onCreateView(inflater, container, savedInstanceState)
        return parentView
    }

    override fun configureOutgoingScreen() {
        val context = activity!!
        outgoingOpponentsRelativeLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.grey_transparent_50))
        allOpponentsTextView.setTextColor(ContextCompat.getColor(context, R.color.white))
        ringingTextView.setTextColor(ContextCompat.getColor(context, R.color.white))
    }

    override fun configureActionBar() {
        actionBar.setDisplayShowTitleEnabled(false)
    }

    override fun configureToolbar() {
        val context = activity!!
        toolbar.visibility = View.VISIBLE
        toolbar.setBackgroundColor(ContextCompat.getColor(context, R.color.black_transparent_50))
        toolbar.setTitleTextColor(ContextCompat.getColor(context, R.color.white))
        toolbar.setSubtitleTextColor(ContextCompat.getColor(context, R.color.white))
    }

    override fun getFragmentLayout(): Int {
        return R.layout.fragment_video_conversation
    }

    override fun initFields() {
        super.initFields()
        localViewOnClickListener = LocalViewOnClickListener()
        amountOpponents = opponents.size
        allOpponents = Collections.synchronizedList(ArrayList(opponents.size))
        allOpponents.addAll(opponents)

        timerCallText = activity!!.findViewById(R.id.timer_call)

        isPeerToPeerCall = opponents.size == 1
    }

    private fun setDuringCallActionBar() {
        actionBar.setDisplayShowTitleEnabled(true)
        actionBar.title = currentUser.fullName
        if (isPeerToPeerCall) {
            actionBar.subtitle = getString(R.string.opponent, opponents[0].fullName)
        } else {
            actionBar.subtitle = getString(R.string.opponents, amountOpponents.toString())
        }

        actionButtonsEnabled(true)
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
    }

    override fun actionButtonsEnabled(inability: Boolean) {
        super.actionButtonsEnabled(inability)
        cameraToggle.isEnabled = inability
        // inactivate toggle buttons
        cameraToggle.isActivated = inability
    }

    override fun onStart() {
        super.onStart()
        Log.i(TAG, "onStart")
        if (!allCallbacksInit) {
            addListeners()
            allCallbacksInit = true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i(TAG, "onCreate")
        setHasOptionsMenu(true)
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
        initCorrectSizeForLocalView()
        localVideoView.setZOrderMediaOverlay(true)

        remoteFullScreenVideoView = view.findViewById(R.id.remote_video_view)
        remoteFullScreenVideoView?.setOnClickListener(localViewOnClickListener)

        if (!isPeerToPeerCall) {
            recyclerView = view.findViewById(R.id.grid_opponents)

            val context = activity!!
            recyclerView.addItemDecoration(DividerItemDecoration(context, R.dimen.grid_item_divider))
            recyclerView.setHasFixedSize(true)
            val columnsCount = defineColumnsCount()
            val layoutManager = LinearLayoutManager(activity, LinearLayout.HORIZONTAL, false)
            recyclerView.layoutManager = layoutManager

            //for correct removing item in adapter
            recyclerView.itemAnimator = null
            recyclerView.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    setGrid(columnsCount)
                    recyclerView.viewTreeObserver?.removeGlobalOnLayoutListener(this)
                }
            })
        }
        connectionStatusLocal = view.findViewById(R.id.connection_status_local)

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

    private fun restoreSession() {
        Log.d(TAG, "restoreSession ")
        if (conversationFragmentCallback?.isCallState() == false) {
            return
        }
        onCallStarted()
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
                    mainHandler.postDelayed({
                        onLocalVideoTrackReceive(null, videoTrack)
                    }, LOCAL_TRACK_INITIALIZE_DELAY)
                } else if (conversationFragmentCallback?.getPeerChannel(userId) != QBRTCTypes.QBRTCConnectionState.QB_RTC_CONNECTION_CLOSED) {
                    Log.d(TAG, "execute restoreSession for user:$userId")
                    mainHandler.postDelayed({
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
        val params = localVideoView.layoutParams
        val displaymetrics = resources.displayMetrics

        val screenWidthPx = displaymetrics.widthPixels
        Log.d(TAG, "screenWidthPx $screenWidthPx")

        val width = (screenWidthPx * 0.3).toInt()
        val height = width / 2 * 3
        params?.width = width
        params?.height = height
        localVideoView.layoutParams = params
    }

    private fun setGrid(columnsCount: Int) {
        val gridWidth = parentView?.measuredWidth
        Log.i(TAG, "onGlobalLayout : gridWidth= $gridWidth; columnsCount= $columnsCount")
        val itemMargin = resources.getDimension(R.dimen.grid_item_divider)

        gridWidth?.let {
            val cellSizeWidth = defineSize(it, columnsCount, itemMargin)
            Log.i(TAG, "onGlobalLayout : cellSize=$cellSizeWidth")
            opponentsAdapter = OpponentsFromCallAdapter(context!!, this, opponents, cellSizeWidth,
                    resources.getDimension(R.dimen.item_height).toInt())
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

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume")
        toggleCamera(cameraToggle.isChecked)
    }

    override fun onPause() {
        // If camera state is CameraState.ENABLED_FROM_USER or CameraState.NONE
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

    override fun onDetach() {
        super.onDetach()
        Log.d(TAG, "onDetach")
    }

    private fun releaseViewHolders() {
        opponentViewHolders.clear()
    }

    private fun releaseViews() {
        localVideoView.release()

        remoteFullScreenVideoView?.release()

        remoteFullScreenVideoView = null
        if (!isPeerToPeerCall) {
            releaseOpponentsViews()
        }
    }

    override fun onCallStopped() {
        super.onCallStopped()
        CallService.stop(activity as Activity)
        Log.i(TAG, "onCallStopped")
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
            remoteFullScreenVideoView!!
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

    ////////////////////////////  callbacks from QBRTCClientVideoTracksCallbacks ///////////////////
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
                    fillVideoView(remoteFullScreenVideoView!!, videoTrack, true)
                    updateVideoView(remoteFullScreenVideoView!!, false)
                }
            } else {
                mainHandler.postDelayed({ setRemoteViewMultiCall(it, videoTrack) }, LOCAL_TRACK_INITIALIZE_DELAY)
            }
        }
    }
    /////////////////////////////////////////    end    ////////////////////////////////////////////

    //last opponent parentView is bind
    override fun onBindLastViewHolder(holder: OpponentsFromCallAdapter.ViewHolder, position: Int) {
        Log.i(TAG, "onBindLastViewHolder position=$position")

    }

    override fun onItemClick(position: Int) {
        val userId = opponentsAdapter.getItem(position)
        Log.d(TAG, "USer onItemClick= $userId")

        val connectionState = conversationFragmentCallback?.getPeerChannel(userId)
        val videoTrackMap = conversationFragmentCallback?.getVideoTrackMap()
        if (videoTrackMap != null && !videoTrackMap.containsKey(userId)
                || connectionState?.ordinal == QBRTCTypes.QBRTCConnectionState.QB_RTC_CONNECTION_CLOSED.ordinal) {
            return
        }

        replaceUsersInAdapter(position)
        updateViewHolders(position)
        swapUsersFullscreenToPreview(userId)
    }

    private fun replaceUsersInAdapter(position: Int) {
        val opponents = allOpponents
        for (qbUser in opponents) {
            if (qbUser.id == userIDFullScreen) {
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

    private fun swapUsersFullscreenToPreview(userId: Int) {
        //      get opponentVideoTrack - opponent's video track from recyclerView
        val videoTrackMap = conversationFragmentCallback?.getVideoTrackMap()

        val opponentVideoTrack = videoTrackMap?.get(userId)

        //      get mainVideoTrack - opponent's video track from full screen
        val mainVideoTrack = videoTrackMap?.get(userIDFullScreen)

        val remoteVideoView = findHolder(userId)?.getOpponentView()

        mainVideoTrack?.let {
            fillVideoView(0, remoteVideoView!!, it)
            Log.d(TAG, "_remoteVideoView enabled")
        }

        opponentVideoTrack?.let {
            fillVideoView(userId, remoteFullScreenVideoView!!, it)
            Log.d(TAG, "fullscreen enabled")
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
        } else {
            isRemoteShown = true
            opponentsAdapter.removeItem(itemHolder.adapterPosition)
            setDuringCallActionBar()
            setRecyclerViewVisibleState()
            remoteFullScreenVideoView?.let {
                fillVideoView(userID, it, videoTrack)
                updateVideoView(remoteFullScreenVideoView!!, false)
            }
        }
    }

    private fun setRecyclerViewVisibleState() {
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
    private fun fillVideoView(videoView: QBRTCSurfaceView, videoTrack: QBRTCVideoTrack,
                              remoteRenderer: Boolean) {
        videoTrack.removeRenderer(videoTrack.renderer)
        videoTrack.addRenderer(videoView)
        if (!remoteRenderer) {
            updateVideoView(videoView, isCurrentCameraFront)
        }
        Log.d(TAG, (if (remoteRenderer) "remote" else "local") + " Track is rendering")
    }

    private fun updateVideoView(videoView: SurfaceViewRenderer, mirror: Boolean) {
        val scalingType = RendererCommon.ScalingType.SCALE_ASPECT_FILL
        Log.i(TAG, "updateVideoView mirror:$mirror, scalingType = $scalingType")
        videoView.setScalingType(scalingType)
        videoView.setMirror(mirror)
        videoView.requestLayout()
    }

    /**
     * @param userId set userId if it from fullscreen videoTrack
     */
    private fun fillVideoView(userId: Int, videoView: QBRTCSurfaceView, videoTrack: QBRTCVideoTrack) {
        if (userId != 0) {
            userIDFullScreen = userId
        }
        fillVideoView(videoView, videoTrack, true)
    }

    private fun setStatusForOpponent(userId: Int?, status: String) {
        if (isPeerToPeerCall) {
            connectionStatusLocal.text = status
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

    private fun setProgressBarForOpponentGone(userId: Int) {
        if (isPeerToPeerCall) {
            return
        }
        val holder = getViewHolderForOpponent(userId) ?: return

        holder.getProgressBar().visibility = View.GONE

    }

    private fun setBackgroundOpponentView(userId: Int?) {
        val holder = findHolder(userId) ?: return

        if (userId != userIDFullScreen) {
            holder.getOpponentView().setBackgroundColor(Color.parseColor("#000000"))
        }
    }

    ///////////////////////////////  QBRTCSessionConnectionCallbacks ///////////////////////////

    override fun onStateChanged(qbrtcSession: QBRTCSession, qbrtcSessionState: BaseSession.QBRTCSessionState) {

    }

    override fun onConnectedToUser(qbrtcSession: QBRTCSession?, userId: Int) {
        connectionEstablished = true
        setStatusForOpponent(userId, getString(R.string.text_status_connected))
        setProgressBarForOpponentGone(userId)
    }

    override fun onConnectionClosedForUser(qbrtcSession: QBRTCSession, userId: Int?) {
        userId?.let {
            setStatusForOpponent(it, getString(R.string.text_status_closed))
            if (!isPeerToPeerCall) {
                Log.d(TAG, "onConnectionClosedForUser videoTrackMap.remove(userId)= $userId")
                setBackgroundOpponentView(it)
            }
        }
    }

    override fun onDisconnectedFromUser(qbrtcSession: QBRTCSession, integer: Int?) {
        setStatusForOpponent(integer, getString(R.string.text_status_disconnected))
    }

    //////////////////////////////////   end     //////////////////////////////////////////

    /////////////////// Callbacks from CallActivity.QBRTCSessionUserCallback //////////////////////
    override fun onUserNotAnswer(session: QBRTCSession, userId: Int) {
        setProgressBarForOpponentGone(userId)
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
            if (userId == userIDFullScreen) {
                Log.d(TAG, "setAnotherUserToFullScreen call userId= $userId")
                setAnotherUserToFullScreen()
            }
        }
    }

    override fun onSessionClosed(session: QBRTCSession) {

    }

    //////////////////////////////////   end     //////////////////////////////////////////

    private fun setAnotherUserToFullScreen() {
        if (opponentsAdapter.opponents.isEmpty()) {
            return
        }
        val userId = opponentsAdapter.getItem(0)
        // get opponentVideoTrack - opponent's video track from recyclerView
        val opponentVideoTrack = conversationFragmentCallback?.getVideoTrack(userId) ?: return

        remoteFullScreenVideoView?.let {
            fillVideoView(userId, it, opponentVideoTrack)
            Log.d(TAG, "fullscreen enabled")
        }

        val itemHolder = findHolder(userId)
        if (itemHolder != null) {
            opponentsAdapter.removeItem(itemHolder.adapterPosition)
            itemHolder.getOpponentView().release()
            Log.d(TAG, "onConnectionClosedForUser opponentsAdapter.removeItem= $userId")
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.conversation_fragment, menu)
        super.onCreateOptionsMenu(menu, inflater)
        optionsMenu = menu
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
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

    override fun onOpponentsListUpdated(newUsers: ArrayList<QBUser>) {
        super.onOpponentsListUpdated(newUsers)
        updateAllOpponentsList(newUsers)
        Log.d(TAG, "updateOpponentsList(), newUsers = $newUsers")
        runUpdateUsersNames(newUsers)
    }

    override fun onCallTimeUpdate(time: String) {
        timerCallText.text = time
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
        //need delayed for synchronization with recycler parentView initialization
        mainHandler.postDelayed({
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
            localVideoView.visibility = View.INVISIBLE
            actionVideoButtonsLayout.visibility = View.GONE
            if (!isPeerToPeerCall) {
                shiftBottomListOpponents()
            }
        }

        private fun showToolBarAndButtons() {
            actionBar.show()
            localVideoView.visibility = View.VISIBLE
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
}