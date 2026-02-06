package com.quickblox.sample.videochat.java.activities;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;

import com.quickblox.videochat.webrtc.view.QBRTCSurfaceView;

import org.webrtc.VideoFrame;

public class ScalableSurfaceView extends QBRTCSurfaceView {
    private static final String TAG = ScalableSurfaceView.class.getSimpleName();

    private volatile int videoWidth = 0;
    private volatile int videoHeight = 0;

    public ScalableSurfaceView(Context context) {
        super(context);
    }

    public ScalableSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void onFrame(VideoFrame frame) {
        int newWidth = frame.getRotatedWidth();
        int newHeight = frame.getRotatedHeight();
        if (newWidth != videoWidth || newHeight != videoHeight) {
            videoWidth = newWidth;
            videoHeight = newHeight;
            Log.d(TAG, "Frame resolution changed: " + videoWidth + "x" + videoHeight);
            post(new Runnable() {
                @Override
                public void run() {
                    requestLayout();
                }
            });
        }
        super.onFrame(frame);
    }

    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
        int maxWidth = MeasureSpec.getSize(widthSpec);
        int maxHeight = MeasureSpec.getSize(heightSpec);

        if (videoWidth == 0 || videoHeight == 0 || maxWidth == 0 || maxHeight == 0) {
            setMeasuredDimension(maxWidth, maxHeight);
            return;
        }

        float videoAspect = (float) videoWidth / videoHeight;
        float displayAspect = (float) maxWidth / maxHeight;

        int measuredW, measuredH;
        if (displayAspect > videoAspect) {
            measuredH = maxHeight;
            measuredW = Math.min(maxWidth, Math.round(maxHeight * videoAspect));
        } else {
            measuredW = maxWidth;
            measuredH = Math.min(maxHeight, Math.round(maxWidth / videoAspect));
        }

        Log.d(TAG, "onMeasure: video=" + videoWidth + "x" + videoHeight
                + " container=" + maxWidth + "x" + maxHeight
                + " → view=" + measuredW + "x" + measuredH);
        setMeasuredDimension(measuredW, measuredH);
    }
}
