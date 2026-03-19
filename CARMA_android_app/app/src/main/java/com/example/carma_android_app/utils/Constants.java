package com.example.carma_android_app.utils;

/**
 * Application-wide constants
 */
public class Constants {

    // API Configuration
    public static final String API_BASE_URL = "http://localhost:8080";
    public static final String API_VERSION = "v1";
    public static final String API_ENDPOINT_RESPONSE = "/api/v1/response";

    // Response Tones
    public static final String TONE_FORMAL = "Formal";
    public static final String TONE_CASUAL = "Casual";
    public static final String TONE_BRIEF = "Brief";

    // Shared Preferences Keys
    public static final String PREF_NAME = "CARMAPrefs";
    public static final String KEY_AUTO_RESPOND_ENABLED = "auto_respond_enabled";
    public static final String KEY_WHITELISTED_CONTACTS = "whitelisted_contacts";
    public static final String KEY_BLACKLISTED_CONTACTS = "blacklisted_contacts";

    // Request/Response Keys
    public static final String EXTRA_REQUEST_ID = "request_id";
    public static final String EXTRA_MESSAGE_TEXT = "message_text";
    public static final String EXTRA_TONE = "tone";

    private Constants() {
        // Private constructor to prevent instantiation
    }
}
