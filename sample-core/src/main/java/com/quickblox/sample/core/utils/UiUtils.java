package com.quickblox.sample.core.utils;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.ColorInt;
import android.support.annotation.IntRange;

import com.quickblox.sample.core.CoreApp;
import com.quickblox.sample.core.R;

import java.util.Random;

public class UiUtils {

    private static final int RANDOM_COLOR_START_RANGE = 0;
    private static final int RANDOM_COLOR_END_RANGE = 9;

    private static final Random random = new Random();
    private static int previousColor;

    private UiUtils() {}

    public static Drawable getGreyCircleDrawable() {
        return getColoredCircleDrawable(ResourceUtils.getColor(R.color.color_grey));
    }

    public static Drawable getRandomColorCircleDrawable() {
        return getColoredCircleDrawable(getRandomCircleColor());
    }

    public static Drawable getColorCircleDrawable(int colorPosition) {
        return getColoredCircleDrawable(getCircleColor(colorPosition % RANDOM_COLOR_END_RANGE));
    }

    public static Drawable getColoredCircleDrawable(@ColorInt int color) {
        GradientDrawable drawable = (GradientDrawable) ResourceUtils.getDrawable(R.drawable.shape_circle);
        drawable.setColor(color);
        return drawable;
    }

    public static int getRandomCircleColor() {
        int randomNumber = random.nextInt(RANDOM_COLOR_END_RANGE) + 1;

        int generatedColor = getCircleColor(randomNumber);
        if (generatedColor != previousColor) {
            previousColor = generatedColor;
            return generatedColor;
        } else {
            do {
                generatedColor = getRandomCircleColor();
            } while (generatedColor != previousColor);
        }
        return previousColor;
    }

    public static int getCircleColor(@IntRange(from = RANDOM_COLOR_START_RANGE, to = RANDOM_COLOR_END_RANGE)
                                     int colorPosition) {
        String colorIdName = String.format("random_color_%d", colorPosition + 1);
        int colorId = CoreApp.getInstance().getResources()
                .getIdentifier(colorIdName, "color", CoreApp.getInstance().getPackageName());

        return ResourceUtils.getColor(colorId);
    }
}
