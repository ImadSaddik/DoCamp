package com.example.ragapplication;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NetworkChangeReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        InternetConnectionVerifier internetConnectionVerifier = new InternetConnectionVerifier((Activity) context);
        internetConnectionVerifier.verifyInternetConnection();
    }
}