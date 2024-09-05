package com.quickblox.sample.videochat.kotlin.adapters

import android.content.Context
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.quickblox.sample.videochat.kotlin.R

class AudioCallAdapter(context: Context?, private var usersList: List<ReconnectingUserModel>) :
    RecyclerView.Adapter<AudioCallAdapter.ViewHolder>() {
    private val inflater: LayoutInflater

    init {
        inflater = LayoutInflater.from(context)
    }

    fun updateList(usersList: List<ReconnectingUserModel>) {
        this.usersList = usersList
        notifyDataSetChanged()
    }

    fun getItemByUserId(userId: Int): ReconnectingUserModel? {
        for (item in usersList) {
            if (item.getUser().id == userId) {
                return item
            }
        }
        return null
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = inflater.inflate(R.layout.audio_call_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = usersList[position].getUser()
        val name: String
        name = if (TextUtils.isEmpty(user.fullName)) {
            user.login
        } else {
            user.fullName
        }
        holder.setName(name)
        if (!TextUtils.isEmpty(usersList[position].getReconnectingState())) {
            holder.setStatus(usersList[position].getReconnectingState())
        }
    }

    override fun getItemCount(): Int {
        return usersList.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameView: TextView
        private val statusView: TextView

        init {
            nameView = itemView.findViewById<View>(R.id.name) as TextView
            statusView = itemView.findViewById<View>(R.id.status) as TextView
        }

        fun setStatus(status: String?) {
            statusView.text = status
        }

        fun setName(name: String?) {
            nameView.text = name
        }
    }
}