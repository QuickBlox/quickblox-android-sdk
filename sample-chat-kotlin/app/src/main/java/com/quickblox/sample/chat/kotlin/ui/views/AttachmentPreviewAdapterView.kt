package com.quickblox.sample.chat.kotlin.ui.views

import android.content.Context
import android.database.DataSetObserver
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.Adapter
import android.widget.HorizontalScrollView
import android.widget.LinearLayout

class AttachmentPreviewAdapterView(context: Context, attrs: AttributeSet) : HorizontalScrollView(context, attrs) {
    private val mainThreadHandler = Handler(Looper.getMainLooper())

    private val container: LinearLayout = LinearLayout(context)
    private var adapter: Adapter? = null
    private val dataSetObserver: DataSetObserver

    init {
        container.orientation = LinearLayout.HORIZONTAL
        val width = ViewGroup.LayoutParams.MATCH_PARENT
        val height = ViewGroup.LayoutParams.MATCH_PARENT
        val layoutParams = LayoutParams(width, height)
        addView(container, layoutParams)
        dataSetObserver = object : DataSetObserver() {
            override fun onChanged() {
                populateWithViewsFromAdapter()
            }

            override fun onInvalidated() {
                populateWithViewsFromAdapter()
            }
        }
    }

    fun setAdapter(newAdapter: Adapter) {
        adapter?.unregisterDataSetObserver(dataSetObserver)
        adapter = newAdapter
        adapter?.registerDataSetObserver(dataSetObserver)
        populateWithViewsFromAdapter()
    }

    private fun populateWithViewsFromAdapter() {
        mainThreadHandler.post {
            container.removeAllViews()
            adapter?.let {
                for (index in 0 until it.count) {
                    val childView = it.getView(index, null, this)
                    container.addView(childView, index)
                }
            }
        }
    }
}