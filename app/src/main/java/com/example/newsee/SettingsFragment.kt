package com.example.newsee

import android.os.Bundle
import androidx.preference.MultiSelectListPreference
import androidx.preference.PreferenceFragmentCompat

class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
        val weeklySummaryListPref =
            findPreference("multiSelectListPreference") as MultiSelectListPreference?
        // Set the entries
        weeklySummaryListPref!!.entries = arrayOf<CharSequence>("Yahoo ニュース API")
        weeklySummaryListPref!!.entryValues =
            arrayOf<CharSequence>("yahoo")
        // Lastly, reenable the preference
        weeklySummaryListPref!!.isEnabled = true
        weeklySummaryListPref!!.setDefaultValue("yahoo")
    }
}