package com.quickblox.sample.videochat.conference.kotlin.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.ToggleButton
import androidx.recyclerview.widget.RecyclerView
import com.quickblox.conference.ConferenceSession
import com.quickblox.conference.view.QBConferenceSurfaceView
import com.quickblox.sample.videochat.conference.kotlin.R
import com.quickblox.users.model.QBUser
import com.quickblox.videochat.webrtc.QBRTCTypes


class OpponentsFromCallAdapter(val context: Context,
                               val session: ConferenceSession,
                               val users: List<QBUser>,
                               val width: Int,
                               val height: Int) : RecyclerView.Adapter<OpponentsFromCallAdapter.ViewHolder>() {

    private var opponents: MutableList<QBUser> = ArrayList()
    var adapterListener: OnAdapterEventListener? = null

    init {
        opponents.addAll(users)
    }

    fun updateUserFullName(user: QBUser) {
        val position = opponents.indexOf(user)
        val userInList = opponents[position]
        userInList.fullName = user.fullName
        notifyItemChanged(position)
    }

    @SuppressLint("InflateParams")
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.list_item_opponent_from_call, null)
        view.findViewById<View>(R.id.innerLayout).layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, height)

        val vh = ViewHolder(view)
        vh.toggleButton.setOnCheckedChangeListener { compoundButton, isChecked ->
            adapterListener?.onToggleButtonItemClick(vh.adapterPosition, isChecked)
        }
        vh.showOpponentView()
        return vh
    }

    override fun getItemCount(): Int {
        return opponents.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = opponents[position]
        val userID = user.id
        holder.opponentsName.text = user.fullName

        if (session.mediaStreamManager != null) {
            holder.toggleButton.isChecked = session.mediaStreamManager.getAudioTrack(userID).enabled()
        }

        holder.getOpponentView().id = user.id
        holder.setUserId(userID)
        val peerConnection = session.getPeerConnection(userID)
        if (peerConnection != null) {
            val state = peerConnection.state
            holder.setStatus(context.resources.getString(SessionStatuses().getStatusDescriptionResource(state)))
        }
        if (position == opponents.size - 1) {
            adapterListener?.onBindLastViewHolder(holder, position)
        }
    }

    fun removeItem(index: Int) {
        opponents.removeAt(index)
        notifyItemRemoved(index)
        notifyItemRangeChanged(index, opponents.size)
    }

    fun getItem(position: Int): Int? {
        return opponents[position].id
    }

    fun getOpponents(): List<QBUser> {
        return opponents
    }

    fun add(item: QBUser) {
        opponents.add(item)
        notifyItemRangeChanged(opponents.size - 1, opponents.size)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal var toggleButton: ToggleButton = itemView.findViewById(R.id.opponent_toggle_mic)
        internal var opponentsName: TextView = itemView.findViewById(R.id.opponentName)
        internal var connectionStatus: TextView = itemView.findViewById(R.id.connectionStatus)
        internal var opponentView: QBConferenceSurfaceView = itemView.findViewById(R.id.opponentView)
        internal var progressBar: ProgressBar = itemView.findViewById(R.id.progress_bar_adapter)
        private var userId: Int = 0

        fun setStatus(status: String) {
            connectionStatus.text = status
        }

        fun getOpponentView(): QBConferenceSurfaceView {
            return opponentView
        }

        fun getProgressBar(): ProgressBar {
            return progressBar
        }

        fun getUserId(): Int {
            return userId
        }

        fun setUserId(userId: Int) {
            this.userId = userId
        }

        fun showOpponentView() {
            opponentView.visibility = View.VISIBLE
        }

        fun hideOpponentView() {
            opponentView.visibility = View.GONE
        }
    }

    private class SessionStatuses {

        @SuppressLint("UseSparseArrays")
        private val peerStateDescriptions = SparseArray<Int>()

        init {
            peerStateDescriptions.put(
                    QBRTCTypes.QBRTCConnectionState.QB_RTC_CONNECTION_NEW.ordinal, R.string.new_connection)
            peerStateDescriptions.put(
                    QBRTCTypes.QBRTCConnectionState.QB_RTC_CONNECTION_PENDING.ordinal, R.string.opponent_pending)
            peerStateDescriptions.put(
                    QBRTCTypes.QBRTCConnectionState.QB_RTC_CONNECTION_CONNECTING.ordinal, R.string.text_status_connect)
            peerStateDescriptions.put(
                    QBRTCTypes.QBRTCConnectionState.QB_RTC_CONNECTION_CHECKING.ordinal, R.string.text_status_checking)
            peerStateDescriptions.put(
                    QBRTCTypes.QBRTCConnectionState.QB_RTC_CONNECTION_CONNECTED.ordinal, R.string.text_status_connected)
            peerStateDescriptions.put(
                    QBRTCTypes.QBRTCConnectionState.QB_RTC_CONNECTION_DISCONNECTED.ordinal, R.string.text_status_disconnected)
            peerStateDescriptions.put(
                    QBRTCTypes.QBRTCConnectionState.QB_RTC_CONNECTION_CLOSED.ordinal, R.string.opponent_closed)
            peerStateDescriptions.put(
                    QBRTCTypes.QBRTCConnectionState.QB_RTC_CONNECTION_DISCONNECT_TIMEOUT.ordinal, R.string.text_status_disconnected)
            peerStateDescriptions.put(
                    QBRTCTypes.QBRTCConnectionState.QB_RTC_CONNECTION_NOT_ANSWER.ordinal, R.string.text_status_no_answer)
            peerStateDescriptions.put(
                    QBRTCTypes.QBRTCConnectionState.QB_RTC_CONNECTION_NOT_OFFER.ordinal, R.string.text_status_no_answer)
            peerStateDescriptions.put(
                    QBRTCTypes.QBRTCConnectionState.QB_RTC_CONNECTION_REJECT.ordinal, R.string.text_status_rejected)
            peerStateDescriptions.put(
                    QBRTCTypes.QBRTCConnectionState.QB_RTC_CONNECTION_HANG_UP.ordinal, R.string.text_status_hang_up)
        }

        fun getStatusDescriptionResource(connectionState: QBRTCTypes.QBRTCConnectionState): Int {
            return peerStateDescriptions.get(connectionState.ordinal)
        }
    }

    interface OnAdapterEventListener {
        fun onBindLastViewHolder(holder: ViewHolder, position: Int)

        fun onToggleButtonItemClick(position: Int, isChecked: Boolean)
    }
}