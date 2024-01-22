package com.example.ragapplication;

public interface ResponseCallback {
    void onResponse(String response);
    void onError(Throwable throwable);
}
