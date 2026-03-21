package com.example.carma_android_app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.carma_android_app.network.ApiClient;
import com.example.carma_android_app.network.ApiRequestBuilder;
import com.example.carma_android_app.network.ResponseParser;
import com.example.carma_android_app.utils.Constants;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import org.json.JSONObject;

import java.util.Calendar;
import java.util.List;
import java.util.UUID;

/**
 * ContextInputActivity — lets the user manually select contextual signals
 * (activity, location, screen state, calendar event, notification app, audio,
 * ringer, background noise, weather, call status) via tap-to-select chips.
 *
 * On "Generate Response" it packages these selections into the backend's
 * ContextSnapshot JSON, calls POST /api/v1/response, and passes the result
 * to PreviewActivity via Intent extras.
 */
public class ContextInputActivity extends AppCompatActivity {

    // Chip groups
    private ChipGroup chipGroupActivity;
    private ChipGroup chipGroupLocation;
    private ChipGroup chipGroupScreen;
    private ChipGroup chipGroupCalendarToggle;
    private ChipGroup chipGroupEventType;
    private ChipGroup chipGroupNotifApp;
    private ChipGroup chipGroupAudio;
    private ChipGroup chipGroupRinger;
    private ChipGroup chipGroupBackground;
    private ChipGroup chipGroupWeather;
    private ChipGroup chipGroupCall;

