package com.quickblox.sample.videochat.conference.kotlin.fragments

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.util.SparseArray
import android.view.*
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.ToggleButton
import androidx.annotation.DimenRes
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.quickblox.chat.model.QBChatDialog
import com.quickblox.chat.model.QBDialogType
import com.quickblox.conference.ConferenceSession
import com.quickblox.conference.view.QBConferenceSurfaceView
import com.quickblox.core.QBEntityCallback
import com.quickblox.core.exception.QBResponseException
import com.quickblox.core.helper.StringifyArrayList
import com.quickblox.sample.videochat.conference.kotlin.R
import com.quickblox.sample.videochat.conference.kotlin.activities.CallActivity
import com.quickblox.sample.videochat.conference.kotlin.activities.EXTRA_AS_LISTENER
import com.quickblox.sample.videochat.conference.kotlin.activities.EXTRA_DIALOG_OCCUPANTS
import com.quickblox.sample.videochat.conference.kotlin.activities.SelectUsersActivity
import com.quickblox.sample.videochat.conference.kotlin.adapter.OpponentsFromCallAdapter
import com.quickblox.sample.videochat.conference.kotlin.db.QbUsersDbManager
import com.quickblox.sample.videochat.conference.kotlin.util.loadUserById
import com.quickblox.sample.videochat.conference.kotlin.utils.SharedPrefsHelper
import com.quickblox.sample.videochat.conference.kotlin.utils.WebRtcSessionManager
import com.quickblox.sample.videochat.conference.kotlin.utils.shortToast
import com.quickblox.users.model.QBUser
import com.quickblox.videochat.webrtc.BaseSession
import com.quickblox.videochat.webrtc.callbacks.QBRTCClientVideoTracksCallbacks
import com.quickblox.videochat.webrtc.callbacks.QBRTCSessionStateCallback
import com.quickblox.videochat.webrtc.view.QBRTCVideoTrack
import org.webrtc.RendererCommon
import org.webrtc.SurfaceViewRenderer
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

