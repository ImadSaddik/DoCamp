package com.example.ragapplication;

import android.util.Log;

import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.ChatFutures;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.BlockThreshold;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.ai.client.generativeai.type.GenerationConfig;
import com.google.ai.client.generativeai.type.HarmCategory;
import com.google.ai.client.generativeai.type.SafetySetting;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

public class GeminiPro {
    public void getResponse(String query, ResponseCallback callback) {
        GenerativeModelFutures model = getModel();

        Content content = new Content.Builder().addText(query).build();
        Executor executor = Runnable::run;

        ListenableFuture<GenerateContentResponse> response = model.generateContent(content);
        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                String resultText = result.getText();
                Log.d("GeminiPro", "Received message: " + resultText);
                callback.onResponse(resultText);
            }

            @Override
            public void onFailure(Throwable throwable) {
                throwable.printStackTrace();
                Log.d("GeminiPro", "Failed to receive message: " + throwable.getMessage());
                callback.onError(throwable);
            }
        }, executor);
    }

    public void getChatResponse(String query, ResponseCallback callback) {
        GenerativeModelFutures model = getModel();
        ChatFutures chat = model.startChat();
        Content.Builder userMessageBuilder = new Content.Builder();
        userMessageBuilder.setRole("user");
        userMessageBuilder.addText(query);
        Content userMessage = userMessageBuilder.build();
        Executor executor = Runnable::run;

        ListenableFuture<GenerateContentResponse> response = chat.sendMessage(userMessage);
        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                String resultText = result.getText();
                Log.d("GeminiPro", "Received message: " + resultText);
                callback.onResponse(resultText);
            }

            @Override
            public void onFailure(Throwable throwable) {
                throwable.printStackTrace();
                Log.d("GeminiPro", "Failed to receive message: " + throwable.getMessage());
                callback.onError(throwable);
            }
        }, executor);
    }

    private GenerativeModelFutures getModel() {
        String apikey = SettingsStore.apiKey;
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