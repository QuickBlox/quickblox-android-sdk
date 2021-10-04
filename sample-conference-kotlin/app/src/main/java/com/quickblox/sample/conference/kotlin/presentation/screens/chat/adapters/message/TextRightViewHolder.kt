package com.quickblox.sample.conference.kotlin.presentation.screens.chat.adapters.message

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.quickblox.sample.conference.kotlin.R
import com.quickblox.sample.conference.kotlin.databinding.ItemInfoBinding
import com.quickblox.sample.conference.kotlin.databinding.ItemTextRightMessageBinding
import com.quickblox.sample.conference.kotlin.presentation.utils.getTime

/*
 * Created by Injoit in 2021-09-30.
 * Copyright © 2021 Quickblox. All rights reserved.
 */
class TextRightViewHolder(val binding: ItemTextRightMessageBinding) : RecyclerView.ViewHolder(binding.root) {
    private var itemInfoBinding: ItemInfoBinding = ItemInfoBinding.bind(binding.root)

    companion object {
        fun newInstance(parent: ViewGroup): TextRightViewHolder {
            return TextRightViewHolder(ItemTextRightMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        }
    }

    fun bind(qbChatMessage: ChatMessage) {
        itemInfoBinding.tvName.setText(R.string.you)
        itemInfoBinding.tvTime.text = qbChatMessage.qbChatMessage.dateSent.getTime()
        binding.tvMessageBody.text = qbChatMessage.qbChatMessage.body
    }
}