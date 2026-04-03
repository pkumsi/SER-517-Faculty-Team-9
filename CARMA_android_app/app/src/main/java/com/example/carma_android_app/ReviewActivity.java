package com.example.carma_android_app;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class ReviewActivity extends AppCompatActivity {
    private BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review); // You will need to create this layout

        // Initialize bottom navigation if present in layout
        bottomNavigation = findViewById(R.id.bottom_navigation);
        if (bottomNavigation != null) {
            bottomNavigation.setOnItemSelectedListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.nav_preview) {
                    // TODO: Navigate to PreviewActivity
                    return true;
                } else if (itemId == R.id.nav_review) {
                    // Already on review screen
                    return true;
                } else if (itemId == R.id.nav_contacts) {
                    // TODO: Navigate to ContactsActivity
                    return true;
                }
                return false;
            });
        }
    }
}
