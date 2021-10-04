package com.quickblox.sample.conference.kotlin.presentation.screens.settings.views.selectionlist

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import androidx.constraintlayout.widget.ConstraintLayout
import com.quickblox.sample.conference.kotlin.R
import com.quickblox.sample.conference.kotlin.databinding.ItemCustomSelectionBinding

/*
 * Created by Injoit in 2021-09-30.
 * Copyright © 2021 Quickblox. All rights reserved.
 */
class CustomSelectionView : ConstraintLayout {
    private lateinit var binding: ItemCustomSelectionBinding
    private var selectionAdapter: SelectionAdapter? = null
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
        val rootView: View = FrameLayout.inflate(context, R.layout.item_custom_selection, this)
        binding = ItemCustomSelectionBinding.bind(rootView)
        if (attrs != null) {
            val attributes = context.obtainStyledAttributes(attrs, R.styleable.CustomSelectionView, 0, 0)
            try {
                proceedAttributes(attributes)
            } finally {
                attributes.recycle()
            }
        }
    }

    private fun proceedAttributes(attributes: TypedArray) {
        textTitle = attributes.getString(R.styleable.CustomSelectionView_title)
    }

    fun setList(items: List<Pair<String, Int>>, selectedResolution: Int?) {
        selectionAdapter = SelectionAdapter(items, selectedResolution)
        binding.rvVideoFormats.adapter = selectionAdapter
    }

    fun setCallBack(selectionCallBack: SelectionCallBack) {
        selectionAdapter?.setCallBack(selectionCallBack)
    }

    interface SelectionCallBack {
        fun changedValue(value: Int?)
    }
}