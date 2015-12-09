package com.quickblox.sample.chat.utils;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;

import com.quickblox.sample.chat.App;
import com.quickblox.sample.chat.R;

import java.util.Random;

public class UiUtils {

    private static final Random random = new Random();
    private static int previousColor;

    private UiUtils() {}

    public static Drawable getGreyCircleDrawable() {
        return getColoredCicleDrawable(getColor(R.color.color_grey));
    }

    public static Drawable getRandomColorCircleDrawable() {
        return getColoredCicleDrawable(getRandomColor());
    }

    private static Drawable getColoredCicleDrawable(@ColorInt int color) {
        GradientDrawable drawable = (GradientDrawable) App.getInstance().getResources().getDrawable(R.drawable.shape_circle);
        drawable.setColor(color);
        return drawable;
    }

    public static int getRandomColor() {
        int randomNumber = random.nextInt(10) + 1;
        String colorIdName = String.format("random_color_%d", randomNumber);

        int colorId = App.getInstance().getResources().getIdentifier(colorIdName, "color", App.getInstance().getPackageName());

        int generatedColor = getColor(colorId);
        if (generatedColor != previousColor) {
            previousColor = generatedColor;
            return generatedColor;
        } else {
            do {
                generatedColor = getRandomColor();
            } while (generatedColor != previousColor);
        }
        return previousColor;
    }

    private static int getColor(@ColorRes int colorId) {
        return App.getInstance().getResources().getColor(colorId);
    }
}
