package com.example.carma_android_app.utils;

import android.content.Context;
import android.provider.Settings;
import java.util.Calendar;
import java.util.UUID;

/**
 * Helper class for retrieving device information
 */
public class DeviceInfoHelper {

    /**
     * Get unique device ID
     */
    public static String getDeviceId(Context context) {
        String androidId = Settings.Secure.getString(
                context.getContentResolver(),
                Settings.Secure.ANDROID_ID
        );

        if (androidId == null || androidId.isEmpty()) {
            // Fallback to random UUID if Android ID is not available
            return UUID.randomUUID().toString();
        }

        return androidId;
    }

    /**
     * Get current hour of day (0-23)
     */
    public static int getCurrentHour() {
        return Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
    }

    /**
     * Get current day of week (1-7, Sunday = 1)
     */
    public static int getCurrentDayOfWeek() {
        return Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
    }

    /**
     * Check if current time is working hours (9 AM - 6 PM, Mon-Fri)
     */
    public static boolean isWorkingHours() {
        Calendar cal = Calendar.getInstance();
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);

        boolean isWeekday = dayOfWeek >= Calendar.MONDAY && dayOfWeek <= Calendar.FRIDAY;
        boolean isWorkHours = hour >= 9 && hour < 18;

        return isWeekday && isWorkHours;
    }

    /**
     * Format timestamp to readable string
     */
    public static String formatTimestamp(long timestamp) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);

        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        return String.format("%02d:%02d", hour, minute);
    }
}
