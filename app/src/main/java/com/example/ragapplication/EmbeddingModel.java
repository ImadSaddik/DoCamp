package com.example.ragapplication;


import java.nio.charset.StandardCharsets;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

public class EmbeddingModel {

    public CompletableFuture<ArrayList<Double>> getEmbedding(String query) {
        return CompletableFuture.supplyAsync(() -> {
            ArrayList<Double> embeddingValues = new ArrayList<>();
            try {
                String apiKey = BuildConfig.apiKey;
                String apiURL = "https://generativelanguage.googleapis.com/v1beta/models/embedding-001:embedContent?key=" + apiKey;
                String content = "{ \"model\": \"models/embedding-001\", \"content\": { \"parts\":[{ \"text\": \"" + query + "\"}]}}";
                String response = sendPostRequest(apiURL, content);
                embeddingValues = parseEmbeddingResponse(response);
            } catch (IOException e) {
                e.printStackTrace();
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

    private ArrayList<Double> parseEmbeddingResponse(String response) {
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

        return embeddingValues;
    }
}
