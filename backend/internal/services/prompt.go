package services

import (
	"fmt"
	"strings"

	"github.com/pkumsi/SER-517-Faculty-Team-9/backend/internal/models"
)

// DefaultRules are the system-level rules used when the caller supplies none.
var DefaultRules = []string{
	"Speak about the user in third person. Do NOT imitate the user directly.",
	"Do NOT fabricate details. Use only the context supplied below.",
	"Adjust tone based on sender relationship: formal for managers or colleagues, casual for friends.",
	"Do NOT suggest or imply when the user will respond or become available. Never use phrases like 'will be available', 'will respond', 'once done', 'shortly after', 'when free', or anything similar. The response must ONLY explain why they are unavailable right now.",
	"Do NOT include specific locations, addresses, or full event titles in the response. Keep event references general (e.g. 'a meeting', 'an event').",
	"Keep the response concise: 1 to 3 sentences maximum.",
	"Do not mention raw sensor data, device state, noise levels, or battery information.",
	"Generate exactly ONE response only. Do not provide multiple options or variations.",
	"Do not use placeholder text like [User's Name]. If no name is provided in the context, refer to the user as 'the user'. Do not invent or assume any name.",
	"The 'response' field must contain only the auto-response message — no extra commentary, questions, or follow-up text.",
}

// buildPrompt assembles the full LLM prompt from the raw ContextSnapshot.
// If rules is nil or empty, DefaultRules are used.
func buildPrompt(ctx *models.ContextSnapshot, rules []string) string {
	return fmt.Sprintf("%s\n\n%s", buildSystemInstruction(rules), buildRawContextBlock(ctx))
}

// buildSystemInstruction returns the system-level instruction block.
// If rules is nil or empty, DefaultRules are used.
func buildSystemInstruction(rules []string) string {
	if len(rules) == 0 {
		rules = DefaultRules
	}
	var sb strings.Builder
	sb.WriteString("You are an auto-response generation system for a mobile messaging application.\n")
	sb.WriteString("Use ALL provided context to understand why the user is unavailable, but the 'response' field must be a generic, privacy-safe message — do NOT reveal specific event names, locations, time remaining, or any identifiable details from the context. The 'explanation' field may reference specific context elements for internal use.\n\n")
	sb.WriteString("You MUST respond with ONLY a valid JSON object in this exact format, with no text before or after:\n")
	sb.WriteString("{\n  \"response\": \"<polite generic message explaining unavailability — no specific details>\",\n  \"explanation\": \"<one sentence referencing the specific context elements that drove this response>\"\n}\n\n")
	sb.WriteString("Rules:\n")
	for _, r := range rules {
		sb.WriteString("- ")
		sb.WriteString(r)
		sb.WriteString("\n")
	}
	return strings.TrimRight(sb.String(), "\n")
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
		return "Context: No context data available.\n\nExplain politely that the user is currently unavailable."
	}
	return "Context:\n" + strings.Join(lines, "\n") + "\n\nBased on the context above, generate a polite message explaining why the user is currently unavailable."
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
