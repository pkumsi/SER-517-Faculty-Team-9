package com.example.carma_android_app.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;

/**
 * Entity class representing a message in the database
 * Maps to the "messages" table
 */
@Entity(tableName = "messages")
public class MessageEntity {
    
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private long id;
    
    @ColumnInfo(name = "request_id")
    private String requestId;
    
    @ColumnInfo(name = "recipient_name")
    private String recipientName;
    
    @ColumnInfo(name = "recipient_number")
    private String recipientNumber;
    
    @ColumnInfo(name = "message_text")
    private String messageText;
    
    @ColumnInfo(name = "timestamp")
    private long timestamp;
    
    @ColumnInfo(name = "tone")
    private String tone; // "Formal", "Casual", "Brief"
    
    @ColumnInfo(name = "status")
    private String status; // "sent", "cancelled", "pending"
    
    @ColumnInfo(name = "context_activity")
    private String contextActivity; // e.g., "In a meeting"
    
    @ColumnInfo(name = "context_sender")
    private String contextSender; // e.g., "Manager"
    
    @ColumnInfo(name = "context_urgency")
    private String contextUrgency; // e.g., "Normal", "High"
    
    @ColumnInfo(name = "context_tags")
    private String contextTags; // JSON string of additional tags
    
    @ColumnInfo(name = "has_feedback")
    private boolean hasFeedback;
    
    @ColumnInfo(name = "created_at")
    private long createdAt;
    
    @ColumnInfo(name = "updated_at")
    private long updatedAt;
    
    // Constructors
    public MessageEntity() {
        long currentTime = System.currentTimeMillis();
        this.timestamp = currentTime;
        this.createdAt = currentTime;
        this.updatedAt = currentTime;
        this.status = "pending";
        this.hasFeedback = false;
    }
    
    public MessageEntity(String requestId, String recipientName, String messageText) {
        this();
        this.requestId = requestId;
        this.recipientName = recipientName;
        this.messageText = messageText;
    }
    
    // Getters and Setters
    public long getId() {
        return id;
    }
    
    public void setId(long id) {
        this.id = id;
    }
    
    public String getRequestId() {
        return requestId;
    }
    
    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }
    
    public String getRecipientName() {
        return recipientName;
    }
    
    public void setRecipientName(String recipientName) {
        this.recipientName = recipientName;
    }
    
    public String getRecipientNumber() {
        return recipientNumber;
    }
    
    public void setRecipientNumber(String recipientNumber) {
        this.recipientNumber = recipientNumber;
    }
    
    public String getMessageText() {
        return messageText;
    }
    
    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    
    public String getTone() {
        return tone;
    }
    
    public void setTone(String tone) {
        this.tone = tone;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
        this.updatedAt = System.currentTimeMillis();
    }
    
    public String getContextActivity() {
        return contextActivity;
    }
    
    public void setContextActivity(String contextActivity) {
        this.contextActivity = contextActivity;
    }
    
    public String getContextSender() {
        return contextSender;
    }
    
    public void setContextSender(String contextSender) {
        this.contextSender = contextSender;
    }
    
    public String getContextUrgency() {
        return contextUrgency;
    }
    
    public void setContextUrgency(String contextUrgency) {
        this.contextUrgency = contextUrgency;
    }
    
    public String getContextTags() {
        return contextTags;
    }
    
    public void setContextTags(String contextTags) {
        this.contextTags = contextTags;
    }
    
    public boolean isHasFeedback() {
        return hasFeedback;
    }
    
    public void setHasFeedback(boolean hasFeedback) {
        this.hasFeedback = hasFeedback;
        this.updatedAt = System.currentTimeMillis();
    }
    
    public long getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
    
    public long getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }
}