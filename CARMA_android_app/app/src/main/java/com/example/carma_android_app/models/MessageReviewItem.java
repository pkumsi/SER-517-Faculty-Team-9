package com.example.carma_android_app.models;

/**
 * Review item for displaying individual messages
 */
public class MessageReviewItem extends ReviewItem {
    private AutoResponseMessage message;
    private Boolean userFeedback; // true = liked, false = disliked, null = no feedback

    public MessageReviewItem(AutoResponseMessage message) {
        super(ItemType.MESSAGE);
        this.message = message;
        this.userFeedback = null;
    }

    public MessageReviewItem(AutoResponseMessage message, Boolean userFeedback) {
        super(ItemType.MESSAGE);
        this.message = message;
        this.userFeedback = userFeedback;
    }

    public AutoResponseMessage getMessage() {
        return message;
    }

    public void setMessage(AutoResponseMessage message) {
        this.message = message;
    }

    public Boolean getUserFeedback() {
        return userFeedback;
    }

    public void setUserFeedback(Boolean userFeedback) {
        this.userFeedback = userFeedback;
    }

    @Override
    public String getDisplayText() {
        String feedbackText = "";
        if (userFeedback != null) {
            feedbackText = userFeedback ? " 👍" : " 👎";
        }
        return message.getMessageText() + feedbackText;
    }

    /**
     * Get recipient name for display
     */
    public String getRecipientName() {
        // This could be enhanced to get actual recipient name
        return "Recipient";
    }

    /**
     * Get formatted timestamp for display
     */
    public String getFormattedTimestamp() {
        // This could be enhanced with proper date formatting
        return String.valueOf(message.getTimestamp());
    }
}