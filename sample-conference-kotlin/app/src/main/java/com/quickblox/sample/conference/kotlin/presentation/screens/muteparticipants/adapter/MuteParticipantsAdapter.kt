package com.quickblox.sample.conference.kotlin.presentation.screens.muteparticipants.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.quickblox.sample.conference.kotlin.domain.call.entities.CallEntity

/*
 * Created by Injoit in 2021-09-30.
 * Copyright © 2021 Quickblox. All rights reserved.
 */
class MuteParticipantsAdapter(private val items: List<CallEntity>, private val currentUserId: Int?) : RecyclerView.Adapter<MuteParticipantsViewHolder>() {
    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MuteParticipantsViewHolder {
        return MuteParticipantsViewHolder.newInstance(parent)
    }

    override fun onBindViewHolder(holder: MuteParticipantsViewHolder, position: Int) {
        holder.bind(items[position], currentUserId)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun getItemId(position: Int): Long {
        return items[position].getUserId()?.toLong() ?: 0
    }
}