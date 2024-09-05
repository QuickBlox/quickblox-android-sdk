package com.quickblox.sample.conference.kotlin.presentation.screens.createchat.newchat

import android.text.TextUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.quickblox.sample.conference.kotlin.R
import com.quickblox.sample.conference.kotlin.databinding.ItemUserSearchBinding
import com.quickblox.sample.conference.kotlin.presentation.utils.AvatarUtils
import com.quickblox.users.model.QBUser
import java.util.*

/*
 * Created by Injoit in 2021-09-30.
 * Copyright © 2021 Quickblox. All rights reserved.
 */
class UserSearchViewHolder(private val binding: ItemUserSearchBinding, var itemClickListener: ItemClickListener) : RecyclerView.ViewHolder(binding.root) {
    var selected = false

    companion object {
        fun newInstance(parent: ViewGroup, itemClickListener: ItemClickListener): UserSearchViewHolder {
            return UserSearchViewHolder(ItemUserSearchBinding.inflate(LayoutInflater.from(parent.context), parent, false), itemClickListener)
        }
    }

    fun bind(qbUser: QBUser, selected: Boolean) {
        this.selected = selected
        fillAvatar(qbUser)
        binding.tvName.text = if (TextUtils.isEmpty(qbUser.fullName)) {
            qbUser.login
        } else {
            qbUser.fullName
        }
        if (this.selected) {
            binding.checkbox.isChecked = true
            binding.root.setBackgroundColor(ContextCompat.getColor(binding.root.context, R.color.selectedItemDialog))
        } else {
            binding.checkbox.isChecked = false
            binding.root.setBackgroundColor(ContextCompat.getColor(binding.root.context, android.R.color.transparent))
        }

        binding.root.setOnClickListener {
            if (this.selected) {
                binding.root.setBackgroundColor(ContextCompat.getColor(binding.root.context, android.R.color.transparent))
                binding.checkbox.isChecked = false
            } else {
                binding.root.setBackgroundColor(ContextCompat.getColor(binding.root.context, R.color.selectedItemDialog))
                binding.checkbox.isChecked = true
            }
            this.selected = !this.selected
            itemClickListener.onSelect(qbUser)
        }
    }

    private fun fillAvatar(user: QBUser) {
       val userName = if (TextUtils.isEmpty(user.fullName)) {
            user.login
        } else {
            user.fullName
        }
        binding.tvAvatar.text = userName.replace(" ", "").substring(0, 1).uppercase(Locale.getDefault())
        binding.ivAvatar.setImageDrawable(AvatarUtils.getDrawableAvatar(binding.root.context, user.id.hashCode()))
    }

    interface ItemClickListener {
        fun onSelect(user: QBUser)
    }
}