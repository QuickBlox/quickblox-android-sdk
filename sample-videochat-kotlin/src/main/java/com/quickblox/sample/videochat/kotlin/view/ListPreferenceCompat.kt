package com.quickblox.sample.videochat.kotlin.view

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.preference.ListPreference
import android.text.TextUtils
import android.util.AttributeSet


class ListPreferenceCompat : ListPreference {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun setValue(value: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            super.setValue(value)
        } else {
            val oldValue = getValue()
            super.setValue(value)
            if (!TextUtils.equals(value, oldValue)) {
                notifyChanged()
            }
        }
    }
}