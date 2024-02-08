package com.example.ragapplication;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class RoomNameHandler {
    private TextView roomNameTextView;
    private RelativeLayout roomNameBackground;
    private Activity activity;
    public static boolean roomNameDialogIsVisible = false;

    public RoomNameHandler(TextView roomNameTextView, RelativeLayout roomNameBackground, Activity activity) {
        this.roomNameTextView = roomNameTextView;
        this.roomNameBackground = roomNameBackground;
        this.activity = activity;
    }

    public void setRoomNameListeners() {
        this.roomNameTextView.setOnClickListener(v -> {
            showInputDialog();
        });

        this.roomNameBackground.setOnClickListener(v -> {
            showInputDialog();
        });
    }

    public void showInputDialog() {
        roomNameDialogIsVisible = true;

        LayoutInflater inflater = this.activity.getLayoutInflater();
        View view = inflater.inflate(R.layout.room_name_dialog, null);

        TextInputLayout textInputLayout = view.findViewById(R.id.roomNameEditTextLayout);
        TextInputEditText editText = view.findViewById(R.id.roomNameEditText);
        editText.setText(this.roomNameTextView.getText().toString());

        Button cancelButton = view.findViewById(R.id.cancelButton);
        Button confirmButton = view.findViewById(R.id.confirmButton);

        AlertDialog dialog = getAlertDialog(view);
        setAlertDialogButtonsListeners(dialog, textInputLayout, editText, cancelButton, confirmButton);

        dialog.show();
    }

    private AlertDialog getAlertDialog(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this.activity);
        builder.setView(view);

        AlertDialog dialog = builder.create();
        dialog.setOnDismissListener(dialogInterface -> roomNameDialogIsVisible = false);

        dialog.getWindow().setBackgroundDrawableResource(R.drawable.rounded_background_room_dialog);

        return dialog;
    }

    private void setAlertDialogButtonsListeners(AlertDialog dialog, TextInputLayout textInputLayout,
                                                TextInputEditText editText, Button cancelButton, Button confirmButton)
    {
        cancelButton.setOnClickListener(v -> {
            dialog.dismiss();
            roomNameDialogIsVisible = false;
        });

        confirmButton.setOnClickListener(v -> {
            roomNameDialogIsVisible = false;
            String newText = editText.getText().toString();

            if (newText.isEmpty()) {
                textInputLayout.setErrorEnabled(true);
                textInputLayout.setError("Room name cannot be empty");
            } else {
                textInputLayout.setErrorEnabled(false);
                roomNameTextView.setText(newText);

                DatabaseHelper databaseHelper = new DatabaseHelper(this.activity);
                databaseHelper.setRoomName(newText, MainActivity.ROOM_ID);

                updateLeftNavigationDrawer();
                dialog.dismiss();
            }
        });
    }

    private void updateLeftNavigationDrawer() {
        MainActivity mainActivity = (MainActivity) this.activity;
        DrawerLayout drawerLayout = mainActivity.getDrawerLayout();
        NavigationView leftNavigationView = mainActivity.getLeftNavigationView();

        HandleLeftNavigationDrawer handleLeftNavigationDrawer = new HandleLeftNavigationDrawer(
                leftNavigationView,
                this.activity,
                drawerLayout
        );
        handleLeftNavigationDrawer.refreshLeftNavigationDrawer();
    }
}
