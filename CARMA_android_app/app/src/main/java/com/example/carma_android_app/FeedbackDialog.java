package com.example.carma_android_app;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.carma_android_app.database.FeedbackEntity;
import com.example.carma_android_app.database.MessageEntity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;

import java.text.DateFormat;
import java.util.Locale;

/**
 * Shows message details and collects questionnaire feedback.
 * Used from Review when the user taps a saved message.
 */
public class FeedbackDialog extends Dialog {

    public interface OnFeedbackSubmitListener {
        void onFeedbackSubmitted(long messageId,
                                 String q1Usefulness,
                                 String q2Comfort,
                                 String q3Appropriateness,
                                 String q4ExplanationSense,
                                 String q5Clarity);
    }

    private final MessageEntity message;
    private final FeedbackEntity existingFeedback;
    private final OnFeedbackSubmitListener listener;

    private TextView tvRecipientName;
    private TextView tvTimestamp;
    private TextView tvStatus;
    private TextView tvMessageContent;
    private ImageButton btnClose;
    private Chip chipActivity;
    private Chip chipSender;
    private Chip chipUrgency;
    private MaterialCardView cardPreviewFeedback;
    private TextView tvPreviewFeedback;
    private LinearLayout layoutActionButtons;
    private MaterialButton btnCancel;
    private MaterialButton btnSubmit;

    private RadioGroup rgQ1;
    private RadioGroup rgQ2;
    private RadioGroup rgQ3;
    private RadioGroup rgQ4;
    private RadioGroup rgQ5;

    public FeedbackDialog(@NonNull Context context,
                          @NonNull MessageEntity message,
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
            int screenHeight = getContext().getResources().getDisplayMetrics().heightPixels;
            w.setLayout(
                    (int) (getContext().getResources().getDisplayMetrics().widthPixels * 0.92),
                    (int) (screenHeight * 0.88)
            );
            w.setGravity(Gravity.CENTER);
            w.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }

        tvRecipientName = findViewById(R.id.tv_recipient_name);
        tvTimestamp = findViewById(R.id.tv_timestamp);
        tvStatus = findViewById(R.id.tv_status);
        tvMessageContent = findViewById(R.id.tv_message_content);
        btnClose = findViewById(R.id.btn_close);
        chipActivity = findViewById(R.id.chip_activity);
        chipSender = findViewById(R.id.chip_sender);
        chipUrgency = findViewById(R.id.chip_urgency);
        cardPreviewFeedback = findViewById(R.id.card_preview_feedback);
        tvPreviewFeedback = findViewById(R.id.tv_preview_feedback);
        layoutActionButtons = findViewById(R.id.layout_action_buttons);
        btnCancel = findViewById(R.id.btn_cancel);
        btnSubmit = findViewById(R.id.btn_submit);

        rgQ1 = findViewById(R.id.rg_q1);
        rgQ2 = findViewById(R.id.rg_q2);
        rgQ3 = findViewById(R.id.rg_q3);
        rgQ4 = findViewById(R.id.rg_q4);
        rgQ5 = findViewById(R.id.rg_q5);

        bindMessage(message);
        prefillExistingFeedbackIfAny();
        applyReadOnlyStateIfReviewed();

        btnClose.setOnClickListener(v -> dismiss());
        btnCancel.setOnClickListener(v -> dismiss());
        btnSubmit.setOnClickListener(v -> submit());
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
        bindPreviewFeedbackSection();
        if (existingFeedback == null) {
            return;
        }
        // Prefer exact questionnaire values when available.
        setSelectionByText(rgQ1, existingFeedback.getQ1Usefulness());
        setSelectionByText(rgQ2, existingFeedback.getQ2Comfort());
        setSelectionByText(rgQ3, existingFeedback.getQ3Appropriateness());
        setSelectionByText(rgQ4, existingFeedback.getQ4ExplanationSense());
        setSelectionByText(rgQ5, existingFeedback.getQ5Clarity());
    }

    private void applyReadOnlyStateIfReviewed() {
        if (!hasQuestionnaireAnswers(existingFeedback)) {
            return;
        }
        if (btnSubmit != null) {
            btnSubmit.setVisibility(View.GONE);
        }
        if (btnCancel != null) {
            btnCancel.setText("Close");
        }
        setRadioGroupEnabled(rgQ1, false);
        setRadioGroupEnabled(rgQ2, false);
        setRadioGroupEnabled(rgQ3, false);
        setRadioGroupEnabled(rgQ4, false);
        setRadioGroupEnabled(rgQ5, false);
    }

    private static void setRadioGroupEnabled(RadioGroup group, boolean enabled) {
        if (group == null) {
            return;
        }
        group.setEnabled(enabled);
        for (int i = 0; i < group.getChildCount(); i++) {
            View child = group.getChildAt(i);
            child.setEnabled(enabled);
        }
    }

    private void bindPreviewFeedbackSection() {
        if (cardPreviewFeedback == null || tvPreviewFeedback == null || existingFeedback == null) {
            return;
        }
        String type = existingFeedback.getPreviewFeedbackType();
        if (type == null || type.trim().isEmpty()) {
            // Backward compatibility for rows saved before preview_feedback_type existed.
            type = existingFeedback.getFeedbackType();
        }
        if (FeedbackEntity.THUMBS_UP.equals(type)) {
            cardPreviewFeedback.setVisibility(View.VISIBLE);
            tvPreviewFeedback.setText("Message liked when sent");
        } else if (FeedbackEntity.THUMBS_DOWN.equals(type)) {
            cardPreviewFeedback.setVisibility(View.VISIBLE);
            tvPreviewFeedback.setText("Message disliked when sent");
        } else {
            cardPreviewFeedback.setVisibility(View.GONE);
        }
    }

    private static boolean hasQuestionnaireAnswers(FeedbackEntity feedback) {
        if (feedback == null) {
            return false;
        }
        return !safe(feedback.getQ1Usefulness()).isEmpty()
                || !safe(feedback.getQ2Comfort()).isEmpty()
                || !safe(feedback.getQ3Appropriateness()).isEmpty()
                || !safe(feedback.getQ4ExplanationSense()).isEmpty()
                || !safe(feedback.getQ5Clarity()).isEmpty();
    }

    private static void setSelectionByText(RadioGroup group, String text) {
        if (group == null || text == null) {
            return;
        }
        int childCount = group.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = group.getChildAt(i);
            if (!(child instanceof RadioButton)) {
                continue;
            }
            RadioButton rb = (RadioButton) child;
            if (text.equalsIgnoreCase(String.valueOf(rb.getText()))) {
                rb.setChecked(true);
                return;
            }
        }
    }

    private static String getSelectedText(RadioGroup group) {
        if (group == null) {
            return null;
        }
        int selectedId = group.getCheckedRadioButtonId();
        if (selectedId == -1) {
            return null;
        }
        RadioButton rb = group.findViewById(selectedId);
        return rb != null ? String.valueOf(rb.getText()) : null;
    }

    private void submit() {
        String q1 = getSelectedText(rgQ1);
        String q2 = getSelectedText(rgQ2);
        String q3 = getSelectedText(rgQ3);
        String q4 = getSelectedText(rgQ4);
        String q5 = getSelectedText(rgQ5);

        if (q1 == null || q2 == null || q3 == null || q4 == null || q5 == null) {
            Toast.makeText(getContext(), "Please answer all questions", Toast.LENGTH_SHORT).show();
            return;
        }

        if (listener != null) {
            listener.onFeedbackSubmitted(message.getId(), q1, q2, q3, q4, q5);
        }
        dismiss();
    }
}
