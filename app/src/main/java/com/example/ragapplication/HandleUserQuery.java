package com.example.ragapplication;

import android.app.Activity;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class HandleUserQuery {
    private TextInputEditText queryEditText;
    private ImageButton uploadFilesButton, sendQueryButton;
    private Activity activity;
    private boolean isSoftKeyboardVisible = false;

    public HandleUserQuery(TextInputEditText queryEditText, ImageButton uploadFilesButton, ImageButton sendQueryButton, Activity activity) {
        this.queryEditText = queryEditText;
        this.uploadFilesButton = uploadFilesButton;
        this.sendQueryButton = sendQueryButton;
        this.activity = activity;

        this.queryEditText.setOnFocusChangeListener((v, hasFocus) -> {
            isSoftKeyboardVisible = true;
        });
    }

    public void hideKeyboardWhenClickingOutside() {
        View rootView = activity.findViewById(R.id.chatHistoryBody);

        rootView.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                v.performClick();
                if (!(v instanceof TextInputEditText) && isSoftKeyboardVisible) {
                    InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
                    View focusedView = activity.getCurrentFocus();
                    if (focusedView != null) {
                        inputMethodManager.hideSoftInputFromWindow(focusedView.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                        focusedView.clearFocus();
                        isSoftKeyboardVisible = false;
                    }
                }
            }

            return false;
        });
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
            DatabaseHelper databaseHelper = new DatabaseHelper(this.activity);
            int fileCountInRoom = databaseHelper.getFileCountInRoom(MainActivity.ROOM_ID);

            if (fileCountInRoom == 0) {
                populateChatBody(
                        "DocGPT",
                        "Please upload a file first, or wait for the processing job to complete!",
                        getDate()
                );
                queryEditText.setText("");
                return;
            }

            String query = queryEditText.getText().toString().trim();
            if (!query.isEmpty()) {
                populateChatBody(SettingsStore.userName, query, getDate());

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

                        populateChatBody("DocGPT", response, getDate());
                        queryEditText.setText("");
                        Log.d("GeminiResponse", response);
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        throwable.printStackTrace();
                    }
                });
            }

        });
    }

    private String getDate() {
        Instant instant = Instant.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd:HH-mm").withZone(ZoneId.systemDefault());

        return formatter.format(instant);
    }

    public void populateChatBody(String userName, String message, String date) {
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
