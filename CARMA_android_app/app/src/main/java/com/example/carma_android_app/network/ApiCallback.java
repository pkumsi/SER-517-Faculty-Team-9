package com.example.carma_android_app.network;

/**
 * Callback interface for async API calls
 */
public interface ApiCallback<T> {
    void onSuccess(T result);

    void onError(String error);
}