package com.quickblox.sample.core.adapter.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.quickblox.sample.core.R;

public class EditMessageTextViewRight extends EditMessageTextView {


    public EditMessageTextViewRight(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    @Override
    protected void setLinearSide(boolean right) {
        LinearLayout linearAgile = (LinearLayout) getRootView().findViewById(R.id.linear_agile);
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) linearAgile.getLayoutParams();
        layoutParams.gravity = right ? Gravity.RIGHT : Gravity.LEFT;

        linearAgile.setLayoutParams(layoutParams);

        RoundedImageView roundedImageViewLeft = (RoundedImageView) getRootView().findViewById(R.id.avatar_imageview_left);
        roundedImageViewLeft.setVisibility(right ? INVISIBLE : VISIBLE);

        RoundedImageView roundedImageViewRight = (RoundedImageView) getRootView().findViewById(R.id.avatar_imageview_right);
        roundedImageViewRight.setVisibility(right ? VISIBLE : INVISIBLE);
    }

}
