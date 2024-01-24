package com.example.ragapplication;

import android.app.Activity;
import android.net.Uri;
import android.widget.Toast;

import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.text.PDFTextStripper;

import java.io.InputStream;
import java.util.concurrent.CompletableFuture;

public class TextExtractorFromFile {
    private Activity activity;

    public TextExtractorFromFile(Activity activity) {
        this.activity = activity;
    }

    public CompletableFuture<String> extractTextFromFile(Uri fileUri, InputStream inputStream) {
        String mimeType = this.activity.getContentResolver().getType(fileUri);

        CompletableFuture<String> resultFuture = new CompletableFuture<>();

        if (mimeType.equals("application/pdf")) {
            extractTextFromPdfFile(fileUri, inputStream, resultFuture);
        } else if (mimeType.equals("text/plain")) {
            extractTextFromTextFile(fileUri, inputStream, resultFuture);
        } else {
            Toast.makeText(this.activity, "Unsupported file type!", Toast.LENGTH_SHORT).show();
            resultFuture.completeExceptionally(new UnsupportedOperationException("Unsupported file type"));
        }

        return resultFuture;
    }


    private void extractTextFromPdfFile(Uri fileUri, InputStream inputStream, CompletableFuture<String> resultFuture) {
        CompletableFuture.supplyAsync(() -> {
            String text = null;
            try {
                PDDocument document = PDDocument.load(inputStream);
                PDFTextStripper pdfStripper = new PDFTextStripper();

                text = pdfStripper.getText(document);

                document.close();
                inputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return text;
        }).thenAccept(text -> activity.runOnUiThread(() -> {
            if (text != null) {
                resultFuture.complete(text);
            } else {
                resultFuture.completeExceptionally(new RuntimeException("Error processing file!"));
            }
        }));
    }

    private void extractTextFromTextFile(Uri fileUri, InputStream inputStream, CompletableFuture<String> resultFuture) {
        CompletableFuture.supplyAsync(() -> {
            String text = null;
            try {
                byte[] buffer = new byte[inputStream.available()];
                inputStream.read(buffer);
                inputStream.close();
                text = new String(buffer);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return text;
        }).thenAccept(text -> activity.runOnUiThread(() -> {
            if (text != null) {
                resultFuture.complete(text);
            } else {
                resultFuture.completeExceptionally(new RuntimeException("Error processing file!"));
            }
        }));
    }
}
