package com.example.ragapplication;

import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class SettingsActivity extends AppCompatActivity {
    private ImageButton backButton, saveButton;
    private TextInputEditText apiKeyInput, userNameInput, modelNameInput,
            chunkSizeInput, overlapSizeInput, temperatureInput, topPInput,
            topKInput, maxNewTokensInput, topKEntriesInput;
    private TextInputLayout apiKeyLayout, userNameLayout, modelNameLayout,
            chunkSizeLayout, overlapSizeLayout, temperatureLayout, topPLayout,
            topKLayout, maxNewTokensLayout, safetySettingsLayout, themeLayout,
            topKEntriesLayout, similarityFunctionLayout, languagesLayout;
    private AutoCompleteTextView safetySettingsDropdown, themeDropdown, similarityFunctionDropdown,
            languagesDropdown;
    private MaterialSwitch apiKeyDelaySwitch;
    private NetworkChangeReceiver receiver;
    public static boolean languageDialogIsVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LanguageManager.changeAppLanguage(this);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_settings);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        receiver = new NetworkChangeReceiver();
        registerReceiver(receiver, filter);

        instantiateViews();
        setHyperLinks();
        setupDropdowns();
        SettingsStore.loadValuesFromSharedPreferences(this);
        loadSettings();

        backButton.setOnClickListener(v -> finish());
        saveButton.setOnClickListener(v -> {
            saveSettings();
        });

        if (savedInstanceState != null) {
            if (savedInstanceState.getBoolean("languageDialogIsVisible")) {
                SharedPreferences sharedPreferences = getSharedPreferences("Settings", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                String newLanguageCode = savedInstanceState.getString("newLanguageCode");

                showLanguageDialog(editor, newLanguageCode);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setupDropdowns();

        if (receiver == null) {
            receiver = new NetworkChangeReceiver();
        }
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(receiver, filter);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        if (languageDialogIsVisible) {
            outState.putBoolean("languageDialogIsVisible", true);

            String newLanguageName = languagesDropdown.getText().toString();
            String newLanguageCode = LanguageManager.getLanguageCode(this, newLanguageName);
            outState.putString("newLanguageCode", newLanguageCode);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (receiver != null) {
            unregisterReceiver(receiver);
            receiver = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (receiver != null) {
            unregisterReceiver(receiver);
            receiver = null;
        }
    }

    private void instantiateViews() {
        backButton = findViewById(R.id.backToHomeButton);
        saveButton = findViewById(R.id.saveSettingsButton);

        apiKeyInput = findViewById(R.id.apiKeyInputEditText);
        userNameInput = findViewById(R.id.userNameInputEditText);
        modelNameInput = findViewById(R.id.modelNameInputEditText);
        chunkSizeInput = findViewById(R.id.chunkSizeInputEditText);
        overlapSizeInput = findViewById(R.id.overlapSizeInputEditText);
        temperatureInput = findViewById(R.id.temperatureInputEditText);
        topPInput = findViewById(R.id.topPInputEditText);
        topKInput = findViewById(R.id.topKInputEditText);
        maxNewTokensInput = findViewById(R.id.maxNewTokensInputEditText);
        topKEntriesInput = findViewById(R.id.topKEntriesInputEditText);

        safetySettingsDropdown = findViewById(R.id.safetySettingsDropdown);
        themeDropdown = findViewById(R.id.themeDropdown);
        similarityFunctionDropdown = findViewById(R.id.similarityFunctionDropdown);
        languagesDropdown = findViewById(R.id.languagesDropdown);

        apiKeyLayout = findViewById(R.id.apiKeyLayout);
        userNameLayout = findViewById(R.id.userNameLayout);
        modelNameLayout = findViewById(R.id.modelNameLayout);
        chunkSizeLayout = findViewById(R.id.chunkSizeLayout);
        overlapSizeLayout = findViewById(R.id.overlapSizeLayout);
        temperatureLayout = findViewById(R.id.temperatureLayout);
        topPLayout = findViewById(R.id.topPLayout);
        topKLayout = findViewById(R.id.topKLayout);
        maxNewTokensLayout = findViewById(R.id.maxNewTokensLayout);
        safetySettingsLayout = findViewById(R.id.safetySettingsLayout);
        themeLayout = findViewById(R.id.themeLayout);
        topKEntriesLayout = findViewById(R.id.topKEntriesLayout);
        similarityFunctionLayout = findViewById(R.id.similarityFunctionLayout);
        languagesLayout = findViewById(R.id.languagesLayout);

        apiKeyDelaySwitch = findViewById(R.id.apiKeyDelaySwitch);
    }

    private void setHyperLinks() {
        TextView apiKeyHelperText = findViewById(R.id.apiKeyHelperText);
        apiKeyHelperText.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private void setupDropdowns() {
        String[] safetySettings = getResources().getStringArray(R.array.safety_settings_items);
        String[] themes = getResources().getStringArray(R.array.theme_settings_items);
        String[] similarityFunctions = getResources().getStringArray(R.array.similarity_functions_items);
        String[] languages = getResources().getStringArray(R.array.languages_items);

        setAdapter(safetySettings, safetySettingsDropdown);
        setAdapter(themes, themeDropdown);
        setAdapter(similarityFunctions, similarityFunctionDropdown);
        setAdapter(languages, languagesDropdown);
    }

    private void setAdapter(String[] values, AutoCompleteTextView dropdown) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                com.google.android.material.R.layout.support_simple_spinner_dropdown_item,
                values
        );

        dropdown.setAdapter(adapter);
    }

    private void loadSettings() {
        apiKeyInput.setText(SettingsStore.apiKey);
        apiKeyDelaySwitch.setChecked(SettingsStore.apiKeyDelaySwitch);

        userNameInput.setText(SettingsStore.userName);
        modelNameInput.setText(SettingsStore.modelName);

        String languageCode = SettingsStore.languageCode;
        languagesDropdown.setText(LanguageManager.getLanguageName(this, languageCode), false);

        themeDropdown.setText(SettingsStore.theme, false);

        chunkSizeInput.setText(String.valueOf(SettingsStore.chunkSize));
        overlapSizeInput.setText(String.valueOf(SettingsStore.overlapSize));

        topKEntriesInput.setText(String.valueOf(SettingsStore.topKEntries));
        similarityFunctionDropdown.setText(SettingsStore.functionChoice.toString(), false);

        temperatureInput.setText(String.valueOf(SettingsStore.temperature));
        topPInput.setText(String.valueOf(SettingsStore.topP));
        topKInput.setText(String.valueOf(SettingsStore.topK));
        maxNewTokensInput.setText(String.valueOf(SettingsStore.maxNewTokens));
        safetySettingsDropdown.setText(SettingsStore.safetySettings, false);
    }

    private void saveSettings() {
        String apiKey = apiKeyInput.getText().toString();
        boolean apiKeySwitchChecked = apiKeyDelaySwitch.isChecked();

        String userName = userNameInput.getText().toString();
        String modelName = modelNameInput.getText().toString();

        String newLanguageName = languagesDropdown.getText().toString();

        String newTheme = themeDropdown.getText().toString();

        String chunkSize = chunkSizeInput.getText().toString();
        String overlapSize = overlapSizeInput.getText().toString();

        String topKEntries = topKEntriesInput.getText().toString();
        String similarityFunction = similarityFunctionDropdown.getText().toString();

        String temperature = temperatureInput.getText().toString();
        String topP = topPInput.getText().toString();
        String topK = topKInput.getText().toString();
        String maxNewTokens = maxNewTokensInput.getText().toString();
        String safetySettings = safetySettingsDropdown.getText().toString();

        boolean isApiKeyValid = checkApiKeyValidity(apiKey);

        boolean isUserNameValid = checkUserNameValidity(userName);
        boolean isModelNameValid = checkModelNameValidity(modelName);

        boolean isLanguageValid = checkLanguageValidity(newLanguageName);

        boolean isThemeValid = checkThemeValidity(newTheme);

        boolean isChunkSizeValid = checkChunkSizeValidity(chunkSize, overlapSize);
        boolean isOverlapSizeValid = checkOverlapSizeValidity(chunkSize, overlapSize);

        boolean isTopKEntriesValid = checkTopKEntriesValidity(topKEntries);
        boolean isSimilarityFunctionValid = checkSimilarityFunctionValidity(similarityFunction);

        boolean isTemperatureValid = checkTemperatureValidity(temperature);
        boolean isTopPValid = checkTopPValidity(topP);
        boolean isTopKValid = checkTopKValidity(topK);
        boolean isMaxNewTokensValid = checkMaxNewTokensValidity(maxNewTokens);
        boolean isSafetySettingsValid = checkSafetySettingsValidity(safetySettings);

        if (isApiKeyValid && isUserNameValid && isModelNameValid && isChunkSizeValid
                && isOverlapSizeValid && isTemperatureValid && isTopPValid &&
                isTopKValid && isMaxNewTokensValid && isSafetySettingsValid && isThemeValid
                && isTopKEntriesValid && isSimilarityFunctionValid && isLanguageValid
        ) {
            SharedPreferences sharedPreferences = getSharedPreferences("Settings", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();

            String oldLanguageCode = SettingsStore.languageCode;
            String oldTheme = SettingsStore.theme;

            editor.putString("apiKey", apiKey);
            editor.putBoolean("apiKeyDelaySwitch", apiKeySwitchChecked);

            editor.putString("userName", userName);
            editor.putString("modelName", modelName);

            editor.putInt("chunkSize", Integer.parseInt(chunkSize));
            editor.putInt("overlapSize", Integer.parseInt(overlapSize));

            editor.putInt("topKEntries", Integer.parseInt(topKEntries));
            editor.putString("similarityFunction", similarityFunction);

            editor.putFloat("temperature", Float.parseFloat(temperature));
            editor.putFloat("topP", Float.parseFloat(topP));
            editor.putInt("topK", Integer.parseInt(topK));
            editor.putInt("maxNewTokens", Integer.parseInt(maxNewTokens));

            editor.putString("safetySettings", safetySettings);

            editor.apply();
            SettingsStore.loadValuesFromSharedPreferences(this);

            if (!oldLanguageCode.equals(LanguageManager.getLanguageCode(this, newLanguageName))) {
                showLanguageDialog(editor, LanguageManager.getLanguageCode(this, newLanguageName));
            } else {
                if (!oldTheme.equals(newTheme)) {
                    editor.putString("theme", newTheme);
                    editor.apply();

                    ThemeManager.changeThemeBasedOnSelection(this);
                    themeDropdown.clearFocus();
                }

                Toast.makeText(this, "Settings Saved", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showLanguageDialog(SharedPreferences.Editor editor, String newLanguageCode) {
        languageDialogIsVisible = true;

        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.language_dialog, null);

        Button cancelButton = view.findViewById(R.id.cancelLanguageButton);
        Button confirmButton = view.findViewById(R.id.confirmLanguageButton);

        AlertDialog dialog = getAlertDialog(view);

        cancelButton.setOnClickListener(v -> {
            languageDialogIsVisible = false;
            dialog.dismiss();
        });

        confirmButton.setOnClickListener(v -> {
            languageDialogIsVisible = false;
            Toast.makeText(this, "Settings Saved", Toast.LENGTH_SHORT).show();

            editor.putString("language", newLanguageCode);
            editor.apply();

            restartApp();
        });

        dialog.show();
    }

    private AlertDialog getAlertDialog(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(view);

        AlertDialog dialog = builder.create();
        dialog.setOnDismissListener(dialogInterface -> languageDialogIsVisible = false);

        dialog.getWindow().setBackgroundDrawableResource(R.drawable.rounded_background_room_dialog);

        return dialog;
    }

    private void restartApp() {
        Intent restartIntent = new Intent(getApplicationContext(), MainActivity.class);
        restartIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        startActivity(restartIntent);
        finishAffinity();
    }

    private boolean checkApiKeyValidity(String apiKey) {
        if (apiKey.equals("")) {
            apiKeyLayout.setError("API Key cannot be empty");
            apiKeyLayout.setErrorEnabled(true);
            return false;
        }

        apiKeyLayout.setErrorEnabled(false);
        return true;
    }

    private boolean checkUserNameValidity(String userName) {
        if (userName.equals("")) {
            userNameLayout.setError("User Name cannot be empty");
            userNameLayout.setErrorEnabled(true);
            return false;
        }

        userNameLayout.setErrorEnabled(false);
        return true;
    }

    private boolean checkModelNameValidity(String modelName) {
        if (modelName.equals("")) {
            modelNameLayout.setError("Model Name cannot be empty");
            modelNameLayout.setErrorEnabled(true);
            return false;
        }

        modelNameLayout.setErrorEnabled(false);
        return true;
    }

    private boolean checkLanguageValidity(String language) {
        if (language.equals("")) {
            languagesLayout.setError("Language cannot be empty");
            languagesLayout.setErrorEnabled(true);
            return false;
        }

        languagesLayout.setErrorEnabled(false);
        return true;
    }

    private boolean checkChunkSizeValidity(String chunkSize, String overlapSize) {
        if (chunkSize.equals("")) {
            chunkSizeLayout.setError("Chunk Size cannot be empty");
            chunkSizeLayout.setErrorEnabled(true);
            return false;
        }

        if (Integer.parseInt(chunkSize) > 2000) {
            chunkSizeLayout.setError("Chunk Size cannot be higher than 2000");
            chunkSizeLayout.setErrorEnabled(true);
            return false;
        }

        if (Integer.parseInt(chunkSize) < Integer.parseInt(overlapSize)) {
            chunkSizeLayout.setError("Chunk Size cannot be lower than Overlap Size");
            chunkSizeLayout.setErrorEnabled(true);
            return false;
        }

        chunkSizeLayout.setErrorEnabled(false);
        return true;
    }

    private boolean checkOverlapSizeValidity(String chunkSize, String overlapSize) {
        if (overlapSize.equals("")) {
            overlapSizeLayout.setError("Overlap Size cannot be empty");
            overlapSizeLayout.setErrorEnabled(true);
            return false;
        }

        if (Integer.parseInt(overlapSize) > 500) {
            overlapSizeLayout.setError("Overlap Size cannot be higher than 500");
            overlapSizeLayout.setErrorEnabled(true);
            return false;
        }

        if (Integer.parseInt(overlapSize) > Integer.parseInt(chunkSize)) {
            overlapSizeLayout.setError("Overlap Size cannot be higher than Chunk Size");
            overlapSizeLayout.setErrorEnabled(true);
            return false;
        }

        overlapSizeLayout.setErrorEnabled(false);
        return true;
    }

    private boolean checkTopKEntriesValidity(String topKEntries) {
        if (topKEntries.equals("")) {
            topKEntriesLayout.setError("Top K Entries cannot be empty");
            topKEntriesLayout.setErrorEnabled(true);
            return false;
        }

        if (Integer.parseInt(topKEntries) > 100) {
            topKEntriesLayout.setError("Top K Entries cannot be higher than 100");
            topKEntriesLayout.setErrorEnabled(true);
            return false;
        }

        topKEntriesLayout.setErrorEnabled(false);
        return true;
    }

    private boolean checkSimilarityFunctionValidity(String similarityFunction) {
        if (similarityFunction.equals("")) {
            similarityFunctionLayout.setError("Similarity Function cannot be empty");
            similarityFunctionLayout.setErrorEnabled(true);
            return false;
        }

        similarityFunctionLayout.setErrorEnabled(false);
        return true;
    }

    private boolean checkTemperatureValidity(String temperature) {
        if (temperature.equals("")) {
            temperatureLayout.setError("Temperature cannot be empty");
            temperatureLayout.setErrorEnabled(true);
            return false;
        }

        if (Float.parseFloat(temperature) > 1.0) {
            temperatureLayout.setError("Temperature cannot be higher than 1.0");
            temperatureLayout.setErrorEnabled(true);
            return false;
        }

        temperatureLayout.setErrorEnabled(false);
        return true;
    }

    private boolean checkTopPValidity(String topP) {
        if (topP.equals("")) {
            topPLayout.setError("Top P cannot be empty");
            topPLayout.setErrorEnabled(true);
            return false;
        }

        if (Float.parseFloat(topP) > 1.0) {
            topPLayout.setError("Top P cannot be higher than 1.0");
            topPLayout.setErrorEnabled(true);
            return false;
        }

        topPLayout.setErrorEnabled(false);
        return true;
    }

    private boolean checkTopKValidity(String topK) {
        if (topK.equals("")) {
            topKLayout.setError("Top K cannot be empty");
            topKLayout.setErrorEnabled(true);
            return false;
        }

        if (Integer.parseInt(topK) > 100) {
            topKLayout.setError("Top K cannot be higher than 100");
            topKLayout.setErrorEnabled(true);
            return false;
        }

        topKLayout.setErrorEnabled(false);
        return true;
    }

    private boolean checkMaxNewTokensValidity(String maxNewTokens) {
        if (maxNewTokens.equals("")) {
            maxNewTokensLayout.setError("Max New Tokens cannot be empty");
            maxNewTokensLayout.setErrorEnabled(true);
            return false;
        }

        if (Integer.parseInt(maxNewTokens) > 2048) {
            maxNewTokensLayout.setError("Max New Tokens cannot be higher than 2048");
            maxNewTokensLayout.setErrorEnabled(true);
            return false;
        }

        maxNewTokensLayout.setErrorEnabled(false);
        return true;
    }

    private boolean checkSafetySettingsValidity(String safetySettings) {
        if (safetySettings.equals("")) {
            safetySettingsLayout.setError("Safety Settings cannot be empty");
            safetySettingsLayout.setErrorEnabled(true);
            return false;
        }

        safetySettingsLayout.setErrorEnabled(false);
        return true;
    }

    private boolean checkThemeValidity(String themeSettings) {
        if (themeSettings.equals("")) {
            themeLayout.setError("Theme cannot be empty");
            themeLayout.setErrorEnabled(true);
            return false;
        }

        themeLayout.setErrorEnabled(false);
        return true;
    }
}