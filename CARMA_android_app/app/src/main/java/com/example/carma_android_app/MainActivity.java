package com.example.carma_android_app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Main entry point for the CARMA Android application Handles navigation between
 * Preview, Review, and Settings screens
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize components
        initializeViews();
        setupNavigation();
    }

    private void initializeViews() {
        // TODO: Initialize UI components
    }

    private void setupNavigation() {
        // TODO: Setup bottom navigation or tabs
    }

    /** Opens the saved-messages review screen from the home layout (US #179 / #200). */
    public void openHomeReview(View v) {
        startActivity(new Intent(this, ReviewActivity.class));
    }
}
