package com.example.ragapplication;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;

public class FileUtilities {
    public static String getFileType(String mimeType) {
        if (mimeType.equals("application/pdf")) {
            return "pdf";
        } else if (mimeType.equals("text/plain")) {
            return "txt";
        } else {
            return "unknown";
        }
    }

    public static long getFileSize(Activity activity, Uri fileUri) {
        Cursor cursor = activity.getContentResolver().query(fileUri, null, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
            if (sizeIndex != -1) {
                long size = cursor.getLong(sizeIndex);
                cursor.close();
                return size;
            }
            cursor.close();
        }

        return 0;
    }

    public static String removeExtension(String fileName) {
        String[] parts = fileName.split("\\.");
        StringBuilder stringBuilder = new StringBuilder();

        for (int i = 0; i < parts.length - 1; i++) {
            stringBuilder.append(parts[i]).append(".");
        }

        stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        return stringBuilder.toString();
    }
}
