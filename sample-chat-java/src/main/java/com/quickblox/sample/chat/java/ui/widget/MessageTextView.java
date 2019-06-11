package com.quickblox.sample.chat.java.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.quickblox.sample.chat.java.R;

import androidx.annotation.LayoutRes;


public abstract class MessageTextView extends FrameLayout {
    private static String TAG = MessageTextView.class.getSimpleName();
    protected LinearLayout frameLinear;
    protected ViewStub viewTextStub;

    protected LinearLayout layoutStub;
    private LayoutInflater inflater;

    public MessageTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(R.layout.widget_text_msg_frame);
        applyAttributes(attrs);
    }

    private void init(@LayoutRes int layoutId) {
        inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(layoutId, this, true);
        frameLinear = (LinearLayout) getRootView().findViewById(R.id.msg_linear_frame);
        viewTextStub = (ViewStub) findViewById(R.id.msg_stub_message);
    }

    private void applyAttributes(AttributeSet attrs) {
        TypedArray array = null;
        int widgetIdBottom;
        int widgetIdTop;
        int widgetIdLinkPreview;

        try {
            array = getContext().obtainStyledAttributes(attrs, R.styleable.MessageTextView);
            widgetIdBottom = array.getResourceId(R.styleable.MessageTextView_widget_id_bottom, 0);
            widgetIdTop = array.getResourceId(R.styleable.MessageTextView_widget_id_top, 0);
            widgetIdLinkPreview = array.getResourceId(R.styleable.MessageTextView_widget_id_link_preview, R.layout.widget_link_preview);
        } finally {
            if (array != null) {
                array.recycle();
            }
        }

        setLinearSide();
        setTextLayout();
        setCustomWidgets(widgetIdBottom, widgetIdTop);
        setLinkPreviewWidget(widgetIdLinkPreview);
    }

    private void setCustomWidgets(@LayoutRes int widgetIdBottom, @LayoutRes int widgetIdTop) {
        if (widgetIdBottom != 0) {
            final ViewGroup widgetFrameBottom = (ViewGroup) findViewById(R.id.msg_custom_widget_frame_bottom);
            View customViewBottom = inflater.inflate(widgetIdBottom, widgetFrameBottom);
            Log.d(TAG, "customViewBottom = null ? " + (customViewBottom == null));
        }

        if (widgetIdTop != 0) {
            final ViewGroup widgetFrameTop = (ViewGroup) findViewById(R.id.msg_custom_widget_frame_top);
            View customViewTop = inflater.inflate(widgetIdTop, widgetFrameTop);
            Log.d(TAG, "customViewTop = null ? " + (customViewTop == null));
        }
    }

    private void setLinkPreviewWidget(@LayoutRes int linkPreviewWidget) {
        if (linkPreviewWidget != 0) {
            final ViewGroup widgetFrameLinkPreview = (ViewGroup) findViewById(R.id.msg_link_preview);
            View linkPreview = inflater.inflate(linkPreviewWidget, widgetFrameLinkPreview);
            Log.d(TAG, "linkPreview = null ? " + (linkPreview == null));
        }
    }

    abstract protected void setLinearSide();

    abstract protected void setTextLayout();
}