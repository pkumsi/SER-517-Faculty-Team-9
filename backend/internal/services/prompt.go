package services

import (
	"fmt"
	"strings"

	"github.com/pkumsi/SER-517-Faculty-Team-9/backend/internal/models"
)

// buildPrompt assembles the full LLM prompt from the raw ContextSnapshot.
// All non-nil context fields are forwarded directly — no inference layer.
func buildPrompt(ctx *models.ContextSnapshot) string {
	return fmt.Sprintf("%s\n\n%s", buildSystemInstruction(), buildRawContextBlock(ctx))
}

// buildSystemInstruction returns the constant system-level instruction for the LLM.
func buildSystemInstruction() string {
	return `You are an auto-response generation system for a mobile messaging application.
Generate a polite, context-aware auto-response message on behalf of the user.

Rules:
- Speak about the user in third person. Do NOT imitate the user directly.
- Do NOT fabricate details. Use only the context supplied below.
- Adjust tone based on sender relationship: formal for managers or colleagues, casual for friends.
- Always set a realistic expectation for when the user will respond.
- Keep the response concise: 1 to 3 sentences maximum.
- Do not mention raw sensor data, device state, noise levels, or battery information.
- Generate exactly ONE response only. Do not provide multiple options or variations.
- Do not use placeholder text like [User's Name]. Use the actual name provided in the context.
- Do not add any explanation, commentary, or follow-up questions after the response.`
}

// buildRawContextBlock formats all meaningful non-nil fields from a ContextSnapshot
// into a human-readable block for the LLM. No inference — values are used as-is.
func buildRawContextBlock(ctx *models.ContextSnapshot) string {
	var lines []string

	// Time
	if ctx.HourOfDay != nil {
		hour := *ctx.HourOfDay
		s := fmt.Sprintf("Time of Day: %s (%02d:00)", hourLabel(hour), hour)
		if ctx.DayOfWeek != nil {
			s += fmt.Sprintf(", %s", dayLabel(*ctx.DayOfWeek))
		}
		if ctx.WorkingDay != nil {
			if *ctx.WorkingDay == 1 {
				s += " (working day)"
			} else {
				s += " (non-working day)"
			}
		}
		lines = append(lines, s)
	} else if ctx.TS != nil {
		lines = append(lines, "Timestamp: "+*ctx.TS)
	}

	// Physical activity
	if ctx.UserActType != nil && *ctx.UserActType != "" {
		lines = append(lines, "Physical Activity: "+*ctx.UserActType)
	}

	// Location
	if ctx.LocType != nil && *ctx.LocType != "" {
		lines = append(lines, "Location: "+*ctx.LocType)
	}

	// Screen state
	if ctx.ScreenValue != nil && *ctx.ScreenValue != "" {
		lines = append(lines, "Screen State: "+*ctx.ScreenValue)
	}

	// Predicted availability
	if ctx.PredictedAvailability != nil && *ctx.PredictedAvailability != "" {
		av := *ctx.PredictedAvailability
		label := av
		if av == "0" {
			label = "Unavailable"
		} else if av == "1" {
			label = "Available"
		}
		lines = append(lines, "Predicted Availability: "+label)
	}

	// On a call
	if ctx.OnCall != nil && *ctx.OnCall == 1 {
		lines = append(lines, "Phone Call: Currently on a call")
	}

	// Calendar event
	if ctx.CalendarEvent != nil && *ctx.CalendarEvent == 1 {
		s := "Calendar Event: Yes"
		if ctx.EventName != nil && *ctx.EventName != "" {
			s = "Calendar Event: " + *ctx.EventName
		}
		if ctx.EventTimeLeft != nil && *ctx.EventTimeLeft > 0 {
			s += fmt.Sprintf(" (%s remaining)", msDuration(*ctx.EventTimeLeft))
		}
		if ctx.EventLocation != nil && *ctx.EventLocation != "" {
			s += " at " + *ctx.EventLocation
		}
		lines = append(lines, s)
	}

	// Notification source
	if ctx.NotifApp != nil && *ctx.NotifApp != "" {
		s := "Notification From: " + resolveAppName(*ctx.NotifApp)
		if ctx.NotifCenterValue != nil && *ctx.NotifCenterValue > 0 {
			s += fmt.Sprintf(" (%d pending)", *ctx.NotifCenterValue)
		}
		lines = append(lines, s)
	}

	// Audio
	if ctx.AudioMusic != nil && *ctx.AudioMusic == 1 {
		lines = append(lines, "Audio: Music playing")
	}
	if ctx.AudioHeadphones != nil && *ctx.AudioHeadphones == 1 {
		lines = append(lines, "Audio: Headphones connected")
	}

	// Ringer / silence
	if ctx.RingerMode != nil && *ctx.RingerMode != "" {
		lines = append(lines, "Ringer Mode: "+*ctx.RingerMode)
	}
	if ctx.IsSilent != nil && *ctx.IsSilent == 1 {
		lines = append(lines, "Device: Silent mode on")
	}
	if ctx.DoNotDisturb != nil && *ctx.DoNotDisturb != "" && *ctx.DoNotDisturb != "all" {
		lines = append(lines, "Do Not Disturb: "+*ctx.DoNotDisturb)
	}

	// Background environment
	if ctx.BackgroundConvo != nil {
		switch *ctx.BackgroundConvo {
		case "voice":
			lines = append(lines, "Background: Conversation nearby")
		case "noise":
			lines = append(lines, "Background: Ambient noise")
		}
	}

	// Weather
	if ctx.Weather != nil && *ctx.Weather != "" {
		s := "Weather: " + *ctx.Weather
		if ctx.Temperature != nil {
			s += fmt.Sprintf(" (%.1f°C)", *ctx.Temperature)
		}
		lines = append(lines, s)
	}

	if len(lines) == 0 {
		return "Context: No context data available.\n\nGenerate a polite auto-response."
	}
	return "Context:\n" + strings.Join(lines, "\n") + "\n\nGenerate a polite auto-response."
}

// hourLabel converts a 0-23 hour to a time-of-day label.
func hourLabel(hour int64) string {
	switch {
	case hour >= 5 && hour < 12:
		return "morning"
	case hour >= 12 && hour < 17:
		return "afternoon"
	case hour >= 17 && hour < 21:
		return "evening"
	default:
		return "night"
	}
}

// dayLabel converts a 0=Sunday…6=Saturday day index to a day name.
func dayLabel(day int64) string {
	days := []string{"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"}
	if day >= 0 && day < int64(len(days)) {
		return days[day]
	}
	return ""
}

// msDuration converts milliseconds to a short human-readable duration string.
func msDuration(ms int64) string {
	minutes := ms / 60000
	if minutes < 1 {
		return "less than a minute"
	}
	if minutes < 60 {
		return fmt.Sprintf("~%d minutes", minutes)
	}
	hours := minutes / 60
	rem := minutes % 60
	if rem == 0 {
		return fmt.Sprintf("~%d hour(s)", hours)
	}
	return fmt.Sprintf("~%d hour(s) %d minutes", hours, rem)
}
