package com.quickblox.sample.videochat.conference.java.utils;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;

import com.quickblox.sample.videochat.conference.java.R;

import androidx.annotation.ColorInt;
import androidx.annotation.IntRange;

public class UiUtils {

    private static final int RANDOM_COLOR_START_RANGE = 0;
    private static final int RANDOM_COLOR_END_RANGE = 9;

    private UiUtils() {

    }

    public static Drawable getColorCircleDrawable(Context context, int colorPosition) {
        return getColoredCircleDrawable(context, getCircleColor(context, colorPosition % RANDOM_COLOR_END_RANGE));
    }

    private static Drawable getColoredCircleDrawable(Context context, @ColorInt int color) {
        GradientDrawable drawable = (GradientDrawable) context.getResources().getDrawable(R.drawable.shape_circle);
        drawable.setColor(color);
        return drawable;
    }

    private static int getCircleColor(Context context, @IntRange(from = RANDOM_COLOR_START_RANGE, to = RANDOM_COLOR_END_RANGE)
            int colorPosition) {
        String colorIdName = String.format("random_color_%d", colorPosition + 1);
        int colorId = context.getResources().getIdentifier(colorIdName, "color", context.getPackageName());
        return context.getResources().getColor(colorId);
    }
}