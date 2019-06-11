package com.quickblox.sample.chat.java.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.LinearLayout;

import com.quickblox.sample.chat.java.R;


public class MessageTextViewCenter extends MessageTextView {

    public MessageTextViewCenter(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void setLinearSide() {
        LayoutParams layoutParams = (LayoutParams) frameLinear.getLayoutParams();
        layoutParams.gravity = Gravity.CENTER;
        frameLinear.setLayoutParams(layoutParams);
    }

    @Override
    protected void setTextLayout() {
        viewTextStub.setLayoutResource(R.layout.widget_notification_msg_center);
        layoutStub = (LinearLayout) viewTextStub.inflate();
    }
}
