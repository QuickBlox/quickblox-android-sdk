package com.quickblox.sample.conference.kotlin.presentation.screens.chatinfo.adapter

import android.text.TextUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.quickblox.sample.conference.kotlin.R
import com.quickblox.sample.conference.kotlin.databinding.ItemUserBinding
import com.quickblox.sample.conference.kotlin.presentation.utils.AvatarUtils
import com.quickblox.users.model.QBUser
import java.util.*

/*
 * Created by Injoit in 2021-09-30.
 * Copyright © 2021 Quickblox. All rights reserved.
 */
class UsersViewHolder(private val binding: ItemUserBinding) : RecyclerView.ViewHolder(binding.root) {
    companion object {
        fun newInstance(parent: ViewGroup): UsersViewHolder {
            return UsersViewHolder(ItemUserBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        }
    }

    fun bind(qbUser: QBUser, currentUserId: Int?) {
        val name = if (TextUtils.isEmpty(qbUser.fullName)) {
            qbUser.login
        } else {
            qbUser.fullName
        }

        if (qbUser.id == currentUserId) {
            binding.tvName.text = binding.root.context.getString(R.string.username_you, name)
            binding.tvName.setTextColor(ContextCompat.getColor(binding.root.context, R.color.mediumGrey))
        } else {
            binding.tvName.text = name
        }

        binding.tvAvatar.text = qbUser.fullName.replace(" ", "").substring(0, 1).toUpperCase(Locale.getDefault())
        binding.ivAvatar.setImageDrawable(AvatarUtils.getDrawableAvatar(binding.root.context, qbUser.id.hashCode()))
    }
}