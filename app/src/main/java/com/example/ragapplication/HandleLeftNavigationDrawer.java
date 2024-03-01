package com.example.ragapplication;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.collection.ArraySet;
import androidx.drawerlayout.widget.DrawerLayout;

import com.bumptech.glide.Glide;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import de.hdodenhof.circleimageview.CircleImageView;

public class HandleLeftNavigationDrawer {
    private NavigationView leftNavigationView;
    private Activity activity;
    private DrawerLayout drawerLayout;
    private LinearLayout selectedRoomRow = null;
    public static boolean signOutDialogIsVisible = false;

    public HandleLeftNavigationDrawer(NavigationView leftNavigationView, Activity activity, DrawerLayout drawerLayout) {
        this.leftNavigationView = leftNavigationView;
        this.activity = activity;
        this.drawerLayout = drawerLayout;
    }

    public void populateTheBodyWithRooms() {
        Set<String> dayOfCreation = new ArraySet<>();

        View leftHeaderView = this.leftNavigationView.getHeaderView(0);
        LinearLayout roomsContainer = leftHeaderView.findViewById(R.id.roomsLinearLayoutContainer);
        LinearLayout roomsInDayContainer = null;

        DatabaseHelper databaseHelper = new DatabaseHelper(this.activity);
        Cursor cursor = databaseHelper.getActiveRooms();

        setNoRoomFoundTextViewVisibility(cursor, leftHeaderView);
        roomsContainer.removeAllViews();

        while (cursor.moveToNext()) {
            int roomIdIndex = cursor.getColumnIndex("Room_ID");
            int roomNameIndex = cursor.getColumnIndex("Room_Name");
            int createdAtIndex = cursor.getColumnIndex("Created_At");

            if (roomIdIndex != -1 && roomNameIndex != -1 && createdAtIndex != -1) {
                int roomId = cursor.getInt(roomIdIndex);
                String roomName = cursor.getString(roomNameIndex);
                String createdAt = cursor.getString(createdAtIndex).substring(0, 10);

                if (!dayOfCreation.contains(createdAt)) {
                    dayOfCreation.add(createdAt);
                    roomsInDayContainer = addNewDayBlock(createdAt, roomsContainer);
                }

                LinearLayout roomRowContainer = getRoomRowContainer(roomName, roomId);

                if (roomId == MainActivity.ROOM_ID) {
                    selectedRoomRow = roomRowContainer;
                    roomRowContainer.setBackgroundColor(ThemeUtils.getBackgroundColorBasedOnTheme(R.attr.roomSelectedBackground, activity));
                }

                roomRowContainer.setOnClickListener(v -> {
                    if (selectedRoomRow != null) {
                        selectedRoomRow.setBackgroundColor(this.activity.getResources().getColor(R.color.transparent));
                    }

                    selectedRoomRow = roomRowContainer;
                    roomRowContainer.setBackgroundColor(ThemeUtils.getBackgroundColorBasedOnTheme(R.attr.roomSelectedBackground, activity));

                    MainActivity.ROOM_ID = roomId;
                    loadChatHistory(roomId);
                    loadRoomName(roomId);
                    loadFilesHistory(roomId);
                    closeNavigationDrawer();
                });
                roomsInDayContainer.addView(roomRowContainer);
            } else {
                System.out.println("One or more column names do not exist in the Room table.");
            }
        }
    }

    private void setNoRoomFoundTextViewVisibility(Cursor cursor, View leftHeaderView) {
        TextView noRoomFoundTextView = leftHeaderView.findViewById(R.id.noRoomDescriptionTextView);
        if (cursor.getCount() == 0) {
            noRoomFoundTextView.setVisibility(View.VISIBLE);
        } else {
            noRoomFoundTextView.setVisibility(View.GONE);
        }
    }

    private LinearLayout addNewDayBlock(String createdAt, LinearLayout roomsContainer) {
        TextView dayTextView = getDayTextView(createdAt);
        roomsContainer.addView(dayTextView);

        LinearLayout roomsInDayContainer = new LinearLayout(this.activity);
        roomsInDayContainer.setOrientation(LinearLayout.VERTICAL);
        roomsContainer.addView(roomsInDayContainer);

        return roomsInDayContainer;
    }

