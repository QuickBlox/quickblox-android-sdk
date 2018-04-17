package com.quickblox.sample.videochatkotlin.fragments

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import com.quickblox.chat.QBChatService
import com.quickblox.sample.videochatkotlin.R
import com.quickblox.sample.videochatkotlin.adapters.OpponentsCallAdapter
import com.quickblox.sample.videochatkotlin.utils.EXTRA_IS_INCOMING_CALL
import com.quickblox.sample.videochatkotlin.utils.getListAllUsersFromIds
import com.quickblox.users.model.QBUser
import com.quickblox.videochat.webrtc.QBRTCSession
import com.quickblox.videochat.webrtc.callbacks.QBRTCClientVideoTracksCallbacks
import com.quickblox.videochat.webrtc.view.QBRTCSurfaceView
import com.quickblox.videochat.webrtc.view.QBRTCVideoTrack
import org.webrtc.RendererCommon
import org.webrtc.SurfaceViewRenderer
import org.webrtc.VideoRenderer
import java.util.*

/**
 * Created by Roman on 15.04.2018.
 */
class VideoConversationFragment : Fragment(), QBRTCClientVideoTracksCallbacks<QBRTCSession> {

    private val TAG = VideoConversationFragment::class.java.simpleName
    lateinit var hangUpCallButton: ImageButton
    lateinit var mainHandler: Handler
    private var isIncomingCall: Boolean = false

