package com.example.ragapplication;


import android.util.Log;

import java.nio.charset.StandardCharsets;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;

public class EmbeddingModel {

    public CompletableFuture<String> getEmbedding(String query) {
        return CompletableFuture.supplyAsync(() -> {
            String embeddingValues = "";
            try {
                String apiKey = BuildConfig.apiKey;
                String apiURL = "https://generativelanguage.googleapis.com/v1beta/models/embedding-001:embedContent?key=" + apiKey;
                String escapedQuery = query.replace("\"", "\\\"").replace("'", "\\'");
                String content = "{ \"model\": \"models/embedding-001\", \"content\": { \"parts\":[{ \"text\": \"" + escapedQuery + "\"}]}}";
                String response = sendPostRequest(apiURL, content);
                embeddingValues = parseEmbeddingResponse(response);
                Log.d("Embedding Response", embeddingValues);
            } catch (IOException e) {
                e.printStackTrace();
                Log.d("Embedding Error", e.getMessage() + "\n" + query);
            }
            return embeddingValues;
        });
    }

    private String sendPostRequest(String apiUrl, String content) throws IOException {
        URL url = new URL(apiUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        // Set the request method to POST
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);

        // Write the content to the request body
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = content.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        // Get the HTTP response
        try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder response = new StringBuilder();
            String responseLine = null;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            return response.toString();
        }
    }

    private String parseEmbeddingResponse(String response) {
        ArrayList<Double> embeddingValues = new ArrayList<>();
        String valuesStartTag = "\"values\": [";
        String valuesEndTag = "]";
        int startIdx = response.indexOf(valuesStartTag);
        int endIdx = response.indexOf(valuesEndTag, startIdx + valuesStartTag.length());

        if (startIdx != -1 && endIdx != -1) {
            String valuesString = response.substring(startIdx + valuesStartTag.length(), endIdx);
            String[] valuesArray = valuesString.split(",");
            for (String value : valuesArray) {
                embeddingValues.add(Double.parseDouble(value.trim()));
            }
        }

        return convertEmbeddingToString(embeddingValues);
    }

    private String convertEmbeddingToString(ArrayList<Double> embeddingValues) {
        Log.d("Embedding Before", embeddingValues.toString());
        StringBuilder embeddingString = new StringBuilder();
        for (Double value : embeddingValues) {
            embeddingString.append(value.toString());
            embeddingString.append(",");
        }
        Log.d("Embedding After", embeddingString.toString());
        if (embeddingString.length() > 0) {
            embeddingString.deleteCharAt(embeddingString.length() - 1);
        }

        return embeddingString.toString();
    }

    public CompletableFuture<List<String>> embedChunks(String[] chunks) {
        List<CompletableFuture<String>> futures = new ArrayList<>();
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        for (String chunk : chunks) {
            futures.add(CompletableFuture.supplyAsync(() -> {
                try {
                    Thread.sleep(1500);
                    return getEmbedding(chunk).join();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(e);
                }
            }, scheduler).thenApply(embedding -> {
                Log.d("Embedding Sleep", embedding);
                return embedding;
            }));
        }

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> futures.stream()
                        .map(CompletableFuture::join)
                        .collect(Collectors.toList()));
    }
}
