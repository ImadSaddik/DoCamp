package com.example.ragapplication;

import android.util.Log;

import com.google.ai.client.generativeai.java.ChatFutures;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.Executor;

public class GeminiProHandler {

    public static void getResponse(ChatFutures chatModel, String query, ResponseCallback callback) {
        Content.Builder userMessageBuilder = new Content.Builder();
        userMessageBuilder.setRole("user");
        userMessageBuilder.addText(query);
        Content userMessage = userMessageBuilder.build();

        ListenableFuture<GenerateContentResponse> response = chatModel.sendMessage(userMessage);
        Executor executor = Runnable::run;
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
}