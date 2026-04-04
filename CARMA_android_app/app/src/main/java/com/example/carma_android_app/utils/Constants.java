package com.example.carma_android_app.utils;


public class Constants {
    // API Configuration
    // Use 10.0.2.2 when running on Android emulator to reach host machine localhost.
    // Change to the host IP when testing on a physical device.
    public static final String API_BASE_URL = "http://10.0.2.2:8080";
    public static final String API_VERSION = "v1";
    public static final String API_ENDPOINT_RESPONSE = "/api/v1/response";
    public static final String API_ENDPOINT_FEEDBACK = "/api/v1/feedback";
    public static final String API_ENDPOINT_STATISTICS = "/api/v1/statistics/messages";
    public static final String API_ENDPOINT_HEALTH = "/health";
    
    // Response Tones
    public static final String TONE_FORMAL = "Formal";
    public static final String TONE_CASUAL = "Casual";
    public static final String TONE_BRIEF = "Brief";
    
    // Shared Preferences Keys
    public static final String PREF_NAME = "CARMAPrefs";
    public static final String KEY_AUTO_RESPOND_ENABLED = "auto_respond_enabled";
    public static final String KEY_WHITELISTED_CONTACTS = "whitelisted_contacts";
    public static final String KEY_BLACKLISTED_CONTACTS = "blacklisted_contacts";
    public static final String KEY_PREFERRED_TONE = "preferred_tone";
    public static final String KEY_DEVICE_UUID = "device_uuid";
    public static final String KEY_DISABLED_RULES = "disabled_rules";
    public static final String KEY_CUSTOM_RULES = "custom_rules";

    // Default auto-response rules (must match backend DefaultRules order)
    public static final String[] DEFAULT_RULES = {
        "Speak about the user in third person. Do NOT imitate the user directly.",
        "Do NOT fabricate details. Use only the context supplied below.",
        "Adjust tone based on sender relationship: formal for managers or colleagues, casual for friends.",
        "Always set a realistic expectation for when the user will respond.",
        "Keep the response concise: 1 to 3 sentences maximum.",
        "Do not mention raw sensor data, device state, noise levels, or battery information.",
        "Generate exactly ONE response only. Do not provide multiple options or variations.",
        "Do not use placeholder text like [User's Name]. If no name is provided in the context, refer to the user as 'the user'. Do not invent or assume any name.",
        "Do not add any explanation, commentary, or follow-up questions after the response."
    };
    
    // Request/Response Keys
    public static final String EXTRA_REQUEST_ID = "request_id";
    public static final String EXTRA_MESSAGE_TEXT = "message_text";
    public static final String EXTRA_TONE = "tone";
    
    // Notification channels
    public static final String NOTIFICATION_CHANNEL_ID = "carma_notifications";
    public static final String NOTIFICATION_CHANNEL_NAME = "CARMA Auto-Response";
    
    // Request codes
    public static final int REQUEST_CODE_PERMISSIONS = 1001;
    public static final int REQUEST_CODE_NOTIFICATION_ACCESS = 1002;
    
    // Error messages
    public static final String ERROR_NO_NETWORK = "No internet connection";
    public static final String ERROR_API_FAILURE = "Failed to communicate with server";
    public static final String ERROR_INVALID_RESPONSE = "Invalid response from server";
    
    private Constants() {
        // Private constructor to prevent instantiation
    }
}