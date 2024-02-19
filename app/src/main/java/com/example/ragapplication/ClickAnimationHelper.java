package com.example.ragapplication;

import android.animation.AnimatorInflater;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.MotionEvent;
import android.view.View;

public class ClickAnimationHelper {
    @SuppressLint("ClickableViewAccessibility")
    public static void setViewClickAnimation(Activity activity, View view) {
        view.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                v.setStateListAnimator(AnimatorInflater.loadStateListAnimator(activity, R.animator.click_animation_down));
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                v.setStateListAnimator(AnimatorInflater.loadStateListAnimator(activity, R.animator.click_animation_up));
            }

            return false;
        });
    }
}
