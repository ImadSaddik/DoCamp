package com.example.ragapplication;

import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.Toast;

import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

class HandleSwipeAndDrawers extends GestureDetector.SimpleOnGestureListener {
    private final MainActivity mainActivity;
    private DrawerLayout drawerLayout;
    private static final int SWIPE_THRESHOLD = 100;
    private static final int SWIPE_VELOCITY_THRESHOLD = 100;

    public HandleSwipeAndDrawers(MainActivity mainActivity, DrawerLayout drawerLayout) {
        this.mainActivity = mainActivity;
        this.drawerLayout = drawerLayout;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        float diffX = e2.getX() - e1.getX();
        float diffY = e2.getY() - e1.getY();

        if (Math.abs(diffX) > Math.abs(diffY)) {
            if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD && isAllDrawersClosed()) {
                if (diffX > 0) {
                    openLeftDrawer();
                } else {
                    openRightDrawer();
                }
                return true;
            }
        }

        return false;
    }

    private void openLeftDrawer() {
        drawerLayout.openDrawer(GravityCompat.START);
    }

    private void openRightDrawer() {
        drawerLayout.openDrawer(GravityCompat.END);
    }

    private boolean isAllDrawersClosed() {
        return !drawerLayout.isDrawerOpen(GravityCompat.START) && !drawerLayout.isDrawerOpen(GravityCompat.END);
    }
}
