package com.example.ragapplication;

import android.animation.AnimatorInflater;
import android.animation.StateListAnimator;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;

import org.w3c.dom.Text;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class HandleUserQuery {
    private TextInputEditText queryEditText;
    private ImageButton uploadFilesButton, sendQueryButton;
    private Activity activity;
    private boolean isSoftKeyboardVisible = false;
    private GeminiProHandler geminiProBuilder;

    public HandleUserQuery(TextInputEditText queryEditText, ImageButton uploadFilesButton,
                           ImageButton sendQueryButton, Activity activity) {
        this.queryEditText = queryEditText;
        this.uploadFilesButton = uploadFilesButton;
        this.sendQueryButton = sendQueryButton;
        this.activity = activity;

        this.queryEditText.setOnFocusChangeListener((v, hasFocus) -> {
            isSoftKeyboardVisible = true;
        });
    }

    public void hideKeyboardWhenClickingOutside(View rootView) {
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
            removeNoFilesIndicator();

            DatabaseHelper databaseHelper = new DatabaseHelper(this.activity);
            int fileCountInRoom = databaseHelper.getFileCountInRoom(MainActivity.ROOM_ID);

            if (fileCountInRoom == 0) {
                populateChatBody(
                        SettingsStore.modelName,
                        "Please upload a file first, or wait for the processing job to complete!",
                        getDate()
                );
                queryEditText.setText("");
                return;
            }

            if (!MainActivity.finishedProcessingFiles) {
                populateChatBody(
                        SettingsStore.modelName,
                        "Please wait for the processing job to complete!",
                        getDate()
                );
                queryEditText.setText("");
                return;
            }

            String query = queryEditText.getText().toString().trim();
            if (!query.isEmpty()) {
                showAdByFrequency();

                sendQueryButton.setEnabled(false);
                showResponseGenerationProgressBar(true);
                populateChatBody(SettingsStore.userName, query, getDate());

                EmbeddingModel embeddingModel = new EmbeddingModel(this.activity);
                String embeddedQueryAsString = embeddingModel.getEmbedding(query).join();
                List<Double> embeddedQuery = convertStringToDoubleVector(embeddedQueryAsString);

                EntriesRetiever entriesRetiever = new EntriesRetiever(
                        embeddedQuery,
                        SettingsStore.functionChoice,
                        SettingsStore.topKEntries
                );
                List<String> topChunks = entriesRetiever.retrieveEntries(activity);

                StringBuilder context = new StringBuilder();
                for (String chunk : topChunks) {
                    context.append(chunk).append("\n");
                }

                String prompt = PromptManager.getPrompt(query, context.toString());
                Log.d("PromptGemini", prompt);

                queryEditText.setText("");
                GeminiProHandler.getResponse(MainActivity.chatModel, prompt, new ResponseCallback() {
                    @Override
                    public void onResponse(String response) {
                        DatabaseHelper databaseHelper = new DatabaseHelper(activity);
                        databaseHelper.insertRowInChatHistory(MainActivity.ROOM_ID, query, response);

                        showResponseGenerationProgressBar(false);
                        populateChatBody(SettingsStore.modelName, response, getDate());
                        sendQueryButton.setEnabled(true);
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        String errorMessage = "There was an error while processing your request. Please try again.";
                        populateChatBody(SettingsStore.modelName, errorMessage, getDate());
                        sendQueryButton.setEnabled(true);
                    }
                });
            }
        });
    }

    private void showAdByFrequency() {
        if (AdManager.promptCount % 10 == 0) {
            AdManager adManager = new AdManager(activity);
            adManager.loadAndShowAd();
        }
        AdManager.promptCount++;
    }

    private void removeNoFilesIndicator() {
        TextView uploadFilesIndicator = activity.findViewById(R.id.uploadFilesIndicator);
        uploadFilesIndicator.setVisibility(View.GONE);
    }

    private String getDate() {
        Instant instant = Instant.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd:HH-mm").withZone(ZoneId.systemDefault());

        return formatter.format(instant);
    }

    private void showResponseGenerationProgressBar(boolean show) {
        LinearProgressIndicator responseGenerationProgressBar = activity.findViewById(R.id.responseGenerationProgressBar);

        if (show) {
            responseGenerationProgressBar.setVisibility(View.VISIBLE);
        } else {
            responseGenerationProgressBar.setVisibility(View.GONE);
        }
    }

    public void populateChatBody(String userName, String message, String date) {
        LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.chat_message_block, null);

        setupUserAgentName(view, userName);
        setupDateTextView(view, date);

        TextView userAgentMessage = setupUserAgentMessage(view, message);
        setupClickEventForMessageBlock(view, userAgentMessage);

        LinearLayout chatBodyContainer = activity.findViewById(R.id.chatBodyContainer);
        chatBodyContainer.addView(view);

        ScrollView scrollView = activity.findViewById(R.id.chatHistoryBody);
        scrollView.post(() -> scrollView.fullScroll(View.FOCUS_DOWN));
    }

    private void setupUserAgentName(View view, String userName) {
        TextView userAgentName = view.findViewById(R.id.userAgentNameTextView);
        userAgentName.setText(userName);
        userAgentName.setTextColor(ThemeUtils.getTextColorBasedOnTheme(R.attr.textPrimaryColor, activity));
    }

    private TextView setupUserAgentMessage(View view, String message) {
        String htmlMessage = Markdown2HTML.markdownToHtml(message);
        TextView userAgentMessage = view.findViewById(R.id.userAgentMessageTextView);
        userAgentMessage.setText(Html.fromHtml(htmlMessage));
        userAgentMessage.setTextColor(ThemeUtils.getTextColorBasedOnTheme(R.attr.textPrimaryColor, activity));

        return userAgentMessage;
    }

    private void setupDateTextView(View view, String date) {
        TextView dateTextView = view.findViewById(R.id.dateTextView);
        dateTextView.setText(date);
        dateTextView.setTextColor(ThemeUtils.getTextColorBasedOnTheme(R.attr.textPrimaryColor, activity));
    }

    private void setupClickEventForMessageBlock(View view, TextView userAgentMessage) {
        StateListAnimator stateListAnimator = AnimatorInflater.loadStateListAnimator(activity, R.animator.click_animation);
        view.setStateListAnimator(stateListAnimator);
        hideKeyboardWhenClickingOutside(view);

        view.setOnLongClickListener(v -> {
            ClipboardManager clipboardManager = (ClipboardManager) v.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clipData = ClipData.newPlainText("Copied text", userAgentMessage.getText());
            clipboardManager.setPrimaryClip(clipData);
            Toast.makeText(activity, "Copied to clipboard", Toast.LENGTH_SHORT).show();
            Log.d("TouchLOG", "long click");
            return true;
        });
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
