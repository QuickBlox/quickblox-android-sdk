package com.quickblox.sample.groupchatwebrtc.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.text.TextUtils;
import android.widget.Toast;

import com.quickblox.sample.groupchatwebrtc.fragments.SettingsFragment;
import com.quickblox.sample.groupchatwebrtc.R;

/**
 * QuickBlox team
 */
public class SettingsActivity extends Activity implements SharedPreferences.OnSharedPreferenceChangeListener {


    private static final int MAX_VIDEO_START_BITRATE = 2000;
    private String bitrateStringKey;
    private SettingsFragment settingsFragment;

    public static void start(Context context) {
        Intent intent = new Intent(context, SettingsActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Display the fragment as the main content.
        settingsFragment = new SettingsFragment();
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, settingsFragment)
                .commit();
        bitrateStringKey = getString(R.string.pref_startbitratevalue_key);
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
            String bitrateValue = sharedPreferences.getString(bitrateStringKey,
                    getString(R.string.pref_startbitratevalue_default));
            if (TextUtils.isEmpty(bitrateValue)){
                Toast.makeText(this, "Value can't be empty:", Toast.LENGTH_LONG).show();
                setDefaultstartingBitrate(sharedPreferences);
                return;
            }
            int startBitrate = Integer.parseInt(bitrateValue);
            if (startBitrate > MAX_VIDEO_START_BITRATE){
                Toast.makeText(this, "Max value is:"+MAX_VIDEO_START_BITRATE, Toast.LENGTH_LONG).show();
                setDefaultstartingBitrate(sharedPreferences);
            }
        }
    }

    private void setDefaultstartingBitrate(SharedPreferences sharedPreferences){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(bitrateStringKey,
                getString(R.string.pref_startbitratevalue_default));
        editor.apply();
        updateSummary(sharedPreferences, bitrateStringKey);
    }

    private void updateSummary(SharedPreferences sharedPreferences, String key) {
        Preference updatedPref = settingsFragment.findPreference(key);
        // Set summary to be the user-description for the selected value
        if (updatedPref instanceof EditTextPreference) {
            ((EditTextPreference) updatedPref).setText(sharedPreferences.getString(key, ""));
        } else {
            updatedPref.setSummary(sharedPreferences.getString(key, ""));
        }
    }
}
