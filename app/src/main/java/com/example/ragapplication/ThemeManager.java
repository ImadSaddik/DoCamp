package com.example.ragapplication;

import android.app.Activity;
import android.app.UiModeManager;
import android.content.Context;
import android.content.res.Configuration;
import android.util.Log;

import androidx.appcompat.app.AppCompatDelegate;

public class ThemeManager {

    public static void changeThemeBasedOnSelection(Activity activity) {
        switch (SettingsStore.theme) {
            case "Light":
                Log.d("MyAPPTheme", "Light");
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case "Dark":
                Log.d("MyAPPTheme", "Dark");
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case "System":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
        }

        activity.recreate();
    }
}
