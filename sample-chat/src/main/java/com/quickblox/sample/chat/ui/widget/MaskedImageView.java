package com.quickblox.sample.chat.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.util.AttributeSet;
import android.view.InflateException;
import android.widget.ImageView;

import com.quickblox.sample.chat.R;

public class MaskedImageView extends ImageView {

    private Paint maskedPaint;
    private Paint copyPaint;
    private Drawable maskDrawable;
    private int maskResourceId;
    private Rect boundsRect;
    private RectF boundsRectF;

    public MaskedImageView(Context context) {
        this(context, null);
    }

    public MaskedImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        maskResourceId = -1;
        TypedArray array = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.MaskedImageView, 0, 0);

        try {
            maskResourceId = array.getResourceId(R.styleable.MaskedImageView_mask, -1);
        } finally {
            array.recycle();
        }

        if (maskResourceId < 0) {
            throw new InflateException("Mandatory 'mask' attribute not set!");
        }

        setMaskResourceId(maskResourceId);
    }

    protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        boundsRect = new Rect(0, 0, width, height);
        boundsRectF = new RectF(boundsRect);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int sc = canvas.saveLayer(boundsRectF, copyPaint,
                Canvas.HAS_ALPHA_LAYER_SAVE_FLAG | Canvas.FULL_COLOR_LAYER_SAVE_FLAG);
        maskDrawable.setBounds(boundsRect);
        maskDrawable.draw(canvas);
        canvas.saveLayer(boundsRectF, maskedPaint, 0);

        super.onDraw(canvas);

        canvas.restoreToCount(sc);
    }

    public void setMaskResourceId(@DrawableRes int maskResourceId) {
        maskedPaint = new Paint();
        maskedPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP));

        copyPaint = new Paint();
        maskDrawable = getResources().getDrawable(maskResourceId);

        invalidate();
    }
}