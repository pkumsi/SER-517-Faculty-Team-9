package com.example.carma_android_app.network;

import com.example.carma_android_app.models.AutoResponseMessage;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

/**
 * Parser for API responses
 */
public class ResponseParser {

    /**
     * Parse auto-response from API response JSON
     */
    public static List<AutoResponseMessage> parseAutoResponseResult(String jsonResponse) {
        List<AutoResponseMessage> messages = new ArrayList<>();

        try {
            JSONObject response = new JSONObject(jsonResponse);
            String requestId = response.optString("request_id", "");

            String explanation = "";
            if (response.has("explainability") && !response.isNull("explainability")) {
                JSONObject explainability = response.getJSONObject("explainability");
                explanation = explainability.optString("Shap_raw", "");
            }

            if (response.has("responses")) {
                JSONArray responsesArray = response.getJSONArray("responses");

                for (int i = 0; i < responsesArray.length(); i++) {
                    String messageText = responsesArray.getString(i);
                    AutoResponseMessage message = new AutoResponseMessage(
                            requestId,
                            messageText,
                            "Casual"
                    );
                    message.setExplanation(explanation);
                    messages.add(message);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return messages;
    }

    /**
     * Check if auto-response was enabled in the response
     */
    public static boolean isAutoResponseEnabled(String jsonResponse) {
        try {
            JSONObject response = new JSONObject(jsonResponse);
            return response.optBoolean("ar_enabled", false);
        } catch (JSONException e) {
            return false;
        }
    }
}