    private TextView getDayTextView(String createdAt) {
        TextView dayTextView = new TextView(this.activity);
        dayTextView.setText(createdAt);
        dayTextView.setTextSize(16);
        dayTextView.setTextColor(ThemeUtils.getTextColorBasedOnTheme(R.attr.textPrimaryColor, activity));
        dayTextView.setPadding(
                UnitConverter.dpInPixels(16),
                UnitConverter.dpInPixels(40),
                0,
                UnitConverter.dpInPixels(10)
        );
        dayTextView.setTypeface(this.activity.getResources().getFont(R.font.roboto), Typeface.BOLD);

        return dayTextView;
    }

    private LinearLayout getRoomRowContainer(String roomName, int roomId) {
        LinearLayout roomRowContainer = new LinearLayout(this.activity);
        roomRowContainer.setPadding(
                UnitConverter.dpInPixels(16),
                UnitConverter.dpInPixels(10),
                UnitConverter.dpInPixels(16),
                UnitConverter.dpInPixels(10)
        );
        roomRowContainer.setGravity(Gravity.CENTER_VERTICAL);

        TextView roomNameTextView = getRoomNameTextView(roomName);
        ImageButton roomRemoveButton = getRoomRemoveButton();

        roomRowContainer.addView(roomNameTextView);
        roomRowContainer.addView(roomRemoveButton);

        roomRemoveButton.setOnClickListener(v -> {
            MainActivity mainActivity = (MainActivity) this.activity;
            DatabaseHelper databaseHelper = new DatabaseHelper(this.activity);
            databaseHelper.deleteRoom(roomId);

            if (roomId == MainActivity.ROOM_ID) {
                cleanHomeBody();
                cleanRightNavigationDrawer();
                resetRoomNameInHeader();
                mainActivity.createRoom();
                resetFileStore();
                resetQueryInput();
                closeNavigationDrawer();
            }

            refreshLeftNavigationDrawer();
            Toast.makeText(this.activity, "Room deleted successfully.", Toast.LENGTH_SHORT).show();
        });

        return roomRowContainer;
    }

    private TextView getRoomNameTextView(String roomName) {
        TextView roomNameTextView = new TextView(this.activity);
        roomNameTextView.setText(roomName);
        roomNameTextView.setTextSize(16);
        roomNameTextView.setTextColor(ThemeUtils.getTextColorBasedOnTheme(R.attr.textPrimaryColor, activity));
        roomNameTextView.setPadding(0, 0, UnitConverter.dpInPixels(10), 0);
        roomNameTextView.setMaxLines(1);
        roomNameTextView.setEllipsize(TextUtils.TruncateAt.END);
        roomNameTextView.setTypeface(this.activity.getResources().getFont(R.font.roboto));

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        layoutParams.weight = 1;
        roomNameTextView.setLayoutParams(layoutParams);

        return roomNameTextView;
    }

    private ImageButton getRoomRemoveButton() {
        ImageButton roomRemoveButton = new ImageButton(this.activity);
        roomRemoveButton.setImageResource(R.drawable.remove_button);
        roomRemoveButton.setBackgroundColor(this.activity.getResources().getColor(R.color.transparent));

        int padding = UnitConverter.dpInPixels(10);
        roomRemoveButton.setPadding(padding, padding, padding, padding);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );

        roomRemoveButton.setLayoutParams(layoutParams);

