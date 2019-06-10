package com.quickblox.sample.videochat.conference.kotlin.activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.EditTextPreference
import com.quickblox.sample.videochat.conference.kotlin.R
import com.quickblox.sample.videochat.conference.kotlin.fragments.SettingsFragment
import com.quickblox.sample.videochat.conference.kotlin.utils.longToast
import com.quickblox.sample.videochat.conference.kotlin.view.SeekBarPreference


class SettingsActivity : BaseActivity(), SharedPreferences.OnSharedPreferenceChangeListener {

    private lateinit var bitrateStringKey: String
    private lateinit var settingsFragment: SettingsFragment

    companion object {
        private const val MAX_VIDEO_START_BITRATE = 2000

        fun start(context: Context) {
            context.startActivity(Intent(context, SettingsActivity::class.java))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.title = getString(R.string.actionbar_title_settings)

        // Display the fragment as the main content.
        settingsFragment = SettingsFragment()
        fragmentManager.beginTransaction()
                .replace(android.R.id.content, settingsFragment)
                .commit()
        bitrateStringKey = getString(R.string.pref_startbitratevalue_key)
    }

    override fun onResume() {
        super.onResume()
        val sharedPreferences = settingsFragment.preferenceScreen.sharedPreferences
        sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        super.onPause()
        val sharedPreferences = settingsFragment.preferenceScreen.sharedPreferences
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key == bitrateStringKey) {
            val bitrateValue = sharedPreferences?.getInt(bitrateStringKey, Integer.parseInt(
                    getString(R.string.pref_startbitratevalue_default)))
            if (bitrateValue == 0) {
                setDefaultStartingBitrate(sharedPreferences)
                return
            }
            if (bitrateValue as Int > MAX_VIDEO_START_BITRATE) {
                longToast("Max value is:$MAX_VIDEO_START_BITRATE")
                setDefaultStartingBitrate(sharedPreferences)
            }
        }
    }

    private fun setDefaultStartingBitrate(sharedPreferences: SharedPreferences) {
        val editor = sharedPreferences.edit()
        editor.putInt(bitrateStringKey, Integer.parseInt(getString(R.string.pref_startbitratevalue_default))).apply()
        updateSummary(sharedPreferences, bitrateStringKey)
    }

    private fun updateSummary(sharedPreferences: SharedPreferences, key: String) {
        val updatedPref = settingsFragment.findPreference(key)
        // Set summary to be the user-description for the selected value
        if (updatedPref is EditTextPreference) {
            updatedPref.text = sharedPreferences.getString(key, "")
        } else if (updatedPref is SeekBarPreference) {
            updatedPref.setSummary(sharedPreferences.getInt(key, 0).toString())
        } else {
            updatedPref?.summary = sharedPreferences.getString(key, "")
        }
    }
}