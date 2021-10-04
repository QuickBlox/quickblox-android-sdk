package com.quickblox.sample.conference.kotlin.presentation.screens.chat.adapters.message

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.quickblox.sample.conference.kotlin.databinding.ItemNotificationMessageBinding

/*
 * Created by Injoit in 2021-09-30.
 * Copyright © 2021 Quickblox. All rights reserved.
 */
class NotificationViewHolder(val binding: ItemNotificationMessageBinding) : RecyclerView.ViewHolder(binding.root) {
    companion object {
        fun newInstance(parent: ViewGroup): NotificationViewHolder {
            return NotificationViewHolder(ItemNotificationMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        }
    }

    fun bind(qbChatMessage: ChatMessage) {
        binding.tvMessageBody.text = qbChatMessage.qbChatMessage.body
    }
}