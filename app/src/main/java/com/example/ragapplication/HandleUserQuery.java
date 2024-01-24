package com.example.ragapplication;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import com.google.android.material.textfield.TextInputEditText;

public class HandleUserQuery {
    private TextInputEditText queryEditText;
    private ImageButton uploadFilesButton, sendQueryButton;

    public HandleUserQuery(TextInputEditText queryEditText, ImageButton uploadFilesButton, ImageButton sendQueryButton) {
        this.queryEditText = queryEditText;
        this.uploadFilesButton = uploadFilesButton;
        this.sendQueryButton = sendQueryButton;
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
//                TODO : Send query to the model
            } else {
            }
        });
    }
}
