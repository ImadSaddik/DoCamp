package com.example.ragapplication;

import android.app.Activity;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputEditText;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class HandleUserQuery {
    private TextInputEditText queryEditText;
    private ImageButton uploadFilesButton, sendQueryButton;
    private Activity activity;

    public HandleUserQuery(TextInputEditText queryEditText, ImageButton uploadFilesButton, ImageButton sendQueryButton, Activity activity) {
        this.queryEditText = queryEditText;
        this.uploadFilesButton = uploadFilesButton;
        this.sendQueryButton = sendQueryButton;
        this.activity = activity;
    }

    public void swapBetweenUploadAndSend() {
        queryEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().trim().isEmpty()) {
                    uploadFilesButton.setVisibility(View.VISIBLE);
                    sendQueryButton.setVisibility(View.GONE);
                } else {
                    uploadFilesButton.setVisibility(View.GONE);
                    sendQueryButton.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    public void setupSendQueryButtonListener() {
        sendQueryButton.setOnClickListener(v -> {
            String query = queryEditText.getText().toString().trim();
            if (!query.isEmpty()) {
                populateChatBody("You", query, Instant.now().toString().substring(0, 10));

                EmbeddingModel embeddingModel = new EmbeddingModel();
                String embeddedQueryAsString = embeddingModel.getEmbedding(query).join();
                List<Double> embeddedQuery = convertStringToDoubleVector(embeddedQueryAsString);

                EntriesRetiever entriesRetiever = new EntriesRetiever(embeddedQuery, FunctionChoices.COSINE_SIMILARITY, 5);
                List<String> topChunks = entriesRetiever.retrieveEntries(activity);

                StringBuilder context = new StringBuilder();
                for (String chunk : topChunks) {
                    context.append(chunk).append("\n");
                }

                String prompt = "You will be tasked to answer questions related to a specific domain." +
                        " Try answering the following question :\n" +
                        "Question : " + query + "\n" +
                        "You might find the following context useful to answer the question :\n" +
                        "Context : " + context.toString();

                GeminiPro model = new GeminiPro();
                model.getChatResponse(prompt, new ResponseCallback() {
                    @Override
                    public void onResponse(String response) {
                        DatabaseHelper databaseHelper = new DatabaseHelper(activity);
                        databaseHelper.insertRowInChatHistory(MainActivity.ROOM_ID, query, response);

                        Instant instant = Instant.now();
                        String date = instant.toString().substring(0, 10);
                        populateChatBody("DocGPT", response, date);
                        Log.d("GeminiResponse", response);
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        throwable.printStackTrace();
                    }
                });
            } else {
            }
        });
    }

    private void populateChatBody(String userName, String message, String date) {
        LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.chat_message_block, null);

        TextView userAgentName = view.findViewById(R.id.userAgentNameTextView);
        userAgentName.setText(userName);

        TextView userAgentMessage = view.findViewById(R.id.userAgentMessageTextView);
        userAgentMessage.setText(message);

        TextView dateTextView = view.findViewById(R.id.dateTextView);
        dateTextView.setText(date);

        LinearLayout chatBodyContainer = activity.findViewById(R.id.chatBodyContainer);
        chatBodyContainer.addView(view);
    }

    private List<Double> convertStringToDoubleVector(String stringVector) {
        List<Double> doubleVector = new ArrayList<>();
        String[] stringVectorArray = stringVector.split(",");

        for (String stringElement : stringVectorArray) {
            doubleVector.add(Double.parseDouble(stringElement));
        }

        return doubleVector;
    }
}
