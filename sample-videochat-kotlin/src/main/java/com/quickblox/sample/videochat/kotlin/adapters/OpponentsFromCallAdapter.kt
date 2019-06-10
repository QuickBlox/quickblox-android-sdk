package com.quickblox.sample.videochat.kotlin.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.util.SparseIntArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.quickblox.sample.videochat.kotlin.R
import com.quickblox.sample.videochat.kotlin.fragments.BaseConversationFragment
import com.quickblox.users.model.QBUser
import com.quickblox.videochat.webrtc.QBRTCTypes
import com.quickblox.videochat.webrtc.view.QBRTCSurfaceView


class OpponentsFromCallAdapter(val context: Context,
                               val baseConversationFragment: BaseConversationFragment,
                               val users: List<QBUser>,
                               val width: Int,
                               val height: Int) : RecyclerView.Adapter<OpponentsFromCallAdapter.ViewHolder>() {

    private val TAG = OpponentsFromCallAdapter::class.java.simpleName

    private var _opponents: MutableList<QBUser> = users as MutableList<QBUser>
    val opponents: List<QBUser>
        get() = _opponents

    private var inflater: LayoutInflater = LayoutInflater.from(context)
    private var adapterListener: OnAdapterEventListener? = null

    fun setAdapterListener(adapterListener: OnAdapterEventListener) {
        this.adapterListener = adapterListener
    }

    override fun getItemCount(): Int {
        return _opponents.size
    }

    fun getItem(position: Int): Int {
        return _opponents[position].id
    }

    fun removeItem(index: Int) {
        _opponents.removeAt(index)
        notifyItemRemoved(index)
        notifyItemRangeChanged(index, _opponents.size)
    }

    fun replaceUsers(position: Int, qbUser: QBUser) {
        _opponents[position] = qbUser
        notifyItemChanged(position)
    }

    @SuppressLint("InflateParams")
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = inflater.inflate(R.layout.list_item_opponent_from_call, null)
        v.findViewById<RelativeLayout>(R.id.inner_layout).layoutParams = FrameLayout.LayoutParams(width, height)

        val vh = ViewHolder(v)
        vh.setListener(object : ViewHolder.ViewHolderClickListener {
            override fun onShowOpponent(callerId: Int) {
                adapterListener?.onItemClick(callerId)
            }
        })
        vh.showOpponentView(true)
        return vh
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = _opponents[position]
        val userID = user.id
        holder.opponentsName.text = user.fullName

        holder.getOpponentView().id = user.id
        holder.setUserId(userID)
        val state = baseConversationFragment.getConnectionState(userID)
        Log.d(TAG, "state ordinal= " + state?.ordinal)
        state?.let {
            holder.setStatus(context.getString(QBRTCSessionStatus().getStatusDescription(it)))
        }
        if (position == _opponents.size - 1) {
            adapterListener?.onBindLastViewHolder(holder, position)
        }
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        internal var opponentsName: TextView
        private var connectionStatus: TextView
        private var opponentView: QBRTCSurfaceView
        private var progressBar: ProgressBar
        private var userId: Int = 0
        private var viewHolderClickListener: ViewHolderClickListener? = null

        init {
            itemView.setOnClickListener(this)
            opponentsName = itemView.findViewById<View>(R.id.opponent_name) as TextView
            connectionStatus = itemView.findViewById<View>(R.id.connection_status) as TextView
            opponentView = itemView.findViewById<View>(R.id.opponent_view) as QBRTCSurfaceView
            progressBar = itemView.findViewById<View>(R.id.progress_bar_adapter) as ProgressBar
        }

        fun setListener(viewHolderClickListener: ViewHolderClickListener) {
            this.viewHolderClickListener = viewHolderClickListener
        }

        fun setStatus(status: String) {
            connectionStatus.text = status
        }

        fun setUserName(userName: String) {
            opponentsName.text = userName
        }

        fun setUserId(userId: Int) {
            this.userId = userId
        }

        fun getUserId(): Int {
            return userId
        }

        fun getProgressBar(): ProgressBar {
            return progressBar
        }

        fun getOpponentView(): QBRTCSurfaceView {
            return opponentView
        }

        fun showOpponentView(show: Boolean) {
            Log.d("OpponentsAdapter", "show? $show")
            opponentView.visibility = if (show) View.VISIBLE else View.GONE
        }

        override fun onClick(v: View) {
            viewHolderClickListener?.onShowOpponent(adapterPosition)
        }

        interface ViewHolderClickListener {
            fun onShowOpponent(callerId: Int)
        }
    }

    private class QBRTCSessionStatus {

        private val peerStateDescriptions = SparseIntArray()

        init {
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

        fun getStatusDescription(connectionState: QBRTCTypes.QBRTCConnectionState): Int {
            return peerStateDescriptions.get(connectionState.ordinal)
        }
    }

    interface OnAdapterEventListener {
        fun onBindLastViewHolder(holder: ViewHolder, position: Int)

        fun onItemClick(position: Int)
    }
}