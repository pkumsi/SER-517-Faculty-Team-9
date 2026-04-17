package com.example.carma_android_app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.carma_android_app.network.ApiClient;
import com.example.carma_android_app.network.ApiRequestBuilder;
import com.example.carma_android_app.network.ResponseParser;
import com.example.carma_android_app.utils.Constants;
import com.example.carma_android_app.utils.PreferencesManager;
import com.google.android.material.button.MaterialButton;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * ContextInputActivity — entry screen.
 * User loads a sample context row from the dataset, then generates an auto-response.
 *
 * Req 2: "Load Sample Context" button will be wired to pick a random row from
 * the embedded dataset (assets/sample_data.json). Placeholder for now.
 */
public class ContextInputActivity extends AppCompatActivity {

    private MaterialButton btnLoadSample;
    private MaterialButton btnGenerate;
    private MaterialButton btnSettings;
    private TextView tvContextPreview;
    private TextView tvSampleBadge;

    /** Holds the currently loaded context snapshot. Null until a sample is loaded. */
    private JSONObject loadedContext = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_context_input);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_settings) {
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            }
            return false;
        });

        btnLoadSample    = findViewById(R.id.btn_load_sample);
        btnGenerate      = findViewById(R.id.btn_generate_response);
        btnSettings      = findViewById(R.id.btn_settings);
        tvContextPreview = findViewById(R.id.tv_context_preview);
        tvSampleBadge    = findViewById(R.id.tv_sample_badge);

        btnLoadSample.setOnClickListener(v -> loadSampleContext());
        btnSettings.setOnClickListener(v -> startActivity(new Intent(this, SettingsActivity.class)));
        setupGenerateButton();
    }

    // -------------------------------------------------------------------------
    // Load Sample — Req 2 placeholder
    // Full implementation will pick a random row from assets/sample_data.json
    // -------------------------------------------------------------------------

    private void loadSampleContext() {
        try {
            InputStream is = getAssets().open("sample_data.json");
            byte[] buffer = new byte[is.available()];
            //noinspection ResultOfMethodCallIgnored
            is.read(buffer);
            is.close();

            JSONArray rows = new JSONArray(new String(buffer, StandardCharsets.UTF_8));
            int index = new Random().nextInt(rows.length());
            loadedContext = rows.getJSONObject(index);

            // Show every field, formatting key names for readability
            StringBuilder preview = new StringBuilder();
            Iterator<String> keys = loadedContext.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                preview.append(formatKey(key))
                       .append(": ")
                       .append(loadedContext.opt(key))
                       .append("\n");
            }

            tvSampleBadge.setText("Sample " + (index + 1) + " / " + rows.length());
            tvContextPreview.setTextColor(0xFF212121);
            tvContextPreview.setText(preview.toString().trim());
            Toast.makeText(this, "Sample " + (index + 1) + " of " + rows.length() + " loaded", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Failed to load sample: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // -------------------------------------------------------------------------
    // Generate Response
    // -------------------------------------------------------------------------

    private void setupGenerateButton() {
        btnGenerate.setOnClickListener(v -> {
            if (loadedContext == null) {
                Toast.makeText(this, "Please load a sample context first", Toast.LENGTH_SHORT).show();
                return;
            }

            btnGenerate.setEnabled(false);
            btnGenerate.setText("Generating...");

            String uuid      = getOrCreateUuid();
            String requestId = "req-" + uuid + "-" + System.currentTimeMillis();

            List<String> effectiveRules = PreferencesManager.getInstance(this).getEffectiveRules();
            JSONArray rulesArray = new JSONArray();
            for (String r : effectiveRules) rulesArray.put(r);

            String requestBody = ApiRequestBuilder.buildAutoResponseRequest(requestId, loadedContext, rulesArray);
            new GenerateResponseTask(requestId, uuid).execute(requestBody);
        });
    }

    // -------------------------------------------------------------------------
    // AsyncTask — POST /api/v1/response
    // -------------------------------------------------------------------------

    private class GenerateResponseTask extends AsyncTask<String, Void, String> {

        private final String requestId;
        private final String uuid;
        private String errorMessage;

        GenerateResponseTask(String requestId, String uuid) {
            this.requestId = requestId;
            this.uuid      = uuid;
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                return ApiClient.getInstance().sendAutoResponseRequest(params[0]);
            } catch (Exception e) {
                errorMessage = e.getMessage();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String response) {
            btnGenerate.setEnabled(true);
            btnGenerate.setText("Generate Response");

            if (response == null) {
                Toast.makeText(ContextInputActivity.this,
                        "Failed to reach backend: " + errorMessage,
                        Toast.LENGTH_LONG).show();
                return;
            }

            List<?> messages = ResponseParser.parseAutoResponseResult(response);
            com.example.carma_android_app.models.AutoResponseMessage firstMessage =
                    messages.isEmpty() ? null
                    : (com.example.carma_android_app.models.AutoResponseMessage) messages.get(0);
            String messageText  = firstMessage != null ? firstMessage.getMessageText() : "No response generated.";
            String explanation  = firstMessage != null && firstMessage.getExplanation() != null
                    ? firstMessage.getExplanation() : "";

            String parsedRequestId = requestId;
            String parsedUuid      = uuid;
            try {
                org.json.JSONObject json = new org.json.JSONObject(response);
                if (json.has("request_id") && !json.isNull("request_id"))
                    parsedRequestId = json.getString("request_id");
                if (json.has("uuid") && !json.isNull("uuid"))
                    parsedUuid = json.getString("uuid");
            } catch (Exception ignored) { }

            Intent intent = new Intent(ContextInputActivity.this, PreviewActivity.class);
            intent.putExtra(PreviewActivity.EXTRA_MESSAGE_TEXT,  messageText);
            intent.putExtra(PreviewActivity.EXTRA_EXPLANATION,   explanation);
            intent.putExtra(PreviewActivity.EXTRA_REQUEST_ID,    parsedRequestId);
            intent.putExtra(PreviewActivity.EXTRA_UUID,          parsedUuid);
            intent.putExtra(PreviewActivity.EXTRA_ACTIVITY,      "");
            intent.putExtra(PreviewActivity.EXTRA_LOCATION,      "");
            intent.putExtra(PreviewActivity.EXTRA_NOTIF_APP,     "");
            startActivity(intent);
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /** Makes raw JSON keys human-readable: "UserAct_Type" → "User Act Type" */
    private String formatKey(String raw) {
        return raw.replace("_", " ");
    }

    private String getOrCreateUuid() {
        SharedPreferences prefs = getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
        String uuid = prefs.getString(Constants.KEY_DEVICE_UUID, null);
        if (uuid == null) {
            uuid = UUID.randomUUID().toString();
            prefs.edit().putString(Constants.KEY_DEVICE_UUID, uuid).apply();
        }
        return uuid;
    }
}
