package com.quickblox.sample.groupchatwebrtc.utils;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.Preference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;

import com.quickblox.sample.groupchatwebrtc.R;

public class SeekBarPreference extends Preference implements SeekBar.OnSeekBarChangeListener {

    private static final String androidns ="http://schemas.android.com/apk/res/android";
    private static final String seekbarns ="http://schemas.android.com/apk/res-auto";


    private Context mContext;
    private SeekBar mSeekBar;
    private int mProgress, mMax, mMin, mStepSize;

    public SeekBarPreference(Context context) {
        this(context, null, 0);
    }

    public SeekBarPreference(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SeekBarPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setLayoutResource(R.layout.seekbar_preference);

        mContext = context;

        final TypedArray a = context.obtainStyledAttributes(
                attrs, R.styleable.SeekBarPreference, defStyle, 0);

        for (int i = a.getIndexCount() - 1; i >= 0; i--) {
            int attr = a.getIndex(i);

            int resourceId;

            switch (attr) {
                case R.styleable.SeekBarPreference_min:
                    resourceId = a.getResourceId(attr, 0);
                    mMin = Integer.parseInt(context.getString(resourceId));
                    break;
                case R.styleable.SeekBarPreference_stepSize:
                    resourceId = a.getResourceId(attr, 0);
                    mStepSize = Integer.parseInt(context.getString(resourceId));
            }
        }

        int maxValueResourceId = attrs.getAttributeResourceValue(androidns, "max", R.integer.pref_default_int_value);
        mMax = context.getResources().getInteger(maxValueResourceId);

        Log.v("Attribute", "max = " + mMax);
        Log.v("Attribute", "mMin = " + mMin);
        Log.v("Attribute", "mStepSize = " + mStepSize);

    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        mSeekBar = (SeekBar) view.findViewById(R.id.seekbar);
        mSeekBar.setMax(mMax);
        mSeekBar.setProgress(mProgress);
        mSeekBar.setOnSeekBarChangeListener(this);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (!fromUser)
            return;

        progress = (progress / mStepSize) * mStepSize;

//        if (progress <= mMin) {
//            progress = mMin + progress;
//        }

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
        Log.d("Attribute", "restoreValue = " + restoreValue + " defaultValue = "+ defaultValue);
        setValue(restoreValue ? getPersistedInt(mProgress) : (Integer) defaultValue);
    }

    public void setValue(int value) {
        if (shouldPersist()) {
            persistInt(value);
        }

        if (value != mProgress) {
            mProgress = value;
            notifyChanged();
        }

        setSummary(String.valueOf(mProgress));
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getInt(index, 0);
    }
}