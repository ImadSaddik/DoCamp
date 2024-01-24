package com.example.ragapplication;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;

public class FileProcessor {
    private Activity activity;
    private CountDownLatch latch;
    private ProgressBar processingTextProgressBar;
    private TextView processingTextProgressDescription;

    public FileProcessor(Activity activity, CountDownLatch latch, ProgressBar processingTextProgressBar, TextView processingTextProgressDescription) {
        this.activity = activity;
        this.latch = latch;
        this.processingTextProgressBar = processingTextProgressBar;
        this.processingTextProgressDescription = processingTextProgressDescription;
    }

    public void processFile(Uri fileUri) {
        try {
            String fileName = removeExtension(getFileName(fileUri));
            String mimeType = this.activity.getContentResolver().getType(fileUri);

            String textDescription = "Processing " + fileName + "...";
            InputStream inputStream = this.activity.getContentResolver().openInputStream(fileUri);
            TextExtractorFromFile textExtractorFromFile = new TextExtractorFromFile(this.activity);

            CompletableFuture<String> resultFuture = textExtractorFromFile.extractTextFromFile(fileUri, inputStream);
            EmbeddingModel embeddingModel = new EmbeddingModel();
            CharacterTextSplitter characterTextSplitter = new CharacterTextSplitter(1000, 100);

            resultFuture.thenAccept(result -> {
                String[] chunks = characterTextSplitter.getChunksFromText(result);
                embeddingModel.embedChunks(chunks).thenAccept(embeddings -> {
                    updateProgress(1, textDescription);
                    DatabaseHelper databaseHelper = new DatabaseHelper(this.activity);

                    for (int i = 0; i < chunks.length; i++) {
                        databaseHelper.insertEmbedding(
                                chunks[i],
                                embeddings.get(i),
                                MainActivity.ROOM_ID,
                                databaseHelper.getFileId(fileName, FileUtilities.getFileType(mimeType))
                        );
                    }

                }).exceptionally(ex -> {
                    ex.printStackTrace();
                    return null;
                });
            }).exceptionally(ex -> {
                ex.printStackTrace();
                updateProgress(1, textDescription);
                return null;
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateProgress(int progress, String description) {
        this.activity.runOnUiThread(() -> {
            this.processingTextProgressBar.incrementProgressBy(progress);
            this.processingTextProgressDescription.setText(description);
        });
        latch.countDown();
    }

    private String getFileName(Uri fileUri) {
        Cursor cursor = this.activity.getContentResolver().query(fileUri, null, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            if (nameIndex != -1) {
                return cursor.getString(nameIndex);
            }
            cursor.close();
        }

        return null;
    }

    private String removeExtension(String fileName) {
        return fileName.split("\\.")[0];
    }

    private String getFileType(String mimeType) {
        if (mimeType.equals("application/pdf")) {
            return "pdf";
        } else if (mimeType.equals("text/plain")) {
            return "txt";
        } else {
            return "unknown";
        }
    }
}
