package com.quickblox.sample.conference.kotlin.presentation.screens.chat.adapters.message

import android.graphics.Canvas
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

/*
 * Created by Injoit in 2021-09-30.
 * Copyright © 2021 Quickblox. All rights reserved.
 */
class HeaderDecoration(listener: HeaderInterface) : RecyclerView.ItemDecoration() {
    private var listener: HeaderInterface? = listener
    private var stickyHeaderHeight = 0

    override fun onDrawOver(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDrawOver(canvas, parent, state)
        val topChild = parent.getChildAt(0) ?: return
        val topChildPosition = parent.getChildAdapterPosition(topChild)

        if (topChildPosition == RecyclerView.NO_POSITION) {
            return
        }

        val headerPos = listener?.getHeaderPosition(topChildPosition)
        val currentHeader = getHeaderViewForItem(headerPos, parent)
        fixLayoutSize(parent, currentHeader)
        val contactPoint = currentHeader?.bottom
        val childInContact = contactPoint?.let { getChildInContact(parent, it, headerPos) }
        val isHeader = childInContact?.let { parent.getChildAdapterPosition(it) }?.let { listener?.isHeader(it) }
        // TODO: 6/17/21 Refactor
        isHeader?.let {
            if (it) {
                if (topChildPosition == 0) {
                    return
                }
                moveHeader(canvas, currentHeader, childInContact)
                return
            }
        }

        // TODO: 6/25/21 Need to fix this logic.
        if ((parent.layoutManager as LinearLayoutManager).findFirstCompletelyVisibleItemPosition() == 0) {
            return
        }
        drawHeader(canvas, currentHeader)
    }

    private fun getHeaderViewForItem(headerPosition: Int?, parent: RecyclerView): View? {
        val layoutResId = listener?.getHeaderLayout(headerPosition)
        val header = layoutResId?.let { LayoutInflater.from(parent.context).inflate(it, parent, false) }
        headerPosition?.let { listener?.bind(header, it) }
        return header
    }

    private fun drawHeader(canvas: Canvas, header: View?) {
        canvas.save()
        canvas.translate(0f, 0f)
        header?.draw(canvas)
        canvas.restore()
    }

    private fun moveHeader(canvas: Canvas, currentHeader: View?, nextHeader: View) {
        canvas.save()
        canvas.translate(0f, (nextHeader.top - (currentHeader?.height ?: 0)).toFloat())
        currentHeader?.draw(canvas)
        canvas.restore()
    }

    private fun getChildInContact(parent: RecyclerView, contactPoint: Int, currentHeaderPos: Int?): View? {
        var childInContact: View? = null
        for (index in 0 until parent.childCount) {
            var heightTolerance = 0
            val child = parent.getChildAt(index)

            if (currentHeaderPos != index) {
                val isChildHeader = listener?.isHeader(parent.getChildAdapterPosition(child))
                isChildHeader?.let { isHeader ->
                    if (isHeader) {
                        heightTolerance = stickyHeaderHeight - child.height
                    }
                }
            }

            val childBottomPosition = if (child.top > 0) {
                child.bottom + heightTolerance
            } else {
                child.bottom
            }
            if (childBottomPosition > contactPoint) {
                if (child.top <= contactPoint) {
                    childInContact = child
                    break
                }
            }
        }
        return childInContact
    }

    private fun fixLayoutSize(parent: ViewGroup, view: View?) {
        val widthSpec = View.MeasureSpec.makeMeasureSpec(parent.width, View.MeasureSpec.EXACTLY)
        val heightSpec = View.MeasureSpec.makeMeasureSpec(parent.height, View.MeasureSpec.UNSPECIFIED)

        val childWidthSpec = view?.layoutParams?.width?.let {
            ViewGroup.getChildMeasureSpec(widthSpec, parent.paddingLeft + parent.paddingRight, it)
        }
        val childHeightSpec = view?.layoutParams?.height?.let {
            ViewGroup.getChildMeasureSpec(heightSpec, parent.paddingTop + parent.paddingBottom, it)
        }

        childWidthSpec?.let { childHeightSpec?.let { it1 -> view.measure(it, it1) } }
        view?.layout(0, 0, view.measuredWidth, view.measuredHeight.also {
            stickyHeaderHeight = it
        })
    }

    interface HeaderInterface {
        fun getHeaderPosition(itemPosition: Int): Int
        fun getHeaderLayout(headerPosition: Int?): Int
        fun bind(header: View?, headerPosition: Int)
        fun isHeader(itemPosition: Int): Boolean
    }
}