package com.quickblox.sample.videochat.kotlin.view

import android.content.Context
import android.content.res.TypedArray
import android.preference.Preference
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.SeekBar
import com.quickblox.sample.videochat.kotlin.R

private const val ANDROID_NS = "http://schemas.android.com/apk/res/android"
private const val SEEKBAR_NS = "http://schemas.android.com/apk/res-auto"

class SeekBarPreference : Preference, SeekBar.OnSeekBarChangeListener {

    private lateinit var seekBar: SeekBar
    private var progress: Int = 0
    private var maxSeekBarValue: Int = 0
    private var minSeekBarValue: Int = 0
    private var seekBarStepSize: Int = 0

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        layoutResource = R.layout.seekbar_preference
        initFields(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        layoutResource = R.layout.seekbar_preference
        initFields(context, attrs)
    }

    private fun initFields(context: Context, attrs: AttributeSet) {
        val maxValueResourceId = attrs.getAttributeResourceValue(ANDROID_NS, "max", R.integer.pref_default_int_value)
        maxSeekBarValue = context.resources.getInteger(maxValueResourceId)

        val minValueResourceId = attrs.getAttributeResourceValue(SEEKBAR_NS, "min", R.integer.pref_default_int_value)
        minSeekBarValue = context.resources.getInteger(minValueResourceId)

        val stepSizeValueResourceId = attrs.getAttributeResourceValue(SEEKBAR_NS, "stepSize", R.integer.pref_seekbar_step_default_int_value)
        seekBarStepSize = context.resources.getInteger(stepSizeValueResourceId)

        Log.v("Attribute", "max = $maxSeekBarValue")
        Log.v("Attribute", "min = $minSeekBarValue")
        Log.v("Attribute", "step = $seekBarStepSize")
    }

    override fun onBindView(view: View) {
        super.onBindView(view)
        seekBar = view.findViewById(R.id.seekbar)
        seekBar.max = maxSeekBarValue
        seekBar.progress = progress
        seekBar.setOnSeekBarChangeListener(this)
    }

    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
        if (fromUser) {
            var changedProgress = progress / seekBarStepSize * seekBarStepSize

            if (changedProgress <= minSeekBarValue) {
                changedProgress += minSeekBarValue
            }

            setValue(changedProgress)
        }
    }

    override fun onStartTrackingTouch(seekBar: SeekBar) {

    }

    override fun onStopTrackingTouch(seekBar: SeekBar) {

    }

    override fun onSetInitialValue(restoreValue: Boolean, defaultValue: Any?) {
        val value = if (restoreValue) {
            getPersistedInt(progress)
        } else {
            defaultValue
        }
        setValue(value as Int)
    }

    private fun setValue(value: Int) {
        if (shouldPersist()) {
            persistInt(value)
        }

        if (value != progress) {
            progress = value
            notifyChanged()
        }

        summary = progress.toString()
    }

    override fun onGetDefaultValue(a: TypedArray, index: Int): Any {
        return a.getInt(index, 0)
    }
}