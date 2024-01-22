package com.example.ragapplication;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

import org.w3c.dom.Text;

import java.io.FileNotFoundException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView leftNavigationView, rightNavigationView;
    private GestureDetectorCompat gestureDetector;
    private ImageView leftNavigationDrawerIcon, rightNavigationDrawerIcon;
    private ImageButton uploadFilesButton;
    private TextView roomNameTextView;
    private RelativeLayout roomNameBackground;
    private DatabaseHelper databaseHelper;
    private SQLiteDatabase sqLiteDatabase;

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
        instatiateViews();
        PDFBoxResourceLoader.init(getApplicationContext());
        gestureDetector = new GestureDetectorCompat(this, new HandleSwipeAndDrawers(this, drawerLayout));

        databaseHelper = new DatabaseHelper(this);
        sqLiteDatabase = databaseHelper.getWritableDatabase();

        // For debugging purposes
        // databaseHelper.numberOfEntriesInEachTable(this);

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

        uploadFilesButton.setOnClickListener(v -> {
            // Create an Intent for text files
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");
            intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"text/plain", "application/pdf"});
            startActivityForResult(intent, 1);
        });

        roomNameTextView.setOnClickListener(v -> {
            showInputDialog();
        });

        roomNameBackground.setOnClickListener(v -> {
            showInputDialog();
        });
    }

    private void instatiateViews() {
        drawerLayout = findViewById(R.id.drawerLayout);
        leftNavigationView = findViewById(R.id.leftNavigationView);
        rightNavigationView = findViewById(R.id.rightNavigationView);
        leftNavigationDrawerIcon = findViewById(R.id.leftNavigationDrawerIcon);
        rightNavigationDrawerIcon = findViewById(R.id.rightNavigationDrawerIcon);
        uploadFilesButton = findViewById(R.id.uploadFilesButton);
        roomNameTextView = findViewById(R.id.roomNameTextView);
        roomNameBackground = findViewById(R.id.roomNameBackground);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        // Delegate the touch event to the gesture detector
        gestureDetector.onTouchEvent(ev);
        return super.dispatchTouchEvent(ev);
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
//                    textExtractorFromFile.extractTextFromPdfFile(fileUri, inputStream, new ResponseCallback() {
//                        @Override
//                        public void onResponse(String response) {
//                            Toast.makeText(MainActivity.this, String.valueOf(response.length()), Toast.LENGTH_SHORT).show();
//                            CharacterTextSplitter characterTextSplitter = new CharacterTextSplitter(1000, 100);
//                            String[] chunks = characterTextSplitter.getChunksFromText(response);
//
//                            Toast.makeText(MainActivity.this, "The number of chunks is : " + chunks.length, Toast.LENGTH_SHORT).show();
//                        }
//
//                        @Override
//                        public void onError(Throwable throwable) {
//                            Toast.makeText(MainActivity.this, "Error processing file!", Toast.LENGTH_SHORT).show();
//                        }
//                    });
                    addFileToContainer(fileName, R.id.linearLayoutPDFContainer);

                } else if ("text/plain".equals(mimeType)) {
//                    textExtractorFromFile.extractTextFromTextFile(fileUri, inputStream, new ResponseCallback() {
//                        @Override
//                        public void onResponse(String response) {
//                            Toast.makeText(MainActivity.this, response, Toast.LENGTH_SHORT).show();
//                        }
//
//                        @Override
//                        public void onError(Throwable throwable) {
//                            Toast.makeText(MainActivity.this, "Error processing file!", Toast.LENGTH_SHORT).show();
//                        }
//                    });
                    addFileToContainer(fileName, R.id.linearLayoutTxtContainer);
                } else {
                    Toast.makeText(MainActivity.this, "Unsupported file type!", Toast.LENGTH_SHORT).show();
                }
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void addFileToContainer(String fileName, int containerId) {
        LinearLayout container = findViewById(containerId);
        TextView noFileFoundTextView = getNoFileFoundTextView(container);
        if (noFileFoundTextView != null && noFileFoundTextView.getVisibility() == View.VISIBLE) {
            noFileFoundTextView.setVisibility(View.GONE);
        }

        LinearLayout fileLayout = new LinearLayout(this);
        fileLayout.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        layoutParams.setMargins(0, 0, 0, getPixelValue(8));
        fileLayout.setLayoutParams(layoutParams);

        TextView textView = new TextView(this);
        textView.setText(fileName);
        textView.setTextSize(16);
        textView.setMaxLines(1);
        textView.setEllipsize(TextUtils.TruncateAt.END);
        textView.setTypeface(getResources().getFont(R.font.roboto));
        textView.setLayoutParams(new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1
        ));
        textView.setPadding(0, 0, getPixelValue(8), 0);
        fileLayout.addView(textView);

        ImageButton removeButton = new ImageButton(this);
        removeButton.setImageResource(R.drawable.remove_button);
        removeButton.setLayoutParams(new LinearLayout.LayoutParams(
                getPixelValue(20),
                getPixelValue(20)
        ));
        removeButton.setBackground(null);
        removeButton.setOnClickListener(v -> {
            container.removeView(fileLayout);
            int containerChildCount = container.getChildCount();
            if (containerChildCount == 1 && noFileFoundTextView != null) {
                noFileFoundTextView.setVisibility(View.VISIBLE);
            }
        });
        fileLayout.addView(removeButton);

        container.addView(fileLayout);
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

    private int getPixelValue(int dpValue) {
        float scale = getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale);
    }

    private void showInputDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.room_name_dialog, null);
        builder.setView(view);

        TextInputEditText editText = view.findViewById(R.id.roomNameEditText);
        Button cancelButton = view.findViewById(R.id.cancelButton);
        Button confirmButton = view.findViewById(R.id.confirmButton);

        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.rounded_background_room_dialog);

        cancelButton.setOnClickListener(v -> dialog.dismiss());

        confirmButton.setOnClickListener(v -> {
            String newText = editText.getText().toString();
            roomNameTextView.setText(newText);
            dialog.dismiss();
        });

        dialog.show();
    }
}