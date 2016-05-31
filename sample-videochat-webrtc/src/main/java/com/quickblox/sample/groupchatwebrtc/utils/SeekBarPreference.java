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

    private static final String androidns="http://schemas.android.com/apk/res/android";

    private Context mContext;
    private SeekBar mSeekBar;
    private int mProgress, mMax, mDefault;
//    private int mMin = mMax;

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

//        final TypedArray a = context.obtainStyledAttributes(
//                attrs, com.android.internal.R.styleable.Preference, defStyle, 0);
//        for (int i = a.getIndexCount() - 1; i >= 0; i--) {
//            int attr = a.getIndex(i);
//
//            Log.d("Attribute", "max = " + attr);
//            String attrName = attrs.getAttributeName(i);
//
//            switch (attrName) {
//                case "max":
//                    int resourceId = a.getResourceId(attr, 0);
//                    mMax = context.getResources().getInteger(resourceId);
//                    Log.d("Attribute", "max = " + mMax);
//                    break;
//            }

            Log.d("Attribute", "max = " + mMax);


////            atributeMaxValue = attrs.getAttribute(androidns, "max");
            mMax = attrs.getAttributeIntValue(androidns, "max", 100);
            mDefault = attrs.getAttributeIntValue(androidns, "defaultValue", 0);

//        }
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