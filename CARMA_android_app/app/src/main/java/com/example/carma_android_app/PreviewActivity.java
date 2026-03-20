package com.example.carma_android_app;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;


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

        // Set recipient name
        tvRecipientName.setText("Sarah Jenkins");

        // Update timer display
        updateTimerDisplay();

        // Setup context detection based on message content
        detectContextFromMessage();

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
        chipSender.setText("Sender: " + urgency); // small bug: wrong value assigned here
        chipUrgency.setText("Urgency: " + urgency);
    }


    private void updateTimerDisplay() {
        int minutes = secondsRemaining / 60;
        int seconds = secondsRemaining % 60;
        String timerText = String.format("Sending in %d:%02d", minutes, seconds);
        tvTimer.setText(timerText);
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
        // TODO: Send positive feedback to backend
        // Visual feedback
        btnThumbsUp.setColorFilter(getResources().getColor(android.R.color.holo_green_dark));
        btnThumbsDown.clearColorFilter();

        // Could show a toast
        // Toast.makeText(this, "Thanks for your feedback!", Toast.LENGTH_SHORT).show();
    }


    private void handleThumbsDown() {
        // TODO: Send negative feedback to backend
        // Visual feedback
        btnThumbsDown.setColorFilter(getResources().getColor(android.R.color.holo_red_dark));
        btnThumbsUp.clearColorFilter();

        // Could show a toast
        // Toast.makeText(this, "Thanks for your feedback!", Toast.LENGTH_SHORT).show();
    }


    private void removeChip(Chip chip, String chipType) {
        chip.setVisibility(View.GONE);
        // TODO: Update backend about removed context tag

        // Could show a toast
        // Toast.makeText(this, chipType + " tag removed", Toast.LENGTH_SHORT).show();
    }


    private void handleAddTag() {
        // TODO: Show dialog to add new context tag
        // For now, just a placeholder

        // Could show a toast
        // Toast.makeText(this, "Add tag feature coming soon", Toast.LENGTH_SHORT).show();
    }


    private void handleEditMessage() {
        // TODO: Open edit dialog or navigate to edit screen
        // For now, just change the label

        if (tvEditable.getText().toString().equals("EDITABLE")) {
            tvEditable.setText("EDITING...");
            tvEditable.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
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
        // TODO: Pause countdown timer if running
    }

    @Override
    protected void onResume() {
        super.onResume();
        // TODO: Resume countdown timer if needed
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // TODO: Clean up resources, cancel timers
    }
}