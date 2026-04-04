package com.example.carma_android_app;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import com.example.carma_android_app.R;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateFilterDialog extends Dialog {
    private CalendarView calendarView;
    private TextView tvFromDate, tvToDate;
    private Button btnApply, btnCancel;
    private long fromDate = -1, toDate = -1;
    private OnDateFilterListener listener;

    public interface OnDateFilterListener {
        void onDateFilter(long from, long to);
    }

    public DateFilterDialog(@NonNull Context context, OnDateFilterListener listener) {
        super(context);
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_date_filter);
        calendarView = findViewById(R.id.calendar_view);
        tvFromDate = findViewById(R.id.tv_from_date);
        tvToDate = findViewById(R.id.tv_to_date);
        btnApply = findViewById(R.id.btn_apply);
        btnCancel = findViewById(R.id.btn_cancel);

        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            long selectedDate = new Date(year - 1900, month, dayOfMonth).getTime();
            if (fromDate == -1 || (fromDate != -1 && toDate != -1)) {
                fromDate = selectedDate;
                toDate = -1;
                tvFromDate.setText(formatDate(selectedDate));
                tvToDate.setText("MM/DD/YYYY");
            } else {
                toDate = selectedDate;
                tvToDate.setText(formatDate(selectedDate));
            }
        });

        btnApply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null && fromDate != -1 && toDate != -1) {
                    listener.onDateFilter(fromDate, toDate);
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

    private String formatDate(long millis) {
        return new SimpleDateFormat("MM/dd/yyyy", Locale.US).format(new Date(millis));
    }
}
