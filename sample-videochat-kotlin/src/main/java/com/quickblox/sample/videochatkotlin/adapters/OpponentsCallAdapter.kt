package com.quickblox.sample.videochatkotlin.adapters

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.quickblox.sample.videochatkotlin.R
import com.quickblox.sample.videochatkotlin.utils.ChatHelper
import com.quickblox.users.model.QBUser
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.list_item_opponent_from_call.*

class OpponentsCallAdapter(context: Context, users: ArrayList<QBUser>, width: Int, height: Int) : RecyclerView.Adapter<OpponentsCallAdapter.ViewHolder>() {
    private val TAG = OpponentsCallAdapter::class.java.simpleName
    var inflater: LayoutInflater = LayoutInflater.from(context)
    var opponents: ArrayList<QBUser> = users
    var currentUserId: Int = 0
    var itemHeight: Int = 0
    var itemWidth: Int = 0

    init {
        currentUserId = ChatHelper.instance.currentUser.id
        itemWidth = width
        itemHeight = height
        Log.d(TAG, "item width=$itemWidth, item height=$itemHeight")
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = inflater.inflate(R.layout.list_item_opponent_from_call, null)
        val vh = ViewHolder(view)
        initCellHeight(vh)
        return vh
    }

    fun initCellHeight(holder: ViewHolder, height: Int = itemHeight) {
        val params = holder.item_layout.layoutParams
        params.height = height
        holder.item_layout.layoutParams = params
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
        holder.opponent_name.id = user.id
        holder.userId = userID
        holder.opponent_name.text = user.fullName ?: user.login
    }

    class ViewHolder(override val containerView: View) : RecyclerView.ViewHolder(containerView), LayoutContainer {
        var userId: Int = 0
    }
}