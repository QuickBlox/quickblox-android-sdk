package com.quickblox.sample.core.adapter.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.annotation.LayoutRes;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
    LayoutInflater inflater;

    Drawable bubble;

    public EditMessageTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(R.layout.edit_message_test_view);
        applyAttributes(attrs);
    }

    protected void init(@LayoutRes int layoutId) {
        inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(layoutId, this, true);
    }

    protected void applyAttributes(AttributeSet attrs) {
        TypedArray array = null;
        boolean stickRight;
        int widgetId;
        try {
            array = getContext().obtainStyledAttributes(attrs, R.styleable.EditMessageTextView);
            stickRight = array.getBoolean(R.styleable.EditMessageTextView_stick_right, false);
            bubble = array.getDrawable(R.styleable.EditMessageTextView_bubble);
            widgetId = array.getResourceId(R.styleable.EditMessageTextView_widget_id, 0);
        } finally {
            if (array != null) {
                array.recycle();
            }
        }
        setLinearSide(stickRight);
        setTextLayout(stickRight);
        setWidget(widgetId);
    }

    private void setWidget(@LayoutRes int widgetId) {
//        ViewStub viewStubWidget = (ViewStub) findViewById(R.id.stub_widget);
        if (widgetId != 0) {
//            viewStubWidget.setLayoutResource(widgetId);
//            TextView viewStubLayout = (TextView) viewStubWidget.inflate();
            final ViewGroup widgetFrame = (ViewGroup) findViewById(R.id.widget_frame);
            View view = inflater.inflate(widgetId, widgetFrame);
            Log.d(TAG, "view=null? " + (view == null));
        }
    }

    private void setLinearSide(boolean right) {
        LinearLayout linearAgile = (LinearLayout) getRootView().findViewById(R.id.linear_agile);
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
