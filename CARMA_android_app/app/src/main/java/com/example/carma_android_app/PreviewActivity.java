package com.example.carma_android_app;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.app.AlertDialog;
import android.widget.EditText;
import android.widget.LinearLayout;
import androidx.core.content.ContextCompat;
import com.example.carma_android_app.network.ApiClient;
import com.example.carma_android_app.models.PreviewScreenData;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import org.json.JSONObject;
import java.util.Arrays;

public class PreviewActivity extends AppCompatActivity {

    // Header views
    private TextView tvHeaderTitle;
    private TextView tvSubtitle;
    private TextView tvRecipientName;

    // Timer section
    private ImageView ivTimerIcon;
    private TextView tvTimer;
    private ImageButton btnThumbsUp;
    private ImageButton btnThumbsDown;

    // Context section
    private MaterialCardView cardContext;
    private LinearLayout layoutContextHeader;
    private LinearLayout layoutContextContent;
    private ImageView ivExpandCollapse;
    private Chip chipActivity;
    private Chip chipSender;
    private Chip chipUrgency;
    private Chip chipAdd;

    // Message section
    private MaterialCardView cardMessage;
    private TextView tvMessageContent;
    private TextView tvEditable;

    // Action buttons
    private MaterialButton btnCancel;
    private MaterialButton btnSendNow;

    // Bottom navigation
    private BottomNavigationView bottomNavigation;

    // State variables
    private boolean isContextExpanded = true;
    private int secondsRemaining = 214; // 3:34 in seconds
    private static final long INITIAL_TIME_MILLIS = 214000; // 214 seconds in milliseconds
    private CountDownTimer countdownTimer;
    private boolean isTimerRunning = false;

    private String requestId = "demo-request-id"; // TODO: Replace with real request ID
    private String uuid = "demo-uuid"; // TODO: Replace with real UUID

