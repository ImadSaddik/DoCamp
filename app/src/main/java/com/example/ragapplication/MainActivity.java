package com.example.ragapplication;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.ContextMenu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.view.MotionEvent;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.GestureDetectorCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.ai.client.generativeai.java.ChatFutures;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader;

import java.io.File;
import java.nio.channels.AcceptPendingException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

public class MainActivity extends AppCompatActivity {
    public static int ROOM_ID;
    public static final int SIGN_IN_REQUEST = 3647;
    private DrawerLayout drawerLayout;
    private NavigationView leftNavigationView, rightNavigationView;
    private GestureDetectorCompat gestureDetector;
    private ImageView leftNavigationDrawerIcon, rightNavigationDrawerIcon;
    private ImageButton uploadFilesButton, sendQueryButton;
    private Button processFilesButton;
    private LinearLayout processingTextProgressContainer;
    private TextInputEditText queryEditText;
    private TextView roomNameTextView, processingTextProgressDescription, uploadFilesIndicator;
    private RelativeLayout roomNameBackground;
    private DatabaseHelper databaseHelper;
    private SQLiteDatabase sqLiteDatabase;
    public static Map<String, Uri> filesUriStore;
    private EmbeddingModel embeddingModel;
    private HandleRightNavigationDrawer handleRightNavigationDrawer;
    private HandleLeftNavigationDrawer handleLeftNavigationDrawer;
    private HandleNavigationDrawersVisibility handleNavigationDrawers;
    private RoomNameHandler roomNameHandler;
    private HandleUserQuery handleUserQuery;
    private HandleSwipeAndDrawers handleSwipeAndDrawers;
    public static ChatFutures chatModel;
    private NetworkChangeReceiver receiver;
    public static boolean finishedProcessingFiles = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SettingsStore.loadValuesFromSharedPreferences(this);
        LanguageManager.changeAppLanguage(this);
        ThemeManager.changeThemeBasedOnSelection(this);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        receiver = new NetworkChangeReceiver();
        registerReceiver(receiver, filter);

        instantiateViews();
        instantiateObjects();
        createRoom();

        handleNavigationDrawers.setNavigationDrawerListeners();
        roomNameHandler.setRoomNameListeners();

        handleLeftNavigationDrawer.setNewRoomButtonListener();
        handleLeftNavigationDrawer.setSettingsButtonListener();
        handleLeftNavigationDrawer.setBackUpDataBaseButtonListener();
        handleLeftNavigationDrawer.setRestoreDatabaseButtonListener();
        handleLeftNavigationDrawer.setResetDatabaseButtonListener();
        handleLeftNavigationDrawer.setRemoveAdsButtonListener();
        handleLeftNavigationDrawer.checkIfUserIsSignedIn();
        handleLeftNavigationDrawer.setGoogleAuthButtonListener();

        if (savedInstanceState != null) {
            ROOM_ID = savedInstanceState.getInt("ROOM_ID");
            handleLeftNavigationDrawer.loadUIOnStateChange(ROOM_ID);

            if (savedInstanceState.getBoolean("roomNameDialogIsVisible")) {
                roomNameHandler.showInputDialog();
            }

            if (savedInstanceState.getBoolean("signOutDialogIsVisible")) {
                View leftHeaderView = leftNavigationView.getHeaderView(0);
                LinearLayout signOutLayout = leftHeaderView.findViewById(R.id.signOutLayout);
                LinearLayout googleAuthLayout = leftHeaderView.findViewById(R.id.googleAuthLayout);

                GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(this.getString(R.string.default_web_client_id))
                        .requestEmail()
                        .build();

                handleLeftNavigationDrawer.showSignOutDialog(googleSignInOptions, signOutLayout, googleAuthLayout);
            }
        }

        handleLeftNavigationDrawer.populateTheBodyWithRooms();

        View rootView = findViewById(R.id.chatHistoryBody);
        handleUserQuery.hideKeyboardWhenClickingOutside(rootView);
        handleUserQuery.swapBetweenUploadAndSend();
        handleUserQuery.setupSendQueryButtonListener();

        setActionButtonsListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (receiver == null) {
            receiver = new NetworkChangeReceiver();
        }
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(receiver, filter);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("ROOM_ID", ROOM_ID);

        if (RoomNameHandler.roomNameDialogIsVisible) {
            outState.putBoolean("roomNameDialogIsVisible", true);
        }

