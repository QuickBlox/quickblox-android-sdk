package com.quickblox.sample.chat.kotlin.ui.adapter

import android.content.Context
import android.util.AttributeSet
import android.widget.ScrollView


class ScrollViewWithMaxHeight : ScrollView {

    companion object {
        const val WITHOUT_MAX_HEIGHT = -1
    }

    private var maxHeight = WITHOUT_MAX_HEIGHT

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var heightMeasure = heightMeasureSpec
        try {
            var heightSize = MeasureSpec.getSize(heightMeasure)
            if (maxHeight != WITHOUT_MAX_HEIGHT && heightSize > maxHeight) {
                heightSize = maxHeight
            }
            heightMeasure = MeasureSpec.makeMeasureSpec(heightSize, MeasureSpec.AT_MOST)
            layoutParams.height = heightSize
        } catch (e: Exception) {

        } finally {
            super.onMeasure(widthMeasureSpec, heightMeasure)
        }
    }

    fun setMaxHeight(maxHeight: Int) {
        this.maxHeight = maxHeight
    }
}