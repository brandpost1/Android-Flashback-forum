package com.dev.flashback_v04.activities;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

import com.dev.flashback_v04.R;

public class SettingsActivity extends PreferenceActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        SharedPreferences appPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean darkTheme = appPrefs.getBoolean("theme_preference", false);

        if(darkTheme) {
            setTheme(R.style.Flashback_Dark);
        } else {
            setTheme(R.style.Flashback_Light);
        }


        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_general);
    }
}
