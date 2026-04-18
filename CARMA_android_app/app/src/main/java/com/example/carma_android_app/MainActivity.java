package com.example.carma_android_app;

import android.os.Bundle;
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

    /** Opens settings from the home layout (US #179 / #201). Uses FQN to ease parallel merges with #200. */
    public void openHomeSettings(android.view.View v) {
        startActivity(new android.content.Intent(this, SettingsActivity.class));
    }
}
