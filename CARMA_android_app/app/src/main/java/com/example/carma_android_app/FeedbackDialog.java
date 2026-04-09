package com.example.carma_android_app;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.carma_android_app.R;

public class FeedbackDialog extends Dialog {
    private TextView tvRecipientName, tvTimestamp, tvStatus, tvMessageContent;
    private ImageButton btnClose;
    private View cardThumbsUp, cardThumbsDown;
    private OnFeedbackSubmitListener listener;

    public interface OnFeedbackSubmitListener {
        void onFeedbackSubmit(boolean isPositive);
    }

    public FeedbackDialog(@NonNull Context context, @Nullable OnFeedbackSubmitListener listener) {
        super(context);
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_feedback);
        
        tvRecipientName = findViewById(R.id.tv_recipient_name);
        tvTimestamp = findViewById(R.id.tv_timestamp);
        tvStatus = findViewById(R.id.tv_status);
        tvMessageContent = findViewById(R.id.tv_message_content);
        btnClose = findViewById(R.id.btn_close);
        cardThumbsUp = findViewById(R.id.card_thumbs_up);
        cardThumbsDown = findViewById(R.id.card_thumbs_down);

        cardThumbsUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onFeedbackSubmit(true);
                }
                dismiss();
            }
        });

        cardThumbsDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onFeedbackSubmit(false);
                }
                dismiss();
            }
        });

        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

    public void setData(String recipient, String time, String status, String content) {
        if (tvRecipientName != null) tvRecipientName.setText(recipient);
        if (tvTimestamp != null) tvTimestamp.setText(time);
        if (tvStatus != null) tvStatus.setText(status);
        if (tvMessageContent != null) tvMessageContent.setText(content);
    }
}
