package com.example.carma_android_app.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;
import androidx.room.ForeignKey;
import androidx.room.Index;

/**
 * Entity class representing feedback for a message
 * Maps to the "feedback" table
 */
@Entity(
    tableName = "feedback",
    foreignKeys = @ForeignKey(
        entity = MessageEntity.class,
        parentColumns = "id",
        childColumns = "message_id",
        onDelete = ForeignKey.CASCADE
    ),
    indices = {@Index("message_id")}
)
public class FeedbackEntity {
    
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private long id;
    
    @ColumnInfo(name = "message_id")
    private long messageId;
    
    @ColumnInfo(name = "feedback_type")
    private String feedbackType; // "thumbs_up" or "thumbs_down"
    
    @ColumnInfo(name = "comment")
    private String comment; // Optional user comment
    
    @ColumnInfo(name = "timestamp")
    private long timestamp;
    
    @ColumnInfo(name = "created_at")
    private long createdAt;
    
    // Feedback type constants
    public static final String THUMBS_UP = "thumbs_up";
    public static final String THUMBS_DOWN = "thumbs_down";
    
    // Constructors
    public FeedbackEntity() {
        long currentTime = System.currentTimeMillis();
        this.timestamp = currentTime;
        this.createdAt = currentTime;
    }
    
    public FeedbackEntity(long messageId, String feedbackType) {
        this();
        this.messageId = messageId;
        this.feedbackType = feedbackType;
    }
    
    public FeedbackEntity(long messageId, String feedbackType, String comment) {
        this(messageId, feedbackType);
        this.comment = comment;
    }
    
    // Getters and Setters
    public long getId() {
        return id;
    }
    
    public void setId(long id) {
        this.id = id;
    }
    
    public long getMessageId() {
        return messageId;
    }
    
    public void setMessageId(long messageId) {
        this.messageId = messageId;
    }
    
    public String getFeedbackType() {
        return feedbackType;
    }
    
    public void setFeedbackType(String feedbackType) {
        this.feedbackType = feedbackType;
    }
    
    public String getComment() {
        return comment;
    }
    
    public void setComment(String comment) {
        this.comment = comment;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    
    public long getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
    
    // Helper methods
    public boolean isPositive() {
        return THUMBS_UP.equals(feedbackType);
    }
    
    public boolean isNegative() {
        return THUMBS_DOWN.equals(feedbackType);
    }
}