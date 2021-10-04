package com.quickblox.sample.conference.kotlin.presentation.utils

import android.content.res.Resources

/*
 * Created by Injoit in 2021-09-30.
 * Copyright © 2021 Quickblox. All rights reserved.
 */
fun Int.convertToPx(): Int {
    val scale = Resources.getSystem().displayMetrics.density
    return (this * scale + 0.5f).toInt()
}