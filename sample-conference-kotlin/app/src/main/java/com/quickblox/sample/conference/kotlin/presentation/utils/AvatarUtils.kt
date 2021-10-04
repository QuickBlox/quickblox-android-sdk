package com.quickblox.sample.conference.kotlin.presentation.utils

import android.content.Context
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import androidx.core.content.ContextCompat
import com.quickblox.sample.conference.kotlin.R

/*
 * Created by Injoit in 2021-09-30.
 * Copyright © 2021 Quickblox. All rights reserved.
 */
object AvatarUtils {
    private const val RANDOM_COLOR_END_RANGE = 9L

    fun getDrawableAvatar(context: Context, colorPosition: Int): Drawable {
        val drawable = ContextCompat.getDrawable(context, R.drawable.shape_circle) as GradientDrawable
        val colorPosition1 = colorPosition % RANDOM_COLOR_END_RANGE.toInt()
        val colorIdName = String.format("randomColor%d", colorPosition1 + 1)
        val colorId = context.resources.getIdentifier(colorIdName, "color", context.packageName)
        drawable.setColor(ContextCompat.getColor(context, colorId))
        return drawable
    }
}