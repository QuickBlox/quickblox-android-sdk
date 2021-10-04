package com.quickblox.sample.conference.kotlin.presentation.screens.settings.views

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.SeekBar
import androidx.constraintlayout.widget.ConstraintLayout
import com.quickblox.sample.conference.kotlin.R
import com.quickblox.sample.conference.kotlin.databinding.ItemCustomSeekbarBinding

/*
 * Created by Injoit in 2021-09-30.
 * Copyright © 2021 Quickblox. All rights reserved.
 */
class CustomSeekBar : ConstraintLayout {
    private lateinit var binding: ItemCustomSeekbarBinding
    private var seekBarCAllBack: SeekBarCAllBack? = null
    private var textTitle: String?
        get() = binding.tvTitle.text?.toString()
        set(text) {
            binding.tvTitle.text = text
        }

    constructor(context: Context) : super(context) {
        init(context, null)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context, attrs)
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        val rootView: View = FrameLayout.inflate(context, R.layout.item_custom_seekbar, this)
        binding = ItemCustomSeekbarBinding.bind(rootView)
        if (attrs != null) {
            val attributes = context.obtainStyledAttributes(attrs, R.styleable.CustomSeekBar, 0, 0)
            try {
                proceedAttributes(attributes)
            } finally {
                attributes.recycle()
            }
        }
    }

    private fun proceedAttributes(attributes: TypedArray) {
        val min = attributes.getInt(R.styleable.CustomSeekBar_min, 0)
        val max = attributes.getInt(R.styleable.CustomSeekBar_max, 0)

        binding.tvMinValue.text = min.toString()
        binding.tvMaxValue.text = max.toString()

        val step = attributes.getInt(R.styleable.CustomSeekBar_step, 1)
        textTitle = attributes.getString(R.styleable.CustomSeekBar_title)
        binding.seekbar.max = max

        binding.seekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val newProgress = (progress / step) * step
                seekBar?.progress = newProgress
                binding.tvValue.text = newProgress.toString()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // empty
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                seekBarCAllBack?.changedValue(seekBar?.progress)
            }
        })
    }

    fun setSeekBarValue(value: Int?) {
        value?.let {
            binding.seekbar.progress = value
        }
    }

    fun setCallBack(seekBarCAllBack: SeekBarCAllBack) {
        this.seekBarCAllBack = seekBarCAllBack
    }

    interface SeekBarCAllBack {
        fun changedValue(value: Int?)
    }
}