package com.example.ragapplication;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.tom_roush.pdfbox.android.PDFBoxResourceLoader;
import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.text.PDFTextStripper;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.concurrent.CompletableFuture;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        PDFBoxResourceLoader.init(getApplicationContext());

        ImageButton uploadFilesButton = findViewById(R.id.uploadFilesButton);
        uploadFilesButton.setOnClickListener(v -> {
            // Create an Intent for text files
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");
            intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[] {"text/plain", "application/pdf"});
            startActivityForResult(intent, 1);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            try {
                Uri fileUri = data.getData();

                String fileName = null;
                Cursor cursor = getContentResolver().query(fileUri, null, null, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex != -1) {
                        fileName = cursor.getString(nameIndex);
                    }
                    cursor.close();
                }
                Toast.makeText(this, fileName, Toast.LENGTH_SHORT).show();

                String mimeType = getContentResolver().getType(fileUri);
                InputStream inputStream = getContentResolver().openInputStream(fileUri);
                TextExtractorFromFile textExtractorFromFile = new TextExtractorFromFile(this);

                if ("application/pdf".equals(mimeType)) {
                    textExtractorFromFile.extractTextFromPdfFile(fileUri, inputStream, new ResponseCallback() {
                        @Override
                        public void onResponse(String response) {
                            Toast.makeText(MainActivity.this, String.valueOf(response.length()), Toast.LENGTH_SHORT).show();
                            CharacterTextSplitter characterTextSplitter = new CharacterTextSplitter(1000, 100);
                            String[] chunks = characterTextSplitter.getChunksFromText(response);

                            Toast.makeText(MainActivity.this, "The number of chunks is : " + chunks.length, Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onError(Throwable throwable) {
                            Toast.makeText(MainActivity.this, "Error processing file!", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else if ("text/plain".equals(mimeType)) {
                    textExtractorFromFile.extractTextFromTextFile(fileUri, inputStream, new ResponseCallback() {
                        @Override
                        public void onResponse(String response) {
                            Toast.makeText(MainActivity.this, response, Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onError(Throwable throwable) {
                            Toast.makeText(MainActivity.this, "Error processing file!", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(MainActivity.this, "Unsupported file type!", Toast.LENGTH_SHORT).show();
                }
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }
}