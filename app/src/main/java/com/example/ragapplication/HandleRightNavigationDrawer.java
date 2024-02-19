package com.example.ragapplication;

import android.app.Activity;
import android.net.Uri;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;

import java.util.Map;

public class HandleRightNavigationDrawer {
    private NavigationView rightNavigationView;
    private Activity activity;
    private Map<String, Uri> filesUriStore;


    public HandleRightNavigationDrawer(NavigationView rightNavigationView, Activity activity, Map<String, Uri> filesUriStore) {
        this.rightNavigationView = rightNavigationView;
        this.activity = activity;
        this.filesUriStore = filesUriStore;
    }

    public void addFilesToNavigationDrawer(String fileType, String fileName) {
        if (fileType.equals("pdf")) {
            addFileToContainer(fileName, R.id.linearLayoutPDFContainer);
        } else if (fileType.equals("txt")) {
            addFileToContainer(fileName, R.id.linearLayoutTxtContainer);
        } else {
            Toast.makeText(this.activity, "Unsupported file type!", Toast.LENGTH_SHORT).show();
        }
    }

    private void addFileToContainer(String fileName, int containerId) {
        NavigationView rightNavigationView = this.activity.findViewById(R.id.rightNavigationView);
        View rightHeaderView = rightNavigationView.getHeaderView(0);
        LinearLayout rowsContainer = rightHeaderView.findViewById(containerId);

        TextView noFileFoundTextView = getNoFileFoundTextView(rowsContainer);
        if (noFileFoundTextView != null && noFileFoundTextView.getVisibility() == View.VISIBLE) {
            noFileFoundTextView.setVisibility(View.GONE);
        }

        LinearLayout fileRowLayout = getFileRowLayout();
        fileRowLayout.setGravity(Gravity.CENTER_VERTICAL);

        TextView fileNameTextView = getfileNameTextView(fileName);
        ImageButton removeButton = getRemoveButton(rowsContainer, fileRowLayout, noFileFoundTextView);

        fileRowLayout.addView(fileNameTextView);
        fileRowLayout.addView(removeButton);

        rowsContainer.addView(fileRowLayout);
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

    private LinearLayout getFileRowLayout() {
        LinearLayout fileRowLayout = new LinearLayout(this.activity);
        fileRowLayout.setOrientation(LinearLayout.HORIZONTAL);
        fileRowLayout.setPadding(0, UnitConverter.dpInPixels(10), 0, UnitConverter.dpInPixels(10));


        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        fileRowLayout.setLayoutParams(layoutParams);

        return fileRowLayout;
    }

    private TextView getfileNameTextView(String fileName) {
        TextView fileNameTextView = new TextView(this.activity);

        fileNameTextView.setText(fileName);
        fileNameTextView.setTextSize(16);
        fileNameTextView.setTextColor(ThemeUtils.getTextColorBasedOnTheme(R.attr.textPrimaryColor, activity));
        fileNameTextView.setMaxLines(1);
        fileNameTextView.setEllipsize(TextUtils.TruncateAt.END);
        fileNameTextView.setTypeface(this.activity.getResources().getFont(R.font.roboto));
        fileNameTextView.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        fileNameTextView.setPadding(0, 0, UnitConverter.dpInPixels(8), 0);

        return fileNameTextView;
    }

    private ImageButton getRemoveButton(LinearLayout rowsContainer, LinearLayout fileRowLayout, TextView noFileFoundTextView) {
        ImageButton removeButton = new ImageButton(this.activity);

        removeButton.setImageResource(R.drawable.remove_button);
        removeButton.setBackgroundColor(this.activity.getResources().getColor(R.color.transparent));

        int padding = UnitConverter.dpInPixels(10);
        removeButton.setPadding(padding, padding, padding, padding);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        removeButton.setLayoutParams(layoutParams);

        removeButton.setOnClickListener(v -> {
            String fileName = ((TextView) fileRowLayout.getChildAt(0)).getText().toString();
            filesUriStore.remove(fileName);
            rowsContainer.removeView(fileRowLayout);
            removeFileAndEmbeddings(fileName);

            int containerChildCount = rowsContainer.getChildCount();
            if (containerChildCount == 1 && noFileFoundTextView != null) {
                noFileFoundTextView.setVisibility(View.VISIBLE);
            }
        });

        return removeButton;
    }

    private void removeFileAndEmbeddings(String fileName) {
        DatabaseHelper databaseHelper = new DatabaseHelper(this.activity);
        String fileType = databaseHelper.getFileType(fileName);
        int fileId = databaseHelper.getFileId(fileName, fileType);

        databaseHelper.deleteEmbeddings(fileId);
        databaseHelper.deleteFile(fileId);
    }
}
