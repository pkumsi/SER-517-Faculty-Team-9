package com.example.carma_android_app;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.carma_android_app.database.FeedbackEntity;
import com.example.carma_android_app.database.MessageEntity;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.textfield.TextInputEditText;

import java.text.DateFormat;
import java.util.Locale;

/**
 * Shows message details and collects thumbs feedback + optional comment.
 * Used from Review when the user taps a saved message.
 */
public class FeedbackDialog extends Dialog {

    public interface OnFeedbackSubmitListener {
        void onFeedbackSubmitted(long messageId, boolean isPositive, @Nullable String comment);
    }

    private final MessageEntity message;
    private final FeedbackEntity existingFeedback;
    private final OnFeedbackSubmitListener listener;

    private TextView tvRecipientName;
    private TextView tvTimestamp;
    private TextView tvStatus;
    private TextView tvMessageContent;
    private ImageButton btnClose;
    private MaterialCardView cardThumbsUp;
    private MaterialCardView cardThumbsDown;
    private ImageView ivThumbsUp;
    private ImageView ivThumbsDown;
    private Chip chipActivity;
    private Chip chipSender;
    private Chip chipUrgency;
    private LinearLayout layoutComments;
    private LinearLayout layoutActionButtons;
    private TextInputEditText etComments;
    private MaterialButton btnCancel;
    private MaterialButton btnSubmit;

    private boolean pendingPositive = true;

    public FeedbackDialog(@NonNull Context context, @NonNull MessageEntity message,
                          @Nullable FeedbackEntity existingFeedback,
                          @Nullable OnFeedbackSubmitListener listener) {
        super(context);
        this.message = message;
        this.existingFeedback = existingFeedback;
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCancelable(true);
        setContentView(R.layout.dialog_feedback);

        android.view.Window w = getWindow();
        if (w != null) {
            w.setLayout(
                    (int) (getContext().getResources().getDisplayMetrics().widthPixels * 0.92),
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }

        tvRecipientName = findViewById(R.id.tv_recipient_name);
        tvTimestamp = findViewById(R.id.tv_timestamp);
        tvStatus = findViewById(R.id.tv_status);
        tvMessageContent = findViewById(R.id.tv_message_content);
        btnClose = findViewById(R.id.btn_close);
        cardThumbsUp = (MaterialCardView) findViewById(R.id.card_thumbs_up);
        cardThumbsDown = (MaterialCardView) findViewById(R.id.card_thumbs_down);
        ivThumbsUp = findViewById(R.id.iv_thumbs_up);
        ivThumbsDown = findViewById(R.id.iv_thumbs_down);
        chipActivity = findViewById(R.id.chip_activity);
        chipSender = findViewById(R.id.chip_sender);
        chipUrgency = findViewById(R.id.chip_urgency);
        layoutComments = findViewById(R.id.layout_comments);
        layoutActionButtons = findViewById(R.id.layout_action_buttons);
        etComments = findViewById(R.id.et_comments);
        btnCancel = findViewById(R.id.btn_cancel);
        btnSubmit = findViewById(R.id.btn_submit);

        bindMessage(message);

        layoutComments.setVisibility(View.GONE);
        layoutActionButtons.setVisibility(View.GONE);

        btnClose.setOnClickListener(v -> dismiss());

        cardThumbsUp.setOnClickListener(v -> setFeedbackSelection(true, true));
        cardThumbsDown.setOnClickListener(v -> setFeedbackSelection(false, true));

        btnCancel.setOnClickListener(v -> {
            layoutComments.setVisibility(View.GONE);
            layoutActionButtons.setVisibility(View.GONE);
            if (etComments != null) {
                etComments.setText("");
            }
        });

        btnSubmit.setOnClickListener(v -> submit());

        prefillExistingFeedbackIfAny();
    }

    private void bindMessage(MessageEntity m) {
        String recipient = m.getRecipientName();
        tvRecipientName.setText(recipient != null && !recipient.isEmpty() ? recipient : "Unknown");

        DateFormat fmt = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, Locale.getDefault());
        tvTimestamp.setText(fmt.format(m.getTimestamp()));

        String st = m.getStatus() != null ? m.getStatus() : "pending";
        if ("sent".equalsIgnoreCase(st)) {
            tvStatus.setText("Sent");
            tvStatus.setTextColor(0xFF4CAF50);
        } else if ("cancelled".equalsIgnoreCase(st)) {
            tvStatus.setText("Cancelled");
            tvStatus.setTextColor(0xFFF44336);
        } else {
            tvStatus.setText("Pending");
            tvStatus.setTextColor(0xFF757575);
        }

        String text = m.getMessageText();
        tvMessageContent.setText(text != null ? text : "");

        String act = safe(m.getContextActivity());
        String sender = safe(m.getContextSender());
        String urg = safe(m.getContextUrgency());

        if (!act.isEmpty()) {
            chipActivity.setVisibility(View.VISIBLE);
            chipActivity.setText("Activity: " + act);
        } else {
            chipActivity.setVisibility(View.GONE);
        }
        if (!sender.isEmpty()) {
            chipSender.setVisibility(View.VISIBLE);
            chipSender.setText("Sender: " + sender);
        } else {
            chipSender.setVisibility(View.GONE);
        }
        if (!urg.isEmpty()) {
            chipUrgency.setVisibility(View.VISIBLE);
            chipUrgency.setText("Urgency: " + urg);
        } else {
            chipUrgency.setVisibility(View.GONE);
        }
    }

    private static String safe(String s) {
        return s != null ? s.trim() : "";
    }

    private void prefillExistingFeedbackIfAny() {
        if (existingFeedback == null || existingFeedback.getFeedbackType() == null) {
            setFeedbackSelection(true, false);
            return;
        }
        boolean isPositive = FeedbackEntity.THUMBS_UP.equals(existingFeedback.getFeedbackType());
        setFeedbackSelection(isPositive, true);
        String existingComment = existingFeedback.getComment();
        if (etComments != null && existingComment != null) {
            etComments.setText(existingComment);
        }
    }

    private void setFeedbackSelection(boolean isPositive, boolean showEditor) {
        pendingPositive = isPositive;

        int selectedStroke = 0xFF2196F3;
        int defaultStroke = 0xFFE0E0E0;
        if (cardThumbsUp != null && cardThumbsDown != null) {
            cardThumbsUp.setStrokeColor(isPositive ? selectedStroke : defaultStroke);
            cardThumbsDown.setStrokeColor(isPositive ? defaultStroke : selectedStroke);
        }
        if (ivThumbsUp != null && ivThumbsDown != null) {
            ivThumbsUp.setColorFilter(isPositive ? 0xFF4CAF50 : 0xFF757575);
            ivThumbsDown.setColorFilter(isPositive ? 0xFF757575 : 0xFFF44336);
        }

        if (showEditor) {
            layoutComments.setVisibility(View.VISIBLE);
            layoutActionButtons.setVisibility(View.VISIBLE);
        }
    }

    private void submit() {
        String comment = etComments != null ? etComments.getText().toString().trim() : "";
        if (listener != null) {
            listener.onFeedbackSubmitted(message.getId(), pendingPositive,
                    comment.isEmpty() ? null : comment);
        }
        dismiss();
    }
}
