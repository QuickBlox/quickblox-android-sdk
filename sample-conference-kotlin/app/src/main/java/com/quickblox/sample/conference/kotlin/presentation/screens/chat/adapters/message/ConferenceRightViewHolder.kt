package com.quickblox.sample.conference.kotlin.presentation.screens.chat.adapters.message

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.quickblox.sample.conference.kotlin.R
import com.quickblox.sample.conference.kotlin.databinding.ItemCoferenceRightMessageBinding
import com.quickblox.sample.conference.kotlin.databinding.ItemInfoBinding
import com.quickblox.sample.conference.kotlin.presentation.utils.getTime
import com.quickblox.sample.conference.kotlin.presentation.utils.setOnClick

/*
 * Created by Injoit in 2021-09-30.
 * Copyright © 2021 Quickblox. All rights reserved.
 */
class ConferenceRightViewHolder(val binding: ItemCoferenceRightMessageBinding, private val clickListener: JoinConferenceClickListener) : RecyclerView.ViewHolder(binding.root) {
    private var itemInfoBinding: ItemInfoBinding = ItemInfoBinding.bind(binding.root)

    companion object {
        fun newInstance(parent: ViewGroup, clickListener: JoinConferenceClickListener): ConferenceRightViewHolder {
            return ConferenceRightViewHolder(ItemCoferenceRightMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false), clickListener)
        }
    }

    fun bind(qbChatMessage: ChatMessage) {
        itemInfoBinding.tvName.setText(R.string.you)
        itemInfoBinding.tvTime.text = qbChatMessage.qbChatMessage.dateSent.getTime()
        binding.tvText.text = qbChatMessage.qbChatMessage.body
        binding.root.setOnClick {
            clickListener.clickJoin(qbChatMessage)
        }
    }
}