package com.example.ragapplication;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

public class APIKeyVerifier {
    public static CompletableFuture<String> testAPIKey(String apiKey, String query) {
        return CompletableFuture.supplyAsync(() -> {
            String response = "";
            try {
                String apiURL = "https://generativelanguage.googleapis.com/v1beta/models/embedding-001:embedContent?key=" + apiKey;
                String escapedQuery = query.replace("\"", "\\\"").replace("'", "\\'");
                String content = "{ \"model\": \"models/embedding-001\", \"content\": { \"parts\":[{ \"text\": \"" + escapedQuery + "\"}]}}";
                response = sendPostRequest(apiURL, content);
                Log.d("EmbeddingResponse", response);
            } catch (IOException e) {
                Log.d("Embedding Error", e.getMessage() + "\n" + query);
                throw new RuntimeException(e);
            }
            return response;
        });
    }

    private static String sendPostRequest(String apiUrl, String content) throws IOException {
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
}
