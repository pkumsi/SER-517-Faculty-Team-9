package com.example.carma_android_app.models;

import java.util.List;

/**
 * Data model for the Preview Screen in CARMA app.
 */
public class PreviewScreenData {
    private String recipientName;
    private String messageContent;
    private String timerText;
    private List<String> contextTags; // e.g. Activity, Sender, Urgency
    private boolean arEnabled;
    private boolean sentAR;
    private boolean liked; // true if user liked, false if disliked, null if not set
    private String requestId;
    private String uuid;

    public PreviewScreenData(String recipientName, String messageContent, String timerText, List<String> contextTags,
                             boolean arEnabled, boolean sentAR, Boolean liked, String requestId, String uuid) {
        this.recipientName = recipientName;
        this.messageContent = messageContent;
        this.timerText = timerText;
        this.contextTags = contextTags;
        this.arEnabled = arEnabled;
        this.sentAR = sentAR;
        this.liked = liked != null && liked;
        this.requestId = requestId;
        this.uuid = uuid;
    }

    // Getters and setters
    public String getRecipientName() { return recipientName; }
    public void setRecipientName(String recipientName) { this.recipientName = recipientName; }

    public String getMessageContent() { return messageContent; }
    public void setMessageContent(String messageContent) { this.messageContent = messageContent; }

    public String getTimerText() { return timerText; }
    public void setTimerText(String timerText) { this.timerText = timerText; }

    public List<String> getContextTags() { return contextTags; }
    public void setContextTags(List<String> contextTags) { this.contextTags = contextTags; }

    public boolean isArEnabled() { return arEnabled; }
    public void setArEnabled(boolean arEnabled) { this.arEnabled = arEnabled; }

    public boolean isSentAR() { return sentAR; }
    public void setSentAR(boolean sentAR) { this.sentAR = sentAR; }

    public boolean isLiked() { return liked; }
    public void setLiked(boolean liked) { this.liked = liked; }

    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }

    public String getUuid() { return uuid; }
    public void setUuid(String uuid) { this.uuid = uuid; }
}
