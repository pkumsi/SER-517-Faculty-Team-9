package com.example.carma_android_app;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.carma_android_app.models.DailyMessageCount;
import com.example.carma_android_app.models.MessageStatistics;
import com.example.carma_android_app.models.ReviewItem;
import com.example.carma_android_app.models.StatisticsReviewItem;
import com.example.carma_android_app.network.ApiClient;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ReviewActivity extends AppCompatActivity {
    private BottomNavigationView bottomNavigation;
    private RecyclerView recyclerView;
    private ReviewItemAdapter adapter;
    private TextView tvTotalMessages;
    private ExecutorService executorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);

        executorService = Executors.newSingleThreadExecutor();

        // Initialize views
        recyclerView = findViewById(R.id.recycler_messages);
        tvTotalMessages = findViewById(R.id.tv_total_count);

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ReviewItemAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

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

        // Load statistics
        loadMessageStatistics();
    }

    private void loadMessageStatistics() {
        executorService.execute(() -> {
            try {
                ApiClient apiClient = ApiClient.getInstance();
                String response = apiClient.getMessageStatistics(7); // Get last 7 days

                JSONObject jsonResponse = new JSONObject(response);
                JSONObject statisticsJson = jsonResponse.getJSONObject("statistics");
                int days = jsonResponse.getInt("days");

                // Parse JSON into data models
                Map<String, Long> statisticsMap = new HashMap<>();
                Iterator<String> keys = statisticsJson.keys();
                while (keys.hasNext()) {
                    String date = keys.next();
                    long count = statisticsJson.getLong(date);
                    statisticsMap.put(date, count);
                }

                MessageStatistics messageStatistics = new MessageStatistics(statisticsMap, days);

                // Create review items from daily counts
                List<ReviewItem> reviewItems = new ArrayList<>();
                for (Map.Entry<String, Long> entry : statisticsMap.entrySet()) {
                    DailyMessageCount dailyCount = new DailyMessageCount(entry.getKey(), entry.getValue());
                    reviewItems.add(new StatisticsReviewItem(dailyCount));
                }

                runOnUiThread(() -> {
                    adapter.updateItems(reviewItems);
                    tvTotalMessages.setText(String.valueOf(messageStatistics.getTotalMessages()));
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Failed to load statistics: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}
