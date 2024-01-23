package com.example.ragapplication;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;

import android.view.MotionEvent;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.GestureDetectorCompat;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView leftNavigationView, rightNavigationView;
    private GestureDetectorCompat gestureDetector;
    private ImageView leftNavigationDrawerIcon, rightNavigationDrawerIcon;
    private ImageButton uploadFilesButton;
    private Button processFilesButton;
    private ProgressBar processingTextProgressBar;
    private LinearLayout processingTextProgressContainer;
    private TextView roomNameTextView, processingTextProgressDescription;
    private RelativeLayout roomNameBackground;
    private DatabaseHelper databaseHelper;
    private SQLiteDatabase sqLiteDatabase;
    private Map<String, Uri> filesUriStore;

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

        instantiateViews();
        instantiateObjects();

        setNavigationDrawerListeners();
        setRoomNameListeners();
        setActionButtonsListeners();

        databaseHelper.numberOfEntriesInEachTable(this);
        databaseHelper.logEmbeddingTable(this);
    }

    private void instantiateViews() {
        drawerLayout = findViewById(R.id.drawerLayout);
        leftNavigationView = findViewById(R.id.leftNavigationView);
        rightNavigationView = findViewById(R.id.rightNavigationView);
        leftNavigationDrawerIcon = findViewById(R.id.leftNavigationDrawerIcon);
        rightNavigationDrawerIcon = findViewById(R.id.rightNavigationDrawerIcon);
        uploadFilesButton = findViewById(R.id.uploadFilesButton);
        roomNameTextView = findViewById(R.id.roomNameTextView);
        roomNameBackground = findViewById(R.id.roomNameBackground);

        View rightHeaderView = rightNavigationView.getHeaderView(0);
        processFilesButton = rightHeaderView.findViewById(R.id.processFilesButton);
        processingTextProgressBar = rightHeaderView.findViewById(R.id.processingTextProgressBar);
        processingTextProgressDescription = rightHeaderView.findViewById(R.id.processingTextProgressDescription);
        processingTextProgressContainer = rightHeaderView.findViewById(R.id.processingTextProgressContainer);
    }

    private void instantiateObjects() {
        PDFBoxResourceLoader.init(getApplicationContext());
        gestureDetector = new GestureDetectorCompat(this, new HandleSwipeAndDrawers(this, drawerLayout));

        databaseHelper = new DatabaseHelper(this);
        sqLiteDatabase = databaseHelper.getWritableDatabase();

        filesUriStore = new HashMap<>();
    }

    private void setNavigationDrawerListeners() {
        leftNavigationDrawerIcon.setOnClickListener(v -> {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START);
            } else {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        rightNavigationDrawerIcon.setOnClickListener(v -> {
            if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
                drawerLayout.closeDrawer(GravityCompat.END);
            } else {
                drawerLayout.openDrawer(GravityCompat.END);
            }
        });
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        gestureDetector.onTouchEvent(ev);
        return super.dispatchTouchEvent(ev);
    }

    private void setRoomNameListeners() {
        roomNameTextView.setOnClickListener(v -> {
            showInputDialog();
        });
        roomNameBackground.setOnClickListener(v -> {
            showInputDialog();
        });
    }

    private void showInputDialog() {
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.room_name_dialog, null);

        TextInputEditText editText = view.findViewById(R.id.roomNameEditText);
        Button cancelButton = view.findViewById(R.id.cancelButton);
        Button confirmButton = view.findViewById(R.id.confirmButton);

        AlertDialog dialog = getAlertDialog(view);
        setAlertDialogButtonsListeners(dialog, editText, cancelButton, confirmButton);

        dialog.show();
    }

    private void setAlertDialogButtonsListeners(AlertDialog dialog, TextInputEditText editText, Button cancelButton, Button confirmButton) {
        cancelButton.setOnClickListener(v -> {
            dialog.dismiss();
        });

        confirmButton.setOnClickListener(v -> {
            String newText = editText.getText().toString();
            roomNameTextView.setText(newText);
            dialog.dismiss();
        });
    }

    private AlertDialog getAlertDialog(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(view);

        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.rounded_background_room_dialog);

        return dialog;
    }

    private void setActionButtonsListeners() {
        uploadFilesButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");
            intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"text/plain", "application/pdf"});
            startActivityForResult(intent, 1);
        });

        processFilesButton.setOnClickListener(v -> {
            if (filesUriStore.isEmpty()) {
                Toast.makeText(this, "No files to process!", Toast.LENGTH_SHORT).show();
                return;
            }

            showProgressContainer();
            processFilesButton.setEnabled(false);
            processingTextProgressDescription.setText(getString(R.string.started_processing_files));

            CountDownLatch latch = new CountDownLatch(filesUriStore.size());
            for (Map.Entry<String, Uri> entry : filesUriStore.entrySet()) {
                try {
                    processFiles(entry.getValue(), latch);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }

            new Thread(() -> {
                try {
                    latch.await();
                    runOnUiThread(() -> {
                        processingTextProgressContainer.setVisibility(View.GONE);
                        processFilesButton.setEnabled(true);
                        databaseHelper.numberOfEntriesInEachTable(this);
                        Toast.makeText(this, "Finished processing files!", Toast.LENGTH_SHORT).show();
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        });
    }

    private void showProgressContainer() {
        processingTextProgressContainer.setVisibility(View.VISIBLE);

        processingTextProgressBar.setMax(filesUriStore.size());
        processingTextProgressBar.setProgress(0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            try {
                Uri fileUri = data.getData();
                String fileName = getFileName(fileUri);
                String mimeType = getContentResolver().getType(fileUri);

                if (filesUriStore.containsKey(fileName)) {
                    Toast.makeText(this, "File already added!", Toast.LENGTH_SHORT).show();
                } else {
                    filesUriStore.put(fileName, fileUri);
                    addFileToLeftNavigationDrawer(mimeType, fileName);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private String getFileName(Uri fileUri) {
        Cursor cursor = getContentResolver().query(fileUri, null, null, null, null);

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

    private void addFileToLeftNavigationDrawer(String mimeType, String fileName) {
        if ("application/pdf".equals(mimeType)) {
            addFileToContainer(fileName, R.id.linearLayoutPDFContainer);
        } else if ("text/plain".equals(mimeType)) {
            addFileToContainer(fileName, R.id.linearLayoutTxtContainer);
        } else {
            Toast.makeText(MainActivity.this, "Unsupported file type!", Toast.LENGTH_SHORT).show();
        }
    }

    private void addFileToContainer(String fileName, int containerId) {
        LinearLayout rowsContainer = findViewById(containerId);

        TextView noFileFoundTextView = getNoFileFoundTextView(rowsContainer);
        if (noFileFoundTextView != null && noFileFoundTextView.getVisibility() == View.VISIBLE) {
            noFileFoundTextView.setVisibility(View.GONE);
        }

        LinearLayout fileRowLayout = getFileRowLayout();
        TextView fileNameTextView = getfileNameTextView(fileName);
        ImageButton removeButton = getRemoveButton(rowsContainer, fileRowLayout, noFileFoundTextView);

        fileRowLayout.addView(fileNameTextView);
        fileRowLayout.addView(removeButton);

        rowsContainer.addView(fileRowLayout);
    }

    private LinearLayout getFileRowLayout() {
        LinearLayout fileRowLayout = new LinearLayout(this);
        fileRowLayout.setOrientation(LinearLayout.HORIZONTAL);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        layoutParams.setMargins(0, 0, 0, getPixelValue(8));
        fileRowLayout.setLayoutParams(layoutParams);

        return fileRowLayout;
    }

    private TextView getfileNameTextView(String fileName) {
        TextView fileNameTextView = new TextView(this);

        fileNameTextView.setText(fileName);
        fileNameTextView.setTextSize(16);
        fileNameTextView.setMaxLines(1);
        fileNameTextView.setEllipsize(TextUtils.TruncateAt.END);
        fileNameTextView.setTypeface(getResources().getFont(R.font.roboto));
        fileNameTextView.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        fileNameTextView.setPadding(0, 0, getPixelValue(8), 0);

        return fileNameTextView;
    }

    private ImageButton getRemoveButton(LinearLayout rowsContainer, LinearLayout fileRowLayout, TextView noFileFoundTextView) {
        ImageButton removeButton = new ImageButton(this);

        removeButton.setImageResource(R.drawable.remove_button);
        removeButton.setLayoutParams(new LinearLayout.LayoutParams(
                getPixelValue(20),
                getPixelValue(20))
        );
        removeButton.setBackground(null);

        removeButton.setOnClickListener(v -> {
            String fileName = ((TextView) fileRowLayout.getChildAt(0)).getText().toString();
            filesUriStore.remove(fileName);
            rowsContainer.removeView(fileRowLayout);
            int containerChildCount = rowsContainer.getChildCount();
            if (containerChildCount == 1 && noFileFoundTextView != null) {
                noFileFoundTextView.setVisibility(View.VISIBLE);
            }
        });

        return removeButton;
    }

    private int getPixelValue(int dpValue) {
        float scale = getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale);
    }

    private TextView getNoFileFoundTextView(LinearLayout container) {
        for (int i = 0; i < container.getChildCount(); i++) {
            View view = container.getChildAt(i);
            if (view instanceof TextView) {
                TextView textView = (TextView) view;
                if (textView.getText().toString().equals(getString(R.string.no_file_found))) {
                    return textView;
                }
            }
        }
        return null;
    }

    private void processFiles(Uri fileUri, CountDownLatch latch) throws FileNotFoundException {
        String mimeType = getContentResolver().getType(fileUri);
        String textDescription = "Processing " + getFileName(fileUri);
        InputStream inputStream = getContentResolver().openInputStream(fileUri);
        TextExtractorFromFile textExtractorFromFile = new TextExtractorFromFile(this);

        if ("application/pdf".equals(mimeType)) {
            String fileName = getFileName(fileUri);
            fileName = removeExtension(fileName);
            databaseHelper.insertFile(fileName, "PDF", null);
            textExtractorFromFile.extractTextFromPdfFile(fileUri, inputStream, new ResponseCallback() {
                @Override
                public void onResponse(String response) {
                    CharacterTextSplitter characterTextSplitter = new CharacterTextSplitter(1000, 100);
                    String[] chunks = characterTextSplitter.getChunksFromText(response);
                    List<ArrayList<Double>> listOfEmbeddings = embedChunks(chunks);
                    List<String> listOfEmbeddingsAsString = convertEmbeddingsToString(listOfEmbeddings);

                    for (int i = 0; i < chunks.length; i++) {
                        int fileId = databaseHelper.getFileId(removeExtension(getFileName(fileUri)), "PDF");
                        if (fileId != -1) {
                            databaseHelper.insertEmbedding(chunks[i], listOfEmbeddingsAsString.get(i), null, 1);
                        }
                    }

                    runOnUiThread(() -> {
                        processingTextProgressBar.incrementProgressBy(1);
                        processingTextProgressDescription.setText(textDescription);
                    });
                    latch.countDown();
                }

                @Override
                public void onError(Throwable throwable) {
                    Toast.makeText(MainActivity.this, "Error processing file!", Toast.LENGTH_SHORT).show();
                    latch.countDown();
                }
            });

        } else if ("text/plain".equals(mimeType)) {
            String fileName = getFileName(fileUri);
            fileName = removeExtension(fileName);
            databaseHelper.insertFile(fileName, "TXT", null);
            textExtractorFromFile.extractTextFromTextFile(fileUri, inputStream, new ResponseCallback() {
                @Override
                public void onResponse(String response) {
                    CharacterTextSplitter characterTextSplitter = new CharacterTextSplitter(1000, 100);
                    String[] chunks = characterTextSplitter.getChunksFromText(response);
                    List<ArrayList<Double>> listOfEmbeddings = embedChunks(chunks);
                    List<String> listOfEmbeddingsAsString = convertEmbeddingsToString(listOfEmbeddings);

                    for (int i = 0; i < chunks.length; i++) {
                        int fileId = databaseHelper.getFileId(removeExtension(getFileName(fileUri)), "TXT");
                        if (fileId != -1) {
                            databaseHelper.insertEmbedding(chunks[i], listOfEmbeddingsAsString.get(i), null, 1);
                        }
                    }

                    runOnUiThread(() -> {
                        processingTextProgressBar.incrementProgressBy(1);
                        processingTextProgressDescription.setText(textDescription);
                    });
                    latch.countDown();
                }

                @Override
                public void onError(Throwable throwable) {
                    Toast.makeText(MainActivity.this, "Error processing file!", Toast.LENGTH_SHORT).show();
                    latch.countDown();
                }
            });
        } else {
            Toast.makeText(MainActivity.this, "Unsupported file type!", Toast.LENGTH_SHORT).show();
            latch.countDown();
        }
    }

    private List<ArrayList<Double>> embedChunks(String[] chunks) {
        EmbeddingModel embeddingModel = new EmbeddingModel();
        List<ArrayList<Double>> listOfEmbeddings = new ArrayList<>();

        for (String chunk : chunks) {
            try {
                embeddingModel.getEmbedding(chunk).thenAccept(embedding -> {
                    listOfEmbeddings.add(embedding);
                    Log.d("EmbeddingOutputut", embedding.toString());
                });
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        return listOfEmbeddings;
    }

    private List<String> convertEmbeddingsToString(List<ArrayList<Double>> listOfEmbeddings) {
        List<String> listOfEmbeddingsAsString = new ArrayList<>();
        StringBuilder stringBuilder = new StringBuilder();
        for (ArrayList<Double> embedding : listOfEmbeddings) {
            for (Double value : embedding) {
                stringBuilder.append(value);
                stringBuilder.append(",");
            }
            if (stringBuilder.length() > 0) {
                stringBuilder.deleteCharAt(stringBuilder.length() - 1);
            }
            String embeddingAsString = stringBuilder.toString();
            listOfEmbeddingsAsString.add(embeddingAsString);
        }

        return listOfEmbeddingsAsString;
    }
}