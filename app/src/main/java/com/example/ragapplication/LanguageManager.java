package com.example.ragapplication;

import android.app.Activity;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.DisplayMetrics;

import java.util.Locale;

public class LanguageManager {

    public static void changeAppLanguage(Activity activity) {
        String language = getLanguageAbbreviation(activity, SettingsStore.language);

        Locale locale = new Locale(language);
        Resources resources = activity.getResources();
        DisplayMetrics displayMetrics = resources.getDisplayMetrics();
        Configuration configuration = resources.getConfiguration();
        configuration.setLocale(locale);

        resources.updateConfiguration(configuration, displayMetrics);
    }

    private static String getLanguageAbbreviation(Activity activity, String language) {
        if (language.equals(activity.getString(R.string.english_language))) {
            return "en";
        } else if (language.equals(activity.getString(R.string.french_language))) {
            return "fr";
        }

        return "en";
    }
}
