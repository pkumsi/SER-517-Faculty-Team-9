package com.example.carma_android_app.network;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Builder class for constructing API requests
 */
public class ApiRequestBuilder {

    /**
     * Build auto-response request JSON
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
     * Build context snapshot JSON from sensor data
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