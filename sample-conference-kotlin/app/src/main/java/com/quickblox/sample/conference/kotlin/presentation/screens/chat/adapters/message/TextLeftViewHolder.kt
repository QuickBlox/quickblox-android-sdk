package com.quickblox.sample.conference.kotlin.presentation.screens.chat.adapters.message

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.quickblox.sample.conference.kotlin.databinding.ItemInfoBinding
import com.quickblox.sample.conference.kotlin.databinding.ItemTextLeftMessageBinding
import com.quickblox.sample.conference.kotlin.presentation.utils.AvatarUtils
import com.quickblox.sample.conference.kotlin.presentation.utils.getTime
import com.quickblox.users.model.QBUser
import java.util.*

/*
 * Created by Injoit in 2021-09-30.
 * Copyright © 2021 Quickblox. All rights reserved.
 */
class TextLeftViewHolder(val binding: ItemTextLeftMessageBinding) : RecyclerView.ViewHolder(binding.root) {
    private var itemInfoBinding: ItemInfoBinding = ItemInfoBinding.bind(binding.root)

    companion object {
        fun newInstance(parent: ViewGroup): TextLeftViewHolder {
            return TextLeftViewHolder(ItemTextLeftMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        }
    }

    fun bind(qbChatMessage: ChatMessage, qbUser: QBUser?) {
        itemInfoBinding.tvName.text = qbUser?.fullName
        itemInfoBinding.tvTime.text = qbChatMessage.qbChatMessage.dateSent.getTime()
        binding.tvMessageBody.text = qbChatMessage.qbChatMessage.body
        binding.ivAvatar.setImageDrawable(AvatarUtils.getDrawableAvatar(binding.root.context, qbChatMessage.qbChatMessage.senderId.hashCode()))
        binding.tvAvatar.text = qbUser?.fullName?.replace(" ", "")?.substring(0, 1)?.toUpperCase(Locale.getDefault())
    }
}