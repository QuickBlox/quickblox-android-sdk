package com.quickblox.sample.core.adapter.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.annotation.LayoutRes;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.quickblox.sample.core.R;

public abstract class EditMessageTextView extends RelativeLayout {
    private static String TAG = EditMessageTextView.class.getSimpleName();
    protected LinearLayout frameLinear;
    protected ViewStub viewTextStub;

    LinearLayout layoutStub;
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
        frameLinear = (LinearLayout) getRootView().findViewById(R.id.linear_frame);
        viewTextStub = (ViewStub) findViewById(R.id.stub_text);
    }

    protected void applyAttributes(AttributeSet attrs) {
        TypedArray array = null;
        int textViewId;
        int widgetId;

        try {
            array = getContext().obtainStyledAttributes(attrs, R.styleable.EditMessageTextView);
            bubble = array.getDrawable(R.styleable.EditMessageTextView_bubble);
            textViewId = array.getResourceId(R.styleable.EditMessageTextView_message_text_id, 0);
            widgetId = array.getResourceId(R.styleable.EditMessageTextView_widget_id, 0);
        } finally {
            if (array != null) {
                array.recycle();
            }
        }

        setLinearSide();
        setTextLayout(textViewId);
        setBubble(bubble);
        setWidget(widgetId);
    }

    protected void setWidget(@LayoutRes int widgetId) {
        if (widgetId != 0) {
            final ViewGroup widgetFrame = (ViewGroup) findViewById(R.id.widget_frame);
            View view = inflater.inflate(widgetId, widgetFrame);
            Log.d(TAG, "view=null? " + (view == null));
        }
    }

    protected void setBubble(Drawable draw) {
        layoutStub.setBackgroundDrawable(draw);
        layoutStub.setPadding(20, 0, 10, 0);
    }

    abstract protected void setLinearSide();

    abstract protected void setTextLayout(@LayoutRes int textViewId);
}
