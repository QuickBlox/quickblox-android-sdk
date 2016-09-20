package com.quickblox.sample.core.adapter.views;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewStub;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.quickblox.sample.core.R;

public class EditMessageTextViewRight extends EditMessageTextView {

    public EditMessageTextViewRight(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    @Override
    protected void setLinearSide() {
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) linearAgile.getLayoutParams();
        layoutParams.gravity = Gravity.RIGHT;
        linearAgile.setLayoutParams(layoutParams);

        RoundedImageView roundedImageViewRight = (RoundedImageView) getRootView().findViewById(R.id.avatar_imageview_right);
        roundedImageViewRight.setVisibility(VISIBLE);
    }

    @Override
    protected void setTextLayout(@LayoutRes int customWidgetId) {
        int widgetId = R.layout.item_stub_right_text;

        if(customWidgetId != 0){
           widgetId = customWidgetId;
        }
        viewStub.setLayoutResource(widgetId);
        viewStubLayout = (LinearLayout) viewStub.inflate();
    }


}
