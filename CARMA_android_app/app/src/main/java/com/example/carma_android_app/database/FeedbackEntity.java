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

    @ColumnInfo(name = "preview_feedback_type")
    private String previewFeedbackType; // preview-time thumbs value
    
    @ColumnInfo(name = "comment")
    private String comment; // Optional user comment

    @ColumnInfo(name = "q1_usefulness")
    private String q1Usefulness;

    @ColumnInfo(name = "q2_comfort")
    private String q2Comfort;

    @ColumnInfo(name = "q3_appropriateness")
    private String q3Appropriateness;

    @ColumnInfo(name = "q4_explanation_sense")
    private String q4ExplanationSense;

    @ColumnInfo(name = "q5_clarity")
    private String q5Clarity;
    
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

    public FeedbackEntity(long messageId,
                          String feedbackType,
                          String q1Usefulness,
                          String q2Comfort,
                          String q3Appropriateness,
                          String q4ExplanationSense,
                          String q5Clarity) {
        this(messageId, feedbackType);
        this.q1Usefulness = q1Usefulness;
        this.q2Comfort = q2Comfort;
        this.q3Appropriateness = q3Appropriateness;
        this.q4ExplanationSense = q4ExplanationSense;
        this.q5Clarity = q5Clarity;
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

    public String getPreviewFeedbackType() {
        return previewFeedbackType;
    }

    public void setPreviewFeedbackType(String previewFeedbackType) {
        this.previewFeedbackType = previewFeedbackType;
    }
    
    public String getComment() {
        return comment;
    }
    
    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getQ1Usefulness() {
        return q1Usefulness;
    }

    public void setQ1Usefulness(String q1Usefulness) {
        this.q1Usefulness = q1Usefulness;
    }

    public String getQ2Comfort() {
        return q2Comfort;
    }

    public void setQ2Comfort(String q2Comfort) {
        this.q2Comfort = q2Comfort;
    }

    public String getQ3Appropriateness() {
        return q3Appropriateness;
    }

    public void setQ3Appropriateness(String q3Appropriateness) {
        this.q3Appropriateness = q3Appropriateness;
    }

    public String getQ4ExplanationSense() {
        return q4ExplanationSense;
    }

    public void setQ4ExplanationSense(String q4ExplanationSense) {
        this.q4ExplanationSense = q4ExplanationSense;
    }

    public String getQ5Clarity() {
        return q5Clarity;
    }

    public void setQ5Clarity(String q5Clarity) {
        this.q5Clarity = q5Clarity;
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