        return roomRemoveButton;
    }

    public void setNewRoomButtonListener() {
        View leftHeaderView = this.leftNavigationView.getHeaderView(0);
        LinearLayout newRoomButton = leftHeaderView.findViewById(R.id.createNewRoomLayout);
        ClickAnimationHelper.setViewClickAnimation(activity, newRoomButton);

        newRoomButton.setOnClickListener(v -> {
            MainActivity mainActivity = (MainActivity) this.activity;

            resetSelectedRoomRow();
            showNoFilesIndicator();
            cleanHomeBody();
            cleanRightNavigationDrawer();
            closeNavigationDrawer();
            resetRoomNameInHeader();
            mainActivity.createRoom();
            resetFileStore();
            resetQueryInput();
            resetModel(null);
        });
    }

    private void resetSelectedRoomRow() {
        if (selectedRoomRow != null) {
            selectedRoomRow.setBackgroundColor(this.activity.getResources().getColor(R.color.transparent));
            selectedRoomRow = null;
        }
    }

    private void showNoFilesIndicator() {
        TextView uploadFilesIndicator = activity.findViewById(R.id.uploadFilesIndicator);
        uploadFilesIndicator.setText(activity.getString(R.string.please_upload_files));
        uploadFilesIndicator.setVisibility(View.VISIBLE);
    }

    private void cleanHomeBody() {
        LinearLayout chatBodyContainer = activity.findViewById(R.id.chatBodyContainer);

        chatBodyContainer.removeAllViews();
    }

    private void removeNoFilesIndicator() {
        TextView uploadFilesIndicator = activity.findViewById(R.id.uploadFilesIndicator);
        uploadFilesIndicator.setVisibility(View.GONE);
    }

    private void cleanRightNavigationDrawer() {
        cleanFilesContainer(R.id.linearLayoutTxtContainer);
        cleanFilesContainer(R.id.linearLayoutPDFContainer);
    }

    private void cleanFilesContainer(int containerID) {
        NavigationView rightNavigationView = this.activity.findViewById(R.id.rightNavigationView);
        View rightHeaderView = rightNavigationView.getHeaderView(0);
        LinearLayout textFilesContainer = rightHeaderView.findViewById(containerID);

        TextView noFileFoundTextView = getNoFileFoundTextView(textFilesContainer);
        noFileFoundTextView.setVisibility(View.VISIBLE);

        textFilesContainer.removeAllViews();
        textFilesContainer.addView(noFileFoundTextView);
    }

    private TextView getNoFileFoundTextView(LinearLayout container) {
        for (int i = 0; i < container.getChildCount(); i++) {
            View view = container.getChildAt(i);
            if (view instanceof TextView) {
                TextView textView = (TextView) view;
                if (textView.getText().toString().equals(this.activity.getString(R.string.no_file_found))) {
                    return textView;
                }
            }
        }
        return null;
    }

    private void closeNavigationDrawer() {
        this.drawerLayout.closeDrawer(this.leftNavigationView);
    }

    public void refreshLeftNavigationDrawer() {
        populateTheBodyWithRooms();
        setNewRoomButtonListener();
    }

    private void resetRoomNameInHeader() {
        TextView roomNameTextView = this.activity.findViewById(R.id.roomNameTextView);
        roomNameTextView.setText(this.activity.getString(R.string.default_room_name));
    }


    private void resetFileStore() {
        MainActivity.filesUriStore = new HashMap<>();
    }

    private void resetQueryInput() {
        MainActivity mainActivity = (MainActivity) this.activity;
        TextInputEditText queryEditText = mainActivity.getQueryEditText();

        queryEditText.setText("");
    }

    private void resetModel(List<Content> chatHistory) {
        GeminiPro geminiPro = new GeminiPro();
        GenerativeModelFutures generativeModelFutures = geminiPro.getModel();

        if (chatHistory == null) {
            MainActivity.chatModel = generativeModelFutures.startChat();
        } else {
            MainActivity.chatModel = generativeModelFutures.startChat(chatHistory);
        }
    }

    private void loadChatHistory(int roomId) {
        DatabaseHelper databaseHelper = new DatabaseHelper(this.activity);
        Cursor cursor = databaseHelper.getChatHistory(roomId);
        List<Content> chatHistory = new ArrayList<>();

        cleanHomeBody();
        if (cursor.getCount() == 0) {
            showNoFilesIndicator();
            return;
        }

        removeNoFilesIndicator();

        while (cursor.moveToNext()) {
            int userQueryIndex = cursor.getColumnIndex("User_Query");
            int modelResponseIndex = cursor.getColumnIndex("Model_Response");
            int dateIndex = cursor.getColumnIndex("Created_At");

            if (userQueryIndex != -1 && modelResponseIndex != -1 && dateIndex != -1) {
                String userQuery = cursor.getString(userQueryIndex);
                String modelResponse = cursor.getString(modelResponseIndex);
                String date = cursor.getString(dateIndex);

                MainActivity mainActivity = (MainActivity) this.activity;
                HandleUserQuery handleUserQuery = mainActivity.getHandleUserQuery();
                handleUserQuery.populateChatBody(SettingsStore.userName, userQuery, date);
                handleUserQuery.populateChatBody(SettingsStore.modelName, modelResponse, date);

                chatHistory.add(getContent(userQuery, "user"));
                chatHistory.add(getContent(modelResponse, "model"));
            } else {
                System.out.println("One or more column names do not exist in the ChatHistory table.");
            }
        }

        resetModel(chatHistory);
    }

    private Content getContent(String text, String role) {
        Content.Builder contentBuilder = new Content.Builder();
        contentBuilder.setRole(role);
        contentBuilder.addText(text);

        return contentBuilder.build();
    }

    private void loadRoomName(int roomId) {
        DatabaseHelper databaseHelper = new DatabaseHelper(this.activity);
        Cursor cursor = databaseHelper.getRoom(roomId);

        while (cursor.moveToNext()) {
            int roomNameIndex = cursor.getColumnIndex("Room_Name");

            if (roomNameIndex != -1) {
                String roomName = cursor.getString(roomNameIndex);

                TextView roomNameTextView = this.activity.findViewById(R.id.roomNameTextView);
                roomNameTextView.setText(roomName);
            } else {
                System.out.println("One or more column names do not exist in the Room table.");
            }
        }
    }

    private void loadFilesHistory(int roomId) {
        DatabaseHelper databaseHelper = new DatabaseHelper(this.activity);
        Cursor cursor = databaseHelper.getFilesHistory(roomId);

        cleanRightNavigationDrawer();

        if (cursor.getCount() != 0) {
            TextView uploadFilesIndicator = activity.findViewById(R.id.uploadFilesIndicator);
            uploadFilesIndicator.setText(activity.getString(R.string.ready_to_chat));
        }

        while (cursor.moveToNext()) {
            int fileNameIndex = cursor.getColumnIndex("File_Name");
            int fileTypeIndex = cursor.getColumnIndex("File_Type");

            if (fileNameIndex != -1 && fileTypeIndex != -1) {
                String fileName = cursor.getString(fileNameIndex);
                String fileType = cursor.getString(fileTypeIndex);

                MainActivity mainActivity = (MainActivity) this.activity;
                HandleRightNavigationDrawer handleRightNavigationDrawer = mainActivity.getHandleRightNavigationDrawer();
                handleRightNavigationDrawer.addFilesToNavigationDrawer(fileType, fileName);
            } else {
                System.out.println("One or more column names do not exist in the FilesHistory table.");
            }
        }
    }

    public void setSettingsButtonListener() {
        View leftHeaderView = this.leftNavigationView.getHeaderView(0);
        LinearLayout settingsButton = leftHeaderView.findViewById(R.id.settingsLinearLayout);
        ClickAnimationHelper.setViewClickAnimation(activity, settingsButton);

        settingsButton.setOnClickListener(v -> {
            Intent intent = new Intent(this.activity, SettingsActivity.class);
            this.activity.startActivity(intent);
        });
    }

    public void setBackUpDataBaseButtonListener() {
        View leftHeaderView = this.leftNavigationView.getHeaderView(0);
        LinearLayout backUpDataBaseButton = leftHeaderView.findViewById(R.id.backUpDBLayout);
        ClickAnimationHelper.setViewClickAnimation(activity, backUpDataBaseButton);

        backUpDataBaseButton.setOnClickListener(v -> {
            DatabaseUtils.triggerSaveDatabase(this.activity);
        });
    }

    public void setRestoreDatabaseButtonListener() {
        View leftHeaderView = this.leftNavigationView.getHeaderView(0);
        LinearLayout restoreDatabaseButton = leftHeaderView.findViewById(R.id.restoreDBLayout);
        ClickAnimationHelper.setViewClickAnimation(activity, restoreDatabaseButton);

        restoreDatabaseButton.setOnClickListener(v -> {
            DatabaseUtils.triggerLoadDatabase(this.activity);
        });
    }

    public void setResetDatabaseButtonListener() {
        View leftHeaderView = this.leftNavigationView.getHeaderView(0);
        LinearLayout resetDatabaseButton = leftHeaderView.findViewById(R.id.resetDBLayout);
        ClickAnimationHelper.setViewClickAnimation(activity, resetDatabaseButton);

        resetDatabaseButton.setOnClickListener(v -> {
            DatabaseUtils.resetDatabase(this.activity);
            refreshLeftNavigationDrawer();
        });
    }

    public void setRemoveAdsButtonListener() {
        View leftHeaderView = this.leftNavigationView.getHeaderView(0);
        LinearLayout removeAdsButton = leftHeaderView.findViewById(R.id.removeAdsLayout);
        ClickAnimationHelper.setViewClickAnimation(activity, removeAdsButton);

        removeAdsButton.setOnClickListener(v -> {
        });
    }

    public void checkIfUserIsSignedIn() {
        View leftHeaderView = this.leftNavigationView.getHeaderView(0);
        LinearLayout signOutLayout = leftHeaderView.findViewById(R.id.signOutLayout);
        LinearLayout googleAuthLayout = leftHeaderView.findViewById(R.id.googleAuthLayout);
        ClickAnimationHelper.setViewClickAnimation(activity, signOutLayout);

        GoogleSignInAccount googleSignInAccount = GoogleSignIn.getLastSignedInAccount(this.activity);
        if (googleSignInAccount != null) {
            String userName = googleSignInAccount.getDisplayName();
            Uri userPhoto = googleSignInAccount.getPhotoUrl();
            setSignOutButton(userName, userPhoto);
        } else {
            googleAuthLayout.setVisibility(View.VISIBLE);
            signOutLayout.setVisibility(View.GONE);
        }
    }

    public void setGoogleAuthButtonListener() {
        View leftHeaderView = this.leftNavigationView.getHeaderView(0);
        LinearLayout googleAuthButton = leftHeaderView.findViewById(R.id.googleAuthLayout);
        ClickAnimationHelper.setViewClickAnimation(activity, googleAuthButton);

        googleAuthButton.setOnClickListener(v -> {
            GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(this.activity.getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build();

            GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(this.activity, googleSignInOptions);
            googleSignInClient.signOut()
                    .addOnCompleteListener(this.activity, task -> {
                        Log.d("SIGN_OUT_TAG", "User signed out.");
                    });
            Intent signInIntent = googleSignInClient.getSignInIntent();
            this.activity.startActivityForResult(signInIntent, MainActivity.SIGN_IN_REQUEST);
        });
    }

    public void setSignOutButton(String userName, Uri userPhoto) {
        View leftHeaderView = this.leftNavigationView.getHeaderView(0);
        LinearLayout signOutLayout = leftHeaderView.findViewById(R.id.signOutLayout);
        LinearLayout googleAuthLayout = leftHeaderView.findViewById(R.id.googleAuthLayout);
        ClickAnimationHelper.setViewClickAnimation(activity, signOutLayout);

        CircleImageView userPhotoImageView = leftHeaderView.findViewById(R.id.userPhotoCircleImageView);
        TextView userNameTextView = leftHeaderView.findViewById(R.id.userNameTextView);

        userNameTextView.setText(userName);
        Glide.with(this.activity).load(userPhoto).into(userPhotoImageView);

        googleAuthLayout.setVisibility(View.GONE);
        signOutLayout.setVisibility(View.VISIBLE);
        signOutLayout.setOnClickListener(v -> {
            GoogleSignInAccount googleSignInAccount = GoogleSignIn.getLastSignedInAccount(this.activity);
            if (googleSignInAccount != null) {
                GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(this.activity.getString(R.string.default_web_client_id))
                        .requestEmail()
                        .build();

                showSignOutDialog(googleSignInOptions, signOutLayout, googleAuthLayout);
            }
        });
    }

    public void showSignOutDialog(GoogleSignInOptions googleSignInOptions, LinearLayout signOutLayout, LinearLayout googleAuthLayout) {
        signOutDialogIsVisible = true;

        LayoutInflater inflater = activity.getLayoutInflater();
        View view = inflater.inflate(R.layout.sign_out_dialog, null);

        Button cancelButton = view.findViewById(R.id.cancelSignOutButton);
        Button confirmButton = view.findViewById(R.id.confirmSignOutButton);

        AlertDialog dialog = getAlertDialog(view);

        cancelButton.setOnClickListener(v -> {
            signOutDialogIsVisible = false;
            dialog.dismiss();
        });

        confirmButton.setOnClickListener(v -> {
            signOutDialogIsVisible = false;

            GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(this.activity, googleSignInOptions);
            googleSignInClient.signOut()
                    .addOnCompleteListener(this.activity, task -> {
                        Toast.makeText(this.activity, "Signed out successfully.", Toast.LENGTH_SHORT).show();
                        googleAuthLayout.setVisibility(View.VISIBLE);
                        signOutLayout.setVisibility(View.GONE);
                    });
            dialog.dismiss();
        });

        dialog.show();
    }

    private AlertDialog getAlertDialog(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this.activity);
        builder.setView(view);

        AlertDialog dialog = builder.create();
        dialog.setOnDismissListener(dialogInterface -> signOutDialogIsVisible = false);

        dialog.getWindow().setBackgroundDrawableResource(R.drawable.rounded_background_room_dialog);

        return dialog;
    }

    public void loadUIOnStateChange(int roomId) {
        loadChatHistory(roomId);
        loadRoomName(roomId);
        loadFilesHistory(roomId);
    }
}
