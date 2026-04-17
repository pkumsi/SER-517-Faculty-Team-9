package com.example.carma_android_app;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
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
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Review screen: loads saved preview messages from the local Room database.
 */
public class ReviewActivity extends AppCompatActivity {
    private static final int FILTER_TODAY = 0;
    private static final int FILTER_YESTERDAY = 1;
    private static final int FILTER_LAST_7_DAYS = 2;
    private static final int FILTER_ALL = 3;
    private static final int FILTER_CUSTOM = 4;

    private BottomNavigationView bottomNavigation;
    private RecyclerView recyclerView;
    private SavedMessageAdapter adapter;
    private ImageButton btnDateFilter;
    private TextView tvSelectedDate;
    private TextView tvTotalMessages;
    private TextView tvPositiveCount;
    private TextView tvNegativeCount;
    private ChipGroup chipGroupFilters;
    private Chip chipToday;
    private Chip chipYesterday;
    private Chip chipLast7Days;
    private Chip chipAll;
    private View layoutEmptyState;
    private ExecutorService executorService;
    private FeedbackRepository feedbackRepository;
    private final List<MessageEntity> allMessages = new ArrayList<>();
    private final Map<Long, String> feedbackByMessage = new HashMap<>();
    private final Map<Long, FeedbackEntity> feedbackEntityByMessage = new HashMap<>();
    private final Map<Long, Boolean> reviewedByMessage = new HashMap<>();
    private int activeFilter = FILTER_TODAY;
    private long customFromMillis = -1L;
    private long customToMillis = -1L;
    private final SimpleDateFormat dateHeaderFormat = new SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault());
    private final SimpleDateFormat shortDateFormat = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);

        executorService = Executors.newSingleThreadExecutor();
        feedbackRepository = new FeedbackRepository(this);

        btnDateFilter = findViewById(R.id.btn_date_filter);
        tvSelectedDate = findViewById(R.id.tv_selected_date);
        recyclerView = findViewById(R.id.recycler_messages);
        tvTotalMessages = findViewById(R.id.tv_total_count);
        tvPositiveCount = findViewById(R.id.tv_positive_count);
        tvNegativeCount = findViewById(R.id.tv_negative_count);
        chipGroupFilters = findViewById(R.id.chip_group_filters);
        chipToday = findViewById(R.id.chip_today);
        chipYesterday = findViewById(R.id.chip_yesterday);
        chipLast7Days = findViewById(R.id.chip_last_7_days);
        chipAll = findViewById(R.id.chip_all);
        layoutEmptyState = findViewById(R.id.layout_empty_state);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SavedMessageAdapter();
        recyclerView.setAdapter(adapter);

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
                return false;
            });
            bottomNavigation.setSelectedItemId(R.id.nav_review);
        }

        setupDateFiltering();
        updateDateHeaderText();
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

                Map<Long, String> feedbackByMessageLocal = new HashMap<>();
                Map<Long, FeedbackEntity> feedbackEntityByMessageLocal = new HashMap<>();
                Map<Long, Boolean> reviewedByMessageLocal = new HashMap<>();
                for (FeedbackEntity f : feedbackList) {
                    feedbackByMessageLocal.put(f.getMessageId(), f.getFeedbackType());
                    feedbackEntityByMessageLocal.put(f.getMessageId(), f);
                    reviewedByMessageLocal.put(f.getMessageId(), hasQuestionnaireAnswers(f));
                }

                runOnUiThread(() -> {
                    allMessages.clear();
                    allMessages.addAll(messages);
                    feedbackByMessage.clear();
                    feedbackByMessage.putAll(feedbackByMessageLocal);
                    feedbackEntityByMessage.clear();
                    feedbackEntityByMessage.putAll(feedbackEntityByMessageLocal);
                    reviewedByMessage.clear();
                    reviewedByMessage.putAll(reviewedByMessageLocal);

                    applyCurrentFilterAndRender();
                    adapter.setOnMessageClickListener(message -> {
                        FeedbackDialog dialog = new FeedbackDialog(
                                this,
                                message,
                                feedbackEntityByMessage.get(message.getId()),
                                (messageId, q1Usefulness, q2Comfort, q3Appropriateness, q4ExplanationSense, q5Clarity) ->
                                        feedbackRepository.saveOrReplaceQuestionnaireFeedback(
                                                messageId,
                                                q1Usefulness,
                                                q2Comfort,
                                                q3Appropriateness,
                                                q4ExplanationSense,
                                                q5Clarity,
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
                                                })
                        );
                        dialog.show();
                    });
                });
            } catch (Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(this, "Failed to load saved messages: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show());
            }
        });
    }

    private static boolean hasQuestionnaireAnswers(FeedbackEntity feedback) {
        if (feedback == null) {
            return false;
        }
        return notBlank(feedback.getQ1Usefulness())
                || notBlank(feedback.getQ2Comfort())
                || notBlank(feedback.getQ3Appropriateness())
                || notBlank(feedback.getQ4ExplanationSense())
                || notBlank(feedback.getQ5Clarity());
    }

    private static boolean notBlank(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private void setupDateFiltering() {
        if (chipToday != null) {
            chipToday.setOnClickListener(v -> {
                activeFilter = FILTER_TODAY;
                applyCurrentFilterAndRender();
            });
        }
        if (chipYesterday != null) {
            chipYesterday.setOnClickListener(v -> {
                activeFilter = FILTER_YESTERDAY;
                applyCurrentFilterAndRender();
            });
        }
        if (chipLast7Days != null) {
            chipLast7Days.setOnClickListener(v -> {
                activeFilter = FILTER_LAST_7_DAYS;
                applyCurrentFilterAndRender();
            });
        }
        if (chipAll != null) {
            chipAll.setOnClickListener(v -> {
                activeFilter = FILTER_ALL;
                applyCurrentFilterAndRender();
            });
        }
        if (btnDateFilter != null) {
            btnDateFilter.setOnClickListener(v -> openDateFilterDialog());
        }
    }

    public void onDateFilterClicked(View view) {
        openDateFilterDialog();
    }

    private void openDateFilterDialog() {
        DateFilterDialog dialog = new DateFilterDialog(this, (from, to) -> {
            long start = startOfDay(Math.min(from, to));
            long end = endOfDay(Math.max(from, to));
            customFromMillis = start;
            customToMillis = end;
            activeFilter = FILTER_CUSTOM;
            if (chipGroupFilters != null) {
                chipGroupFilters.clearCheck();
            }
            applyCurrentFilterAndRender();
        });
        dialog.show();
    }

    private void applyCurrentFilterAndRender() {
        List<MessageEntity> filteredMessages = filterMessagesByActiveRange();

        int positive = 0;
        int negative = 0;
        for (MessageEntity m : filteredMessages) {
            String feedbackType = feedbackByMessage.get(m.getId());
            if (FeedbackEntity.THUMBS_UP.equals(feedbackType)) {
                positive++;
            } else if (FeedbackEntity.THUMBS_DOWN.equals(feedbackType)) {
                negative++;
            }
        }

        adapter.setData(filteredMessages, feedbackByMessage, reviewedByMessage);
        tvTotalMessages.setText(String.valueOf(filteredMessages.size()));
        tvPositiveCount.setText(String.valueOf(positive));
        tvNegativeCount.setText(String.valueOf(negative));
        updateDateHeaderText();

        boolean empty = filteredMessages.isEmpty();
        recyclerView.setVisibility(empty ? View.GONE : View.VISIBLE);
        if (layoutEmptyState != null) {
            layoutEmptyState.setVisibility(empty ? View.VISIBLE : View.GONE);
        }
    }

    private List<MessageEntity> filterMessagesByActiveRange() {
        long now = System.currentTimeMillis();
        long start;
        long end;
        switch (activeFilter) {
            case FILTER_TODAY:
                start = startOfDay(now);
                end = endOfDay(now);
                break;
            case FILTER_YESTERDAY:
                long yesterday = now - (24L * 60L * 60L * 1000L);
                start = startOfDay(yesterday);
                end = endOfDay(yesterday);
                break;
            case FILTER_LAST_7_DAYS:
                end = endOfDay(now);
                start = startOfDay(now - (6L * 24L * 60L * 60L * 1000L));
                break;
            case FILTER_CUSTOM:
                if (customFromMillis <= 0 || customToMillis <= 0) {
                    return new ArrayList<>(allMessages);
                }
                start = customFromMillis;
                end = customToMillis;
                break;
            case FILTER_ALL:
            default:
                return new ArrayList<>(allMessages);
        }

        List<MessageEntity> filtered = new ArrayList<>();
        for (MessageEntity message : allMessages) {
            long ts = message.getTimestamp();
            if (ts >= start && ts <= end) {
                filtered.add(message);
            }
        }
        return filtered;
    }

    private void updateDateHeaderText() {
        if (tvSelectedDate == null) {
            return;
        }
        long now = System.currentTimeMillis();
        if (activeFilter == FILTER_TODAY) {
            tvSelectedDate.setText(dateHeaderFormat.format(now));
            return;
        }
        if (activeFilter == FILTER_YESTERDAY) {
            long yesterday = now - (24L * 60L * 60L * 1000L);
            tvSelectedDate.setText("Yesterday (" + dateHeaderFormat.format(yesterday) + ")");
            return;
        }
        if (activeFilter == FILTER_LAST_7_DAYS) {
            long start = startOfDay(now - (6L * 24L * 60L * 60L * 1000L));
            long end = endOfDay(now);
            tvSelectedDate.setText("Last 7 Days: " + shortDateFormat.format(start) + " - " + shortDateFormat.format(end));
            return;
        }
        if (activeFilter == FILTER_CUSTOM && customFromMillis > 0 && customToMillis > 0) {
            tvSelectedDate.setText(shortDateFormat.format(customFromMillis) + " - " + shortDateFormat.format(customToMillis));
            return;
        }
        tvSelectedDate.setText("All Messages");
    }

    private long startOfDay(long millis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(millis);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    private long endOfDay(long millis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(millis);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return calendar.getTimeInMillis();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}
