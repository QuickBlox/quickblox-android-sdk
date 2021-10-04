package com.quickblox.sample.conference.kotlin.presentation.screens.chatinfo.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.quickblox.users.model.QBUser

/*
 * Created by Injoit in 2021-09-30.
 * Copyright © 2021 Quickblox. All rights reserved.
 */
class UsersAdapter(private val items: List<QBUser>, private val currentUserId: Int?) : RecyclerView.Adapter<UsersViewHolder>() {
    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsersViewHolder {
        return UsersViewHolder.newInstance(parent)
    }

    override fun onBindViewHolder(holder: UsersViewHolder, position: Int) {
        holder.bind(items[position], currentUserId)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun getItemId(position: Int): Long {
        return items[position].id.toLong()
    }
}