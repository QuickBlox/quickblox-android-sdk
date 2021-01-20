package com.quickblox.sample.videochat.conference.java.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;

import com.quickblox.sample.videochat.conference.java.R;
import com.quickblox.sample.videochat.conference.java.fragments.SettingsFragment;
import com.quickblox.sample.videochat.conference.java.utils.Consts;
import com.quickblox.sample.videochat.conference.java.utils.ToastUtils;
import com.quickblox.sample.videochat.conference.java.views.SeekBarPreference;


public class SettingsActivity extends BaseActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final int MAX_VIDEO_START_BITRATE = 2000;

    private String bitrateStringKey;
    private SettingsFragment settingsFragment;
    private boolean isVideoSettings;

    public static void start(Context context, boolean isVideoSettings) {
        Intent intent = new Intent(context, SettingsActivity.class);
        intent.putExtra(Consts.EXTRA_SETTINGS_TYPE, isVideoSettings);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        isVideoSettings = getIntent().getBooleanExtra(Consts.EXTRA_SETTINGS_TYPE, true);
        initActionBar();

        Bundle bundle = new Bundle();
        bundle.putBoolean(Consts.EXTRA_SETTINGS_TYPE, isVideoSettings);

        settingsFragment = new SettingsFragment();
        settingsFragment.setArguments(bundle);
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, settingsFragment)
                .commit();
        bitrateStringKey = getString(R.string.pref_startbitratevalue_key);
    }

    private void initActionBar() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(isVideoSettings ? R.string.menu_settings_video : R.string.menu_settings_audio);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences sharedPreferences =
                settingsFragment.getPreferenceScreen().getSharedPreferences();
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences sharedPreferences =
                settingsFragment.getPreferenceScreen().getSharedPreferences();
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(bitrateStringKey)) {
            int bitrateValue = sharedPreferences.getInt(bitrateStringKey, Integer.parseInt(
                    getString(R.string.pref_startbitratevalue_default)));
            if (bitrateValue == 0) {
                setDefaultstartingBitrate(sharedPreferences);
                return;
            }
            if (bitrateValue > MAX_VIDEO_START_BITRATE) {
                ToastUtils.longToast(getApplicationContext(), "Max value is:" + MAX_VIDEO_START_BITRATE);
                setDefaultstartingBitrate(sharedPreferences);
            }
        }
    }

    private void setDefaultstartingBitrate(SharedPreferences sharedPreferences) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(bitrateStringKey,
                Integer.parseInt(getString(R.string.pref_startbitratevalue_default)));
        editor.apply();
        updateSummary(sharedPreferences, bitrateStringKey);
    }

    private void updateSummary(SharedPreferences sharedPreferences, String key) {
        Preference updatedPref = settingsFragment.findPreference(key);
        // Set summary to be the user-description for the selected value
        if (updatedPref instanceof EditTextPreference) {
            ((EditTextPreference) updatedPref).setText(sharedPreferences.getString(key, ""));
        } else if (updatedPref instanceof SeekBarPreference) {
            updatedPref.setSummary(String.valueOf(sharedPreferences.getInt(key, 0)));
        } else {
            updatedPref.setSummary(sharedPreferences.getString(key, ""));
        }
    }
}