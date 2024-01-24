package com.example.ragapplication;

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
}
