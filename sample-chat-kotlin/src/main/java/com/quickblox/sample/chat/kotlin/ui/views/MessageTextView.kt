package com.quickblox.sample.chat.kotlin.ui.views

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewStub
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.annotation.LayoutRes
import com.quickblox.sample.chat.kotlin.R


abstract class MessageTextView(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {
    private val TAG = MessageTextView::class.java.simpleName

    protected lateinit var frameLinear: LinearLayout
    protected lateinit var viewTextStub: ViewStub
    private lateinit var inflater: LayoutInflater

    protected var layoutStub: LinearLayout? = null

    init {
        init(R.layout.widget_text_msg_frame)
        applyAttributes(attrs)
    }

    private fun init(@LayoutRes layoutId: Int) {
        inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        inflater.inflate(layoutId, this, true)
        frameLinear = rootView.findViewById(R.id.msg_linear_frame)
        viewTextStub = findViewById(R.id.msg_stub_message)
    }

    @SuppressLint("CustomViewStyleable")
    private fun applyAttributes(attrs: AttributeSet) {
        var array: TypedArray? = null
        val widgetIdBottom: Int
        val widgetIdTop: Int
        val widgetIdLinkPreview: Int

        try {
            array = context.obtainStyledAttributes(attrs, R.styleable.MessageTextView)
            widgetIdBottom = array.getResourceId(R.styleable.MessageTextView_widget_id_bottom, 0)
            widgetIdTop = array.getResourceId(R.styleable.MessageTextView_widget_id_top, 0)
            widgetIdLinkPreview = array.getResourceId(R.styleable.MessageTextView_widget_id_link_preview, R.layout.widget_link_preview)
        } finally {
            array?.recycle()
        }

        setLinearSide()
        setTextLayout()
        setCustomWidgets(widgetIdBottom, widgetIdTop)
        setLinkPreviewWidget(widgetIdLinkPreview)
    }

    private fun setCustomWidgets(@LayoutRes widgetIdBottom: Int, @LayoutRes widgetIdTop: Int) {
        if (widgetIdBottom != 0 && findViewById<View>(R.id.msg_custom_widget_frame_bottom) != null) {
            val widgetFrameBottom = findViewById<View>(R.id.msg_custom_widget_frame_bottom) as ViewGroup
            val customViewBottom = inflater.inflate(widgetIdBottom, widgetFrameBottom)
            Log.d(TAG, "customViewBottom = null ? " + (customViewBottom == null))
        }
        if (widgetIdTop != 0 && findViewById<View>(R.id.msg_custom_widget_frame_top) != null) {
            val widgetFrameTop = findViewById<View>(R.id.msg_custom_widget_frame_top) as ViewGroup
            val customViewTop = inflater.inflate(widgetIdTop, widgetFrameTop)
            Log.d(TAG, "customViewTop = null ? " + (customViewTop == null))
        }
    }

    private fun setLinkPreviewWidget(@LayoutRes linkPreviewWidget: Int) {
        if (linkPreviewWidget != 0 && findViewById<View>(R.id.msg_link_preview) != null) {
            val widgetFrameLinkPreview = findViewById<View>(R.id.msg_link_preview) as ViewGroup
            val linkPreview = inflater.inflate(linkPreviewWidget, widgetFrameLinkPreview)
            Log.d(TAG, "linkPreview = null ? " + (linkPreview == null))
        }
    }

    protected abstract fun setLinearSide()

    protected abstract fun setTextLayout()
}