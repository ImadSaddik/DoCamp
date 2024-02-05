package com.example.ragapplication;

import android.app.UiModeManager;
import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.w3c.dom.Text;

public class SettingsActivity extends AppCompatActivity {
    private ImageButton backButton, saveButton;
    private TextInputEditText apiKeyInput, userNameInput, modelNameInput,
            chunkSizeInput, overlapSizeInput, temperatureInput, topPInput,
            topKInput, maxNewTokensInput;
    private TextInputLayout apiKeyLayout, userNameLayout, modelNameLayout,
            chunkSizeLayout, overlapSizeLayout, temperatureLayout, topPLayout,
            topKLayout, maxNewTokensLayout, safetySettingsLayout, themeLayout;
    private AutoCompleteTextView safetySettingsDropdown, themeDropdown;
    private MaterialSwitch streamSwitch;
    private NetworkChangeReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

        streamSwitch = findViewById(R.id.streamSwitch);

        apiKeyInput = findViewById(R.id.apiKeyInputEditText);
        userNameInput = findViewById(R.id.userNameInputEditText);
        modelNameInput = findViewById(R.id.modelNameInputEditText);
        chunkSizeInput = findViewById(R.id.chunkSizeInputEditText);
        overlapSizeInput = findViewById(R.id.overlapSizeInputEditText);
        temperatureInput = findViewById(R.id.temperatureInputEditText);
        topPInput = findViewById(R.id.topPInputEditText);
        topKInput = findViewById(R.id.topKInputEditText);
        maxNewTokensInput = findViewById(R.id.maxNewTokensInputEditText);

        safetySettingsDropdown = findViewById(R.id.safetySettingsDropdown);
        themeDropdown = findViewById(R.id.themeDropdown);

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
    }

    private void setHyperLinks() {
        TextView apiKeyHelperText = findViewById(R.id.apiKeyHelperText);
        apiKeyHelperText.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private void setupDropdowns() {
        String[] safetySettings = getResources().getStringArray(R.array.safety_settings_items);
        String[] themes = getResources().getStringArray(R.array.theme_settings_items);

        ArrayAdapter<String> safetySettingsAdapter = new ArrayAdapter<>(
                this,
                com.google.android.material.R.layout.support_simple_spinner_dropdown_item,
                safetySettings
        );

        ArrayAdapter<String> themeAdapter = new ArrayAdapter<>(
                this,
                com.google.android.material.R.layout.support_simple_spinner_dropdown_item,
                themes
        );

        safetySettingsDropdown.setAdapter(safetySettingsAdapter);
        themeDropdown.setAdapter(themeAdapter);
    }

    private void loadSettings() {
        apiKeyInput.setText(SettingsStore.apiKey);

        userNameInput.setText(SettingsStore.userName);
        modelNameInput.setText(SettingsStore.modelName);

        themeDropdown.setText(SettingsStore.theme, false);

        chunkSizeInput.setText(String.valueOf(SettingsStore.chunkSize));
        overlapSizeInput.setText(String.valueOf(SettingsStore.overlapSize));

        temperatureInput.setText(String.valueOf(SettingsStore.temperature));
        topPInput.setText(String.valueOf(SettingsStore.topP));
        topKInput.setText(String.valueOf(SettingsStore.topK));
        maxNewTokensInput.setText(String.valueOf(SettingsStore.maxNewTokens));
        safetySettingsDropdown.setText(SettingsStore.safetySettings, false);
        streamSwitch.setChecked(SettingsStore.stream);
    }

    private void saveSettings() {
        String apiKey = apiKeyInput.getText().toString();

        String userName = userNameInput.getText().toString();
        String modelName = modelNameInput.getText().toString();

        String themeSettings = themeDropdown.getText().toString();

        String chunkSize = chunkSizeInput.getText().toString();
        String overlapSize = overlapSizeInput.getText().toString();

        String temperature = temperatureInput.getText().toString();
        String topP = topPInput.getText().toString();
        String topK = topKInput.getText().toString();
        String maxNewTokens = maxNewTokensInput.getText().toString();
        String safetySettings = safetySettingsDropdown.getText().toString();
        boolean stream = streamSwitch.isChecked();

        boolean isApiKeyValid = checkApiKeyValidity(apiKey);

        boolean isUserNameValid = checkUserNameValidity(userName);
        boolean isModelNameValid = checkModelNameValidity(modelName);

        boolean isThemeValid = checkThemeValidity(themeSettings);

        boolean isChunkSizeValid = checkChunkSizeValidity(chunkSize);
        boolean isOverlapSizeValid = checkOverlapSizeValidity(overlapSize);

        boolean isTemperatureValid = checkTemperatureValidity(temperature);
        boolean isTopPValid = checkTopPValidity(topP);
        boolean isTopKValid = checkTopKValidity(topK);
        boolean isMaxNewTokensValid = checkMaxNewTokensValidity(maxNewTokens);
        boolean isSafetySettingsValid = checkSafetySettingsValidity(safetySettings);

        if (isApiKeyValid && isUserNameValid && isModelNameValid && isChunkSizeValid
                && isOverlapSizeValid && isTemperatureValid && isTopPValid &&
                isTopKValid && isMaxNewTokensValid && isSafetySettingsValid && isThemeValid
        ) {
            SharedPreferences sharedPreferences = getSharedPreferences("Settings", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();

            editor.putString("apiKey", apiKey);

            editor.putString("userName", userName);
            editor.putString("modelName", modelName);

            editor.putInt("chunkSize", Integer.parseInt(chunkSize));
            editor.putInt("overlapSize", Integer.parseInt(overlapSize));

            editor.putFloat("temperature", Float.parseFloat(temperature));
            editor.putFloat("topP", Float.parseFloat(topP));
            editor.putInt("topK", Integer.parseInt(topK));
            editor.putInt("maxNewTokens", Integer.parseInt(maxNewTokens));
            editor.putBoolean("stream", stream);

            editor.putString("safetySettings", safetySettings);
            editor.putString("theme", themeSettings);

            editor.apply();

            SettingsStore.loadValuesFromSharedPreferences(this);
            ThemeManager.changeThemeBasedOnSelection(this);
            themeDropdown.clearFocus();
            Toast.makeText(this, "Settings Saved", Toast.LENGTH_SHORT).show();
        }
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

    private boolean checkChunkSizeValidity(String chunkSize) {
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

        chunkSizeLayout.setErrorEnabled(false);
        return true;
    }

    private boolean checkOverlapSizeValidity(String overlapSize) {
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

        overlapSizeLayout.setErrorEnabled(false);
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