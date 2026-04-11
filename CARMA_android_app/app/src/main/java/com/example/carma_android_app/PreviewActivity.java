package com.example.carma_android_app;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.ImageButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.text.InputType;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.example.carma_android_app.database.MessageRepository;
import com.example.carma_android_app.database.MessageEntity;
import com.example.carma_android_app.models.PreviewScreenData;
import com.example.carma_android_app.network.ApiClient;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.Arrays;

public class PreviewActivity extends AppCompatActivity {

    // Intent extra keys — set by ContextInputActivity
    public static final String EXTRA_MESSAGE_TEXT = "extra_message_text";
    public static final String EXTRA_EXPLANATION  = "extra_explanation";
    public static final String EXTRA_REQUEST_ID   = "extra_request_id";
    public static final String EXTRA_UUID         = "extra_uuid";
    public static final String EXTRA_ACTIVITY     = "extra_activity";
    public static final String EXTRA_LOCATION     = "extra_location";
    public static final String EXTRA_NOTIF_APP    = "extra_notif_app";

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

    // Explanation section
    private TextView tvExplanation;

    // Action buttons
    private MaterialButton btnCancel;
    private MaterialButton btnSendNow;

    // Bottom navigation
    private BottomNavigationView bottomNavigation;

    // Database
    private MessageRepository messageRepository;
    private com.example.carma_android_app.database.FeedbackRepository feedbackRepository;
    private long currentMessageId = -1; // Track the current message being edited
    private Boolean pendingThumbFeedback = null; // true = thumbs up, false = thumbs down
    private boolean hasLocalEdits = false;
    private String initialMessageText = "";

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

        // Initialize database
        messageRepository = new MessageRepository(this);
        feedbackRepository = new com.example.carma_android_app.database.FeedbackRepository(this);

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

