package com.quickblox.sample.videochatkotlin.adapters

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.quickblox.chat.QBChatService
import com.quickblox.sample.videochatkotlin.R
import com.quickblox.users.model.QBUser
import com.quickblox.videochat.webrtc.QBRTCSession
import com.quickblox.videochat.webrtc.view.QBRTCSurfaceView

class OpponentsCallAdapter(context: Context, var session: QBRTCSession, users: ArrayList<QBUser>, width: Int, height: Int) : RecyclerView.Adapter<OpponentsCallAdapter.ViewHolder>() {
    private val TAG = OpponentsCallAdapter::class.java.simpleName
    var inflater: LayoutInflater = LayoutInflater.from(context)
    var opponents: ArrayList<QBUser> = users
    var adapterListener: OnAdapterEventListener? = null
    var currentUserId: Int = 0
    var itemHeight: Int = 0
    var itemWidth: Int = 0

    init {
        currentUserId = QBChatService.getInstance().user.id
        itemWidth = width
        itemHeight = height
        Log.d(TAG, "item width=$itemWidth, item height=$itemHeight")
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = inflater.inflate(R.layout.list_item_opponent_from_call, null)
        val vh = ViewHolder(view)
        initCellHeight(vh)
        vh.toggleButton.setOnCheckedChangeListener { _, isChecked -> adapterListener!!.onToggleButtonItemClick(vh.adapterPosition, isChecked) }
        return vh
    }

    fun initCellHeight(holder: ViewHolder, height: Int = itemHeight) {
        val params = holder.itemLayout.layoutParams
        params.height = height
        holder.itemLayout.layoutParams = params
    }

    override fun getItemCount(): Int {
        return opponents.size
    }

    fun getItem(position: Int): Int? {
        return opponents[position].id
    }

    fun add(item: QBUser) {
        opponents.add(item)
        notifyItemRangeChanged(opponents.size - 1, opponents.size)
    }

    fun removeItem(index: Int) {
        opponents.removeAt(index)
        notifyItemRemoved(index)
        notifyItemRangeChanged(index, opponents.size)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = opponents[position]
        val userID = user.id
        holder.toggleButton.isChecked = if (currentUserId == userID) session.mediaStreamManager.localAudioTrack.enabled() else session.mediaStreamManager.getAudioTrack(userID).enabled()
        holder.opponentView.id = user.id
        holder.userId = userID
        holder.opponentsName.text = user.fullName ?: user.login
    }

    interface OnAdapterEventListener {
        fun onToggleButtonItemClick(position: Int, isChecked: Boolean)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var itemLayout: RelativeLayout = itemView.findViewById(R.id.itemLayout)
        var opponentsName: TextView = itemView.findViewById(R.id.opponentName)
        var connectionStatus: TextView = itemView.findViewById(R.id.connectionStatus)
        var opponentView: QBRTCSurfaceView = itemView.findViewById(R.id.opponentView)
        var progressBar: ProgressBar = itemView.findViewById(R.id.progress_bar_adapter)
        var toggleButton: ToggleButton = itemView.findViewById(R.id.opponent_toggle_mic);
        var userId: Int = 0
    }
}