    // Preview screen data model
    private PreviewScreenData previewScreenData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);

        // Initialize all views
        initializeViews();

        // Setup click listeners
        setupClickListeners();

        // Load initial data
        loadPreviewData();

        // Start the countdown timer
        startCountdownTimer();
    }


    private void initializeViews() {
        // Header
        tvHeaderTitle = findViewById(R.id.tv_header_title);
        tvSubtitle = findViewById(R.id.tv_subtitle);
        tvRecipientName = findViewById(R.id.tv_recipient_name);

        // Timer section
        ivTimerIcon = findViewById(R.id.iv_timer_icon);
        tvTimer = findViewById(R.id.tv_timer);
        btnThumbsUp = findViewById(R.id.btn_thumbs_up);
        btnThumbsDown = findViewById(R.id.btn_thumbs_down);

        // Context section
        cardContext = findViewById(R.id.card_context);
        layoutContextHeader = findViewById(R.id.layout_context_header);
        layoutContextContent = findViewById(R.id.layout_context_content);
        ivExpandCollapse = findViewById(R.id.iv_expand_collapse);
        chipActivity = findViewById(R.id.chip_activity);
        chipSender = findViewById(R.id.chip_sender);
        chipUrgency = findViewById(R.id.chip_urgency);
        chipAdd = findViewById(R.id.chip_add);

        // Message section
        cardMessage = findViewById(R.id.card_message);
        tvMessageContent = findViewById(R.id.tv_message_content);
        tvEditable = findViewById(R.id.tv_editable);

        // Action buttons
        btnCancel = findViewById(R.id.btn_cancel);
        btnSendNow = findViewById(R.id.btn_send_now);

        // Bottom navigation
        bottomNavigation = findViewById(R.id.bottom_navigation);
    }


    private void setupClickListeners() {
        // Thumbs up/down feedback
        btnThumbsUp.setOnClickListener(v -> handleThumbsUp());
        btnThumbsDown.setOnClickListener(v -> handleThumbsDown());

        // Context section expand/collapse
        layoutContextHeader.setOnClickListener(v -> toggleContextSection());

        // Context chips close buttons
        chipActivity.setOnCloseIconClickListener(v -> removeChip(chipActivity, "Activity"));
        chipSender.setOnCloseIconClickListener(v -> removeChip(chipSender, "Sender"));
        chipUrgency.setOnCloseIconClickListener(v -> removeChip(chipUrgency, "Urgency"));

        // Add tag button
        chipAdd.setOnClickListener(v -> handleAddTag());

        // Edit message
        tvEditable.setOnClickListener(v -> handleEditMessage());

        // Action buttons
        btnCancel.setOnClickListener(v -> handleCancelSend());
        btnSendNow.setOnClickListener(v -> handleSendNow());

        // Bottom navigation
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_preview) {
                // Already on preview screen
                return true;
            } else if (itemId == R.id.nav_review) {
                navigateToReview();
                return true;
            } else if (itemId == R.id.nav_contacts) {
                navigateToContacts();
                return true;
            }
            return false;
        });
    }


    private void loadPreviewData() {
        // TODO: Fetch data from backend API
        // For now, using hardcoded values from layout
        previewScreenData = new PreviewScreenData(
            "Sarah Jenkins", // recipientName
            "I'll be in a meeting until 3:30pm. Will reply after.", // messageContent
            "Sending in 3:34", // timerText
            Arrays.asList("In a meeting", "Manager", "High"), // contextTags
            true, // arEnabled
            true, // sentAR
            false, // liked
            requestId,
            uuid
        );
        // Set recipient name
        tvRecipientName.setText(previewScreenData.getRecipientName());
        // Set message content
        tvMessageContent.setText(previewScreenData.getMessageContent());
        // Update timer display
        tvTimer.setText(previewScreenData.getTimerText());
        // Setup context chips
        bindContextChips(
            previewScreenData.getContextTags().get(0),
            previewScreenData.getContextTags().get(1),
            previewScreenData.getContextTags().get(2)
        );
        // Set default bottom navigation selection
        bottomNavigation.setSelectedItemId(R.id.nav_preview);
    }


    private void detectContextFromMessage() {
        String message = tvMessageContent.getText().toString().toLowerCase();

        String detectedActivity;
        if (message.contains("meeting") || message.contains("sync")) {
            detectedActivity = "In a meeting";
        } else if (message.contains("review")) {
            detectedActivity = "Reviewing a file";
        } else {
            detectedActivity = "Working";
        }

        String detectedSender;
        if (message.contains("manager") || message.contains("lead")) {
            detectedSender = "Manager";
        } else {
            detectedSender = "Coworker";
        }

        String detectedUrgency;
        if (message.contains("as soon as") || message.contains("urgent")) {
            detectedUrgency = "High";
        } else {
            detectedUrgency = "Normal";
        }

        bindContextChips(detectedActivity, detectedSender, detectedUrgency);
    }

    private void bindContextChips(String activity, String sender, String urgency) {
        chipActivity.setText("Activity: " + activity);
        chipSender.setText("Sender: " + sender);
        chipUrgency.setText("Urgency: " + urgency);
    }


    private void updateTimerDisplay() {
        int minutes = secondsRemaining / 60;
        int seconds = secondsRemaining % 60;
        String timerText = String.format("Sending in %d:%02d", minutes, seconds);
        tvTimer.setText(timerText);
    }

    private void startCountdownTimer() {
        // Cancel any existing timer
        if (countdownTimer != null) {
            countdownTimer.cancel();
        }

        // Create and start new countdown timer
        countdownTimer = new CountDownTimer(INITIAL_TIME_MILLIS, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                // Update secondsRemaining based on milliseconds remaining
                secondsRemaining = (int) (millisUntilFinished / 1000);
                updateTimerDisplay();
            }

            @Override
            public void onFinish() {
                // Timer finished - auto-send the message
                secondsRemaining = 0;
                updateTimerDisplay();
                isTimerRunning = false;
                handleAutoSend();
            }
        };

        countdownTimer.start();
        isTimerRunning = true;
    }

    private void pauseCountdownTimer() {
        if (countdownTimer != null && isTimerRunning) {
            countdownTimer.cancel();
            isTimerRunning = false;
        }
    }

    private void resumeCountdownTimer() {
        if (!isTimerRunning && secondsRemaining > 0) {
            // Resume timer with remaining time
            if (countdownTimer != null) {
                countdownTimer.cancel();
            }

            countdownTimer = new CountDownTimer(secondsRemaining * 1000, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    secondsRemaining = (int) (millisUntilFinished / 1000);
                    updateTimerDisplay();
                }

                @Override
                public void onFinish() {
                    secondsRemaining = 0;
                    updateTimerDisplay();
                    isTimerRunning = false;
                    handleAutoSend();
                }
            };

            countdownTimer.start();
            isTimerRunning = true;
        }
    }

    private void handleAutoSend() {
        // Auto-send when timer reaches zero
        handleSendNow();
    }


    private void toggleContextSection() {
        if (isContextExpanded) {
            // Collapse
            layoutContextContent.setVisibility(View.GONE);
            ivExpandCollapse.setRotation(180); // Arrow points down
            isContextExpanded = false;
        } else {
            // Expand
            layoutContextContent.setVisibility(View.VISIBLE);
            ivExpandCollapse.setRotation(0); // Arrow points up
            isContextExpanded = true;
        }
    }


    private void handleThumbsUp() {
        btnThumbsUp.setColorFilter(getResources().getColor(android.R.color.holo_green_dark));
        btnThumbsDown.clearColorFilter();
        sendFeedbackToBackend(true);
    }


    private void handleThumbsDown() {
        btnThumbsDown.setColorFilter(getResources().getColor(android.R.color.holo_red_dark));
        btnThumbsUp.clearColorFilter();
        sendFeedbackToBackend(false);
    }

    private void sendFeedbackToBackend(boolean like) {
        // Build JSON body
        try {
            JSONObject json = new JSONObject();
            json.put("request_id", requestId);
            json.put("uuid", uuid);
            json.put("like", like);
            String body = json.toString();
            new FeedbackTask().execute(body);
        } catch (Exception e) {
            Toast.makeText(this, "Failed to build feedback request", Toast.LENGTH_SHORT).show();
        }
    }

    private class FeedbackTask extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... params) {
            try {
                String response = ApiClient.getInstance().sendFeedback(params[0]);
                return response != null;
            } catch (Exception e) {
                return false;
            }
        }
        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                Toast.makeText(PreviewActivity.this, "Thanks for your feedback!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(PreviewActivity.this, "Failed to send feedback", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void removeChip(Chip chip, String chipType) {
        chip.setVisibility(View.GONE);
        // TODO: Update backend about removed context tag

        // Could show a toast
        // Toast.makeText(this, chipType + " tag removed", Toast.LENGTH_SHORT).show();
    }


    private void handleAddTag() {
        // Create dialog to add new context tag
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Context Tag");

        // Create EditText for user input
        final EditText input = new EditText(this);
        input.setHint("Enter tag name (e.g., Location: Office)");
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        input.setLayoutParams(lp);
        builder.setView(input);

        // Set up dialog buttons
        builder.setPositiveButton("Add", (dialog, which) -> {
            String tagText = input.getText().toString().trim();
            if (!tagText.isEmpty()) {
                addNewContextChip(tagText);
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void addNewContextChip(String tagText) {
        // Create new chip programmatically
        Chip newChip = new Chip(this);
        newChip.setText(tagText);
        newChip.setTextColor(ContextCompat.getColor(this, android.R.color.holo_blue_dark));
        newChip.setChipBackgroundColorResource(android.R.color.holo_blue_light);
        newChip.setCloseIconVisible(true);
        newChip.setCloseIconTintResource(android.R.color.holo_blue_dark);

        // Set layout parameters
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.bottomMargin = (int) (8 * getResources().getDisplayMetrics().density); // 8dp margin
        newChip.setLayoutParams(params);

        // Handle chip removal
        newChip.setOnCloseIconClickListener(v -> {
            ((LinearLayout) layoutContextContent).removeView(newChip);
        });

        // Add chip to the layout (before the "Add" button)
        int addButtonIndex = ((LinearLayout) layoutContextContent).indexOfChild(chipAdd);
        ((LinearLayout) layoutContextContent).addView(newChip, addButtonIndex);
    }


    private void handleEditMessage() {
        // TODO: Open edit dialog or navigate to edit screen
        // For now, just change the label

        if (tvEditable.getText().toString().equals("EDITABLE")) {
            tvEditable.setText("EDITING...");
            tvEditable.setTextColor(ContextCompat.getColor(this, android.R.color.holo_orange_dark));
        }

        // Could show a toast
        // Toast.makeText(this, "Edit mode - coming soon", Toast.LENGTH_SHORT).show();
    }


    private void handleCancelSend() {
        // TODO: Cancel the auto-send and close the activity

        // Could show confirmation dialog
        // For now, just finish the activity
        finish();

        // Could show a toast
        // Toast.makeText(this, "Auto-send cancelled", Toast.LENGTH_SHORT).show();
    }


    private void handleSendNow() {
        // TODO: Send the message immediately via backend

        // Could show loading indicator
        // Could navigate to confirmation screen

        // For now, just finish
        finish();

        // Could show a toast
        // Toast.makeText(this, "Message sent!", Toast.LENGTH_SHORT).show();
    }


    private void navigateToReview() {
        // TODO: Implement navigation to ReviewActivity
        // Intent intent = new Intent(this, ReviewActivity.class);
        // startActivity(intent);

        // Toast.makeText(this, "Review screen - coming soon", Toast.LENGTH_SHORT).show();
    }

    private void navigateToContacts() {
        // TODO: Implement navigation to ContactsActivity
        // Intent intent = new Intent(this, ContactsActivity.class);
        // startActivity(intent);

        // Toast.makeText(this, "Contacts screen - coming soon", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Pause countdown timer if running
        pauseCountdownTimer();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Resume countdown timer if needed
        resumeCountdownTimer();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up resources, cancel timers
        if (countdownTimer != null) {
            countdownTimer.cancel();
            countdownTimer = null;
            isTimerRunning = false;
        }
    }
}