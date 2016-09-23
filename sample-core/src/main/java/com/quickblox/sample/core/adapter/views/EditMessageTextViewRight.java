package com.quickblox.sample.core.adapter.views;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.quickblox.sample.core.R;

public class EditMessageTextViewRight extends EditMessageTextView {

    public EditMessageTextViewRight(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

 @Override
    protected void setLinearSide() {
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) frameLinear.getLayoutParams();
        layoutParams.gravity = Gravity.RIGHT;
        frameLinear.setLayoutParams(layoutParams);

    }

    @Override
    protected void setTextLayout(@LayoutRes int customTextId) {
        int textViewId = R.layout.item_stub_right_text;

        if (customTextId != 0) {
            textViewId = customTextId;
        }
        viewTextStub.setLayoutResource(textViewId);
        layoutStub = (LinearLayout) viewTextStub.inflate();
    }
}
