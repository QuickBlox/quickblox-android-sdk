package com.quickblox.sample.conference.kotlin.presentation.screens.chat.adapters.message

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.quickblox.chat.model.QBChatMessage
import com.quickblox.sample.conference.kotlin.R
import com.quickblox.sample.conference.kotlin.databinding.ItemHeaderMessageBinding
import java.text.SimpleDateFormat
import java.util.*

/*
 * Created by Injoit in 2021-09-30.
 * Copyright © 2021 Quickblox. All rights reserved.
 */
class HeaderViewHolder(val binding: ItemHeaderMessageBinding) : RecyclerView.ViewHolder(binding.root) {
    companion object {
        fun newInstance(parent: ViewGroup): HeaderViewHolder {
            return HeaderViewHolder(ItemHeaderMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        }
    }

    fun bind(chatMessage: ChatMessage) {
        binding.tvDate.text = convertDate(chatMessage.qbChatMessage)
    }

    private fun convertDate(chatMessage: QBChatMessage): String {
        val date: String
        val timeInMillis: Long = chatMessage.dateSent * 1000
        val msgTime = Calendar.getInstance()
        msgTime.timeInMillis = timeInMillis

        val now = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("d MMM", Locale.ENGLISH)
        val lastYearFormat = SimpleDateFormat("dd MMM, yyyy", Locale.ENGLISH)

        val sameYear = now[Calendar.YEAR] == msgTime[Calendar.YEAR]
        val sameMonth = now[Calendar.MONTH] == msgTime[Calendar.MONTH]
        val sameDay = now[Calendar.DATE] == msgTime[Calendar.DATE]
        val lastDay = now[Calendar.DAY_OF_YEAR] - msgTime[Calendar.DAY_OF_YEAR] == 1

        date = if (sameDay && sameMonth && sameYear) {
            binding.root.context.getString(R.string.today)
        } else if (lastDay and sameYear) {
            binding.root.context.getString(R.string.yesterday)
        } else if (sameYear) {
            dateFormat.format(Date(timeInMillis))
        } else {
            lastYearFormat.format(Date(timeInMillis))
        }
        return date
    }
}