package com.example.carma_android_app.models;

/**
 * Model class representing sensor data from the device Maps to the
 * ContextSnapshot structure in the Go backend
 */
public class SensorData {

    private String uuid;
    private int hourOfDay;
    private int dayOfWeek;
    private String calendarEvent;
    private String eventName;
    private long eventTimeLeft;
    private String notificationApp;
    private String predictedAvailability;
    private String location;
    private boolean isCharging;
    private int batteryLevel;

    public SensorData(String uuid) {
        this.uuid = uuid;
        this.predictedAvailability = "1"; // Default: available
    }

    // Getters and Setters
    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public int getHourOfDay() {
        return hourOfDay;
    }

    public void setHourOfDay(int hourOfDay) {
        this.hourOfDay = hourOfDay;
    }

    public int getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(int dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public String getCalendarEvent() {
        return calendarEvent;
    }

    public void setCalendarEvent(String calendarEvent) {
        this.calendarEvent = calendarEvent;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public long getEventTimeLeft() {
        return eventTimeLeft;
    }

    public void setEventTimeLeft(long eventTimeLeft) {
        this.eventTimeLeft = eventTimeLeft;
    }

    public String getNotificationApp() {
        return notificationApp;
    }

    public void setNotificationApp(String notificationApp) {
        this.notificationApp = notificationApp;
    }

    public String getPredictedAvailability() {
        return predictedAvailability;
    }

    public void setPredictedAvailability(String predictedAvailability) {
        this.predictedAvailability = predictedAvailability;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public boolean isCharging() {
        return isCharging;
    }

    public void setCharging(boolean charging) {
        isCharging = charging;
    }

    public int getBatteryLevel() {
        return batteryLevel;
    }

    public void setBatteryLevel(int batteryLevel) {
        this.batteryLevel = batteryLevel;
    }
}
