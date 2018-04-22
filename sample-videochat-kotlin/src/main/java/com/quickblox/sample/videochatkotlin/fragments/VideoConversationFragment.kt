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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.ImageButton
import androidx.core.util.forEach
import com.quickblox.chat.QBChatService
import com.quickblox.sample.videochatkotlin.R
import com.quickblox.sample.videochatkotlin.adapters.OpponentsCallAdapter
import com.quickblox.sample.videochatkotlin.utils.EXTRA_IS_INCOMING_CALL
import com.quickblox.sample.videochatkotlin.utils.getListAllUsersFromIds
import com.quickblox.users.model.QBUser
import com.quickblox.videochat.webrtc.BaseSession
import com.quickblox.videochat.webrtc.QBRTCSession
import com.quickblox.videochat.webrtc.callbacks.QBRTCClientVideoTracksCallbacks
import com.quickblox.videochat.webrtc.callbacks.QBRTCSessionStateCallback
import com.quickblox.videochat.webrtc.view.QBRTCSurfaceView
import com.quickblox.videochat.webrtc.view.QBRTCVideoTrack
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
    lateinit var mainHandler: Handler
    private var isIncomingCall: Boolean = false
    lateinit var layoutManager: GridLayoutManager

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

    interface CallFragmentCallbackListener {
        fun onHangUpCall()
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
        Log.d(TAG, "AMBRA onCreate")
        mainHandler = Handler()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_conversation_call, container, false)
        initArguments()
        initOpponentsList()
        initFields(view)
        return view
    }

    override fun onStart() {
        super.onStart()
        initVideoTrackSListener()
        initSessionListeners()
    }

    private fun initArguments() {
        if (arguments != null) {
            Log.d(TAG, "AMBRA arguments != null")
            isIncomingCall = arguments!!.getBoolean(EXTRA_IS_INCOMING_CALL)
        }
        currentUserId = QBChatService.getInstance().user.id
    }

    private fun initFields(view: View) {
        localFullScreenVideoView = view.findViewById<View>(R.id.local_video_view) as QBRTCSurfaceView
        hangUpCallButton = view.findViewById(R.id.button_hangup_call)
        hangUpCallButton.setOnClickListener({ hangUp() })

        recyclerView = view.findViewById<View>(R.id.grid_opponents) as RecyclerView

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

    fun initOpponentsList() {
        val opponentsIds = currentSession!!.opponents
        opponentsIds.add(currentSession!!.callerID)
        opponents = getListAllUsersFromIds(opponentsIds)
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
        Log.d(TAG, "AMBRA7 onLocalVideoTrackReceive")
        setUserToAdapter(currentUserId)
//        mainHandler.postDelayed(Runnable {
//            layoutManager.reverseLayout = false
//        }, 10000)
        mainHandler.postDelayed(Runnable { setViewMultiCall(currentUserId, videoTrack) }, 500)
    }

    override fun onRemoteVideoTrackReceive(session: QBRTCSession, videoTrack: QBRTCVideoTrack, userId: Int) {
        Log.d(TAG, "AMBRA7 onRemoteVideoTrackReceive")
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
        Log.d(TAG, "AMBRA7 setViewMultiCall userId= $userId")

        val itemHolder = getViewHolderForOpponent(userId)
        if (itemHolder != null) {
            val remoteVideoView = itemHolder.opponentView
            Log.d(TAG, "AMBRA setViewMultiCall fillVideoView")
            Log.d(TAG, "AMBRA7 setViewMultiCall remoteVideoView height= " + remoteVideoView.height)
            fillVideoView(userId, remoteVideoView, videoTrack, true)
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
        Log.d(TAG, "AMBRA findHolder for userID $userID")
        val childCount = recyclerView.childCount
        Log.d(TAG, "AMBRA childCount for $childCount")
        for (i in 0 until childCount) {
            Log.d(TAG, "AMBRA findHolder childCount $childCount , i= $i")
            val childView = recyclerView.getChildAt(i)
            Log.d(TAG, "AMBRA childView= " + childView)
            val childViewHolder = recyclerView.getChildViewHolder(childView) as OpponentsCallAdapter.ViewHolder
            Log.d(TAG, "AMBRA childViewHolder= " + childViewHolder)
            Log.d(TAG, "AMBRA childViewHolder.userId= " + childViewHolder.userId)
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
        opponentViewHolders.forEach { key, itemView ->
            itemView.opponentView.release()
        }
    }

    private fun releaseViewHolders() {
        opponentViewHolders.clear()
    }

    fun initSession(session: QBRTCSession?) {
        currentSession = session
    }

//    private fun setRecyclerViewVisibleState() {
//        recyclerView.visibility = View.VISIBLE
//    }

//    private fun adjustRecyclerViewSize(columns: Int) {
////        TODO replace with when
//        val height = screenHeight()
//        val params = recyclerView.layoutParams
//        if (columns <= 2) {
//            params.height = height / 3
//            recyclerView.layoutParams = params
//            Log.d(TAG, "AMBRA columns == 2 params.height= " + params.height)
//        } else if (columns == 3) {
//            params.height = height / 2
//            recyclerView.layoutParams = params
//            Log.d(TAG, "AMBRA columns == 3 params.height= " + params.height)
//        }
//        Log.d(TAG, "AMBRA adjustRecyclerViewSize height= " + height)
//        Log.d(TAG, "AMBRA recyclerView height= " + recyclerView.height)
//    }

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
        Log.d(TAG, "AMBRA onDestroyView")
        removeSessionListeners()
        removeVideoTrackSListener()
        releaseOpponentsViews()
        releaseViewHolders()
    }


    ////////////////////////////  QBRTCSessionStateCallbacks ///////////////////
    override fun onDisconnectedFromUser(p0: QBRTCSession?, p1: Int?) {
    }

    override fun onConnectedToUser(session: QBRTCSession, userId: Int) {
        Log.d(TAG, "AMBRA8 onConnectedToUser userId= $userId")
    }

    override fun onConnectionClosedForUser(session: QBRTCSession, userId: Int) {
        Log.d(TAG, "AMBRA8 onConnectionClosedForUser cleanUpAdapter userId= " + userId)
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
            outRect.set(space, space, space, space)
        }
    }
}