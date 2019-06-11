package com.quickblox.sample.chat.kotlin.ui.views

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.InflateException
import android.widget.ImageView
import androidx.annotation.DrawableRes
import com.quickblox.sample.chat.kotlin.R

private const val HAS_ALPHA_LAYER_SAVE_FLAG = 0x0
private const val FULL_COLOR_LAYER_SAVE_FLAG = 0x08

class MaskedImageView(context: Context, attrs: AttributeSet) : ImageView(context, attrs) {

    private lateinit var maskedPaint: Paint
    private lateinit var copyPaint: Paint
    private lateinit var maskDrawable: Drawable
    private var maskResourceId: Int = 0
    private lateinit var boundsRect: Rect
    private lateinit var boundsRectF: RectF

    init {
        maskResourceId = -1
        val array = context.theme.obtainStyledAttributes(attrs, R.styleable.MaskedImageView,
                0, 0)
        try {
            maskResourceId = array.getResourceId(R.styleable.MaskedImageView_mask, -1)
        } finally {
            array.recycle()
        }
        if (maskResourceId < 0) {
            throw InflateException("Mandatory 'mask' attribute not set!")
        }
        setMaskResourceId(maskResourceId)
    }

    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        boundsRect = Rect(0, 0, width, height)
        boundsRectF = RectF(boundsRect)
    }

    @SuppressLint("WrongConstant")
    override fun onDraw(canvas: Canvas) {
        val saveCount = canvas.saveLayer(boundsRectF, copyPaint,
                HAS_ALPHA_LAYER_SAVE_FLAG or FULL_COLOR_LAYER_SAVE_FLAG)
        maskDrawable.bounds = boundsRect
        maskDrawable.draw(canvas)
        canvas.saveLayer(boundsRectF, maskedPaint, 0)
        super.onDraw(canvas)
        canvas.restoreToCount(saveCount)
    }

    private fun setMaskResourceId(@DrawableRes maskResourceId: Int) {
        maskedPaint = Paint()
        maskedPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP)
        copyPaint = Paint()
        maskDrawable = resources.getDrawable(maskResourceId)
        invalidate()
    }
}