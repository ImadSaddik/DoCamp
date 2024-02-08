package com.example.ragapplication;

import android.app.Activity;
import android.app.UiModeManager;
import android.content.Context;
import android.content.res.Configuration;
import android.util.Log;

import androidx.appcompat.app.AppCompatDelegate;

public class ThemeManager {
    public static final String LIGHT = "Light";
    public static final String DARK = "Dark";
    public static final String SYSTEM = "System";

    public static void changeThemeBasedOnSelection(Activity activity) {
        SettingsStore.loadValuesFromSharedPreferences(activity);

        switch (SettingsStore.theme) {
            case LIGHT:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case DARK:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case SYSTEM:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
        }

        activity.recreate();
    }
}
