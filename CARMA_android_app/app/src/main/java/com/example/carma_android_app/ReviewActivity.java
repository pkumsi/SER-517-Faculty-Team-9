package com.example.carma_android_app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.carma_android_app.database.AppDatabase;
import com.example.carma_android_app.database.FeedbackEntity;
import com.example.carma_android_app.database.FeedbackRepository;
import com.example.carma_android_app.database.MessageEntity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Review screen: loads saved preview messages from the local Room database.
 */
public class ReviewActivity extends AppCompatActivity {
    private BottomNavigationView bottomNavigation;
    private RecyclerView recyclerView;
    private SavedMessageAdapter adapter;
    private TextView tvTotalMessages;
    private TextView tvPositiveCount;
    private TextView tvNegativeCount;
    private View layoutEmptyState;
    private ExecutorService executorService;
    private FeedbackRepository feedbackRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);

        executorService = Executors.newSingleThreadExecutor();
        feedbackRepository = new FeedbackRepository(this);

        recyclerView = findViewById(R.id.recycler_messages);
        tvTotalMessages = findViewById(R.id.tv_total_count);
        tvPositiveCount = findViewById(R.id.tv_positive_count);
        tvNegativeCount = findViewById(R.id.tv_negative_count);
        layoutEmptyState = findViewById(R.id.layout_empty_state);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SavedMessageAdapter();
        recyclerView.setAdapter(adapter);

        adapter.setOnMessageClickListener(message -> {
            FeedbackDialog dialog = new FeedbackDialog(this, message,
                    (messageId, isPositive, comment) ->
                            feedbackRepository.saveOrReplaceFeedback(messageId, isPositive, comment,
                                    new FeedbackRepository.Callback<Long>() {
                                        @Override
                                        public void onSuccess(Long id) {
                                            Toast.makeText(ReviewActivity.this,
                                                    "Feedback saved",
                                                    Toast.LENGTH_SHORT).show();
                                            loadSavedMessages();
                                        }

                                        @Override
                                        public void onError(Exception e) {
                                            Toast.makeText(ReviewActivity.this,
                                                    "Could not save feedback: " + e.getMessage(),
                                                    Toast.LENGTH_LONG).show();
                                        }
                                    }));
            dialog.show();
        });

        bottomNavigation = findViewById(R.id.bottom_navigation);
        if (bottomNavigation != null) {
            bottomNavigation.setOnItemSelectedListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.nav_preview) {
                    finish();
                    return true;
                }
                if (itemId == R.id.nav_review) {
                    return true;
                }
                if (itemId == R.id.nav_contacts) {
                    startActivity(new Intent(this, SettingsActivity.class));
                    return true;
                }
                return false;
            });
            bottomNavigation.setSelectedItemId(R.id.nav_review);
        }

        loadSavedMessages();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadSavedMessages();
    }

    private void loadSavedMessages() {
        executorService.execute(() -> {
            try {
                AppDatabase db = AppDatabase.getInstance(getApplicationContext());
                List<MessageEntity> messages = db.messageDao().getAllMessages();
                List<FeedbackEntity> feedbackList = db.feedbackDao().getAllFeedback();

                Map<Long, String> feedbackByMessage = new HashMap<>();
                int positive = 0;
                int negative = 0;
                for (FeedbackEntity f : feedbackList) {
                    feedbackByMessage.put(f.getMessageId(), f.getFeedbackType());
                    if (FeedbackEntity.THUMBS_UP.equals(f.getFeedbackType())) {
                        positive++;
                    } else if (FeedbackEntity.THUMBS_DOWN.equals(f.getFeedbackType())) {
                        negative++;
                    }
                }

                final int total = messages.size();
                final List<MessageEntity> messagesFinal = messages;
                final Map<Long, String> fbFinal = feedbackByMessage;
                final int posFinal = positive;
                final int negFinal = negative;

                runOnUiThread(() -> {
                    adapter.setData(messagesFinal, fbFinal);
                    tvTotalMessages.setText(String.valueOf(total));
                    tvPositiveCount.setText(String.valueOf(posFinal));
                    tvNegativeCount.setText(String.valueOf(negFinal));

                    boolean empty = total == 0;
                    recyclerView.setVisibility(empty ? View.GONE : View.VISIBLE);
                    if (layoutEmptyState != null) {
                        layoutEmptyState.setVisibility(empty ? View.VISIBLE : View.GONE);
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(this, "Failed to load saved messages: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show());
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
