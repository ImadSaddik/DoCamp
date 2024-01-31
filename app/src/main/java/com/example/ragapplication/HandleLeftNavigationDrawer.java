package com.example.ragapplication;

import android.animation.AnimatorInflater;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.collection.ArraySet;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class HandleLeftNavigationDrawer {
    private NavigationView leftNavigationView;
    private Activity activity;
    private DrawerLayout drawerLayout;
    private int selectedRoomId = -1;

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
                roomRowContainer.setOnClickListener(v -> {
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
        dayTextView.setPadding(0, 80, 0, 40);
        dayTextView.setTypeface(this.activity.getResources().getFont(R.font.roboto), Typeface.BOLD);

        return dayTextView;
    }

    private LinearLayout getRoomRowContainer(String roomName, int roomId) {
        LinearLayout roomRowContainer = new LinearLayout(this.activity);
        roomRowContainer.setOrientation(LinearLayout.HORIZONTAL);
        roomRowContainer.setPadding(0, 20, 0, 20);
        roomRowContainer.setGravity(Gravity.CENTER_VERTICAL);
        roomRowContainer.setBackgroundColor(this.activity.getResources().getColor(R.color.purple));

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
        });

        return roomRowContainer;
    }

    private TextView getRoomNameTextView(String roomName) {
        TextView roomNameTextView = new TextView(this.activity);
        roomNameTextView.setText(roomName);
        roomNameTextView.setTextSize(16);
        roomNameTextView.setPadding(0, 0, 20, 0);
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
        int padding = 20;
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

        newRoomButton.setOnClickListener(v -> {
            v.setStateListAnimator(AnimatorInflater.loadStateListAnimator(this.activity, R.animator.click_animation));
            MainActivity mainActivity = (MainActivity) this.activity;

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

    private void showNoFilesIndicator() {
        TextView uploadFilesIndicator = activity.findViewById(R.id.uploadFilesIndicator);
        uploadFilesIndicator.setText(activity.getString(R.string.no_file_found));
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
        LinearLayout textFilesContainer = activity.findViewById(containerID);

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

        removeNoFilesIndicator();
        cleanHomeBody();

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
        LinearLayout settingsButton = leftHeaderView.findViewById(R.id.settingsLiearLayout);

        settingsButton.setOnClickListener(v -> {
            Intent intent = new Intent(this.activity, SettingsActivity.class);
            this.activity.startActivity(intent);
        });
    }
}
