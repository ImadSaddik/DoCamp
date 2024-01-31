package com.example.ragapplication;

import android.content.res.Resources;

public class UnitConverter {
    public static int dpInPixels(int dpValue) {
        float density = Resources.getSystem().getDisplayMetrics().density;
        return (int) (dpValue * density);
    }
}
