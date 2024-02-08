package com.example.ragapplication;

import android.app.Activity;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.DisplayMetrics;

import java.util.Locale;

public class LanguageManager {
    public static final String ENGLISH_CODE = "en";
    public static final String FRENCH_CODE = "fr";

    public static void changeAppLanguage(Activity activity) {
        String language = SettingsStore.languageCode;

        Locale locale = new Locale(language);
        Resources resources = activity.getResources();
        DisplayMetrics displayMetrics = resources.getDisplayMetrics();
        Configuration configuration = resources.getConfiguration();
        configuration.setLocale(locale);

        resources.updateConfiguration(configuration, displayMetrics);
    }

    public static String getLanguageCode(Activity activity, String language) {
        if (language.equals(activity.getString(R.string.english_language))) {
            return ENGLISH_CODE;
        } else if (language.equals(activity.getString(R.string.french_language))) {
            return FRENCH_CODE;
        }

        return ENGLISH_CODE;
    }

    public static String getLanguageName(Activity activity, String languageCode) {
        if (languageCode.equals(ENGLISH_CODE)) {
            return activity.getString(R.string.english_language);
        } else if (languageCode.equals(FRENCH_CODE)) {
            return activity.getString(R.string.french_language);
        }

        return activity.getString(R.string.english_language);
    }
}
