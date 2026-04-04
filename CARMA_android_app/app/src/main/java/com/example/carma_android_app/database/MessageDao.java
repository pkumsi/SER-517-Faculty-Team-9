package com.example.carma_android_app.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Update;
import androidx.room.Delete;
import androidx.room.Query;

import java.util.List;

/**
 * Data Access Object for Message operations
 * Defines all database queries for messages
 */
@Dao
public interface MessageDao {
    
    // ============ INSERT OPERATIONS ============
    
    /**
     * Insert a new message
     * @return The ID of the inserted message
     */
    @Insert
    long insert(MessageEntity message);
    
    /**
     * Insert multiple messages
     * @return Array of inserted message IDs
     */
    @Insert
    long[] insertAll(MessageEntity... messages);
    
    // ============ UPDATE OPERATIONS ============
    
    /**
     * Update an existing message
     */
    @Update
    void update(MessageEntity message);
    
    /**
     * Update message status
     */
    @Query("UPDATE messages SET status = :status, updated_at = :updatedAt WHERE id = :messageId")
    void updateStatus(long messageId, String status, long updatedAt);
    
    /**
     * Mark message as having feedback
     */
    @Query("UPDATE messages SET has_feedback = 1, updated_at = :updatedAt WHERE id = :messageId")
    void markAsFeedbackGiven(long messageId, long updatedAt);
    
    // ============ DELETE OPERATIONS ============
    
    /**
     * Delete a message
     */
    @Delete
    void delete(MessageEntity message);
    
    /**
     * Delete all messages
     */
    @Query("DELETE FROM messages")
    void deleteAll();
    
    /**
     * Delete messages older than a specific timestamp
     */
    @Query("DELETE FROM messages WHERE timestamp < :timestamp")
    void deleteMessagesBefore(long timestamp);
    
    /**
     * Delete message by ID
     */
    @Query("DELETE FROM messages WHERE id = :messageId")
    void deleteById(long messageId);
    
    // ============ SELECT OPERATIONS ============
    
    /**
     * Get all messages ordered by timestamp (newest first)
     */
    @Query("SELECT * FROM messages ORDER BY timestamp DESC")
    List<MessageEntity> getAllMessages();
    
    /**
     * Get a message by ID
     */
    @Query("SELECT * FROM messages WHERE id = :messageId")
    MessageEntity getMessageById(long messageId);
    
    /**
     * Get a message by request ID
     */
    @Query("SELECT * FROM messages WHERE request_id = :requestId LIMIT 1")
    MessageEntity getMessageByRequestId(String requestId);
    
    /**
     * Get messages by date range
     */
    @Query("SELECT * FROM messages WHERE timestamp >= :startTime AND timestamp <= :endTime ORDER BY timestamp DESC")
    List<MessageEntity> getMessagesByDateRange(long startTime, long endTime);
    
    /**
     * Get today's messages
     */
    @Query("SELECT * FROM messages WHERE timestamp >= :startOfDay AND timestamp < :endOfDay ORDER BY timestamp DESC")
    List<MessageEntity> getTodayMessages(long startOfDay, long endOfDay);
    
    /**
     * Get messages by status
     */
    @Query("SELECT * FROM messages WHERE status = :status ORDER BY timestamp DESC")
    List<MessageEntity> getMessagesByStatus(String status);
    
    /**
     * Get messages by recipient
     */
    @Query("SELECT * FROM messages WHERE recipient_name = :recipientName ORDER BY timestamp DESC")
    List<MessageEntity> getMessagesByRecipient(String recipientName);
    
    /**
     * Get messages with feedback
     */
    @Query("SELECT * FROM messages WHERE has_feedback = 1 ORDER BY timestamp DESC")
    List<MessageEntity> getMessagesWithFeedback();
    
    /**
     * Get messages without feedback
     */
    @Query("SELECT * FROM messages WHERE has_feedback = 0 ORDER BY timestamp DESC")
    List<MessageEntity> getMessagesWithoutFeedback();
    
    // ============ COUNT OPERATIONS ============
    
    /**
     * Get total message count
     */
    @Query("SELECT COUNT(*) FROM messages")
    int getTotalMessageCount();
    
    /**
     * Get count of messages by status
     */
    @Query("SELECT COUNT(*) FROM messages WHERE status = :status")
    int getMessageCountByStatus(String status);
    
    /**
     * Get count of messages with feedback
     */
    @Query("SELECT COUNT(*) FROM messages WHERE has_feedback = 1")
    int getMessagesWithFeedbackCount();
    
    /**
     * Get count of messages without feedback
     */
    @Query("SELECT COUNT(*) FROM messages WHERE has_feedback = 0")
    int getMessagesWithoutFeedbackCount();
    
    /**
     * Get count of messages in date range
     */
    @Query("SELECT COUNT(*) FROM messages WHERE timestamp >= :startTime AND timestamp <= :endTime")
    int getMessageCountInRange(long startTime, long endTime);
    
    // ============ SEARCH OPERATIONS ============
    
    /**
     * Search messages by text content
     */
    @Query("SELECT * FROM messages WHERE message_text LIKE '%' || :searchText || '%' ORDER BY timestamp DESC")
    List<MessageEntity> searchMessages(String searchText);
    
    /**
     * Get messages that were edited by users
     */
    @Query("SELECT * FROM messages WHERE user_edited = 1 ORDER BY timestamp DESC")
    List<MessageEntity> getUserEditedMessages();
    
    /**
     * Get count of user-edited messages
     */
    @Query("SELECT COUNT(*) FROM messages WHERE user_edited = 1")
    int getUserEditedMessagesCount();
}