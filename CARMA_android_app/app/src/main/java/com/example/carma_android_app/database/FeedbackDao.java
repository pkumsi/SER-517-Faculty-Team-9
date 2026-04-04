package com.example.carma_android_app.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Update;
import androidx.room.Delete;
import androidx.room.Query;

import java.util.List;

/**
 * Data Access Object for Feedback operations
 * Defines all database queries for feedback
 */
@Dao
public interface FeedbackDao {
    
    // ============ INSERT OPERATIONS ============
    
    /**
     * Insert new feedback
     * @return The ID of the inserted feedback
     */
    @Insert
    long insert(FeedbackEntity feedback);
    
    /**
     * Insert multiple feedback entries
     */
    @Insert
    long[] insertAll(FeedbackEntity... feedbacks);
    
    // ============ UPDATE OPERATIONS ============
    
    /**
     * Update existing feedback
     */
    @Update
    void update(FeedbackEntity feedback);
    
    /**
     * Update feedback comment
     */
    @Query("UPDATE feedback SET comment = :comment WHERE id = :feedbackId")
    void updateComment(long feedbackId, String comment);
    
    // ============ DELETE OPERATIONS ============
    
    /**
     * Delete feedback
     */
    @Delete
    void delete(FeedbackEntity feedback);
    
    /**
     * Delete all feedback
     */
    @Query("DELETE FROM feedback")
    void deleteAll();
    
    /**
     * Delete feedback by message ID
     */
    @Query("DELETE FROM feedback WHERE message_id = :messageId")
    void deleteByMessageId(long messageId);
    
    /**
     * Delete feedback by ID
     */
    @Query("DELETE FROM feedback WHERE id = :feedbackId")
    void deleteById(long feedbackId);
    
    // ============ SELECT OPERATIONS ============
    
    /**
     * Get all feedback ordered by timestamp (newest first)
     */
    @Query("SELECT * FROM feedback ORDER BY timestamp DESC")
    List<FeedbackEntity> getAllFeedback();
    
    /**
     * Get feedback by ID
     */
    @Query("SELECT * FROM feedback WHERE id = :feedbackId")
    FeedbackEntity getFeedbackById(long feedbackId);
    
    /**
     * Get feedback for a specific message
     */
    @Query("SELECT * FROM feedback WHERE message_id = :messageId LIMIT 1")
    FeedbackEntity getFeedbackByMessageId(long messageId);
    
    /**
     * Get all positive feedback
     */
    @Query("SELECT * FROM feedback WHERE feedback_type = 'thumbs_up' ORDER BY timestamp DESC")
    List<FeedbackEntity> getPositiveFeedback();
    
    /**
     * Get all negative feedback
     */
    @Query("SELECT * FROM feedback WHERE feedback_type = 'thumbs_down' ORDER BY timestamp DESC")
    List<FeedbackEntity> getNegativeFeedback();
    
    /**
     * Get feedback by type
     */
    @Query("SELECT * FROM feedback WHERE feedback_type = :feedbackType ORDER BY timestamp DESC")
    List<FeedbackEntity> getFeedbackByType(String feedbackType);
    
    /**
     * Get feedback with comments
     */
    @Query("SELECT * FROM feedback WHERE comment IS NOT NULL AND comment != '' ORDER BY timestamp DESC")
    List<FeedbackEntity> getFeedbackWithComments();
    
    /**
     * Get feedback in date range
     */
    @Query("SELECT * FROM feedback WHERE timestamp >= :startTime AND timestamp <= :endTime ORDER BY timestamp DESC")
    List<FeedbackEntity> getFeedbackByDateRange(long startTime, long endTime);
    
    // ============ COUNT OPERATIONS ============
    
    /**
     * Get total feedback count
     */
    @Query("SELECT COUNT(*) FROM feedback")
    int getTotalFeedbackCount();
    
    /**
     * Get positive feedback count
     */
    @Query("SELECT COUNT(*) FROM feedback WHERE feedback_type = 'thumbs_up'")
    int getPositiveFeedbackCount();
    
    /**
     * Get negative feedback count
     */
    @Query("SELECT COUNT(*) FROM feedback WHERE feedback_type = 'thumbs_down'")
    int getNegativeFeedbackCount();
    
    /**
     * Get feedback count for a specific message
     */
    @Query("SELECT COUNT(*) FROM feedback WHERE message_id = :messageId")
    int getFeedbackCountByMessage(long messageId);
    
    /**
     * Get count of feedback with comments
     */
    @Query("SELECT COUNT(*) FROM feedback WHERE comment IS NOT NULL AND comment != ''")
    int getFeedbackWithCommentsCount();
    
    // ============ JOINED QUERIES ============
    
    /**
     * Check if message has feedback
     */
    @Query("SELECT EXISTS(SELECT 1 FROM feedback WHERE message_id = :messageId)")
    boolean messageHasFeedback(long messageId);
    
    /**
     * Get feedback type for a message
     */
    @Query("SELECT feedback_type FROM feedback WHERE message_id = :messageId LIMIT 1")
    String getFeedbackTypeByMessageId(long messageId);
}