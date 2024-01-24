package com.example.ragapplication;

import android.widget.ImageView;

import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

public class HandleNavigationDrawersVisibility {
    private ImageView leftNavigationDrawerIcon, rightNavigationDrawerIcon;
    private NavigationView leftNavigationView, rightNavigationView;
    private DrawerLayout drawerLayout;

    public HandleNavigationDrawersVisibility(ImageView leftNavigationDrawerIcon, ImageView rightNavigationDrawerIcon, NavigationView leftNavigationView, NavigationView rightNavigationView, DrawerLayout drawerLayout) {
        this.leftNavigationDrawerIcon = leftNavigationDrawerIcon;
        this.rightNavigationDrawerIcon = rightNavigationDrawerIcon;
        this.leftNavigationView = leftNavigationView;
        this.rightNavigationView = rightNavigationView;
        this.drawerLayout = drawerLayout;
    }

    public void setNavigationDrawerListeners() {
        this.leftNavigationDrawerIcon.setOnClickListener(v -> {
            if (this.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                this.drawerLayout.closeDrawer(GravityCompat.START);
            } else {
                this.drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        this.rightNavigationDrawerIcon.setOnClickListener(v -> {
            if (this.drawerLayout.isDrawerOpen(GravityCompat.END)) {
                this.drawerLayout.closeDrawer(GravityCompat.END);
            } else {
                this.drawerLayout.openDrawer(GravityCompat.END);
            }
        });
    }
}
