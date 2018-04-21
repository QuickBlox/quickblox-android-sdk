package com.quickblox.sample.videochatkotlin.fragments

import android.app.Activity
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
import android.view.ViewTreeObserver
import android.widget.FrameLayout
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
        initVideoTrackSListener()
        return view
    }

    private fun initArguments() {
        if (arguments != null) {
            Log.d(TAG, "AMBRA arguments != null")
            isIncomingCall = arguments.getBoolean(EXTRA_IS_INCOMING_CALL)
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
        val columnsCount = defineColumnsCount()
        layoutManager = GridLayoutManager(activity, 2)
        layoutManager.reverseLayout = true
        val spanSizeLookup = SpanSizeLookupImpl()
        spanSizeLookup.setSpanIndexCacheEnabled(false)
        layoutManager.setSpanSizeLookup(spanSizeLookup)
        recyclerView.setLayoutManager(layoutManager)

        recyclerView.itemAnimator = null
        initAdapter()
        recyclerView.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                if (initCellHeight(recyclerView.height)) {
                    recyclerView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                }
            }
        })

    }


    private fun initAdapter() {
        val cellSizeWidth = 0
        val cellSizeHeight = screenHeight()

        val qbUsers = ArrayList<QBUser>()
        opponentsAdapter = OpponentsCallAdapter(context, qbUsers, cellSizeWidth, cellSizeHeight)
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
        Log.d(TAG, "AMBRA onLocalVideoTrackReceive")
        setUserToAdapter(currentUserId)
//        mainHandler.postDelayed(Runnable {
//            layoutManager.reverseLayout = false
//        }, 10000)
        mainHandler.postDelayed(Runnable { setViewMultiCall(QBChatService.getInstance().user.id, videoTrack) }, 500)
    }

    override fun onRemoteVideoTrackReceive(session: QBRTCSession, videoTrack: QBRTCVideoTrack, userId: Int) {
        Log.d(TAG, "AMBRA onRemoteVideoTrackReceive")
        updateCellSizeIfNeed(recyclerView.height / 2)
        setUserToAdapter(userId)
        mainHandler.postDelayed(Runnable { setViewMultiCall(userId, videoTrack) }, 500)
    }

    fun updateCellSizeIfNeed(height: Int) {
        if (!isRemoteShown) {
            isRemoteShown = true

            val itemHolder = getViewHolderForOpponent(currentUserId)
            Log.d(TAG, "AMBRA5 updateCellSizeIfNeed itemHolder= " + itemHolder)
            itemHolder?.cellView?.layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, height)
            opponentsAdapter.itemHeight = height
        }
    }

    private fun initCellHeight(height: Int): Boolean {
        if (opponentsAdapter.innerLayout == null) {
            return false
        }
        opponentsAdapter.innerLayout?.layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, height)
        return true
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
        Log.d(TAG, "AMBRA setViewMultiCall")

        val itemHolder = getViewHolderForOpponent(userId)
        if (itemHolder != null) {
            val remoteVideoView = itemHolder.opponentView
            Log.d(TAG, "AMBRA setViewMultiCall fillVideoView")
            Log.d(TAG, "AMBRA setViewMultiCall remoteVideoView height= " + remoteVideoView.height)
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
//        releaseViewHolders()
//        removeConnectionStateListeners()
//        removeVideoTrackSListener()
//        removeVideoTrackRenderers()
//        releaseViews()
    }

    private inner class SpanSizeLookupImpl : GridLayoutManager.SpanSizeLookup() {


        override fun getSpanSize(position: Int): Int {
            Log.d("MORADIN", "position= $position")
            if (position % 3 > 0) {
                Log.d("MORADIN", "return 1")
                return 1
            } else {
                Log.d("MORADIN", "return 2")
                return 2
            }
//            val itemCount = opponentsAdapter.itemCount
//            Log.d("MORADIN","itemCount = $itemCount")
//            if (itemCount <= 2) {
//                Log.d("MORADIN","return 2")
//                return 2
//            }
//            else {
//                Log.d("MORADIN","return 3")
//                return 3
//            }
        }
    }
}