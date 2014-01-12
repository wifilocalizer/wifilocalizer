package de.kk.wifilocalizer.ui.activities;

import android.os.Bundle;
import android.preference.PreferenceActivity;

import de.kk.wifilocalizer.R;

/**
 * Adds preferences from resource preferences.xml
 */
public class SettingsActivity extends PreferenceActivity {
    @SuppressWarnings("deprecation")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }
}
