package com.example.ragapplication;

import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public class SettingsStore {
    public static String apiKey;
    public static String userName;
    public static String modelName;
    public static boolean stream;
    public static int chunkSize;
    public static int overlapSize;
    public static float temperature;
    public static float topP;
    public static int topK;
    public static int maxNewTokens;
    public static String safetySettings;
    public static String theme;

    public static void loadValuesFromSharedPreferences(Activity context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("Settings", context.MODE_PRIVATE);

        apiKey = sharedPreferences.getString("apiKey", BuildConfig.apiKey);

        userName = sharedPreferences.getString("userName", "YOU");
        modelName = sharedPreferences.getString("modelName", "DOCGPT");

        theme = sharedPreferences.getString("theme", "Light");

        chunkSize = sharedPreferences.getInt("chunkSize", 1000);
        overlapSize = sharedPreferences.getInt("overlapSize", 100);

        temperature = sharedPreferences.getFloat("temperature", 0.9f);
        topP = sharedPreferences.getFloat("topP", 0.1f);
        topK = sharedPreferences.getInt("topK", 16);
        maxNewTokens = sharedPreferences.getInt("maxNewTokens", 2048);
        safetySettings = sharedPreferences.getString("safetySettings", "ONLY_HIGH");
        stream = sharedPreferences.getBoolean("stream", false);

        Log.d("SettingsStore", "Loaded settings from SharedPreferences");
        Log.d("SettingsStore", "apiKey: " + apiKey);

        Log.d("SettingsStore", "userName: " + userName);
        Log.d("SettingsStore", "modelName: " + modelName);

        Log.d("SettingsStore", "theme: " + theme);
        Log.d("SettingsStore", "chunkSize: " + chunkSize);

        Log.d("SettingsStore", "overlapSize: " + overlapSize);
        Log.d("SettingsStore", "temperature: " + temperature);

        Log.d("SettingsStore", "topP: " + topP);
        Log.d("SettingsStore", "topK: " + topK);
        Log.d("SettingsStore", "maxNewTokens: " + maxNewTokens);
        Log.d("SettingsStore", "safetySettings: " + safetySettings);
        Log.d("SettingsStore", "stream: " + stream);
    }
}
