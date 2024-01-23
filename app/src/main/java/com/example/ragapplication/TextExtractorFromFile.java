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

    public void extractTextFromPdfFile(Uri fileUri, InputStream inputStream, ResponseCallback callback) {
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
                callback.onResponse(text);
            } else {
                callback.onError(new Throwable("Error processing file!"));
            }
        }));
    }

    public void extractTextFromTextFile(Uri fileUri, InputStream inputStream, ResponseCallback callback) {
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
                callback.onResponse(text);
            } else {
                callback.onError(new Throwable("Error processing file!"));
            }
        }));
    }
}
