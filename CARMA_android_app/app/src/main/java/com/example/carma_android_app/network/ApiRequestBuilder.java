package com.example.carma_android_app.network;

import org.json.JSONException;
import org.json.JSONObject;

// noinspection ALL — JSONObject/JSONException are provided by the Android SDK (org.json)

/**
 * Builder class for constructing API requests.
 *
 * Field names match the Go backend's ContextSnapshot struct exactly
 * (see backend/internal/models/models_input.go).
 */
public class ApiRequestBuilder {

    /**
     * Wraps a context snapshot in the top-level LLMResponseRequest envelope.
     *
     * @param requestId  caller-generated request ID
     * @param contextData  already-built ContextSnapshot JSON object
     * @return JSON string ready to POST to /api/v1/response
     */
    public static String buildAutoResponseRequest(String requestId, JSONObject contextData) {
        try {
            JSONObject request = new JSONObject();
            request.put("request_id", requestId);
            request.put("context", contextData);
            return request.toString();
        } catch (JSONException e) {
            e.printStackTrace();
            return "{}";
        }
    }

    /**
     * Builds a full ContextSnapshot JSON object from the user's manual selections
     * in ContextInputActivity, plus device-sourced time fields.
     *
     * Only non-null / meaningful values are included; all others are omitted so
     * the backend's omitempty handling works correctly.
     *
     * @param uuid             persistent device UUID
     * @param hourOfDay        0-23, from Calendar.HOUR_OF_DAY
     * @param dayOfWeek        0=Sun … 6=Sat
     * @param workingDay       1 if Mon-Fri, 0 otherwise
     * @param userActType      "still" | "on_foot" | "in_vehicle" | "on_bicycle" | "unknown"
     * @param locType          "Home" | "Passing" | "NoLoc"
     * @param screenValue      "Unlocked" | "On" | "Locked" | "Off"
     * @param calendarEvent    1 if event present, 0 otherwise
     * @param eventName        event label, or null
     * @param notifApp         package name of the messaging app, or null
     * @param audioMusic       1 if music playing, 0 otherwise
     * @param audioHeadphones  1 if headphones on, 0 otherwise
     * @param ringerMode       "SILENT" | "VIBRATE" | null (normal)
     * @param isSilent         1 if silent, 0 otherwise
     * @param backgroundConvo  "voice" | "noise" | null
     * @param weather          weather string e.g. "Clear: clear sky", or null
     * @param onCall           "0" | "1" | "ringing"
     */
    public static JSONObject buildFullContextSnapshot(
            String uuid,
            int hourOfDay,
            int dayOfWeek,
            int workingDay,
            String userActType,
            String locType,
            String screenValue,
            int calendarEvent,
            String eventName,
            String notifApp,
            int audioMusic,
            int audioHeadphones,
            String ringerMode,
            int isSilent,
            String backgroundConvo,
            String weather,
            String onCall) {

        try {
            JSONObject ctx = new JSONObject();

            // Identity / time (always present)
            ctx.put("uuid",        uuid);
            ctx.put("HourOfDay",   hourOfDay);
            ctx.put("DayOfWeek",   dayOfWeek);
            ctx.put("WorkingDay",  workingDay);

            // User activity
            if (userActType != null) {
                ctx.put("UserAct_Type", userActType);
                ctx.put("UserAct_p",    100); // confidence — always 100 for manual selection
            }

            // Location
            if (locType != null) ctx.put("Loc_Type", locType);

            // Screen
            if (screenValue != null) ctx.put("Screen_Value", screenValue);

            // Calendar
            ctx.put("Calendar_Event", calendarEvent);
            if (calendarEvent == 1 && eventName != null) {
                ctx.put("Event_Name", eventName);
            }

            // Notification source
            if (notifApp != null) ctx.put("Notif_App", notifApp);

            // Audio
            ctx.put("Audio_Music",      audioMusic);
            ctx.put("Audio_Headphones", audioHeadphones);

            // Ringer / silence
            ctx.put("Is_Silent", isSilent);
            if (ringerMode != null) ctx.put("Ringer_Mode", ringerMode);

            // Background
            if (backgroundConvo != null) ctx.put("Background_Convo", backgroundConvo);

            // Weather
            if (weather != null) ctx.put("Weather", weather);

            // Call status — On_Call is int64 in backend model (0 or 1)
            if (onCall != null) ctx.put("On_Call", Integer.parseInt(onCall));

            // Predicted availability — 0 = busy (user is manually triggering
            // auto-reply, so we assume they are not immediately available)
            ctx.put("Predicted_Availability", "0");

            return ctx;
        } catch (JSONException e) {
            e.printStackTrace();
            return new JSONObject();
        }
    }

    /**
     * Legacy minimal snapshot — kept for backward compatibility.
     * Prefer buildFullContextSnapshot() for new code.
     */
    public static JSONObject buildContextSnapshot(String uuid, int hourOfDay,
            String eventName, String notifApp) {
        try {
            JSONObject context = new JSONObject();
            context.put("uuid", uuid);
            context.put("HourOfDay", hourOfDay);
            if (eventName != null) {
                context.put("Calendar_Event", 1);
                context.put("Event_Name", eventName);
            }
            if (notifApp != null) {
                context.put("Notif_App", notifApp);
            }
            context.put("Predicted_Availability", "0");
            return context;
        } catch (JSONException e) {
            e.printStackTrace();
            return new JSONObject();
        }
    }
}