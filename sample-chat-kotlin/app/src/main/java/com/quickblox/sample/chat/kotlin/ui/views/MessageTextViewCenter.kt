package com.quickblox.sample.chat.kotlin.ui.views

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.quickblox.sample.chat.kotlin.R

class MessageTextViewCenter(context: Context, attrs: AttributeSet) : MessageTextView(context, attrs) {

    override fun setLinearSide() {
        val layoutParams = frameLinear.layoutParams as FrameLayout.LayoutParams
        layoutParams.gravity = Gravity.CENTER
        frameLinear.layoutParams = layoutParams
    }

    override fun setTextLayout() {
        viewTextStub.layoutResource = R.layout.widget_notification_msg_center
        layoutStub = viewTextStub.inflate() as LinearLayout
    }
}