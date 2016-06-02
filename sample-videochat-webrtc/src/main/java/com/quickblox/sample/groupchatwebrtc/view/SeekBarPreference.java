package com.quickblox.sample.groupchatwebrtc.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.Preference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;

import com.quickblox.sample.groupchatwebrtc.R;

public class SeekBarPreference extends Preference implements SeekBar.OnSeekBarChangeListener {

    private static final String ANDROID_NS ="http://schemas.android.com/apk/res/android";
    private static final String SEEKBAR_NS ="http://schemas.android.com/apk/res-auto";


    private Context context;
    private SeekBar seekBar;
    private int progress, maxSeekBarValue, minSeekBarValue, seekBarStepSize;

    public SeekBarPreference(Context context) {
        this(context, null, 0);
    }

    public SeekBarPreference(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SeekBarPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setLayoutResource(R.layout.seekbar_preference);

        this.context = context;

        initFields(context, attrs);
    }

    private void initFields(Context context, AttributeSet attrs) {
        int maxValueResourceId = attrs.getAttributeResourceValue(ANDROID_NS, "max", R.integer.pref_default_int_value);
        maxSeekBarValue = context.getResources().getInteger(maxValueResourceId);

        int minValueResourceId = attrs.getAttributeResourceValue(SEEKBAR_NS, "min", R.integer.pref_default_int_value);
        minSeekBarValue = context.getResources().getInteger(minValueResourceId);

        int stepSizeValueResourceId = attrs.getAttributeResourceValue(SEEKBAR_NS, "stepSize", R.integer.pref_default_int_value);
        seekBarStepSize = context.getResources().getInteger(stepSizeValueResourceId);

        Log.v("Attribute", "max = " + maxSeekBarValue);
        Log.v("Attribute", "min = " + minSeekBarValue);
        Log.v("Attribute", "step = " + seekBarStepSize);
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        seekBar = (SeekBar) view.findViewById(R.id.seekbar);
        seekBar.setMax(maxSeekBarValue);
        seekBar.setProgress(progress);
        seekBar.setOnSeekBarChangeListener(this);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (!fromUser)
            return;

        progress = (progress / seekBarStepSize) * seekBarStepSize;

        if (progress <= minSeekBarValue) {
            progress = minSeekBarValue + progress;
        }

        setValue(progress);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        // not used
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        // not used
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        setValue(restoreValue ? getPersistedInt(progress) : (Integer) defaultValue);
    }

    public void setValue(int value) {
        if (shouldPersist()) {
            persistInt(value);
        }

        if (value != progress) {
            progress = value;
            notifyChanged();
        }

        setSummary(String.valueOf(progress));
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getInt(index, 0);
    }
}