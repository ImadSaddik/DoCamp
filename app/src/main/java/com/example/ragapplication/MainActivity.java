package com.example.ragapplication;

import android.content.Context;
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
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;

import android.view.MotionEvent;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.GestureDetectorCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView leftNavigationView, rightNavigationView;
    private GestureDetectorCompat gestureDetector;
    private ImageView leftNavigationDrawerIcon, rightNavigationDrawerIcon;
    private ImageButton uploadFilesButton, sendQueryButton;
    private Button processFilesButton;
    private ProgressBar processingTextProgressBar;
    private LinearLayout processingTextProgressContainer, chatBodyContainer;
    private TextInputEditText queryEditText;
    private TextView roomNameTextView, processingTextProgressDescription;
    private RelativeLayout roomNameBackground;
    private DatabaseHelper databaseHelper;
    private SQLiteDatabase sqLiteDatabase;
    private Map<String, Uri> filesUriStore;
    private EmbeddingModel embeddingModel;
    private HandleRightNavigationDrawer handleRightNavigationDrawer;
    private HandleNavigationDrawersVisibility handleNavigationDrawers;
    private RoomNameHandler roomNameHandler;
    private HandleUserQuery handleUserQuery;
    private HandleSwipeAndDrawers handleSwipeAndDrawers;

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

        handleNavigationDrawers.setNavigationDrawerListeners();
        roomNameHandler.setRoomNameListeners();

        setActionButtonsListeners();
        handleUserQuery.swapBetweenUploadAndSend();
        handleUserQuery.setupSendQueryButtonListener();

        for (int i = 0; i < 20; i++) {
            populateChatBody();
        }
    }

    private void populateChatBody() {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.chat_message_block, null);

        chatBodyContainer.addView(view);
    }

    private void instantiateViews() {
        drawerLayout = findViewById(R.id.drawerLayout);
        leftNavigationView = findViewById(R.id.leftNavigationView);
        rightNavigationView = findViewById(R.id.rightNavigationView);
        leftNavigationDrawerIcon = findViewById(R.id.leftNavigationDrawerIcon);
        rightNavigationDrawerIcon = findViewById(R.id.rightNavigationDrawerIcon);
        uploadFilesButton = findViewById(R.id.uploadFilesButton);
        sendQueryButton = findViewById(R.id.sendQueryButton);
        roomNameTextView = findViewById(R.id.roomNameTextView);
        roomNameBackground = findViewById(R.id.roomNameBackground);
        chatBodyContainer = findViewById(R.id.chatBodyContainer);
        queryEditText = findViewById(R.id.queryEditText);

        View rightHeaderView = rightNavigationView.getHeaderView(0);
        processFilesButton = rightHeaderView.findViewById(R.id.processFilesButton);
        processingTextProgressBar = rightHeaderView.findViewById(R.id.processingTextProgressBar);
        processingTextProgressDescription = rightHeaderView.findViewById(R.id.processingTextProgressDescription);
        processingTextProgressContainer = rightHeaderView.findViewById(R.id.processingTextProgressContainer);
    }

    private void instantiateObjects() {
        PDFBoxResourceLoader.init(getApplicationContext());

        databaseHelper = new DatabaseHelper(this);
        sqLiteDatabase = databaseHelper.getWritableDatabase();

        filesUriStore = new HashMap<>();
        embeddingModel = new EmbeddingModel();

        handleSwipeAndDrawers = new HandleSwipeAndDrawers(
                this,
                drawerLayout
        );
        gestureDetector = new GestureDetectorCompat(
                this,
                handleSwipeAndDrawers
        );
        handleRightNavigationDrawer = new HandleRightNavigationDrawer(
                rightNavigationView,
                this,
                filesUriStore
        );
        handleNavigationDrawers = new HandleNavigationDrawersVisibility(
                leftNavigationDrawerIcon,
                rightNavigationDrawerIcon,
                leftNavigationView,
                rightNavigationView,
                drawerLayout
        );
        roomNameHandler = new RoomNameHandler(
                roomNameTextView,
                roomNameBackground,
                this
        );
        handleUserQuery = new HandleUserQuery(
                queryEditText,
                uploadFilesButton,
                sendQueryButton
        );
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        gestureDetector.onTouchEvent(ev);
        return super.dispatchTouchEvent(ev);
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
            FileProcessor fileProcessor = new FileProcessor(
                    this,
                    latch,
                    processingTextProgressBar,
                    processingTextProgressDescription
            );

            for (Map.Entry<String, Uri> entry : filesUriStore.entrySet()) {
                try {
                    fileProcessor.processFile(entry.getValue());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            new Thread(() -> {
                try {
                    latch.await();
                    runOnUiThread(() -> {
                        processingTextProgressContainer.setVisibility(View.GONE);
                        processFilesButton.setEnabled(true);

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
                    handleRightNavigationDrawer.addFilesToNavigationDrawer(mimeType, fileName);
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
}