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
import com.google.android.material.chip.Chip;

import java.text.DateFormat;
import java.util.Locale;

/**
 * Shows message details and collects questionnaire feedback.
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
    private Chip chipActivity;
    private Chip chipSender;
    private Chip chipUrgency;
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
        if (existingFeedback == null) {
            return;
        }
        String type = existingFeedback.getFeedbackType();
        if (FeedbackEntity.THUMBS_UP.equals(type)) {
            setSelectionByText(rgQ1, "Somewhat useful");
        } else if (FeedbackEntity.THUMBS_DOWN.equals(type)) {
            setSelectionByText(rgQ1, "Not very useful");
        }
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
            boolean isPositive = "Very useful".equalsIgnoreCase(q1) || "Somewhat useful".equalsIgnoreCase(q1);
            listener.onFeedbackSubmitted(message.getId(), isPositive, null);
        }
        dismiss();
    }
}
