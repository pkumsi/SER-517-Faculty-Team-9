package com.example.carma_android_app.models;

import java.util.Map;

/**
 * Data model for message statistics response from the backend
 */
public class MessageStatistics {
    private Map<String, Long> statistics;
    private int days;

    public MessageStatistics(Map<String, Long> statistics, int days) {
        this.statistics = statistics;
        this.days = days;
    }

    public Map<String, Long> getStatistics() {
        return statistics;
    }

    public void setStatistics(Map<String, Long> statistics) {
        this.statistics = statistics;
    }

    public int getDays() {
        return days;
    }

    public void setDays(int days) {
        this.days = days;
    }

    /**
     * Get total messages across all days
     */
    public long getTotalMessages() {
        return statistics.values().stream().mapToLong(Long::longValue).sum();
    }
}