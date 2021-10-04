package com.quickblox.sample.conference.kotlin.presentation.screens.call

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import com.quickblox.sample.conference.kotlin.R
import com.quickblox.sample.conference.kotlin.databinding.ItemConferenceBinding
import com.quickblox.sample.conference.kotlin.domain.call.entities.CallEntity

/*
 * Created by Injoit in 2021-09-30.
 * Copyright © 2021 Quickblox. All rights reserved.
 */
class ConversationItem : FrameLayout {
    private lateinit var binding: ItemConferenceBinding
    private var listener: ConversationItemListener? = null

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context)
    }

    private fun init(context: Context) {
        val rootView: View = inflate(context, R.layout.item_conference, this)
        binding = ItemConferenceBinding.bind(rootView)
    }

    fun setClickListener(listener: ConversationItemListener) {
        this.listener = listener
    }

    fun setView(callEntity: CallEntity) {
        try {
            callEntity.addViewRender(binding.opponentView)
            binding.opponentName.text = callEntity.getUserName()
            binding.root.setOnClickListener {
                listener?.onItemClick(callEntity)
            }
            if (callEntity.isLocalEntity()) {
                return
            }
            if (callEntity.isEnableAudioTrack() == true) {
                binding.ivMutedIndicator.visibility = GONE
            } else {
                binding.ivMutedIndicator.visibility = VISIBLE
            }
        } catch (e: Exception) {
            Log.e("Exception", "${e.message}")
        }
    }

    interface ConversationItemListener {
        fun onItemClick(callEntity: CallEntity)
    }
}