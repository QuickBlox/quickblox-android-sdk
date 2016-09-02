package com.quickblox.sample.core.adapter.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.annotation.LayoutRes;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.quickblox.sample.core.R;

public class EditMessageTextView extends RelativeLayout {
    LinearLayout bubbleBackground;
    LinearLayout linearAgile;

    public EditMessageTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(R.layout.edit_message_test_view);
        applyAttributes(attrs);
    }

    private void init(@LayoutRes int layoutId) {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(layoutId, this, true);

        bubbleBackground = (LinearLayout) getRootView().findViewById(R.id.bubble_background);
        linearAgile = (LinearLayout) getRootView().findViewById(R.id.linear_agile);
    }

    private void applyAttributes(AttributeSet attrs) {
        TypedArray array = null;
        boolean stickRight;
        Drawable bubble;
        try {
            array = getContext().obtainStyledAttributes(attrs, R.styleable.EditMessageTextView);
            stickRight = array.getBoolean(R.styleable.EditMessageTextView_stick_right, false);
            bubble = array.getDrawable(R.styleable.EditMessageTextView_bubble);
        } finally {
            if (array != null) {
                array.recycle();
            }
        }
        setAvatarSide(stickRight);
        setBubble(bubble);
    }

    private void setAvatarSide(boolean right) {

        RelativeLayout.LayoutParams avatarParams = (RelativeLayout.LayoutParams) linearAgile.getLayoutParams();
        avatarParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, right ? 1 : 0);

        linearAgile.setLayoutParams(avatarParams);

       /* RelativeLayout.LayoutParams bubbleParams = (RelativeLayout.LayoutParams) linearAgile.getLayoutParams();
        bubbleParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, right ? 1 : 0);

        bubbleParams.setMargins(!right ? 70 : 0, 0, right ? 60 : 0, 0);

        linearAgile.setLayoutParams(bubbleParams);*/
    }

    private void setBubble(Drawable draw) {
        //использовать другой метод
        bubbleBackground.setBackgroundDrawable(draw);
    }
}
