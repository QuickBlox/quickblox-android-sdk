package com.quickblox.sample.chat.kotlin.utils

import java.text.SimpleDateFormat
import java.util.*

private const val MMMM_dd_PATTERN = "MMMM dd"
private const val ddMMyyyy_PATTERN = "ddMMyyyy"

fun getDate(milliseconds: Long): String {
    val dateFormat = SimpleDateFormat(MMMM_dd_PATTERN, Locale.getDefault())
    return dateFormat.format(Date(milliseconds))
}

fun getDateAsHeaderId(milliseconds: Long): Long {
    val dateFormat = SimpleDateFormat(ddMMyyyy_PATTERN, Locale.getDefault())
    return java.lang.Long.parseLong(dateFormat.format(Date(milliseconds)))
}