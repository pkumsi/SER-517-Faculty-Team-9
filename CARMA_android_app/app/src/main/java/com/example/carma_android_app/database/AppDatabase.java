package com.example.carma_android_app.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;


@Database(
    entities = {MessageEntity.class, FeedbackEntity.class},
    version = 1,
    exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {
    
    // Database name
    private static final String DATABASE_NAME = "carma_database";
    
    // Singleton instance
    private static volatile AppDatabase INSTANCE;
    
    // DAOs
    public abstract MessageDao messageDao();
    public abstract FeedbackDao feedbackDao();
    
    /**
     * Get database instance (Singleton pattern)
     */
    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            DATABASE_NAME
                        )
                        // For production, remove this and use migrations
                        .fallbackToDestructiveMigration()
                        .build();
                }
            }
        }
        return INSTANCE;
    }
    
    /**
     * Close database instance (for testing)
     */
    public static void destroyInstance() {
        if (INSTANCE != null) {
            INSTANCE.close();
            INSTANCE = null;
        }
    }
}