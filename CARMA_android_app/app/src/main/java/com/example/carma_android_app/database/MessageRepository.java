package com.example.carma_android_app.database;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class MessageRepository {

    private final MessageDao messageDao;
    private final ExecutorService executor;
    private final Handler mainHandler;

    public MessageRepository(Context context) {
        AppDatabase database = AppDatabase.getInstance(context);
        this.messageDao = database.messageDao();
        this.executor = Executors.newSingleThreadExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    // ============ CALLBACK INTERFACES ============

    public interface Callback<T> {
        void onSuccess(T result);
        void onError(Exception e);
    }


    public interface SimpleCallback {
        void onSuccess();
        void onError(Exception e);
    }

    // ============ INSERT OPERATIONS ============

    /**
     * Insert a new message
     */
    public void insertMessage(MessageEntity message, Callback<Long> callback) {
        executor.execute(() -> {
            try {
                long messageId = messageDao.insert(message);
                mainHandler.post(() -> callback.onSuccess(messageId));
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError(e));
            }
        });
    }

    /**
     * Insert multiple messages
     */
    public void insertMessages(List<MessageEntity> messages, Callback<long[]> callback) {
        executor.execute(() -> {
            try {
                MessageEntity[] messageArray = messages.toArray(new MessageEntity[0]);
                long[] ids = messageDao.insertAll(messageArray);
                mainHandler.post(() -> callback.onSuccess(ids));
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError(e));
            }
        });
    }

    // ============ UPDATE OPERATIONS ============

    /**
     * Update an existing message
     */
    public void updateMessage(MessageEntity message, SimpleCallback callback) {
        executor.execute(() -> {
            try {
                messageDao.update(message);
                mainHandler.post(callback::onSuccess);
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError(e));
            }
        });
    }

    /**
     * Update message status
     */
    public void updateMessageStatus(long messageId, String status, SimpleCallback callback) {
        executor.execute(() -> {
            try {
                messageDao.updateStatus(messageId, status, System.currentTimeMillis());
                mainHandler.post(callback::onSuccess);
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError(e));
            }
        });
    }

    /**
     * Get a message by ID
     */
    public void getMessageById(long messageId, Callback<MessageEntity> callback) {
        executor.execute(() -> {
            try {
                MessageEntity message = messageDao.getMessageById(messageId);
                mainHandler.post(() -> callback.onSuccess(message));
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError(e));
            }
        });
    }

    // ============ DELETE OPERATIONS ============

    /**
     * Delete a message
     */
    public void deleteMessage(MessageEntity message, SimpleCallback callback) {
        executor.execute(() -> {
            try {
                messageDao.delete(message);
                mainHandler.post(callback::onSuccess);
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError(e));
            }
        });
    }

    /**
     * Delete all messages
     */
    public void deleteAllMessages(SimpleCallback callback) {
        executor.execute(() -> {
            try {
                messageDao.deleteAll();
                mainHandler.post(callback::onSuccess);
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError(e));
            }
        });
    }

    /**
     * Delete messages older than specified timestamp
     */
    public void deleteMessagesOlderThan(long timestamp, SimpleCallback callback) {
        executor.execute(() -> {
            try {
                messageDao.deleteMessagesBefore(timestamp);
                mainHandler.post(callback::onSuccess);
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError(e));
            }
        });
    }

    /**
     * Delete message by ID
     */
    public void deleteMessageById(long messageId, SimpleCallback callback) {
        executor.execute(() -> {
            try {
                messageDao.deleteById(messageId);
                mainHandler.post(callback::onSuccess);
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError(e));
            }
        });
    }

    // ============ SELECT OPERATIONS ============

    /**
     * Get all messages
     */
    public void getAllMessages(Callback<List<MessageEntity>> callback) {
        executor.execute(() -> {
            try {
                List<MessageEntity> messages = messageDao.getAllMessages();
                mainHandler.post(() -> callback.onSuccess(messages));
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError(e));
            }
        });
    }

    /**
     * Get message by ID
     */
    public void getMessageById(long messageId, Callback<MessageEntity> callback) {
        executor.execute(() -> {
            try {
                MessageEntity message = messageDao.getMessageById(messageId);
                mainHandler.post(() -> callback.onSuccess(message));
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError(e));
            }
        });
    }

    /**
     * Get messages by date range
     */
    public void getMessagesByDateRange(long startTime, long endTime, Callback<List<MessageEntity>> callback) {
        executor.execute(() -> {
            try {
                List<MessageEntity> messages = messageDao.getMessagesByDateRange(startTime, endTime);
                mainHandler.post(() -> callback.onSuccess(messages));
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError(e));
            }
        });
    }

    /**
     * Get today's messages
     */
    public void getTodayMessages(long startOfDay, long endOfDay, Callback<List<MessageEntity>> callback) {
        executor.execute(() -> {
            try {
                List<MessageEntity> messages = messageDao.getTodayMessages(startOfDay, endOfDay);
                mainHandler.post(() -> callback.onSuccess(messages));
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError(e));
            }
        });
    }

    /**
     * Get messages by status
     */
    public void getMessagesByStatus(String status, Callback<List<MessageEntity>> callback) {
        executor.execute(() -> {
            try {
                List<MessageEntity> messages = messageDao.getMessagesByStatus(status);
                mainHandler.post(() -> callback.onSuccess(messages));
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError(e));
            }
        });
    }

    /**
     * Get messages by recipient
     */
    public void getMessagesByRecipient(String recipientName, Callback<List<MessageEntity>> callback) {
        executor.execute(() -> {
            try {
                List<MessageEntity> messages = messageDao.getMessagesByRecipient(recipientName);
                mainHandler.post(() -> callback.onSuccess(messages));
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError(e));
            }
        });
    }

    /**
     * Get messages with feedback
     */
    public void getMessagesWithFeedback(Callback<List<MessageEntity>> callback) {
        executor.execute(() -> {
            try {
                List<MessageEntity> messages = messageDao.getMessagesWithFeedback();
                mainHandler.post(() -> callback.onSuccess(messages));
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError(e));
            }
        });
    }

    /**
     * Get messages without feedback
     */
    public void getMessagesWithoutFeedback(Callback<List<MessageEntity>> callback) {
        executor.execute(() -> {
            try {
                List<MessageEntity> messages = messageDao.getMessagesWithoutFeedback();
                mainHandler.post(() -> callback.onSuccess(messages));
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError(e));
            }
        });
    }

    // ============ COUNT OPERATIONS ============

    /**
     * Get total message count
     */
    public void getTotalMessageCount(Callback<Integer> callback) {
        executor.execute(() -> {
            try {
                int count = messageDao.getTotalMessageCount();
                mainHandler.post(() -> callback.onSuccess(count));
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError(e));
            }
        });
    }

    /**
     * Get message count by status
     */
    public void getMessageCountByStatus(String status, Callback<Integer> callback) {
        executor.execute(() -> {
            try {
                int count = messageDao.getMessageCountByStatus(status);
                mainHandler.post(() -> callback.onSuccess(count));
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError(e));
            }
        });
    }

    /**
     * Get count of messages with feedback
     */
    public void getMessagesWithFeedbackCount(Callback<Integer> callback) {
        executor.execute(() -> {
            try {
                int count = messageDao.getMessagesWithFeedbackCount();
                mainHandler.post(() -> callback.onSuccess(count));
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError(e));
            }
        });
    }

    /**
     * Get count of messages without feedback
     */
    public void getMessagesWithoutFeedbackCount(Callback<Integer> callback) {
        executor.execute(() -> {
            try {
                int count = messageDao.getMessagesWithoutFeedbackCount();
                mainHandler.post(() -> callback.onSuccess(count));
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError(e));
            }
        });
    }

    // ============ SEARCH OPERATIONS ============

    /**
     * Search messages by text content
     */
    public void searchMessages(String searchText, Callback<List<MessageEntity>> callback) {
        executor.execute(() -> {
            try {
                List<MessageEntity> messages = messageDao.searchMessages(searchText);
                mainHandler.post(() -> callback.onSuccess(messages));
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError(e));
            }
        });
    }

    /**
     * Search messages by recipient name
     */
    public void searchByRecipient(String searchText, Callback<List<MessageEntity>> callback) {
        executor.execute(() -> {
            try {
                List<MessageEntity> messages = messageDao.searchByRecipient(searchText);
                mainHandler.post(() -> callback.onSuccess(messages));
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError(e));
            }
        });
    }

    /**
     * Get messages that were edited by users
     */
    public void getUserEditedMessages(Callback<List<MessageEntity>> callback) {
        executor.execute(() -> {
            try {
                List<MessageEntity> messages = messageDao.getUserEditedMessages();
                mainHandler.post(() -> callback.onSuccess(messages));
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError(e));
            }
        });
    }
    
    /**
     * Get count of user-edited messages
     */
    public void getUserEditedMessagesCount(Callback<Integer> callback) {
        executor.execute(() -> {
            try {
                int count = messageDao.getUserEditedMessagesCount();
                mainHandler.post(() -> callback.onSuccess(count));
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError(e));
            }
        });
    }

    // ============ UTILITY METHODS ============

    public void cleanupOldMessages(SimpleCallback callback) {
        long thirtyDaysAgo = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000);
        deleteMessagesOlderThan(thirtyDaysAgo, callback);
    }

    public void shutdown() {
        executor.shutdown();
    }
}