        // Explanation section
        tvExplanation = findViewById(R.id.tv_explanation);

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
            }
            return false;
        });
    }


    private void loadPreviewData() {
        Intent intent = getIntent();

        // Check if we were launched from ContextInputActivity with real API data
        boolean hasRealData = intent != null && intent.hasExtra(EXTRA_MESSAGE_TEXT);

        if (hasRealData) {
            // Populate from backend response passed via Intent
            String messageText  = intent.getStringExtra(EXTRA_MESSAGE_TEXT);
            String explanText   = intent.getStringExtra(EXTRA_EXPLANATION);
            requestId           = intent.getStringExtra(EXTRA_REQUEST_ID);
            uuid                = intent.getStringExtra(EXTRA_UUID);
            String activity     = intent.getStringExtra(EXTRA_ACTIVITY);
            String location     = intent.getStringExtra(EXTRA_LOCATION);
            String notifApp     = intent.getStringExtra(EXTRA_NOTIF_APP);

            // Map raw values back to human-readable chip labels
            String activityLabel = mapActivityLabel(activity);
            String senderLabel   = mapNotifAppLabel(notifApp);
            String locationLabel = (location != null && !location.equals("NoLoc")) ? location : "Unknown location";

            previewScreenData = new PreviewScreenData(
                "Recipient",    // TODO: pass recipient name from notification in a future task
                messageText != null ? messageText : "No response generated.",
                "Sending in 3:34",
                Arrays.asList(activityLabel, senderLabel, locationLabel),
                true,
                false,
                false,
                requestId != null ? requestId : "unknown-request-id",
                uuid != null ? uuid : "unknown-uuid"
            );
        } else {
            // Fallback: demo data when launched directly (e.g. during development)
            previewScreenData = new PreviewScreenData(
                "Sarah Jenkins",
                "I'll be in a meeting until 3:30pm. Will reply after.",
                "Sending in 3:34",
                Arrays.asList("In a meeting", "Manager", "High"),
                true,
                true,
                false,
                requestId,
                uuid
            );
        }

        // Bind to UI
        tvRecipientName.setText(previewScreenData.getRecipientName());
        tvMessageContent.setText(previewScreenData.getMessageContent());
        initialMessageText = previewScreenData.getMessageContent() != null
                ? previewScreenData.getMessageContent()
                : "";
        tvTimer.setText(previewScreenData.getTimerText());
        bindContextChips(
            previewScreenData.getContextTags().get(0),
            previewScreenData.getContextTags().get(1),
            previewScreenData.getContextTags().get(2)
        );

        // Show explanation if available
        if (hasRealData) {
            String explanText = intent.getStringExtra(EXTRA_EXPLANATION);
            if (explanText != null && !explanText.isEmpty()) {
                tvExplanation.setText("Why this response: " + explanText);
                tvExplanation.setVisibility(android.view.View.VISIBLE);
            } else {
                tvExplanation.setVisibility(android.view.View.GONE);
            }
        } else {
            tvExplanation.setVisibility(android.view.View.GONE);
        }

        bottomNavigation.setSelectedItemId(R.id.nav_preview);
    }

    /** Maps UserAct_Type values back to human-readable labels for the chips. */
    private String mapActivityLabel(String actType) {
        if (actType == null) return "Activity unknown";
        switch (actType) {
            case "still":      return "Still / Sitting";
            case "walking":    return "Walking";
            case "in_vehicle": return "In Vehicle";
            case "on_bicycle": return "On Bicycle";
            default:           return "Activity unknown";
        }
    }

    /** Maps notification app package name to a readable sender label. */
    private String mapNotifAppLabel(String pkg) {
        if (pkg == null) return "Unknown sender";
        switch (pkg) {
            case "com.whatsapp":                      return "WhatsApp";
            case "com.Slack":                         return "Slack";
            case "com.google.android.gm":             return "Gmail";
            case "com.facebook.orca":                 return "Messenger";
            case "com.skype.raider":                  return "Skype";
            case "com.samsung.android.messaging":     return "SMS";
            default:                                  return "Message";
        }
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
        btnThumbsUp.setColorFilter(ContextCompat.getColor(this, android.R.color.holo_green_dark));
        btnThumbsDown.clearColorFilter();
        pendingThumbFeedback = true;
        persistPreviewFeedbackIfPossible();
        sendFeedbackToBackend(true);
    }


    private void handleThumbsDown() {
        btnThumbsDown.setColorFilter(ContextCompat.getColor(this, android.R.color.holo_red_dark));
        btnThumbsUp.clearColorFilter();
        pendingThumbFeedback = false;
        persistPreviewFeedbackIfPossible();
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
        persistMessageSnapshotIfAlreadySaved();
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
            persistMessageSnapshotIfAlreadySaved();
        });

        // Add chip to the layout (before the "Add" button)
        int addButtonIndex = ((LinearLayout) layoutContextContent).indexOfChild(chipAdd);
        ((LinearLayout) layoutContextContent).addView(newChip, addButtonIndex);
        persistMessageSnapshotIfAlreadySaved();
    }


    private void handleEditMessage() {
        // Open a simple edit dialog allowing the user to edit the message preview.
        String current = tvMessageContent.getText().toString();
        EditText editText = new EditText(this);
        editText.setText(current);
        editText.setSelection(current.length());
        editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        editText.setMinLines(3);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit message")
                .setView(editText)
                .setPositiveButton("Save", (dialog, which) -> {
                    String updated = editText.getText().toString().trim();
                    if (!updated.isEmpty()) {
                        // Update UI and model
                        tvMessageContent.setText(updated);
                        if (previewScreenData != null) {
                            previewScreenData.setMessageContent(updated);
                        }
                        hasLocalEdits = true;
                        // Re-detect context based on edited message
                        detectContextFromMessage();

                        // Update message in database if it exists
                        updateMessageInDatabase(updated);

                        Toast.makeText(PreviewActivity.this, "Message updated", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(PreviewActivity.this, "Message cannot be empty", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    // no-op
                })
                .show();
    }


    private void handleCancelSend() {
        // Cancel the countdown timer and notify the user.
        if (countdownTimer != null) {
            countdownTimer.cancel();
            countdownTimer = null;
            isTimerRunning = false;
        }

        // Update timer display to reflect cancellation
        tvTimer.setText("Send cancelled");
        tvTimer.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));

        // Disable both buttons so action cannot be repeated
        btnCancel.setEnabled(false);
        btnSendNow.setEnabled(false);

        // Save cancelled message to database (Toast comes from save callback)
        saveMessageToDatabase("cancelled");
    }


    private void handleSendNow() {
        // Stop the countdown timer — message is being sent now.
        if (countdownTimer != null) {
            countdownTimer.cancel();
            countdownTimer = null;
            isTimerRunning = false;
        }

        // Update timer display to reflect the send action
        tvTimer.setText("Sending…");

        // Disable both buttons so action cannot be repeated
        btnCancel.setEnabled(false);
        btnSendNow.setEnabled(false);

        // Save message to database (Toast comes from save callback)
        saveMessageToDatabase("sent");
    }


    private void navigateToReview() {
        startActivity(new Intent(this, ReviewActivity.class));
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
    /**
     * Save message to database when sent or cancelled
     */
    private void saveMessageToDatabase(String status) {
        if (previewScreenData == null) {
            return;
        }

        // Always persist what the user actually sees in the preview (may differ from model if UI drifted).
        String body = tvMessageContent.getText().toString().trim();
        previewScreenData.setMessageContent(body);

        if (currentMessageId == -1) {
            MessageEntity message = new MessageEntity();
            applyCurrentPreviewToMessage(message, status);
            messageRepository.insertMessage(message, new MessageRepository.Callback<Long>() {
                @Override
                public void onSuccess(Long messageId) {
                    currentMessageId = messageId;
                    persistPreviewFeedbackIfPossible();
                    Toast.makeText(PreviewActivity.this,
                            "Saved (" + status + ") to device database",
                            Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onError(Exception e) {
                    android.util.Log.e("PreviewActivity", "Room insert failed", e);
                    Toast.makeText(PreviewActivity.this,
                            "Could not save to database: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                }
            });
            return;
        }

        messageRepository.getMessageById(currentMessageId, new MessageRepository.Callback<MessageEntity>() {
            @Override
            public void onSuccess(MessageEntity existing) {
                if (existing == null) return;
                applyCurrentPreviewToMessage(existing, status);
                messageRepository.updateMessage(existing, new MessageRepository.SimpleCallback() {
                    @Override
                    public void onSuccess() {
                        persistPreviewFeedbackIfPossible();
                        Toast.makeText(PreviewActivity.this,
                                "Updated (" + status + ") in device database",
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(Exception e) {
                        android.util.Log.e("PreviewActivity", "Room update failed", e);
                        Toast.makeText(PreviewActivity.this,
                                "Could not update database: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                android.util.Log.e("PreviewActivity", "Room fetch failed", e);
            }
        });
    }

    /**
     * Update message in database when edited
     */
    private void updateMessageInDatabase(String updatedText) {
        if (currentMessageId == -1) {
            // Message not saved yet; keep edits in memory and persist once user sends/cancels.
            return;
        }

        // Get current message and update it
        messageRepository.getMessageById(currentMessageId, new MessageRepository.Callback<MessageEntity>() {
            @Override
            public void onSuccess(MessageEntity message) {
                if (message != null) {
                    // If this is the first edit, store the original text
                    if (!message.isUserEdited()) {
                        message.setOriginalText(message.getMessageText());
                    }
                    message.setMessageText(updatedText);
                    message.setUserEdited(true);
                    message.setUpdatedAt(System.currentTimeMillis());
                    applyCurrentPreviewToMessage(message, message.getStatus());

                    messageRepository.updateMessage(message, new MessageRepository.SimpleCallback() {
                        @Override
                        public void onSuccess() {
                            // Message updated successfully
                        }

                        @Override
                        public void onError(Exception e) {
                            e.printStackTrace();
                        }
                    });
                }
            }

            @Override
            public void onError(Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void applyCurrentPreviewToMessage(MessageEntity message, String status) {
        String body = tvMessageContent.getText().toString().trim();
        String activity = chipActivity.getVisibility() == View.VISIBLE
                ? chipActivity.getText().toString().replace("Activity: ", "")
                : "";
        String sender = chipSender.getVisibility() == View.VISIBLE
                ? chipSender.getText().toString().replace("Sender: ", "")
                : "";
        String urgency = chipUrgency.getVisibility() == View.VISIBLE
                ? chipUrgency.getText().toString().replace("Urgency: ", "")
                : "";

        message.setRequestId(requestId);
        message.setRecipientName(tvRecipientName.getText().toString().trim());
        message.setMessageText(body);
        message.setTimestamp(System.currentTimeMillis());
        message.setTone("Auto");
        message.setStatus(status);
        message.setContextActivity(activity);
        message.setContextSender(sender);
        message.setContextUrgency(urgency);
        message.setContextTags(buildAdditionalContextTagsJson());
        if (hasLocalEdits) {
            message.setUserEdited(true);
            String existingOriginal = message.getOriginalText();
            if (existingOriginal == null || existingOriginal.trim().isEmpty()) {
                message.setOriginalText(initialMessageText);
            }
        }
    }

    private String buildAdditionalContextTagsJson() {
        JSONArray tags = new JSONArray();
        if (!(layoutContextContent instanceof LinearLayout)) {
            return tags.toString();
        }

        LinearLayout container = (LinearLayout) layoutContextContent;
        for (int i = 0; i < container.getChildCount(); i++) {
            View child = container.getChildAt(i);
            if (!(child instanceof Chip)) {
                continue;
            }
            int id = child.getId();
            if (id == R.id.chip_activity || id == R.id.chip_sender || id == R.id.chip_urgency || id == R.id.chip_add) {
                continue;
            }
            if (child.getVisibility() != View.VISIBLE) {
                continue;
            }
            String tag = ((Chip) child).getText().toString().trim();
            if (!tag.isEmpty()) {
                tags.put(tag);
            }
        }
        return tags.toString();
    }

    private void persistMessageSnapshotIfAlreadySaved() {
        if (currentMessageId == -1) {
            return;
        }
        messageRepository.getMessageById(currentMessageId, new MessageRepository.Callback<MessageEntity>() {
            @Override
            public void onSuccess(MessageEntity message) {
                if (message == null) return;
                applyCurrentPreviewToMessage(message, message.getStatus());
                messageRepository.updateMessage(message, new MessageRepository.SimpleCallback() {
                    @Override
                    public void onSuccess() {
                        // no-op
                    }

                    @Override
                    public void onError(Exception e) {
                        android.util.Log.e("PreviewActivity", "Room snapshot update failed", e);
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                android.util.Log.e("PreviewActivity", "Room fetch failed", e);
            }
        });
    }

    private void persistPreviewFeedbackIfPossible() {
        if (pendingThumbFeedback == null || currentMessageId == -1) {
            return;
        }
        feedbackRepository.saveOrReplaceFeedback(currentMessageId, pendingThumbFeedback, null,
                new com.example.carma_android_app.database.FeedbackRepository.Callback<Long>() {
                    @Override
                    public void onSuccess(Long id) {
                        // no-op
                    }

                    @Override
                    public void onError(Exception e) {
                        android.util.Log.e("PreviewActivity", "Room feedback save failed", e);
                    }
                });
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
