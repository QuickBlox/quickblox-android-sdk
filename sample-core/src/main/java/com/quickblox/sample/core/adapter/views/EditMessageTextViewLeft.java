package com.quickblox.sample.core.adapter.views;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.quickblox.sample.core.R;

public class EditMessageTextViewLeft extends EditMessageTextView {
    public EditMessageTextViewLeft(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    @Override
    protected void setLinearSide() {
        RoundedImageView roundedImageViewRight = (RoundedImageView) getRootView().findViewById(R.id.avatar_imageview_left);
        roundedImageViewRight.setVisibility(VISIBLE);
    }

    @Override
    protected void setTextLayout(@LayoutRes int customWidgetId) {
        int widgetId = R.layout.item_stub_left_text;

        if(customWidgetId != 0){
            widgetId = customWidgetId;
        }
        viewStub.setLayoutResource(widgetId);
        viewStubLayout = (LinearLayout) viewStub.inflate();
    }


}
