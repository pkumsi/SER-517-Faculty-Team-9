package com.example.carma_android_app.database;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FeedbackRepository {

    private final FeedbackDao feedbackDao;
    private final MessageDao messageDao;
    private final ExecutorService executor;
    private final Handler mainHandler;

    public FeedbackRepository(Context context) {
        AppDatabase database = AppDatabase.getInstance(context);
        this.feedbackDao = database.feedbackDao();
        this.messageDao = database.messageDao();
        this.executor = Executors.newSingleThreadExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    // ============ CALLBACK INTERFACES ============

    public interface Callback<T> {
        void onSuccess(T result);
        void onError(Exception e);
    }

    /**
     * Simple callback for operations without return value
     */
    public interface SimpleCallback {
        void onSuccess();
        void onError(Exception e);
    }

    // ============ INSERT OPERATIONS ============

    /**
     * Insert feedback and update message
     */
    public void insertFeedback(FeedbackEntity feedback, Callback<Long> callback) {
        executor.execute(() -> {
            try {
                // Insert feedback
                long feedbackId = feedbackDao.insert(feedback);

                // Mark message as having feedback
                messageDao.markAsFeedbackGiven(feedback.getMessageId(), System.currentTimeMillis());

                mainHandler.post(() -> callback.onSuccess(feedbackId));
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError(e));
            }
        });
    }

    /**
     * Insert feedback with thumbs up
     */
    public void insertThumbsUpFeedback(long messageId, String comment, Callback<Long> callback) {
        FeedbackEntity feedback = new FeedbackEntity(messageId, FeedbackEntity.THUMBS_UP, comment);
        insertFeedback(feedback, callback);
    }

    /**
     * Insert feedback with thumbs down
     */
    public void insertThumbsDownFeedback(long messageId, String comment, Callback<Long> callback) {
        FeedbackEntity feedback = new FeedbackEntity(messageId, FeedbackEntity.THUMBS_DOWN, comment);
        insertFeedback(feedback, callback);
    }

    /**
     * Replace any existing feedback for this message, then insert the new row.
     * Use when the user changes their mind or submits feedback again from Review.
     */
    public void saveOrReplaceFeedback(long messageId, boolean positive, String comment, Callback<Long> callback) {
        executor.execute(() -> {
            try {
                feedbackDao.deleteByMessageId(messageId);
                String type = positive ? FeedbackEntity.THUMBS_UP : FeedbackEntity.THUMBS_DOWN;
                String c = (comment != null && !comment.trim().isEmpty()) ? comment.trim() : null;
                FeedbackEntity feedback = new FeedbackEntity(messageId, type, c);
                feedback.setPreviewFeedbackType(type);
                long id = feedbackDao.insert(feedback);
                messageDao.markAsFeedbackGiven(messageId, System.currentTimeMillis());
                mainHandler.post(() -> callback.onSuccess(id));
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError(e));
            }
        });
    }

    /**
     * Save questionnaire responses for a message.
     */
    public void saveOrReplaceQuestionnaireFeedback(long messageId,
                                                   String q1Usefulness,
                                                   String q2Comfort,
                                                   String q3Appropriateness,
                                                   String q4ExplanationSense,
                                                   String q5Clarity,
                                                   Callback<Long> callback) {
        executor.execute(() -> {
            try {
                FeedbackEntity existing = feedbackDao.getFeedbackByMessageId(messageId);
                feedbackDao.deleteByMessageId(messageId);
                String type = deriveFeedbackTypeFromUsefulness(q1Usefulness);
                FeedbackEntity feedback = new FeedbackEntity(
                        messageId,
                        type,
                        q1Usefulness,
                        q2Comfort,
                        q3Appropriateness,
                        q4ExplanationSense,
                        q5Clarity
                );
                String preservedPreviewType = extractPreviewFeedbackType(existing);
                feedback.setPreviewFeedbackType(preservedPreviewType);
                long id = feedbackDao.insert(feedback);
                messageDao.markAsFeedbackGiven(messageId, System.currentTimeMillis());
                mainHandler.post(() -> callback.onSuccess(id));
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError(e));
            }
        });
    }

    private static String deriveFeedbackTypeFromUsefulness(String usefulness) {
        if (usefulness == null) {
            return "neutral";
        }
        String normalized = usefulness.trim().toLowerCase();
        if ("very useful".equals(normalized) || "somewhat useful".equals(normalized)) {
            return FeedbackEntity.THUMBS_UP;
        }
        if ("not very useful".equals(normalized) || "not useful at all".equals(normalized)) {
            return FeedbackEntity.THUMBS_DOWN;
        }
        return "neutral";
    }

    private static String extractPreviewFeedbackType(FeedbackEntity existing) {
        if (existing == null) {
            return null;
        }
        String explicit = existing.getPreviewFeedbackType();
        if (FeedbackEntity.THUMBS_UP.equals(explicit) || FeedbackEntity.THUMBS_DOWN.equals(explicit)) {
            return explicit;
        }
        String legacy = existing.getFeedbackType();
        if (FeedbackEntity.THUMBS_UP.equals(legacy) || FeedbackEntity.THUMBS_DOWN.equals(legacy)) {
            return legacy;
        }
        return null;
    }

    // ============ UPDATE OPERATIONS ============

    /**
     * Update existing feedback
     */
    public void updateFeedback(FeedbackEntity feedback, SimpleCallback callback) {
        executor.execute(() -> {
            try {
                feedbackDao.update(feedback);
                mainHandler.post(callback::onSuccess);
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError(e));
            }
        });
    }

    /**
     * Update feedback comment
     */
    public void updateFeedbackComment(long feedbackId, String comment, SimpleCallback callback) {
        executor.execute(() -> {
            try {
                feedbackDao.updateComment(feedbackId, comment);
                mainHandler.post(callback::onSuccess);
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError(e));
            }
        });
    }

    // ============ DELETE OPERATIONS ============

    /**
     * Delete feedback
     */
    public void deleteFeedback(FeedbackEntity feedback, SimpleCallback callback) {
        executor.execute(() -> {
            try {
                feedbackDao.delete(feedback);

                // Update message to mark as no feedback
                messageDao.updateStatus(feedback.getMessageId(), "sent", System.currentTimeMillis());

                mainHandler.post(callback::onSuccess);
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError(e));
            }
        });
    }

    /**
     * Delete all feedback
     */
    public void deleteAllFeedback(SimpleCallback callback) {
        executor.execute(() -> {
            try {
                feedbackDao.deleteAll();
                mainHandler.post(callback::onSuccess);
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError(e));
            }
        });
    }

    /**
     * Delete feedback by message ID
     */
    public void deleteFeedbackByMessageId(long messageId, SimpleCallback callback) {
        executor.execute(() -> {
            try {
                feedbackDao.deleteByMessageId(messageId);
                mainHandler.post(callback::onSuccess);
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError(e));
            }
        });
    }

    // ============ SELECT OPERATIONS ============

    /**
     * Get all feedback
     */
    public void getAllFeedback(Callback<List<FeedbackEntity>> callback) {
        executor.execute(() -> {
            try {
                List<FeedbackEntity> feedback = feedbackDao.getAllFeedback();
                mainHandler.post(() -> callback.onSuccess(feedback));
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError(e));
            }
        });
    }

    /**
     * Get feedback by ID
     */
    public void getFeedbackById(long feedbackId, Callback<FeedbackEntity> callback) {
        executor.execute(() -> {
            try {
                FeedbackEntity feedback = feedbackDao.getFeedbackById(feedbackId);
                mainHandler.post(() -> callback.onSuccess(feedback));
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError(e));
            }
        });
    }

    /**
     * Get feedback for a specific message
     */
    public void getFeedbackByMessageId(long messageId, Callback<FeedbackEntity> callback) {
        executor.execute(() -> {
            try {
                FeedbackEntity feedback = feedbackDao.getFeedbackByMessageId(messageId);
                mainHandler.post(() -> callback.onSuccess(feedback));
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError(e));
            }
        });
    }

    /**
     * Get all positive feedback
     */
    public void getPositiveFeedback(Callback<List<FeedbackEntity>> callback) {
        executor.execute(() -> {
            try {
                List<FeedbackEntity> feedback = feedbackDao.getPositiveFeedback();
                mainHandler.post(() -> callback.onSuccess(feedback));
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError(e));
            }
        });
    }

    /**
     * Get all negative feedback
     */
    public void getNegativeFeedback(Callback<List<FeedbackEntity>> callback) {
        executor.execute(() -> {
            try {
                List<FeedbackEntity> feedback = feedbackDao.getNegativeFeedback();
                mainHandler.post(() -> callback.onSuccess(feedback));
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError(e));
            }
        });
    }

    /**
     * Get feedback with comments
     */
    public void getFeedbackWithComments(Callback<List<FeedbackEntity>> callback) {
        executor.execute(() -> {
            try {
                List<FeedbackEntity> feedback = feedbackDao.getFeedbackWithComments();
                mainHandler.post(() -> callback.onSuccess(feedback));
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError(e));
            }
        });
    }

    /**
     * Get feedback in date range
     */
    public void getFeedbackByDateRange(long startTime, long endTime, Callback<List<FeedbackEntity>> callback) {
        executor.execute(() -> {
            try {
                List<FeedbackEntity> feedback = feedbackDao.getFeedbackByDateRange(startTime, endTime);
                mainHandler.post(() -> callback.onSuccess(feedback));
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError(e));
            }
        });
    }

    // ============ COUNT OPERATIONS ============

    /**
     * Get total feedback count
     */
    public void getTotalFeedbackCount(Callback<Integer> callback) {
        executor.execute(() -> {
            try {
                int count = feedbackDao.getTotalFeedbackCount();
                mainHandler.post(() -> callback.onSuccess(count));
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError(e));
            }
        });
    }

    /**
     * Get positive feedback count
     */
    public void getPositiveFeedbackCount(Callback<Integer> callback) {
        executor.execute(() -> {
            try {
                int count = feedbackDao.getPositiveFeedbackCount();
                mainHandler.post(() -> callback.onSuccess(count));
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError(e));
            }
        });
    }

    /**
     * Get negative feedback count
     */
    public void getNegativeFeedbackCount(Callback<Integer> callback) {
        executor.execute(() -> {
            try {
                int count = feedbackDao.getNegativeFeedbackCount();
                mainHandler.post(() -> callback.onSuccess(count));
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError(e));
            }
        });
    }

    /**
     * Get feedback count with comments
     */
    public void getFeedbackWithCommentsCount(Callback<Integer> callback) {
        executor.execute(() -> {
            try {
                int count = feedbackDao.getFeedbackWithCommentsCount();
                mainHandler.post(() -> callback.onSuccess(count));
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError(e));
            }
        });
    }

    // ============ CHECK OPERATIONS ============

    /**
     * Check if message has feedback
     */
    public void messageHasFeedback(long messageId, Callback<Boolean> callback) {
        executor.execute(() -> {
            try {
                boolean hasFeedback = feedbackDao.messageHasFeedback(messageId);
                mainHandler.post(() -> callback.onSuccess(hasFeedback));
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError(e));
            }
        });
    }

    /**
     * Get feedback type for a message
     */
    public void getFeedbackTypeByMessageId(long messageId, Callback<String> callback) {
        executor.execute(() -> {
            try {
                String feedbackType = feedbackDao.getFeedbackTypeByMessageId(messageId);
                mainHandler.post(() -> callback.onSuccess(feedbackType));
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError(e));
            }
        });
    }

    // ============ UTILITY METHODS ============

    /**
     * Shutdown executor (call in onDestroy)
     */
    public void shutdown() {
        executor.shutdown();
    }
}
