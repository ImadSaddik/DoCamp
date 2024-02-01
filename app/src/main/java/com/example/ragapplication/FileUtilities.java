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
}