abstract class BaseConversationFragment : BaseToolBarFragment(), CallActivity.CurrentCallStateCallback, QBRTCSessionStateCallback<ConferenceSession>,
        QBRTCClientVideoTracksCallbacks<ConferenceSession>, OpponentsFromCallAdapter.OnAdapterEventListener {

    private var TAG: String = BaseConversationFragment::class.java.simpleName

    private lateinit var opponents: ArrayList<QBUser>
    private lateinit var usersToDestroy: MutableSet<Int>
    private lateinit var localViewOnClickListener: LocalViewOnClickListener

    private lateinit var recyclerView: RecyclerView
    private lateinit var gridLayoutManager: GridLayoutManager
    private lateinit var micToggleCall: ToggleButton
    private lateinit var handUpCall: ImageButton
    protected lateinit var connectionStatusLocal: TextView
    protected lateinit var actionButtonsLayout: LinearLayout
    protected lateinit var outgoingOpponentsRelativeLayout: View
    protected lateinit var allOpponentsTextView: TextView
    protected lateinit var ringingTextView: TextView
    protected var localVideoView: QBConferenceSurfaceView? = null

    private var allCallbacksInit: Boolean = false
    private var allOpponents: MutableList<QBUser>? = null
    private var spanSizeLookup: SpanSizeLookupImpl? = null
    private var videoTrackMap: Map<Int, QBRTCVideoTrack>? = null
    private var opponentViewHolders: SparseArray<OpponentsFromCallAdapter.ViewHolder>? = null
    protected var currentSession: ConferenceSession? = null
    protected var opponentsIds: ArrayList<Int>? = null
    protected var isRemoteShown: Boolean = false
    protected var opponentsAdapter: OpponentsFromCallAdapter? = null
    protected var isNeedCleanUp: Boolean = false
    protected var conversationFragmentCallbackListener: ConversationFragmentCallbackListener? = null
    protected var currentUser: QBUser? = null
    protected var asListenerRole: Boolean = false

    companion object {
        private const val DISPLAY_ROW_AMOUNT = 3
        private const val LOCAL_TRACK_INITIALIZE_DELAY = 500.toLong()
        private const val REQUEST_ADD_OCCUPANTS_CODE = 176

        fun newInstance(baseConversationFragment: BaseConversationFragment): BaseConversationFragment {
            val args = Bundle()
            baseConversationFragment.arguments = args
            return baseConversationFragment
        }
    }

    override fun onAttach(activity: Activity?) {
        super.onAttach(activity)
        try {
            conversationFragmentCallbackListener = activity as ConversationFragmentCallbackListener?
        } catch (e: ClassCastException) {
            throw ClassCastException(activity?.toString() + " must implement ConversationFragmentCallbackListener")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        conversationFragmentCallbackListener?.addCurrentCallStateCallback(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        opponentsIds = this.arguments?.getIntegerArrayList(EXTRA_DIALOG_OCCUPANTS)
        asListenerRole = this.arguments?.getBoolean(EXTRA_AS_LISTENER) as Boolean
        currentSession = WebRtcSessionManager.getCurrentSession()
        if (currentSession == null) {
            return view
        }
        initFields()
        initViews(view as View)
        initButtonsListener()
        configureToolbar()
        configureActionBar()
        prepareAndShowOutgoingScreen()
        return view
    }

    private fun prepareAndShowOutgoingScreen() {
        configureOutgoingScreen()
        allOpponentsTextView.text = getUsersFullNames(opponents)
    }

    private fun getUsersFullNames(opponents: java.util.ArrayList<QBUser>): String {
        val usersFullNames = StringifyArrayList<String>()
        for (usr in opponents) {
            if (usr.fullName != null) {
                usersFullNames.add(usr.fullName)
            } else if (usr.id != null) {
                usersFullNames.add(usr.id.toString())
            }
        }
        return usersFullNames.itemsAsString.replace(",", ", ")
    }

    override fun getFragmentLayout(): Int {
        return R.layout.fragment_conversation
    }

    protected fun configureOutgoingScreen() {
        val context = activity?.applicationContext as Context
        outgoingOpponentsRelativeLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.grey_transparent_50))
        allOpponentsTextView.setTextColor(ContextCompat.getColor(context, R.color.white))
        ringingTextView.setTextColor(ContextCompat.getColor(context, R.color.white))
    }

    protected fun configureActionBar() {
        actionBar = (activity as AppCompatActivity).supportActionBar as ActionBar
        actionBar.setDisplayShowTitleEnabled(false)
    }

    protected fun configureToolbar() {
        val context = activity?.applicationContext as Context
        toolbar.visibility = View.VISIBLE
        toolbar.setBackgroundColor(ContextCompat.getColor(context, R.color.black_transparent_50))
        toolbar.setTitleTextColor(ContextCompat.getColor(context, R.color.white))
        toolbar.setSubtitleTextColor(ContextCompat.getColor(context, R.color.white))
    }

    protected fun initFields() {
        currentUser = SharedPrefsHelper.getQbUser()
        currentSession = WebRtcSessionManager.getCurrentSession()

        initOpponentsList()

        localViewOnClickListener = LocalViewOnClickListener()
        usersToDestroy = HashSet()
        allOpponents = Collections.synchronizedList<QBUser>(ArrayList<QBUser>(opponents.size))
        allOpponents?.addAll(opponents)
    }

    protected fun setOpponentToAdapter(userID: Int?) {
        var qbUser = getUserById(userID as Int)
        if (qbUser == null) {
            qbUser = QBUser(userID)
            qbUser.fullName = getString(R.string.load_user)
            loadUserById(userID)
        }
        allOpponents?.add(qbUser)
        opponentsAdapter!!.add(qbUser)
        recyclerView.requestLayout()
    }

    fun setDuringCallActionBar() {
        actionBar.setDisplayShowTitleEnabled(true)
        actionBar.title = currentUser?.fullName
    }

    protected fun updateActionBar(amountOpponents: Int) {
        actionBar.subtitle = getString(R.string.opponents, amountOpponents.toString())
    }

    private fun setProgressBarForOpponentGone(userId: Int) {
        val holder = getViewHolderForOpponent(userId) ?: return
        holder.getProgressBar().visibility = View.GONE
    }

    //last opponent view is bind
    override fun onBindLastViewHolder(holder: OpponentsFromCallAdapter.ViewHolder, position: Int) {
        Log.i(TAG, "onBindLastViewHolder position=$position")
    }

    override fun onToggleButtonItemClick(position: Int, isChecked: Boolean) {
        val userId = opponentsAdapter?.getItem(position)
        Log.d(TAG, "onToggleButtonItemClick userId= $userId")
        adjustOpponentAudio(userId as Int, isChecked)
    }

    private fun adjustOpponentAudio(userID: Int, isAudioEnabled: Boolean) {
        currentSession?.mediaStreamManager?.getAudioTrack(userID)?.setEnabled(isAudioEnabled)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK) {
            return
        }
        if (requestCode == REQUEST_ADD_OCCUPANTS_CODE) {
            Log.d(TAG, "onActivityResult REQUEST_ADD_OCCUPANTS_CODE")
            val addedOccupants = data?.getSerializableExtra(SelectUsersActivity.RESULT_USERS_KEY) as ArrayList<QBUser>
            allOpponents?.addAll(0, addedOccupants)
        }
    }

    override fun onStart() {
        super.onStart()
        if (currentSession == null) {
            Log.d(TAG, "currentSession = null onStart")
            return
        }

        if (currentSession?.state != BaseSession.QBRTCSessionState.QB_RTC_SESSION_CONNECTED) {
            startJoinConference()
        }

        if (!allCallbacksInit) {
            conversationFragmentCallbackListener?.addClientConnectionCallback(this)
            initTrackListeners()
            allCallbacksInit = true
        }
    }

    protected open fun initTrackListeners() {
        initVideoTracksListener()
    }

    protected open fun removeTrackListeners() {
        removeVideoTracksListener()
    }

    override fun onResume() {
        super.onResume()
        isNeedCleanUp = true
        cleanAdapterIfNeed()
    }

    override fun onPause() {
        super.onPause()
        isNeedCleanUp = false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "onDestroyView")
        removeVideoTrackRenderers()
        releaseViews()
        opponentViewHolders?.clear()
        removeConnectionStateListeners()
        removeTrackListeners()
    }

    private fun removeConnectionStateListeners() {
        conversationFragmentCallbackListener?.removeClientConnectionCallback(this)
    }

    protected fun releaseOpponentsViews() {
        val layoutManager = recyclerView.layoutManager
        val childCount = layoutManager?.childCount
        Log.d(TAG, " releaseOpponentsViews for  $childCount views")
        for (i in 0 until childCount as Int) {
            val childView = layoutManager.getChildAt(i)
            Log.d(TAG, " release View for  $i, $childView")
            val childViewHolder =
                    recyclerView.getChildViewHolder(childView as View) as OpponentsFromCallAdapter.ViewHolder
            childViewHolder.getOpponentView().release()
        }
    }

    private fun getUserById(userID: Int): QBUser? {
        for (qbUser in allOpponents as MutableList) {
            if (qbUser.id == userID) {
                return qbUser
            }
        }
        return null
    }

    override fun onDestroy() {
        conversationFragmentCallbackListener?.removeCurrentCallStateCallback(this)
        super.onDestroy()
    }

    private fun startJoinConference() {
        conversationFragmentCallbackListener?.onStartJoinConference()
    }

    protected open fun initViews(view: View) {
        micToggleCall = view.findViewById<View>(R.id.toggle_mic) as ToggleButton
        handUpCall = view.findViewById<View>(R.id.button_hangup_call) as ImageButton
        outgoingOpponentsRelativeLayout = view.findViewById(R.id.layout_background_outgoing_screen)
        allOpponentsTextView = view.findViewById<View>(R.id.text_outgoing_opponents_names) as TextView
        ringingTextView = view.findViewById<View>(R.id.text_ringing) as TextView

        opponentViewHolders = SparseArray(opponents.size)
        isRemoteShown = false

        localVideoView = view.findViewById<View>(R.id.local_video_view) as QBConferenceSurfaceView
        localVideoView?.setOnClickListener(localViewOnClickListener)

        recyclerView = view.findViewById<View>(R.id.grid_opponents) as RecyclerView

        recyclerView.addItemDecoration(DividerItemDecoration(activity?.applicationContext as Context, R.dimen.grid_item_divider))
        recyclerView.setHasFixedSize(false)

        gridLayoutManager = GridManager(activity?.applicationContext as Context, 12)
        gridLayoutManager.reverseLayout = false
        spanSizeLookup = SpanSizeLookupImpl()
        spanSizeLookup?.isSpanIndexCacheEnabled = false
        gridLayoutManager.spanSizeLookup = spanSizeLookup
        recyclerView.layoutManager = gridLayoutManager

        //for correct removing item in adapter
        recyclerView.itemAnimator = null
        recyclerView.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                setGrid(recyclerView.height)
                recyclerView.viewTreeObserver.removeGlobalOnLayoutListener(this)
            }
        })
        connectionStatusLocal = view.findViewById<View>(R.id.connectionStatusLocal) as TextView
        actionButtonsLayout = view.findViewById<View>(R.id.element_set_call_buttons) as LinearLayout
        actionButtonsEnabled(false)
        setActionButtonsVisibility()
    }

    private fun setActionButtonsVisibility() {
        if (asListenerRole) {
            setActionButtonsInvisible()
        }
    }

    protected open fun setActionButtonsInvisible() {
        micToggleCall.visibility = View.INVISIBLE
    }

    private fun setGrid(recycleViewHeight: Int) {
        val qbUsers = java.util.ArrayList<QBUser>()
        val itemHeight = recycleViewHeight / DISPLAY_ROW_AMOUNT
        opponentsAdapter = OpponentsFromCallAdapter(activity?.applicationContext as Context,
                currentSession as ConferenceSession, qbUsers,
                resources.getDimension(R.dimen.item_width).toInt(),
                itemHeight)
        opponentsAdapter?.adapterListener = this
        recyclerView.adapter = opponentsAdapter
    }

    private fun removeVideoTrackRenderers() {
        Log.d(TAG, "removeVideoTrackRenderers")
        Log.d(TAG, "remove opponents video Tracks")
        val videoTrackMap = getVideoTrackMap()
        for (videoTrack in videoTrackMap.values) {
            if (videoTrack.renderer != null) {
                Log.d(TAG, "remove opponent video Tracks")
                videoTrack.removeRenderer(videoTrack.renderer)
            }
        }
    }

    private fun releaseViews() {
        localVideoView?.release()
        localVideoView = null

        releaseOpponentsViews()
    }

    protected open fun initButtonsListener() {
        micToggleCall.setOnCheckedChangeListener { buttonView, isChecked -> conversationFragmentCallbackListener?.onSetAudioEnabled(isChecked) }
        handUpCall.setOnClickListener {
            actionButtonsEnabled(false)
            handUpCall.isEnabled = false
            handUpCall.isActivated = false
            conversationFragmentCallbackListener?.onLeaveCurrentSession()
            Log.d(TAG, "Call is stopped")
        }
    }

    protected open fun actionButtonsEnabled(inability: Boolean) {
        micToggleCall.isEnabled = inability

        // inactivate toggle buttons
        micToggleCall.isActivated = inability
    }

    private fun hideOutgoingScreen() {
        outgoingOpponentsRelativeLayout.visibility = View.GONE
    }

    override fun onCallStarted() {
        hideOutgoingScreen()
        actionButtonsEnabled(true)
    }

    private fun initOpponentsList() {
        opponents = QbUsersDbManager.getUsersByIds(opponentsIds as ArrayList)
    }

    protected fun getViewHolderForOpponent(userID: Int?): OpponentsFromCallAdapter.ViewHolder? {
        var holder: OpponentsFromCallAdapter.ViewHolder? = opponentViewHolders?.get(userID as Int)
        if (holder == null) {
            Log.d(TAG, "holder not found in cache")
            holder = findHolder(userID)
            if (holder != null) {
                opponentViewHolders?.put(userID as Int, holder)
            }
        }
        return holder
    }

    private fun setStatusForOpponent(userId: Int, status: String) {
        if (userId == currentUser?.id) {
            return
        }
        val holder = getViewHolderForOpponent(userId) ?: return

        holder.setStatus(status)
    }

    protected fun setStatusForCurrentUser(status: String) {
        connectionStatusLocal.text = status
    }

    protected fun cleanUpAdapter(userId: Int) {
        Log.d(TAG, "onConnectionClosedForUser cleanUpAdapter userId= $userId")
        val itemHolder = getViewHolderForOpponent(userId)
        if (itemHolder != null) {
            if (itemHolder.adapterPosition != -1) {
                Log.d(TAG, "onConnectionClosedForUser  opponentsAdapter.removeItem")
                opponentsAdapter?.removeItem(itemHolder.adapterPosition)
                opponentViewHolders?.remove(userId)
            }
        }
        updateActionBar(opponentsAdapter?.itemCount as Int)
        recyclerView.requestLayout()
        getVideoTrackMap().remove(userId)
    }

    protected fun addOpponentToDialog() {
        SelectUsersActivity.startForResult(this, REQUEST_ADD_OCCUPANTS_CODE,
                getChatDialog(currentSession?.dialogID as String))
    }

    private fun getChatDialog(dialogId: String): QBChatDialog {
        val chatDialog = QBChatDialog(dialogId)
        chatDialog.type = QBDialogType.GROUP
        return chatDialog
    }

    protected fun cleanAdapterIfNeed() {
        if (!usersToDestroy.isEmpty()) {
            val iterator = usersToDestroy.iterator()
            while (iterator.hasNext()) {
                cleanUpAdapter(iterator.next())
                iterator.remove()
            }
        }
    }

    protected fun setRecyclerViewVisibleState() {
        recyclerView.visibility = View.VISIBLE
    }

    protected fun findHolder(userID: Int?): OpponentsFromCallAdapter.ViewHolder? {
        Log.d(TAG, "findHolder for " + userID)
        val childCount = recyclerView.childCount
        Log.d(TAG, "findHolder for childCount= $childCount")
        for (i in 0 until childCount) {
            val childView = recyclerView.getChildAt(i)
            val childViewHolder = recyclerView.getChildViewHolder(childView) as OpponentsFromCallAdapter.ViewHolder
            Log.d(TAG, "childViewHolder.getUserId= " + childViewHolder.getUserId())
            if (userID == childViewHolder.getUserId()) {
                Log.d(TAG, "return childViewHolder")
                return childViewHolder
            }
        }
        return null
    }

    private fun setOpponentView(userID: Int) {
        setOpponentToAdapter(userID)
        if (!isRemoteShown) {
            isRemoteShown = true
            setRecyclerViewVisibleState()
            setDuringCallActionBar()
        }
        updateActionBar(opponentsAdapter?.itemCount as Int)
    }

    private fun checkIfUserInAdapter(userId: Int): Boolean {
        for (user in opponentsAdapter?.getOpponents() as List) {
            if (user.id == userId) {
                return true
            }
        }
        return false
    }

    private fun loadUserById(userId: Int) {
        loadUserById(userId, object : QBEntityCallback<QBUser> {
            override fun onSuccess(qbUser: QBUser, params: Bundle?) {
                QbUsersDbManager.saveUser(qbUser)
                if (!TextUtils.isEmpty(qbUser.fullName)) {
                    opponentsAdapter!!.updateUserFullName(qbUser)
                }
            }

            override fun onError(exception: QBResponseException?) {
                shortToast(exception?.message!!)
            }
        })
    }

    ///////////////////////////////  QBRTCSessionConnectionCallbacks ///////////////////////////

    override fun onConnectedToUser(p0: ConferenceSession?, userId: Int?) {
        if (checkIfUserInAdapter(userId as Int)) {
            setStatusForOpponent(userId, getString(R.string.text_status_connected))
            Log.d(TAG, "onConnectedToUser user already in, userId= $userId")
            return
        }
        setOpponentView(userId)

        mainHandler.postDelayed({
            setRemoteViewMultiCall(userId)
            setStatusForOpponent(userId, getString(R.string.text_status_connected))
            setProgressBarForOpponentGone(userId)
        }, LOCAL_TRACK_INITIALIZE_DELAY)
    }

    override fun onConnectionClosedForUser(p0: ConferenceSession?, userId: Int?) {
        Log.d(TAG, "onConnectionClosedForUser userId= $userId")

        if (currentSession?.isDestroyed as Boolean) {
            Log.d(TAG, "onConnectionClosedForUser isDestroyed userId= $userId")
            return
        }

        if (isNeedCleanUp) {
            setStatusForOpponent(userId as Int, getString(R.string.text_status_closed))
            cleanUpAdapter(userId)
        } else {
            usersToDestroy.add(userId as Int)
        }
    }

    override fun onDisconnectedFromUser(p0: ConferenceSession?, integer: Int?) {
        setStatusForOpponent(integer as Int, getString(R.string.text_status_disconnected))
    }

    override fun onStateChanged(p0: ConferenceSession?, p1: BaseSession.QBRTCSessionState?) {

    }

    //////////////////////////////////   end     //////////////////////////////////////////

    protected fun getVideoTrackMap(): MutableMap<Int, QBRTCVideoTrack> {
        if (videoTrackMap == null) {
            videoTrackMap = HashMap()
        }
        return videoTrackMap as HashMap
    }

    override fun onLocalVideoTrackReceive(p0: ConferenceSession?, videoTrack: QBRTCVideoTrack?) {
        Log.d(TAG, "onLocalVideoTrackReceive")
    }

    override fun onRemoteVideoTrackReceive(p0: ConferenceSession?, videoTrack: QBRTCVideoTrack?, userId: Int?) {
        Log.d(TAG, "onRemoteVideoTrackReceive for opponent= $userId")
        getVideoTrackMap()[userId as Int] = videoTrack as QBRTCVideoTrack
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.conversation_fragment, menu)
        if (asListenerRole) {
            val cameraSwitchItem = menu?.findItem(R.id.camera_switch)
            cameraSwitchItem?.isVisible = false
        }
        super.onCreateOptionsMenu(menu, inflater)
    }

    protected fun setRemoteViewMultiCall(userID: Int) {
        if (currentSession?.isDestroyed as Boolean) {
            Log.d(TAG, "setRemoteViewMultiCall currentSession.isDestroyed RETURN")
            return
        }
        updateActionBar(opponentsAdapter?.itemCount as Int)
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
        val remoteVideoTrack = getVideoTrackMap().get(userID)
        if (remoteVideoTrack != null) {
            fillVideoView(remoteVideoView, remoteVideoTrack, true)
        }
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

    protected open fun fillVideoView(videoView: QBConferenceSurfaceView, videoTrack: QBRTCVideoTrack,
                                     remoteRenderer: Boolean) {
        videoTrack.removeRenderer(videoTrack.renderer)
        videoTrack.addRenderer(videoView)
        Log.d(TAG, (if (remoteRenderer) "remote" else "local") + " Track is rendering")
    }

    private fun initVideoTracksListener() {
        if (currentSession != null) {
            currentSession?.addVideoTrackCallbacksListener(this)
        }
    }

    private fun removeVideoTracksListener() {
        if (currentSession != null) {
            currentSession?.removeVideoTrackCallbacksListener(this)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.add_opponent -> {
                Log.d(TAG, "add_opponent")
                addOpponentToDialog()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    private inner class GridManager(context: Context, spanCount: Int) : GridLayoutManager(context, spanCount) {
        override fun onItemsAdded(recyclerView: RecyclerView, positionStart: Int, itemCount: Int) {
            super.onItemsAdded(recyclerView, positionStart, itemCount)
            Log.d("GridManager", "onItemsAdded positionStart= $positionStart")
        }

        override fun onItemsRemoved(recyclerView: RecyclerView, positionStart: Int, itemCount: Int) {
            super.onItemsRemoved(recyclerView, positionStart, itemCount)
            Log.d("GridManager", "onItemsRemoved positionStart= $positionStart")
            updateAdaptersItems()
        }

        private fun updateAdaptersItems() {
            if (opponentsAdapter?.itemCount as Int > 0) {
                val itemHolder = getViewHolderForOpponent(opponentsAdapter?.getItem(0))
                if (itemHolder != null) {
                    itemHolder.itemView.requestLayout()
                }
            }
        }

        override fun onItemsUpdated(recyclerView: RecyclerView, positionStart: Int, itemCount: Int, payload: Any?) {
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

    private inner class SpanSizeLookupImpl : GridLayoutManager.SpanSizeLookup() {

        override fun getSpanSize(position: Int): Int {
            val itemCount = opponentsAdapter?.itemCount as Int
            if (itemCount % 4 == 0) {
                return 3
            }
            if (itemCount % 4 == 1) {
                //check last position
                if (position == itemCount - 1) {
                    return 12
                }
            } else if (itemCount % 4 == 2) {
                if (position == itemCount - 1 || position == itemCount - 2) {
                    return 6
                }
            } else if (itemCount % 4 == 3) {
                if (position == itemCount - 1 || position == itemCount - 2 || position == itemCount - 3) {
                    return 4
                }
            }
            return 3
        }
    }

    private inner class DividerItemDecoration(context: Context, @DimenRes dimensionDivider: Int) : RecyclerView.ItemDecoration() {

        private val space: Int

        init {
            this.space = context.resources.getDimensionPixelSize(dimensionDivider)
        }

        override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
            outRect.set(space, space, space, space)
        }
    }

    private inner class LocalViewOnClickListener : View.OnClickListener {
        override fun onClick(v: View) {
            Log.d(TAG, "localView onClick")
        }
    }
}