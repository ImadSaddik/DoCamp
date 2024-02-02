package com.example.ragapplication;

import android.app.Activity;
import android.content.res.TypedArray;
import android.graphics.Color;

public class ThemeUtils {
    public static int getTextColorBasedOnTheme(int color, Activity activity) {
        android.content.res.Resources.Theme theme = activity.getTheme();

        int[] attrs = {color};
        TypedArray typedArray = theme.obtainStyledAttributes(attrs);

        int textPrimaryColor = typedArray.getColor(0, Color.BLACK);
        typedArray.recycle();

        return textPrimaryColor;
    }

    public static int getBackgroundColorBasedOnTheme(int color, Activity activity) {
        android.content.res.Resources.Theme theme = activity.getTheme();

        int[] attrs = {color};
        TypedArray typedArray = theme.obtainStyledAttributes(attrs);

        int backgroundColor = typedArray.getColor(0, Color.BLACK);
        typedArray.recycle();

        return backgroundColor;
    }
}
