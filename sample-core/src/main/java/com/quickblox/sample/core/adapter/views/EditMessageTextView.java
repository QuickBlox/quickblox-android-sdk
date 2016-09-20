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

public abstract class EditMessageTextView extends RelativeLayout {
    private static String TAG = EditMessageTextView.class.getSimpleName();
    protected LinearLayout linearAgile;
    protected ViewStub viewStub;

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
        linearAgile = (LinearLayout) getRootView().findViewById(R.id.linear_agile);
        viewStub = (ViewStub) findViewById(R.id.stub_text);
    }

    protected void applyAttributes(AttributeSet attrs) {
        TypedArray array = null;
        boolean stickRight;
        int textViewId;
        int widgetId;
        try {
            array = getContext().obtainStyledAttributes(attrs, R.styleable.EditMessageTextView);
            stickRight = array.getBoolean(R.styleable.EditMessageTextView_stick_right, false);
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

    abstract protected void setLinearSide();

    protected void setBubble(Drawable draw) {
        //использовать другой метод
        viewStubLayout.setBackgroundDrawable(draw);
        viewStubLayout.setPadding(20, 0, 10, 0);
    }

    abstract protected void setTextLayout(@LayoutRes int textViewId);
}
