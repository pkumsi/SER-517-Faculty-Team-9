package services

import (
	"fmt"
	"math"

	"github.com/pkumsi/SER-517-Faculty-Team-9/backend/internal/models"
)

// InferredElements holds the 5 essential contextual elements
// derived from raw sensor data as per Sprint 1 research findings.
// These are the only elements passed to the LLM prompt.
type InferredElements struct {
	Activity             string // What the user is currently doing
	CurrentTime          string // Human-readable time context
	ExpectedResponseTime string // When the user is expected to be available
	SenderRole           string // Who sent the message and via which app
	Urgency              string // How urgent the incoming message appears
}

// InferFromSnapshot derives the 5 essential elements from a raw ContextSnapshot.
// Raw sensor values (noise, light, wifi, battery etc.) are used only as
// inputs to inference and are never passed directly to the LLM.
func InferFromSnapshot(ctx *models.ContextSnapshot) *InferredElements {
	return &InferredElements{
		Activity:             inferActivity(ctx),
		CurrentTime:          inferCurrentTime(ctx),
		ExpectedResponseTime: inferExpectedResponseTime(ctx),
		SenderRole:           inferSenderRole(ctx),
		Urgency:              inferUrgency(ctx),
	}
}

// inferActivity determines what the user is currently doing.
// Priority: Predicted_Availability > Calendar_Event > UserAct_Type > fallback
func inferActivity(ctx *models.ContextSnapshot) string {
	// Highest priority: use predicted availability if present
	if ctx.PredictedAvailability != nil && *ctx.PredictedAvailability != "" {
		availability := *ctx.PredictedAvailability
		if availability == "0" || availability == "unavailable" {
			// Check if there is a calendar event to give more detail
			if ctx.CalendarEvent != nil && *ctx.CalendarEvent == 1 && ctx.EventName != nil && *ctx.EventName != "" {
				return fmt.Sprintf("User is currently in a scheduled event: %s", *ctx.EventName)
			}
			return "User is currently unavailable"
		}
		if availability == "1" || availability == "available" {
			return "User appears to be available"
		}
	}

	// Calendar event takes priority over generic activity
	if ctx.CalendarEvent != nil && *ctx.CalendarEvent == 1 {
		if ctx.EventName != nil && *ctx.EventName != "" {
			return fmt.Sprintf("User has a scheduled event: %s", *ctx.EventName)
		}
		return "User is in a scheduled calendar event"
	}

	// On an active call
	if ctx.OnCall != nil && *ctx.OnCall == 1 {
		return "User is currently on a phone call"
	}

	// Use physical activity type from sensor
	if ctx.UserActType != nil {
		switch *ctx.UserActType {
		case "in_vehicle":
			return "User is currently driving or in a vehicle"
		case "on_bicycle":
			return "User is currently cycling"
		case "running":
			return "User is currently running"
		case "walking":
			return "User is currently walking"
		case "still":
			// Check screen state for more context
			if ctx.ScreenValue != nil && *ctx.ScreenValue == "Locked" {
				return "User's phone is locked and they appear to be idle"
			}
			return "User is stationary"
		}
	}

	// Background conversation suggests user is talking to someone
	if ctx.BackgroundConvo != nil && *ctx.BackgroundConvo != "" && *ctx.BackgroundConvo != "noise" {
		return "User appears to be in a conversation"
	}

	return "User's current activity is unknown"
}

// inferCurrentTime builds a human-readable time description.
// Uses HourOfDay, DayOfWeek, and WorkingDay from the snapshot.
func inferCurrentTime(ctx *models.ContextSnapshot) string {
	if ctx.HourOfDay == nil && ctx.DayOfWeek == nil {
		// Fall back to raw timestamp string if available
		if ctx.TS != nil {
			return fmt.Sprintf("Timestamp: %s", *ctx.TS)
		}
		return "Current time is unknown"
	}

	timeOfDay := ""
	if ctx.HourOfDay != nil {
		hour := *ctx.HourOfDay
		switch {
		case hour >= 5 && hour < 12:
			timeOfDay = fmt.Sprintf("morning (%d:00)", hour)
		case hour >= 12 && hour < 17:
			timeOfDay = fmt.Sprintf("afternoon (%d:00)", hour)
		case hour >= 17 && hour < 21:
			timeOfDay = fmt.Sprintf("evening (%d:00)", hour)
		default:
			timeOfDay = fmt.Sprintf("night (%d:00)", hour)
		}
	}

	dayName := ""
	if ctx.DayOfWeek != nil {
		days := []string{"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"}
		dow := *ctx.DayOfWeek
		if dow >= 0 && dow <= 6 {
			dayName = days[dow]
		}
	}

	workContext := ""
	if ctx.WorkingDay != nil {
		if *ctx.WorkingDay == 1 {
			workContext = ", working day"
		} else {
			workContext = ", non-working day"
		}
	}

	return fmt.Sprintf("%s %s%s", dayName, timeOfDay, workContext)
}

