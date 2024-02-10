package com.example.ragapplication;

import android.app.Activity;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;

public class AdManager {
    private Activity activity;
    public static int promptCount = 1;
    public static final String TAG = "AD_LOG";
    private InterstitialAd interstitialAd;

    public AdManager(Activity activity) {
        this.activity = activity;
    }

    public void loadAndShowAd() {
        MobileAds.initialize(activity);
        AdRequest adRequest = new AdRequest.Builder().build();
        InterstitialAd.load(activity, "ca-app-pub-3940256099942544/1033173712", adRequest, new InterstitialAdLoadCallback() {
            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                super.onAdFailedToLoad(loadAdError);
                Log.e(TAG, "Ad failed to load.");
                interstitialAd = null;
            }

            @Override
            public void onAdLoaded(@NonNull InterstitialAd interstitialAdObject) {
                super.onAdLoaded(interstitialAdObject);
                Log.d(TAG, "Ad loaded.");
                interstitialAd = interstitialAdObject;
                interstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                    @Override
                    public void onAdClicked() {
                        super.onAdClicked();
                        Log.d(TAG, "Ad was clicked.");
                    }

                    @Override
                    public void onAdDismissedFullScreenContent() {
                        super.onAdDismissedFullScreenContent();
                        Log.d(TAG, "Ad dismissed fullscreen content.");
                        interstitialAd = null;
                    }

                    @Override
                    public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                        super.onAdFailedToShowFullScreenContent(adError);
                        Log.e(TAG, "Ad failed to show fullscreen content.");
                        interstitialAd = null;
                    }

                    @Override
                    public void onAdImpression() {
                        super.onAdImpression();
                        Log.d(TAG, "Ad recorded an impression.");
                    }

                    @Override
                    public void onAdShowedFullScreenContent() {
                        super.onAdShowedFullScreenContent();
                        Log.d(TAG, "Ad showed fullscreen content.");
                    }
                });

                showAd();
            }
        });
    }

    private void showAd() {
        Log.d("AD_LOG", "Prompt count: " + promptCount);
        if (interstitialAd != null) {
            interstitialAd.show(activity);
        } else {
            Log.d("AD_LOG", "The interstitial ad wasn't ready yet.");
        }
    }
}
