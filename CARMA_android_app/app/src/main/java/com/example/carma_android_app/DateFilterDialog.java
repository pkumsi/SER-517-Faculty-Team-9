package com.example.carma_android_app;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import com.example.carma_android_app.R;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateFilterDialog extends Dialog {
    private CalendarView calendarView;
    private TextView tvFromDate, tvToDate;
    private Button btnApply, btnCancel;
    private long fromDate = -1, toDate = -1;
    private boolean selectingStart = true;
    private OnDateFilterListener listener;

    public interface OnDateFilterListener {
        void onDateFilter(long from, long to);
    }

    public DateFilterDialog(@NonNull Context context, OnDateFilterListener listener) {
        super(context);
        this.listener = listener;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (getWindow() != null) {
            int width = (int) (getContext().getResources().getDisplayMetrics().widthPixels * 0.92f);
            getWindow().setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
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
        btnApply.setEnabled(false);

        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            long selectedDate = calendar.getTimeInMillis();
            if (selectingStart) {
                fromDate = selectedDate;
                toDate = -1;
                selectingStart = false;
                tvFromDate.setText(formatDate(fromDate));
                tvToDate.setText("Not selected");
                btnApply.setEnabled(false);
            } else {
                long start = Math.min(fromDate, selectedDate);
                long end = Math.max(fromDate, selectedDate);
                fromDate = start;
                toDate = end;
                selectingStart = true;
                tvFromDate.setText(formatDate(fromDate));
                tvToDate.setText(formatDate(toDate));
                btnApply.setEnabled(true);
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
