package com.example.carma_android_app.models;

/**
 * Model class representing an auto-response message
 */
public class AutoResponseMessage {

    private String requestId;
    private String messageText;
    private String tone;
    private String context;
    private long timestamp;
    private boolean isSent;

    public AutoResponseMessage(String requestId, String messageText, String tone) {
        this.requestId = requestId;
        this.messageText = messageText;
        this.tone = tone;
        this.timestamp = System.currentTimeMillis();
        this.isSent = false;
    }

    // Getters and setters
    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public String getTone() {
        return tone;
    }

    public void setTone(String tone) {
        this.tone = tone;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isSent() {
        return isSent;
    }

    public void setSent(boolean sent) {
        isSent = sent;
    }
}