// inferExpectedResponseTime estimates when the user will be available to respond.
// Uses Event_TimeLeft and Event_End from calendar data.
func inferExpectedResponseTime(ctx *models.ContextSnapshot) string {
	// Use time left in current event if available
	if ctx.EventTimeLeft != nil && *ctx.EventTimeLeft > 0 {
		minutes := int(math.Round(float64(*ctx.EventTimeLeft) / 60000)) // ms to minutes
		if minutes <= 0 {
			return "User should be available shortly"
		}
		if minutes < 60 {
			return fmt.Sprintf("User is expected to be available in approximately %d minutes", minutes)
		}
		hours := minutes / 60
		remainingMins := minutes % 60
		if remainingMins == 0 {
			return fmt.Sprintf("User is expected to be available in approximately %d hour(s)", hours)
		}
		return fmt.Sprintf("User is expected to be available in approximately %d hour(s) and %d minutes", hours, remainingMins)
	}

	// If no event time left, check if there's an active calendar event
	if ctx.CalendarEvent != nil && *ctx.CalendarEvent == 1 {
		return "User is expected to be available after their current event (~30 minutes)"
	}

	// Use time since last screen unlock as a proxy for availability
	if ctx.TimeSinceLastScreenUnlocked != nil {
		minutes := int(*ctx.TimeSinceLastScreenUnlocked / 60000)
		if minutes < 5 {
			return "User recently unlocked their phone and may respond soon"
		}
	}

	return "Expected response time is unknown; user may respond when available"
}

// inferSenderRole determines the context of who sent the message.
// Uses Notif_App and key (contact identifier) from the snapshot.
func inferSenderRole(ctx *models.ContextSnapshot) string {
	app := ""
	if ctx.NotifApp != nil && *ctx.NotifApp != "" {
		app = *ctx.NotifApp
	}

	// Map app package name to human-readable name
	appName := resolveAppName(app)

	// Use key to determine if sender is a known contact
	if ctx.Key != nil && *ctx.Key != "" {
		return fmt.Sprintf("Message received via %s from a known contact", appName)
	}

	if appName != "Unknown app" {
		return fmt.Sprintf("Message received via %s", appName)
	}

	return "Sender information is unavailable"
}

// resolveAppName maps Android package names to readable app names.
func resolveAppName(packageName string) string {
	appNames := map[string]string{
		"com.whatsapp":              "WhatsApp",
		"com.facebook.messenger":    "Facebook Messenger",
		"org.telegram.messenger":    "Telegram",
		"com.google.android.gm":     "Gmail",
		"com.microsoft.teams":       "Microsoft Teams",
		"com.slack":                 "Slack",
		"com.instagram.android":     "Instagram",
		"com.twitter.android":       "Twitter",
		"com.snapchat.android":      "Snapchat",
		"com.linkedin.android":      "LinkedIn",
		"com.google.android.apps.messaging": "Google Messages",
	}

	if name, ok := appNames[packageName]; ok {
		return name
	}
	if packageName != "" {
		return packageName
	}
	return "Unknown app"
}

// inferUrgency estimates the urgency of the incoming message.
// Uses NotifCenter_Value, timeSinceLastMessageSession, and BackgroundConvo.
func inferUrgency(ctx *models.ContextSnapshot) string {
	urgencyScore := 0

	// Multiple notifications pending suggests urgency
	if ctx.NotifCenterValue != nil {
		if *ctx.NotifCenterValue >= 5 {
			urgencyScore += 2
		} else if *ctx.NotifCenterValue >= 2 {
			urgencyScore += 1
		}
	}

	// Long time since last message session suggests this may be important
	if ctx.TimeSinceLastMessageSession != nil {
		minutesSince := *ctx.TimeSinceLastMessageSession / 60000
		if minutesSince > 60 {
			urgencyScore += 1
		}
	}

	// Active background conversation suggests real-time interaction
	if ctx.BackgroundConvo != nil && *ctx.BackgroundConvo != "" && *ctx.BackgroundConvo != "noise" {
		urgencyScore += 1
	}

	// Do Not Disturb being off when a message arrives may suggest urgency
	if ctx.DoNotDisturb != nil && *ctx.DoNotDisturb == "none" {
		urgencyScore += 1
	}

	switch {
	case urgencyScore >= 4:
		return "High urgency — multiple signals suggest this message requires prompt attention"
	case urgencyScore >= 2:
		return "Medium urgency — message may require timely attention"
	default:
		return "Low urgency — no strong signals indicating immediate response required"
	}
}
