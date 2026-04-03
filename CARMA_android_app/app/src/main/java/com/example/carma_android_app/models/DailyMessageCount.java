package com.example.carma_android_app.models;

/**
 * Data model for daily message count statistics
 */
public class DailyMessageCount {
    private String date;
    private long count;

    public DailyMessageCount(String date, long count) {
        this.date = date;
        this.count = count;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    /**
     * Get formatted display text for the daily count
     */
    public String getDisplayText() {
        return date + ": " + count + " messages";
    }

    /**
     * Get formatted date for display (could be enhanced with proper date formatting)
     */
    public String getFormattedDate() {
        // For now, return the date as-is. Could be enhanced to format dates properly
        return date;
    }
}