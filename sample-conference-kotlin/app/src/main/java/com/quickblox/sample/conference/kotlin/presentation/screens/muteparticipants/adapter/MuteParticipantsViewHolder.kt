package com.quickblox.sample.conference.kotlin.presentation.screens.muteparticipants.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.quickblox.sample.conference.kotlin.R
import com.quickblox.sample.conference.kotlin.databinding.ItemMuteParticipantBinding
import com.quickblox.sample.conference.kotlin.domain.call.entities.CallEntity
import com.quickblox.sample.conference.kotlin.presentation.utils.AvatarUtils
import java.util.*

/*
 * Created by Injoit in 2021-09-30.
 * Copyright © 2021 Quickblox. All rights reserved.
 */
class MuteParticipantsViewHolder(private val binding: ItemMuteParticipantBinding) : RecyclerView.ViewHolder(binding.root) {
    companion object {
        fun newInstance(parent: ViewGroup): MuteParticipantsViewHolder {
            return MuteParticipantsViewHolder(ItemMuteParticipantBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        }
    }

    fun bind(callEntity: CallEntity?, currentUserId: Int?) {
        val enabled = callEntity?.isEnableAudioTrack()

        if (callEntity?.getUserId() == currentUserId) {
            binding.tvName.text = binding.root.context.getString(R.string.username_you, callEntity?.getUserName())
            binding.tvName.setTextColor(ContextCompat.getColor(binding.root.context, R.color.mediumGrey))
            binding.cbMute.isChecked = true
        } else {
            enabled?.let {
                binding.cbMute.isChecked = enabled
            }

            binding.tvName.text = callEntity?.getUserName()
            binding.root.setOnClickListener {
                val mute = binding.cbMute.isChecked
                binding.cbMute.isChecked = !mute
                callEntity?.setEnabledAudioTrack(!mute)
            }
        }

        binding.tvAvatar.text = callEntity?.getUserName()?.replace(" ", "")?.substring(0, 1)?.toUpperCase(Locale.getDefault())
        binding.ivAvatar.setImageDrawable(AvatarUtils.getDrawableAvatar(binding.root.context, callEntity?.getUserId().hashCode()))
    }
}