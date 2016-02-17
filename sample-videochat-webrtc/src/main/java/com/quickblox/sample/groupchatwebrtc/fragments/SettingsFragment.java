package com.quickblox.sample.groupchatwebrtc.fragments;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.quickblox.sample.groupchatwebrtc.R;

/**
 * QuickBlox team
 */
public class SettingsFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }
}
