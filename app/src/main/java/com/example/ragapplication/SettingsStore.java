package com.example.ragapplication;

import android.app.Activity;
import android.content.SharedPreferences;
import android.util.Log;

public class SettingsStore {
    public static String apiKey;
    public static String userName;
    public static String modelName;
    public static int chunkSize;
    public static int overlapSize;
    public static int topKEntries;
    public static FunctionChoices functionChoice;
    public static float temperature;
    public static float topP;
    public static int topK;
    public static int maxNewTokens;
    public static String safetySettings;
    public static String theme;
    public static String languageCode;

    public static void loadValuesFromSharedPreferences(Activity context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("Settings", context.MODE_PRIVATE);

        apiKey = sharedPreferences.getString("apiKey", BuildConfig.apiKey);

        userName = sharedPreferences.getString("userName", "YOU");
        modelName = sharedPreferences.getString("modelName", "DOCGPT");

        languageCode = sharedPreferences.getString("language", LanguageManager.ENGLISH_CODE);

        theme = sharedPreferences.getString("theme", "Light");

        chunkSize = sharedPreferences.getInt("chunkSize", 1000);
        overlapSize = sharedPreferences.getInt("overlapSize", 100);

        topKEntries = sharedPreferences.getInt("topKEntries", 5);
        functionChoice = FunctionChoices.valueOf(sharedPreferences.getString("similarityFunction", FunctionChoices.COSINE.toString()));

        temperature = sharedPreferences.getFloat("temperature", 0.9f);
        topP = sharedPreferences.getFloat("topP", 0.1f);
        topK = sharedPreferences.getInt("topK", 16);
        maxNewTokens = sharedPreferences.getInt("maxNewTokens", 2048);
        safetySettings = sharedPreferences.getString("safetySettings", "ONLY_HIGH");
    }
}
