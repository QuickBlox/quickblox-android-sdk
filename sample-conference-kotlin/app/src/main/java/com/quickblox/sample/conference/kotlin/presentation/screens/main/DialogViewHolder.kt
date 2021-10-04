package com.quickblox.sample.conference.kotlin.presentation.screens.main

import android.text.TextUtils
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.quickblox.chat.model.QBChatDialog
import com.quickblox.sample.conference.kotlin.R
import com.quickblox.sample.conference.kotlin.databinding.ItemDialogBinding
import com.quickblox.sample.conference.kotlin.presentation.screens.main.DialogsAdapter.DialogsAdapterStates.Companion.DEFAULT
import com.quickblox.sample.conference.kotlin.presentation.screens.main.DialogsAdapter.DialogsAdapterStates.Companion.SELECT
import com.quickblox.sample.conference.kotlin.presentation.utils.AvatarUtils
import java.text.SimpleDateFormat
import java.util.*

/*
 * Created by Injoit in 2021-09-30.
 * Copyright © 2021 Quickblox. All rights reserved.
 */
class DialogViewHolder(private val binding: ItemDialogBinding, var itemListener: ItemListener) : RecyclerView.ViewHolder(binding.root) {
    private var selected = false

    companion object {
        fun newInstance(parent: ViewGroup, itemListener: ItemListener): DialogViewHolder {
            return DialogViewHolder(
                    ItemDialogBinding.inflate(LayoutInflater.from(parent.context), parent, false), itemListener)
        }
    }

    fun bind(qbChatDialog: QBChatDialog, position: Int, adapterState: Int, selected: Boolean) {
        this.selected = selected
        changeState(adapterState, qbChatDialog)

        binding.tvDialogName.text = qbChatDialog.name
        binding.tvLastMessage.text = getLastMessage(qbChatDialog)
        binding.tvTime.text = getDialogLastMessageTime(qbChatDialog.lastMessageDateSent)

        fillAvatar(qbChatDialog, position)

        if (this.selected) {
            binding.checkbox.isChecked = true
            binding.root.setBackgroundColor(ContextCompat.getColor(binding.root.context, R.color.selectedItemDialog))
        } else {
            binding.checkbox.isChecked = false
            val outValue = TypedValue()
            binding.root.context.theme.resolveAttribute(android.R.attr.selectableItemBackground, outValue, true)
            binding.root.setBackgroundResource(outValue.resourceId)
        }

        binding.checkbox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.root.setBackgroundColor(ContextCompat.getColor(binding.root.context, R.color.selectedItemDialog))
            } else {
                binding.root.setBackgroundColor(ContextCompat.getColor(binding.root.context, android.R.color.transparent))
            }
        }
    }

    private fun changeState(adapterState: Int, qbChatDialog: QBChatDialog) {
        when (adapterState) {
            DEFAULT -> {
                binding.tvTime.visibility = View.VISIBLE
                binding.checkbox.visibility = View.INVISIBLE

                val counter = qbChatDialog.unreadMessageCount ?: 0
                if (counter > 0) {
                    binding.tvCounter.visibility = View.VISIBLE
                    binding.tvCounter.text = counter.toString()
                } else {
                    binding.tvCounter.visibility = View.GONE
                }

                binding.root.setOnClickListener {
                    itemListener.onClick(qbChatDialog)
                }

                binding.root.setOnLongClickListener {
                    binding.checkbox.isChecked = true
                    binding.root.setBackgroundColor(ContextCompat.getColor(binding.root.context, R.color.selectedItemDialog))
                    itemListener.onLongClick(qbChatDialog)
                    return@setOnLongClickListener true
                }
            }
            SELECT -> {
                binding.tvCounter.visibility = View.GONE
                binding.tvTime.visibility = View.GONE
                binding.checkbox.visibility = View.VISIBLE

                binding.root.setOnClickListener {
                    if (selected) {
                        binding.root.setBackgroundColor(ContextCompat.getColor(binding.root.context, android.R.color.transparent))
                        binding.checkbox.isChecked = false
                    } else {
                        binding.root.setBackgroundColor(ContextCompat.getColor(binding.root.context, R.color.selectedItemDialog))
                        binding.checkbox.isChecked = true
                    }
                    selected = !selected
                    itemListener.onSelect(qbChatDialog)
                }
                binding.root.setOnLongClickListener(null)
            }
        }
    }

    private fun fillAvatar(qbChatDialog: QBChatDialog, position: Int) {
        binding.tvAvatar.text = qbChatDialog.name.replace(" ", "").substring(0, 1).toUpperCase(Locale.getDefault())
        binding.ivAvatar.setImageDrawable(AvatarUtils.getDrawableAvatar(binding.root.context, position))
    }

    private fun getLastMessage(qbChatDialog: QBChatDialog): String {
        return if (TextUtils.isEmpty(qbChatDialog.lastMessage) && qbChatDialog.lastMessageUserId != null) {
            binding.root.context.getString(R.string.chat_attachment)
        } else {
            qbChatDialog.lastMessage ?: ""
        }
    }

    private fun getDialogLastMessageTime(seconds: Long): String {
        var messageTime = ""

        if (seconds == 0L) {
            return messageTime
        }
        val timeFormatToday = SimpleDateFormat("HH:mm", Locale.ENGLISH)
        val dateFormatThisYear = SimpleDateFormat("d MMM", Locale.ENGLISH)
        val lastYearFormat = SimpleDateFormat("dd.MM.yy", Locale.ENGLISH)

        val msgTime = Calendar.getInstance()
        val timeInMillis = seconds * 1000
        msgTime.timeInMillis = timeInMillis

        val nowTime = Calendar.getInstance()

        val sameYear = nowTime[Calendar.YEAR] == msgTime[Calendar.YEAR]
        val sameMonth = nowTime[Calendar.MONTH] == msgTime[Calendar.MONTH]
        val sameDay = nowTime[Calendar.DATE] == msgTime[Calendar.DATE]
        val today = sameDay && sameMonth && sameYear

        val yesterday =
                nowTime[Calendar.DAY_OF_YEAR] - msgTime[Calendar.DAY_OF_YEAR] == 1 && sameYear

        messageTime = when {
            today -> {
                timeFormatToday.format(Date(timeInMillis))
            }
            yesterday -> {
                binding.root.context.getString(R.string.yesterday)
            }
            sameYear -> {
                dateFormatThisYear.format(Date(timeInMillis))
            }
            else -> {
                lastYearFormat.format(Date(timeInMillis))
            }
        }
        return messageTime
    }

    interface ItemListener {
        fun onClick(dialog: QBChatDialog)
        fun onLongClick(dialog: QBChatDialog)
        fun onSelect(dialog: QBChatDialog)
    }
}