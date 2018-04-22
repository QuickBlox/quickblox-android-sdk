package com.quickblox.sample.videochatkotlin.adapters

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.RelativeLayout
import com.quickblox.sample.videochatkotlin.R
import com.quickblox.users.model.QBUser
import com.quickblox.videochat.webrtc.view.QBRTCSurfaceView

class OpponentsCallAdapter(context: Context, users: ArrayList<QBUser>, width: Int, height: Int) : RecyclerView.Adapter<OpponentsCallAdapter.ViewHolder>() {
    private val TAG = OpponentsCallAdapter::class.java.simpleName
    lateinit var inflater: LayoutInflater
    lateinit var opponents: ArrayList<QBUser>
    var itemHeight: Int = 0
    var itemWidth: Int = 0

    init {
        this.opponents = users
        this.inflater = LayoutInflater.from(context)
        itemWidth = width
        itemHeight = height
        Log.d(TAG, "item width=$itemWidth, item height=$itemHeight")
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
        Log.d(TAG, "AMBRA7 onCreateViewHolder")
        val view = inflater.inflate(R.layout.list_item_opponent_from_call, null)
        val vh = ViewHolder(view)
        initCellHeight(vh)
        vh.showOpponentView(true)
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

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = opponents[position]
        Log.d(TAG, "AMBRA onBindViewHolder user.id= " + user.id)
        val userID = user.id

        holder.opponentView.id = user.id
        holder.userId = userID
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var itemLayout: RelativeLayout
        var opponentView: QBRTCSurfaceView
        var progressBar: ProgressBar
        var userId: Int = 0

        init {
            itemLayout = itemView.findViewById(R.id.itemLayout)
            opponentView = itemView.findViewById(R.id.opponentView)
            progressBar = itemView.findViewById(R.id.progress_bar_adapter)
        }

        fun showOpponentView(show: Boolean) {
            Log.d("OpponentsAdapter", "AMBRA show? $show")
            opponentView.visibility = if (show) View.VISIBLE else View.GONE
        }
    }
}