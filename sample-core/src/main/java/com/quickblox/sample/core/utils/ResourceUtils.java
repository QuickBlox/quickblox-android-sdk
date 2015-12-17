package com.quickblox.sample.core.utils;

import android.graphics.drawable.Drawable;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;

import com.quickblox.sample.core.CoreApp;

public class ResourceUtils {

    public static Drawable getDrawable(@DrawableRes int drawableId) {
        return CoreApp.getInstance().getResources().getDrawable(drawableId);
    }

    public static int getColor(@ColorRes int colorId) {
        return CoreApp.getInstance().getResources().getColor(colorId);
    }

}
