package com.example.carma_android_app;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.carma_android_app.R;

public class FeedbackDialog extends Dialog {
    private EditText etFeedbackTag;
    private Button btnSubmit, btnCancel;
    private OnFeedbackSubmitListener listener;

    public interface OnFeedbackSubmitListener {
        void onFeedbackSubmit(String tag);
    }

    public FeedbackDialog(@NonNull Context context, @Nullable OnFeedbackSubmitListener listener) {
        super(context);
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_feedback);
        etFeedbackTag = findViewById(R.id.et_feedback_tag);
        btnSubmit = findViewById(R.id.btn_feedback_submit);
        btnCancel = findViewById(R.id.btn_feedback_cancel);

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tag = etFeedbackTag.getText().toString().trim();
                if (listener != null) {
                    listener.onFeedbackSubmit(tag);
                }
                dismiss();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }
}
