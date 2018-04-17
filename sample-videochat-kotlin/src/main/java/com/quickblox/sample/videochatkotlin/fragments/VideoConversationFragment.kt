package com.quickblox.sample.videochatkotlin.fragments

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.GridLayout
import android.widget.ImageButton
import android.widget.LinearLayout.HORIZONTAL
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
import java.util.ArrayList

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

    private fun initArguments(){
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
//        recyclerView.addItemDecoration(DividerItemDecoration(activity, R.dimen.grid_item_divider))
        recyclerView.setHasFixedSize(true)
        val columnsCount = defineColumnsCount()
        val layoutManager = LinearLayoutManager(activity, HORIZONTAL, false)
//        val layoutManager = GridLayoutManager(activity, columnsCount)
        recyclerView.setLayoutManager(layoutManager)

        recyclerView.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                val cellSizeWidth = resources.getDimension(R.dimen.item_width).toInt()
                val cellSizeHeight = resources.getDimension(R.dimen.item_height).toInt()
                opponentsAdapter = OpponentsCallAdapter(context, opponents, cellSizeWidth, cellSizeHeight)
                recyclerView.adapter = opponentsAdapter
                recyclerView.viewTreeObserver.removeGlobalOnLayoutListener(this)
            }
        })
    }

    fun initOpponentsList() {
        Log.d(TAG, "AMBRA initOpponentsList isIncomingCall $isIncomingCall")
        val opponentsIds = currentSession!!.opponents
        if(isIncomingCall){
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
        if(itemHolder !=null) {
            val remoteVideoView = itemHolder!!.opponentView
            updateVideoView(remoteVideoView, false)
            setRecyclerViewVisibleState()
            Log.d(TAG, "AMBRA setRemoteViewMultiCall fillVideoView")
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
        val params = recyclerView.layoutParams
        params.height = resources.getDimension(R.dimen.item_height).toInt()
        recyclerView.layoutParams = params
        recyclerView.visibility = View.VISIBLE
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