package com.example.ragapplication;

import android.util.Log;

import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.BlockThreshold;
import com.google.ai.client.generativeai.type.GenerationConfig;
import com.google.ai.client.generativeai.type.HarmCategory;
import com.google.ai.client.generativeai.type.SafetySetting;

import java.util.Collections;

public class GeminiPro {
    public GenerativeModelFutures getModel() {
        String apikey = SettingsStore.apiKey;
        Log.d("SafetySetting", SettingsStore.safetySettings);
        SafetySetting harassmentSafety = new SafetySetting(HarmCategory.HARASSMENT,
                getBlockThreshold(SettingsStore.safetySettings));

        GenerationConfig.Builder configBuilder = new GenerationConfig.Builder();
        configBuilder.temperature = SettingsStore.temperature;
        configBuilder.topK = SettingsStore.topK;
        configBuilder.topP = SettingsStore.topP;
        GenerationConfig generationConfig = configBuilder.build();

        GenerativeModel gm = new GenerativeModel(
                "gemini-pro",
                apikey,
                generationConfig,
                Collections.singletonList(harassmentSafety)
        );

        return GenerativeModelFutures.from(gm);
    }

    private BlockThreshold getBlockThreshold(String safetySetting) {
        Log.d("SafetySetting", safetySetting);
        switch (safetySetting) {
            case "ONLY_HIGH":
                return BlockThreshold.ONLY_HIGH;
            case "MEDIUM_AND_ABOVE":
                return BlockThreshold.MEDIUM_AND_ABOVE;
            case "LOW_AND_ABOVE":
                return BlockThreshold.LOW_AND_ABOVE;
            case "UNSPECIFIED":
                return BlockThreshold.UNSPECIFIED;
            case "NONE":
                return BlockThreshold.NONE;
            default:
                return BlockThreshold.ONLY_HIGH;
        }
    }
}
