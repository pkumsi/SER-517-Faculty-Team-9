package com.example.carma_android_app;

import android.app.Application;
import com.example.carma_android_app.utils.Logger;
import com.example.carma_android_app.utils.PreferencesManager;

/**
 * Application class for CARMA
 * Handles app-wide initialization and provides singleton instances
 */
public class CARMAApplication extends Application {

    private static CARMAApplication instance;
    private PreferencesManager preferencesManager;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        // Initialize managers
        preferencesManager = PreferencesManager.getInstance(this);

        Logger.i("Application", "CARMA Application started");
    }

    public static CARMAApplication getInstance() {
        return instance;
    }

    public PreferencesManager getPreferencesManager() {
        return preferencesManager;
    }
}