package com.quickblox.sample.conference.kotlin.presentation.screens.createchat.newchat

import android.view.ViewGroup
import androidx.collection.ArraySet
import androidx.recyclerview.widget.RecyclerView
import com.quickblox.users.model.QBUser

/*
 * Created by Injoit in 2021-09-30.
 * Copyright © 2021 Quickblox. All rights reserved.
 */
class UsersSearchAdapter(private val items:ArrayList<QBUser>, var usersAdapterListener: UsersAdapterListener, val selectedUsers: ArraySet<QBUser>) : RecyclerView.Adapter<UserSearchViewHolder>() {

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserSearchViewHolder {
        return UserSearchViewHolder.newInstance(parent, object : UserSearchViewHolder.ItemClickListener {
            override fun onSelect(user: QBUser) {
                if (selectedUsers.contains(user)) {
                    selectedUsers.remove(user)
                } else {
                    selectedUsers.add(user)
                }
                usersAdapterListener.onSelected()
            }
        })
    }

    override fun onBindViewHolder(holderSearch: UserSearchViewHolder, position: Int) {
        val isSelected = selectedUsers.contains(items[position])
        holderSearch.bind(items[position], isSelected)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun getItemId(position: Int): Long {
        return items[position].id.toLong()
    }

    interface UsersAdapterListener {
        fun onSelected()
    }
}