    private var isCurrentCameraFront: Boolean = true
    var currentSession: QBRTCSession? = null
    lateinit var localFullScreenVideoView: QBRTCSurfaceView
    lateinit var eventListener: CallFragmentCallbackListener
    private var opponentsAdapter: OpponentsCallAdapter? = null
    lateinit var recyclerView: RecyclerView
    lateinit var opponents: ArrayList<QBUser>
    lateinit var opponentViewHolders: SparseArray<OpponentsCallAdapter.ViewHolder>

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
        initVideoTrackSListener()
        return view
    }

    private fun initArguments() {
        if (arguments != null) {
            Log.d(TAG, "AMBRA arguments != null")
            isIncomingCall = arguments.getBoolean(EXTRA_IS_INCOMING_CALL)
        }
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
        val columnsCount = defineColumnsCount()
//        val layoutManager = LinearLayoutManager(activity, HORIZONTAL, false)
        val layoutManager = GridManager(activity, 1)
        recyclerView.setLayoutManager(layoutManager)
        recyclerView.itemAnimator = null
        initAdapterCellSize()
    }

    private inner class GridManager internal constructor(context: Context, spanCount: Int) : GridLayoutManager(context, spanCount) {

        override fun onItemsAdded(recyclerView: RecyclerView?, positionStart: Int, itemCount: Int) {
            super.onItemsAdded(recyclerView, positionStart, itemCount)
            Log.d("GridManager", "onItemsAdded positionStart= $positionStart")
        }

        override fun onItemsRemoved(recyclerView: RecyclerView?, positionStart: Int, itemCount: Int) {
            super.onItemsRemoved(recyclerView, positionStart, itemCount)
            Log.d("GridManager", "onItemsRemoved positionStart= $positionStart")
            updateAdaptersItems()
        }

        private fun updateAdaptersItems() {
            if (opponentsAdapter!!.getItemCount() > 0) {
                val itemHolder = getViewHolderForOpponent(opponentsAdapter!!.getItem(0))
                itemHolder?.itemView?.requestLayout()
            }
        }

        override fun onItemsUpdated(recyclerView: RecyclerView?, positionStart: Int, itemCount: Int,
                                    payload: Any?) {
            super.onItemsUpdated(recyclerView, positionStart, itemCount, payload)
            Log.d("GridManager", "onItemsUpdated positionStart= $positionStart")
        }

        override fun onItemsChanged(recyclerView: RecyclerView) {
            super.onItemsChanged(recyclerView)
            Log.d("GridManager", "onItemsChanged")
        }

        override fun onLayoutCompleted(state: RecyclerView.State?) {
            super.onLayoutCompleted(state)
            Log.d("GridManager", "onLayoutCompleted")

        }
    }

    private fun initAdapterCellSize() {
        val cellSizeWidth = cellWidth(opponents.size)
        val cellSizeHeight = cellHeight(opponents.size)
//        val cellSizeHeight = recyclerView.getHeight() / 2
        Log.d(TAG, "AMBRA initAdapterCellSize cellSizeWidth= $cellSizeWidth, cellSizeHeight= $cellSizeHeight")
        opponentsAdapter = OpponentsCallAdapter(context, opponents, cellSizeWidth, cellSizeHeight)
        recyclerView.adapter = opponentsAdapter
    }

    private fun cellHeight(columns: Int): Int {
        if (columns == 1) {
            return screenHeight() / 3
        } else if (columns == 2) {

        }
        return 0
    }

    private fun cellWidth(columns: Int): Int {
        if (columns == 1) {
            return screenWidth()
        }else if (columns == 2) {

        }
        return 0
    }

    fun initOpponentsList() {
        Log.d(TAG, "AMBRA initOpponentsList isIncomingCall $isIncomingCall")
        val opponentsIds = currentSession!!.opponents
        if (isIncomingCall) {
            opponentsIds.add(currentSession!!.callerID)
            opponentsIds.remove(QBChatService.getInstance().user.id)
        }
        opponents = getListAllUsersFromIds(opponentsIds)
        Log.d(TAG, "AMBRA initOpponentsList opponents= " + opponents)
        Log.d(TAG, "AMBRA initOpponentsList callerID= " + currentSession!!.callerID)
    }

    private fun defineColumnsCount(): Int {
        return opponents.size
    }

    fun hangUp() {
        eventListener.onHangUpCall()
    }

    override fun onLocalVideoTrackReceive(session: QBRTCSession, videoTrack: QBRTCVideoTrack) {
        Log.d(TAG, "AMBRA onLocalVideoTrackReceive")
        fillVideoView(localFullScreenVideoView, videoTrack, false)
    }

    override fun onRemoteVideoTrackReceive(session: QBRTCSession, videoTrack: QBRTCVideoTrack, userId: Int) {
        Log.d(TAG, "AMBRA onRemoteVideoTrackReceive")
        mainHandler.postDelayed(Runnable { setRemoteViewMultiCall(userId, videoTrack) }, 500)
    }

    fun setRemoteViewMultiCall(userId: Int, videoTrack: QBRTCVideoTrack) {
        Log.d(TAG, "AMBRA setRemoteViewMultiCall")
        val itemHolder = getViewHolderForOpponent(userId)
        if (itemHolder != null) {
            val remoteVideoView = itemHolder.opponentView
            updateVideoView(remoteVideoView, false)
            adjustRecyclerViewSize(opponents.size)
            setRecyclerViewVisibleState()
            Log.d(TAG, "AMBRA setRemoteViewMultiCall fillVideoView")
            Log.d(TAG, "AMBRA setRemoteViewMultiCall remoteVideoView height= " + remoteVideoView.height)
            fillVideoView(userId, remoteVideoView, videoTrack, true)
        }
    }

    private fun getViewHolderForOpponent(userID: Int?): OpponentsCallAdapter.ViewHolder? {
        var holder: OpponentsCallAdapter.ViewHolder? = opponentViewHolders.get(userID!!)
        if (holder == null) {
            Log.d(TAG, "holder not found in cache")
            holder = findHolder(userID)
            if (holder != null) {
                opponentViewHolders.append(userID, holder)
            }
        }
        return holder
    }

    private fun findHolder(userID: Int?): OpponentsCallAdapter.ViewHolder? {
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

    private fun initVideoTrackSListener() {
        if (currentSession != null) {
            currentSession!!.addVideoTrackCallbacksListener(this)
        }
    }

    private fun removeVideoTrackSListener() {
        if (currentSession != null) {
            currentSession!!.removeVideoTrackCallbacksListener(this)
        }
    }

    fun initSession(session: QBRTCSession?) {
        currentSession = session
    }

    private fun setRecyclerViewVisibleState() {
        recyclerView.visibility = View.VISIBLE
    }

    private fun adjustRecyclerViewSize(columns: Int) {
//        TODO replace with when
        if (columns == 1) {
            val height = screenHeight()
            val params = recyclerView.layoutParams
            params.height = height / 3
            recyclerView.layoutParams = params
            Log.d(TAG, "adjustRecyclerViewSize height= " + height)
        } else if (columns == 2) {

        }

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
        Log.d(TAG, "AMBRA onDestroyView")
//        releaseViewHolders()
//        removeConnectionStateListeners()
//        removeVideoTrackSListener()
//        removeVideoTrackRenderers()
//        releaseViews()
    }
}