package com.quickblox.sample.groupchatwebrtc.fragments;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.quickblox.sample.groupchatwebrtc.R;


public class SettingsFragment extends PreferenceFragment {

    private static final String TAG = SettingsFragment.class.getSimpleName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }

}
