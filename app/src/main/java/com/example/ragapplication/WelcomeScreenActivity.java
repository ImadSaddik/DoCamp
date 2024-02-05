package com.example.ragapplication;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.ChatFutures;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class WelcomeScreenActivity extends AppCompatActivity {
    private Button next1Button, next2Button, previous2Button, previous3Button, confirmButton;
    private TextInputLayout apiKeyLayout, userNameLayout, modelNameLayout;
    private TextInputEditText apiKeyEditText, userNameEditText, modelNameEditText;
    private RelativeLayout page1, page2, page3;
    private LinearLayout checkingAPIKeyLayout;
    private SharedPreferences sharedPreferences;
    private NetworkChangeReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sharedPreferences = getSharedPreferences("Settings", MODE_PRIVATE);

        // For debug purposes
//        SharedPreferences.Editor editor1 = sharedPreferences.edit();
//        editor1.putBoolean("tutorialDone", false);
//        editor1.apply();
        // For debug purposes

        if (sharedPreferences.getBoolean("tutorialDone", false)) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_welcome_screen);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        receiver = new NetworkChangeReceiver();
        registerReceiver(receiver, filter);

        instantiateViews();

        next1Button.setOnClickListener(v -> {
            String apiKey = apiKeyEditText.getText().toString();
            if (apiKey.isEmpty()) {
                apiKeyLayout.setError("API Key can not be empty");
                apiKeyLayout.setErrorEnabled(true);
                return;
            } else {
                apiKeyLayout.setErrorEnabled(false);
                checkAPIKeyValidity(apiKey);
            }
        });

        previous2Button.setOnClickListener(v -> {
            page1.setVisibility(RelativeLayout.VISIBLE);
            page2.setVisibility(RelativeLayout.GONE);
        });

        next2Button.setOnClickListener(v -> {
            String userName = userNameEditText.getText().toString();
            String modelName = modelNameEditText.getText().toString();

            boolean isUserNameEmpty = userName.isEmpty();
            boolean isModelNameEmpty = modelName.isEmpty();

            if (isUserNameEmpty) {
                userNameLayout.setError("Username cannot be empty");
                userNameLayout.setErrorEnabled(true);
            } else {
                userNameLayout.setErrorEnabled(false);
            }

            if (isModelNameEmpty) {
                modelNameLayout.setError("Model Name cannot be empty");
                modelNameLayout.setErrorEnabled(true);
            } else {
                modelNameLayout.setErrorEnabled(false);
            }

            if (!isUserNameEmpty && !isModelNameEmpty) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("userName", userName);
                editor.putString("modelName", modelName);
                editor.apply();

                page2.setVisibility(RelativeLayout.GONE);
                page3.setVisibility(RelativeLayout.VISIBLE);
            }
        });

        previous3Button.setOnClickListener(v -> {
            page2.setVisibility(RelativeLayout.VISIBLE);
            page3.setVisibility(RelativeLayout.GONE);
        });

        confirmButton.setOnClickListener(v -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("tutorialDone", true);
            editor.apply();

            startActivity(new Intent(this, MainActivity.class));
            finish();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
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
        next1Button = findViewById(R.id.next1Button);
        next2Button = findViewById(R.id.next2Button);
        previous2Button = findViewById(R.id.previous2Button);
        previous3Button = findViewById(R.id.previous3Button);
        confirmButton = findViewById(R.id.confirmButtonWelcomeScreen);

        apiKeyLayout = findViewById(R.id.apiKeyLayoutWelcomeScreen);
        userNameLayout = findViewById(R.id.userNameLayoutWelcomeScreen);
        modelNameLayout = findViewById(R.id.modelNameLayoutWelcomeScreen);

        apiKeyEditText = findViewById(R.id.apiKeyEditTextWelcomeScreen);
        userNameEditText = findViewById(R.id.userNameEditTextWelcomeScreen);
        modelNameEditText = findViewById(R.id.modelNameEditTextWelcomeScreen);

        page1 = findViewById(R.id.page1);
        page2 = findViewById(R.id.page2);
        page3 = findViewById(R.id.page3);

        checkingAPIKeyLayout = findViewById(R.id.apiKeyCheckingContainer);
    }

    private void checkAPIKeyValidity(String apiKey) {
        checkingAPIKeyLayout.setVisibility(LinearLayout.VISIBLE);

        GenerativeModel gm = new GenerativeModel("gemini-pro", apiKey);
        GenerativeModelFutures model = GenerativeModelFutures.from(gm);
        ChatFutures chatModel = model.startChat();
        String query = "Can you tell me how many days are in one year ?";

        GeminiProHandler.getResponse(chatModel, query, new ResponseCallback() {
            @Override
            public void onResponse(String response) {
                apiKeyLayout.setErrorEnabled(false);
                page1.setVisibility(RelativeLayout.GONE);
                page2.setVisibility(RelativeLayout.VISIBLE);
                checkingAPIKeyLayout.setVisibility(LinearLayout.GONE);

                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("apiKey", apiKey);
                editor.apply();

                Toast.makeText(WelcomeScreenActivity.this, "API Key is valid", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(Throwable throwable) {
                apiKeyLayout.setError("Invalid API Key");
                apiKeyLayout.setErrorEnabled(true);

                checkingAPIKeyLayout.setVisibility(LinearLayout.GONE);

                Log.d("API_KEY_ERROR", throwable.getMessage());
            }
        });
    }
}