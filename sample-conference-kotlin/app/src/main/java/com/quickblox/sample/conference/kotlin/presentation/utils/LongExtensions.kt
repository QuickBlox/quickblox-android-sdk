package com.quickblox.sample.conference.kotlin.presentation.utils

import java.text.SimpleDateFormat
import java.util.*

/*
 * Created by Injoit in 2021-09-30.
 * Copyright © 2021 Quickblox. All rights reserved.
 */
fun Long.getTime(): String {
    val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    return dateFormat.format(Date(this * 1000))
}