    private MaterialButton btnGenerate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_context_input);

        initViews();
        setupCalendarToggle();
        setupGenerateButton();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Init
    // ─────────────────────────────────────────────────────────────────────────

    private void initViews() {
        chipGroupActivity        = findViewById(R.id.chip_group_activity);
        chipGroupLocation        = findViewById(R.id.chip_group_location);
        chipGroupScreen          = findViewById(R.id.chip_group_screen);
        chipGroupCalendarToggle  = findViewById(R.id.chip_group_calendar_toggle);
        chipGroupEventType       = findViewById(R.id.chip_group_event_type);
        chipGroupNotifApp        = findViewById(R.id.chip_group_notif_app);
        chipGroupAudio           = findViewById(R.id.chip_group_audio);
        chipGroupRinger          = findViewById(R.id.chip_group_ringer);
        chipGroupBackground      = findViewById(R.id.chip_group_background);
        chipGroupWeather         = findViewById(R.id.chip_group_weather);
        chipGroupCall            = findViewById(R.id.chip_group_call);
        btnGenerate              = findViewById(R.id.btn_generate_response);
    }

    /**
     * Show / hide event-type sub-chips based on whether "Yes, I have an event"
     * is selected in the calendar toggle group.
     */
    private void setupCalendarToggle() {
        chipGroupCalendarToggle.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.contains(R.id.chip_calendar_yes)) {
                chipGroupEventType.setVisibility(View.VISIBLE);
            } else {
                chipGroupEventType.setVisibility(View.GONE);
                chipGroupEventType.clearCheck();
            }
        });
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Generate button
    // ─────────────────────────────────────────────────────────────────────────

    private void setupGenerateButton() {
        btnGenerate.setOnClickListener(v -> {
            btnGenerate.setEnabled(false);
            btnGenerate.setText("Generating…");

            JSONObject contextSnapshot = buildContextSnapshot();
            String uuid      = getOrCreateUuid();
            String requestId = "req-" + uuid + "-" + System.currentTimeMillis();
            String requestBody = ApiRequestBuilder.buildAutoResponseRequest(requestId, contextSnapshot);

            new GenerateResponseTask(requestId, uuid).execute(requestBody);
        });
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Context snapshot builder — maps chip selections → ContextSnapshot fields
    // ─────────────────────────────────────────────────────────────────────────

    private JSONObject buildContextSnapshot() {
        // Auto-fill time fields from device
        Calendar cal     = Calendar.getInstance();
        int hourOfDay    = cal.get(Calendar.HOUR_OF_DAY);
        int dayOfWeek    = cal.get(Calendar.DAY_OF_WEEK) - 1; // 0=Sun … 6=Sat
        int isWorkingDay = (dayOfWeek >= 1 && dayOfWeek <= 5) ? 1 : 0;
        String uuid      = getOrCreateUuid();

        return ApiRequestBuilder.buildFullContextSnapshot(
                uuid,
                hourOfDay,
                dayOfWeek,
                isWorkingDay,
                getActivityValue(),
                getLocationValue(),
                getScreenValue(),
                getCalendarEvent(),
                getEventName(),
                getNotifApp(),
                getAudioMusic(),
                getAudioHeadphones(),
                getRingerMode(),
                getIsSilent(),
                getBackgroundConvo(),
                getWeather(),
                getOnCall()
        );
    }

    // ── Selection readers ─────────────────────────────────────────────────────

    private String getActivityValue() {
        int id = getCheckedId(chipGroupActivity);
        if (id == R.id.chip_activity_still)   return "still";
        if (id == R.id.chip_activity_walking) return "walking";   // inference.go uses "walking" not "on_foot"
        if (id == R.id.chip_activity_vehicle) return "in_vehicle";
        if (id == R.id.chip_activity_bicycle) return "on_bicycle";
        return "still"; // default to still — "unknown" returns "" and causes 500
    }

    private String getLocationValue() {
        int id = getCheckedId(chipGroupLocation);
        if (id == R.id.chip_loc_home)    return "Home";
        if (id == R.id.chip_loc_passing) return "Passing";
        return "NoLoc";
    }

    private String getScreenValue() {
        int id = getCheckedId(chipGroupScreen);
        if (id == R.id.chip_screen_unlocked) return "Unlocked";
        if (id == R.id.chip_screen_on)       return "On";
        if (id == R.id.chip_screen_off)      return "Off";
        return "Locked";
    }

    /** Returns 1 if user said they have a calendar event, 0 otherwise. */
    private int getCalendarEvent() {
        List<Integer> checked = chipGroupCalendarToggle.getCheckedChipIds();
        return checked.contains(R.id.chip_calendar_yes) ? 1 : 0;
    }

    /** Returns the event name chip text, or null if no event. */
    private String getEventName() {
        if (getCalendarEvent() == 0) return null;
        int id = getCheckedId(chipGroupEventType);
        if (id == R.id.chip_event_meeting)   return "meeting";
        if (id == R.id.chip_event_coffee)    return "coffee chat";
        if (id == R.id.chip_event_class)     return "class";
        if (id == R.id.chip_event_happyhour) return "happy hour";
        if (id == R.id.chip_event_other)     return "event";
        return "event";
    }

    private String getNotifApp() {
        int id = getCheckedId(chipGroupNotifApp);
        if (id == R.id.chip_notif_whatsapp)  return "com.whatsapp";
        if (id == R.id.chip_notif_slack)     return "com.Slack";
        if (id == R.id.chip_notif_gmail)     return "com.google.android.gm";
        if (id == R.id.chip_notif_messenger) return "com.facebook.orca";
        if (id == R.id.chip_notif_skype)     return "com.skype.raider";
        if (id == R.id.chip_notif_sms)       return "com.samsung.android.messaging";
        return null;
    }

    private int getAudioMusic() {
        return chipGroupAudio.getCheckedChipIds().contains(R.id.chip_audio_music) ? 1 : 0;
    }

    private int getAudioHeadphones() {
        return chipGroupAudio.getCheckedChipIds().contains(R.id.chip_audio_headphones) ? 1 : 0;
    }

    private String getRingerMode() {
        int id = getCheckedId(chipGroupRinger);
        if (id == R.id.chip_ringer_silent)  return "SILENT";
        if (id == R.id.chip_ringer_vibrate) return "VIBRATE";
        return null; // normal — omit from JSON
    }

    private int getIsSilent() {
        int id = getCheckedId(chipGroupRinger);
        return (id == R.id.chip_ringer_silent) ? 1 : 0;
    }

    private String getBackgroundConvo() {
        int id = getCheckedId(chipGroupBackground);
        if (id == R.id.chip_bg_voice) return "voice";
        if (id == R.id.chip_bg_quiet) return "noise";
        return null;
    }

    private String getWeather() {
        int id = getCheckedId(chipGroupWeather);
        if (id == R.id.chip_weather_clear)   return "Clear: clear sky";
        if (id == R.id.chip_weather_cloudy)  return "Clouds: overcast clouds";
        if (id == R.id.chip_weather_rain)    return "Rain: light rain";
        if (id == R.id.chip_weather_thunder) return "Thunderstorm: thunderstorm";
        if (id == R.id.chip_weather_mist)    return "Mist: mist";
        return null;
    }

    private String getOnCall() {
        int id = getCheckedId(chipGroupCall);
        if (id == R.id.chip_call_yes)     return "1";
        if (id == R.id.chip_call_ringing) return "1"; // ringing treated as on-call
        if (id == R.id.chip_call_no)      return "0";
        return "0";
    }

    // ─────────────────────────────────────────────────────────────────────────
    // AsyncTask — POST /api/v1/response
    // ─────────────────────────────────────────────────────────────────────────

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

            // Parse the first suggested response
            List<?> messages = ResponseParser.parseAutoResponseResult(response);
            String messageText = messages.isEmpty()
                    ? "No response generated."
                    : ((com.example.carma_android_app.models.AutoResponseMessage) messages.get(0)).getMessageText();

            // Parse request_id / uuid from backend response if provided
            String parsedRequestId = requestId;
            String parsedUuid      = uuid;
            try {
                org.json.JSONObject json = new org.json.JSONObject(response);
                if (json.has("request_id") && !json.isNull("request_id"))
                    parsedRequestId = json.getString("request_id");
                if (json.has("uuid") && !json.isNull("uuid"))
                    parsedUuid = json.getString("uuid");
            } catch (Exception ignored) { }

            // Pass data to PreviewActivity
            Intent intent = new Intent(ContextInputActivity.this, PreviewActivity.class);
            intent.putExtra(PreviewActivity.EXTRA_MESSAGE_TEXT, messageText);
            intent.putExtra(PreviewActivity.EXTRA_REQUEST_ID,   parsedRequestId);
            intent.putExtra(PreviewActivity.EXTRA_UUID,         parsedUuid);
            intent.putExtra(PreviewActivity.EXTRA_ACTIVITY,     getActivityValue());
            intent.putExtra(PreviewActivity.EXTRA_LOCATION,     getLocationValue());
            intent.putExtra(PreviewActivity.EXTRA_NOTIF_APP,    getNotifApp());
            startActivity(intent);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Returns the single checked chip ID from a ChipGroup, or -1 if none.
     * Works for singleSelection groups.
     */
    private int getCheckedId(ChipGroup group) {
        List<Integer> ids = group.getCheckedChipIds();
        return ids.isEmpty() ? -1 : ids.get(0);
    }

    /**
     * Returns the stored device UUID from SharedPreferences, creating one if
     * this is the first launch.
     */
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
