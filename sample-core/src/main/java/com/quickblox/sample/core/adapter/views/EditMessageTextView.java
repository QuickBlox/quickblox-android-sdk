package com.quickblox.sample.core.adapter.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.annotation.LayoutRes;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.ViewStub;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.quickblox.sample.core.R;

public class EditMessageTextView extends RelativeLayout {
    private static String TAG = EditMessageTextView.class.getSimpleName();

    public int stubLayout;
    LinearLayout viewStubLayout;
    LinearLayout linearAgile;
    Drawable bubble;

    public EditMessageTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(R.layout.edit_message_test_view);
        applyAttributes(attrs);
    }

    private void init(@LayoutRes int layoutId) {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(layoutId, this, true);

//        viewStubLayout = (LinearLayout) getRootView().findViewById(R.id.bubble_background);
        linearAgile = (LinearLayout) getRootView().findViewById(R.id.linear_agile);


    }

    private void applyAttributes(AttributeSet attrs) {
        TypedArray array = null;
        boolean stickRight;
//        Drawable bubble;
        try {
            array = getContext().obtainStyledAttributes(attrs, R.styleable.EditMessageTextView);
            stickRight = array.getBoolean(R.styleable.EditMessageTextView_stick_right, false);
            bubble = array.getDrawable(R.styleable.EditMessageTextView_bubble);
        } finally {
            if (array != null) {
                array.recycle();
            }
        }
        setLinearSide(stickRight);
//        setBubble(bubble);
        setTextLayout(stickRight);
    }

    private void setLinearSide(boolean right) {

        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) linearAgile.getLayoutParams();
        layoutParams.gravity = right ? Gravity.RIGHT : Gravity.LEFT;

        linearAgile.setLayoutParams(layoutParams);

        RoundedImageView roundedImageViewLeft = (RoundedImageView) getRootView().findViewById(R.id.avatar_imageview_left);
        roundedImageViewLeft.setVisibility(right ? INVISIBLE : VISIBLE);

        RoundedImageView roundedImageViewRight = (RoundedImageView) getRootView().findViewById(R.id.avatar_imageview_right);
        roundedImageViewRight.setVisibility(right ? VISIBLE : INVISIBLE);
    }

    private void setBubble(Drawable draw) {
        //использовать другой метод
        viewStubLayout.setBackgroundDrawable(draw);
        viewStubLayout.setPadding(20, 0, 10, 0);
    }

    private void setTextLayout(boolean right) {
        Log.d(TAG, "setTextLayout");
        ViewStub viewStub = (ViewStub) findViewById(R.id.stub_text);

        // надуваем либо правый либо левый
        viewStub.setLayoutResource(right ? R.layout.item_stub_right_text : R.layout.item_stub_left_text);
        viewStubLayout = (LinearLayout) viewStub.inflate();
        setBubble(bubble);

        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) viewStubLayout.getLayoutParams();
        layoutParams.gravity = right ? Gravity.RIGHT : Gravity.LEFT;

        TextView timeText = (TextView) viewStubLayout.findViewById(R.id.time_text_message_textview);
        timeText.setLayoutParams(layoutParams);
    }
}
