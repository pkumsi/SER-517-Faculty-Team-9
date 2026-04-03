package com.example.carma_android_app.network;

import com.example.carma_android_app.utils.Constants;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

/**
 * API Client for communicating with the Go backend
 */
public class ApiClient {
    private static ApiClient instance;

    private ApiClient() {
    }

    public static synchronized ApiClient getInstance() {
        if (instance == null) {
            instance = new ApiClient();
        }
        return instance;
    }

    /**
     * Send auto-response request to backend
     * 
     * @param requestBody JSON request body
     * @return API response as String
     */
    public String sendAutoResponseRequest(String requestBody) throws IOException {
        URL url = new URL(Constants.API_BASE_URL + Constants.API_ENDPOINT_RESPONSE);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        try {
            // Setup connection
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(60000);

            // Send request
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = requestBody.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            // Read response
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (Scanner scanner = new Scanner(connection.getInputStream())) {
                    scanner.useDelimiter("\\A");
                    return scanner.hasNext() ? scanner.next() : "";
                }
            } else {
                throw new IOException("HTTP error code: " + responseCode);
            }
        } finally {
            connection.disconnect();
        }
    }

    /**
     * Send feedback (like/dislike) to backend
     * @param requestBody JSON request body
     * @return API response as String
     */
    public String sendFeedback(String requestBody) throws IOException {
        URL url = new URL(Constants.API_BASE_URL + "/api/v1/feedback");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        try {
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(60000);
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = requestBody.getBytes("utf-8");
                os.write(input, 0, input.length);
            }
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (Scanner scanner = new Scanner(connection.getInputStream())) {
                    scanner.useDelimiter("\\A");
                    return scanner.hasNext() ? scanner.next() : "";
                }
            } else {
                throw new IOException("HTTP error code: " + responseCode);
            }
        } finally {
            connection.disconnect();
        }
    }

    /**
     * Health check endpoint
     */
    public boolean checkHealth() {
        try {
            URL url = new URL(Constants.API_BASE_URL + "/health");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);

            int responseCode = connection.getResponseCode();
            connection.disconnect();

            return responseCode == HttpURLConnection.HTTP_OK;
        } catch (IOException e) {
            return false;
        }
    }
}