package com.quickblox.sample.videochat.conference.java.fragments;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.quickblox.sample.videochat.conference.java.R;
import com.quickblox.sample.videochat.conference.java.utils.Consts;

public class SettingsFragment extends PreferenceFragment {

    public SettingsFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        boolean isVideoSettings = this.getArguments().getBoolean(Consts.EXTRA_SETTINGS_TYPE);
        if (isVideoSettings) {
            addPreferencesFromResource(R.xml.preferences_video);
        } else {
            addPreferencesFromResource(R.xml.preferences_audio);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);
        if (v != null) {
            ListView lv = (ListView) v.findViewById(android.R.id.list);
            lv.setPadding(0, 0, 0, 0);
        }
        return v;
    }
}