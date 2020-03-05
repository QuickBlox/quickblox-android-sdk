package com.quickblox.sample.chat.java.ui.adapter;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.ScrollView;

import androidx.annotation.RequiresApi;

public class ScrollViewWithMaxHeight extends ScrollView {
    private static final int WITHOUT_MAX_HEIGHT = -1;

    private int maxHeight = WITHOUT_MAX_HEIGHT;

    public ScrollViewWithMaxHeight(Context context) {
        super(context);
    }

    public ScrollViewWithMaxHeight(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ScrollViewWithMaxHeight(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public ScrollViewWithMaxHeight(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int heightMeasure = heightMeasureSpec;
        try {
            int heightSize = MeasureSpec.getSize(heightMeasure);

            if (maxHeight != WITHOUT_MAX_HEIGHT && heightSize > maxHeight) {
                heightSize = maxHeight;
            }
            heightMeasure = MeasureSpec.makeMeasureSpec(heightSize, MeasureSpec.AT_MOST);
            getLayoutParams().height = heightSize;
        } catch (Exception ignored) {

        } finally {
            super.onMeasure(widthMeasureSpec, heightMeasure);
        }
    }

    public void setMaxHeight(int maxHeight) {
        this.maxHeight = maxHeight;
    }
}