package com.example.carma_android_app.models;

/**
 * Base model for items displayed in the review screen
 */
public abstract class ReviewItem {
    public enum ItemType {
        STATISTICS,
        MESSAGE
    }

    private ItemType type;
    private long timestamp;

    public ReviewItem(ItemType type) {
        this.type = type;
        this.timestamp = System.currentTimeMillis();
    }

    public ItemType getType() {
        return type;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Get the display text for this review item
     */
    public abstract String getDisplayText();
}