        if (HandleLeftNavigationDrawer.signOutDialogIsVisible) {
            outState.putBoolean("signOutDialogIsVisible", true);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (receiver != null) {
            unregisterReceiver(receiver);
            receiver = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (receiver != null) {
            unregisterReceiver(receiver);
            receiver = null;
        }
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
        queryEditText = findViewById(R.id.queryEditText);
        uploadFilesIndicator = findViewById(R.id.uploadFilesIndicator);

        View rightHeaderView = rightNavigationView.getHeaderView(0);
        processFilesButton = rightHeaderView.findViewById(R.id.processFilesButton);
        processingTextProgressDescription = rightHeaderView.findViewById(R.id.processingTextProgressDescription);
        processingTextProgressContainer = rightHeaderView.findViewById(R.id.processingTextProgressContainer);
    }

    private void instantiateObjects() {
        PDFBoxResourceLoader.init(getApplicationContext());

        GeminiPro geminiPro = new GeminiPro();
        GenerativeModelFutures generativeModelFutures = geminiPro.getModel();
        chatModel = generativeModelFutures.startChat();

        databaseHelper = new DatabaseHelper(this);
        sqLiteDatabase = databaseHelper.getWritableDatabase();

        filesUriStore = new HashMap<>();
        embeddingModel = new EmbeddingModel(this);

        handleSwipeAndDrawers = new HandleSwipeAndDrawers(drawerLayout);
        gestureDetector = new GestureDetectorCompat(
                this,
                handleSwipeAndDrawers
        );
        handleRightNavigationDrawer = new HandleRightNavigationDrawer(
                rightNavigationView,
                this,
                filesUriStore
        );
        handleLeftNavigationDrawer = new HandleLeftNavigationDrawer(
                leftNavigationView,
                this,
                drawerLayout
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
                sendQueryButton,
                this
        );
    }

    public DrawerLayout getDrawerLayout() {
        return drawerLayout;
    }

    public NavigationView getLeftNavigationView() {
        return leftNavigationView;
    }

    public TextInputEditText getQueryEditText() {
        return queryEditText;
    }

    public HandleUserQuery getHandleUserQuery() {
        return handleUserQuery;
    }

    public HandleRightNavigationDrawer getHandleRightNavigationDrawer() {
        return handleRightNavigationDrawer;
    }

    public void createRoom() {
        setRoomID();

        String roomName = roomNameTextView.getText().toString();
        databaseHelper.insertRoom(ROOM_ID, roomName);
    }

    private void setRoomID() {
        int maxRoomID = databaseHelper.getMaxRoomID();
        if (maxRoomID == -1) {
            ROOM_ID = 1;
        } else {
            ROOM_ID = maxRoomID + 1;
        }
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
            finishedProcessingFiles = false;

            CountDownLatch latch = new CountDownLatch(filesUriStore.size());
            FileProcessor fileProcessor = new FileProcessor(
                    this,
                    latch,
                    processingTextProgressDescription
            );

            for (Map.Entry<String, Uri> entry : filesUriStore.entrySet()) {
                try {
                    String mimeType = getContentResolver().getType(entry.getValue());
                    databaseHelper.insertFile(entry.getKey(), mimeType, ROOM_ID);

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
                        filesUriStore.clear();
                        handleLeftNavigationDrawer.refreshLeftNavigationDrawer();
                        uploadFilesIndicator.setText(getString(R.string.ready_to_chat));
                        finishedProcessingFiles = true;
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        });
    }

    private void showProgressContainer() {
        processingTextProgressContainer.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SIGN_IN_REQUEST) {
            handleSignIn(data);
        }

        if (resultCode == RESULT_OK && data != null && data.getData() != null) {
            Log.d("SIGN_IN_TAG", "onActivityResult inside: " + requestCode + " " + resultCode);
            if (requestCode == 1) {
                handleFiles(data);
            } else  {
                handleDatabase(requestCode, data);
            }
        }
    }

    private void handleFiles(Intent data) {
        try {
            Uri fileUri = data.getData();
            String fileName = removeExtension(getFileName(fileUri));
            String mimeType = getContentResolver().getType(fileUri);
            int fileId = databaseHelper.getFileId(fileName, FileUtilities.getFileType(mimeType));

            if (filesUriStore.containsKey(fileName) || fileId == ROOM_ID) {
                Toast.makeText(this, "File already added!", Toast.LENGTH_SHORT).show();
            } else {
                filesUriStore.put(fileName, fileUri);
                String fileType = FileUtilities.getFileType(mimeType);
                handleRightNavigationDrawer.addFilesToNavigationDrawer(fileType, fileName);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleSignIn(Intent data) {
        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
        try {
            GoogleSignInAccount googleSignInAccount = task.getResult(ApiException.class);
            AuthCredential authCredential = GoogleAuthProvider.getCredential(googleSignInAccount.getIdToken(), null);
            FirebaseAuth.getInstance().signInWithCredential(authCredential)
                    .addOnCompleteListener(task1 -> {
                        if (task1.isSuccessful()) {
                            String userName = googleSignInAccount.getDisplayName();
                            Uri userPhoto = googleSignInAccount.getPhotoUrl();

                            handleLeftNavigationDrawer.setSignOutButton(userName, userPhoto);
                            Toast.makeText(MainActivity.this, userName + "Signed in successfully!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(MainActivity.this, "Failed to sign in!", Toast.LENGTH_SHORT).show();
                        }
                    });
        } catch (ApiException e) {
            Toast.makeText(this, "Failed to sign in! Something unexpected happened", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleDatabase(int requestCode, Intent data) {
        if (requestCode == DatabaseUtils.REQUEST_CODE_SAVE_DATABASE) {
            Uri destinationUri = data.getData();
            DatabaseUtils.saveDatabase(this, destinationUri);
        } else if (requestCode == DatabaseUtils.REQUEST_CODE_LOAD_DATABASE) {
            Uri sourceUri = data.getData();
            DatabaseUtils.loadDatabase(this, sourceUri);
            handleLeftNavigationDrawer.refreshLeftNavigationDrawer();
        }
    }

    private String removeExtension(String fileName) {
        return fileName.split("\\.")[0];
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