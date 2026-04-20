package services

import (
	"fmt"
	"math"

	"github.com/pkumsi/SER-517-Faculty-Team-9/backend/internal/models"
)

// PromptVariant represents the three template configurations from Task #25 (Prerana Kumsi)
// grounded in combination testing results from Task #20 (Aniket Patil).
//
//	Minimal  → Activity + Expected Response Time (Combo 6)
//	           Good quality, best privacy-to-quality ratio.
//
//	Standard → Activity + Time + Sender + Urgency + Expected Response Time (Combo 3)
//	           Excellent quality. Optimal 5-element set. Recommended default.
//
//	Rich     → Combo 3 with partial elements (Combo 2 fallback)
//	           Good quality when full 5-element set is not available.
type PromptVariant string

const (
	VariantMinimal  PromptVariant = "minimal"
	VariantStandard PromptVariant = "standard"
	VariantRich     PromptVariant = "rich"
)

// InferredElements holds exactly the 5 contextual elements identified as optimal
// in Sprint 1 research (Task #21, Satyam Shekhar) and confirmed by combination
// testing (Task #20, Aniket Patil).
//
// These are the ONLY values passed to the prompt builder and ultimately the LLM.
// Raw sensor values (noise, light, battery, device state) are used only as
// inference inputs here and never leave this layer — per Context Selection
// Guidelines (Task #23, Satyam Shekhar):
//
//	"Noise / Light / Device / Battery — Never in prompt.
//	 Use as inputs to activity inference only. Do not pass to LLM."
type InferredElements struct {
	Activity             string // Critical — Task #21, Task #22 Tier 1
	CurrentTime          string // Critical — Task #21, Task #22 Tier 1
	ExpectedResponseTime string // Moderate — Task #21, always required per Task #23
	SenderRole           string // High — Task #21, Task #22 Tier 2
	Urgency              string // High — Task #21, Task #22 Tier 2

	// Variant is selected by selectVariant() based on which elements
	// are successfully inferred. Controls which prompt template is used.
	Variant PromptVariant
}

// InferFromSnapshot is the single public entry point for the inference layer.
// It maps a raw ContextSnapshot to the 5 essential InferredElements and selects
// the appropriate prompt variant based on what was successfully inferred.
//
// Implements the sensor-to-prompt pipeline from Task #25 (Prerana Kumsi) Section 6:
//
//	Sensor Data → Activity Inference → Context Selection → Prompt Template Assembler
func InferFromSnapshot(ctx *models.ContextSnapshot) *InferredElements {
	elements := &InferredElements{}

	elements.Activity = inferActivity(ctx)
	elements.CurrentTime = inferCurrentTime(ctx)
	elements.ExpectedResponseTime = inferExpectedResponseTime(ctx)
	elements.SenderRole = inferSenderRole(ctx)
	elements.Urgency = inferUrgency(ctx)

	elements.Variant = selectVariant(elements)

	return elements
}

// selectVariant determines which prompt template to use based on which of the
// 5 elements were successfully inferred. Implements graceful degradation so the
// system always produces a response when activity is present.
//
// Per Task #23 Context Selection Guidelines:
//
//	"Activity — Always required. If unavailable, do not generate a response."
//
// Variant mapping to Task #20 combination tests:
//
//	Standard → Combo 3 (all 5 elements, Excellent)
//	Rich     → Combo 2 (4 elements, Good) — used as fallback before Minimal
//	Minimal  → Combo 6 (Activity + Response Time, Good)
//	           Combo 1 (Activity + Time, Acceptable)
func selectVariant(e *InferredElements) PromptVariant {
	hasActivity := e.Activity != ""
	hasTime := e.CurrentTime != ""
	hasSender := e.SenderRole != ""
	hasUrgency := e.Urgency != ""
	hasResponseTime := e.ExpectedResponseTime != ""

	// Standard — full optimal 5-element set (Combo 3, Task #20)
	if hasActivity && hasTime && hasSender && hasUrgency && hasResponseTime {
		return VariantStandard
	}

	// Rich — 4-element fallback (Combo 2, Task #20)
	// "Social context adds professional tone" — still Good quality
	if hasActivity && hasTime && hasSender && hasUrgency {
		return VariantRich
	}

	// Minimal — Activity + Response Time (Combo 6, Task #20)
	// or Activity + Time (Combo 1, Task #20)
	return VariantMinimal
}

// inferActivity determines what the user is currently doing.
// Activity is the single most critical element — removing it causes major
// quality drop: "Response became vague. No justification for unavailability."
// — Task #19 T1 (Aniket Patil)
//
// Priority chain based on data reliability:
//  1. Calendar_Event + Event_Name — structured, highest reliability
//  2. PredictedAvailability — already inferred upstream by the Laila pipeline
//  3. OnCall — direct sensor signal
//  4. UserAct_Type — motion sensor (still, walking, in_vehicle etc.)
//  5. BackgroundConvo — audio inference signal
//
// Noise (Noise_Value), Light (Light_Value), and Device state (ScreenValue,
// IsSilent) are used ONLY as disambiguation signals here and are never
// passed to the LLM — per Task #23 and Task #19 T6, T7.
func inferActivity(ctx *models.ContextSnapshot) string {
	// Priority 1 — Calendar event gives the most specific activity description
	// Calendar_Event is Tier 2 High in Task #22, directly available from Laila system
	if ctx.CalendarEvent != nil && *ctx.CalendarEvent == 1 {
		if ctx.EventName != nil && *ctx.EventName != "" {
			return fmt.Sprintf("in a scheduled event: %s", *ctx.EventName)
		}
		return "in a scheduled calendar event"
	}

	// Priority 2 — Predicted availability from upstream Laila inference pipeline
	if ctx.PredictedAvailability != nil && *ctx.PredictedAvailability != "" {
		av := *ctx.PredictedAvailability
		if av == "0" || av == "unavailable" {
			return "currently unavailable"
		}
		if av == "1" || av == "available" {
			return "currently available but may have stepped away"
		}
	}

	// Priority 3 — Active phone call
	if ctx.OnCall != nil && *ctx.OnCall == 1 {
		return "currently on a phone call"
	}

	// Priority 4 — Physical activity from motion sensor (UserAct_Type)
	// Noise and light used only for disambiguation, never passed to LLM
	if ctx.UserActType != nil {
		switch *ctx.UserActType {
		case "in_vehicle":
			return "currently driving or traveling"
		case "on_bicycle":
			return "currently cycling"
		case "running":
			return "currently running"
		case "walking":
			return "currently walking"
		case "still":
			// Use screen state only for disambiguation here
			if ctx.ScreenValue != nil && *ctx.ScreenValue == "Locked" {
				return "away from their phone"
			}
			return "currently stationary and may not be checking messages"
		}
	}

	// Priority 5 — Background conversation signal (audio inference)
	if ctx.BackgroundConvo != nil &&
		*ctx.BackgroundConvo != "" &&
		*ctx.BackgroundConvo != "noise" {
		return "currently in a conversation"
	}

	return ""
}

// inferCurrentTime builds a human-readable time description.
// Current Time is Tier 1 Critical — Task #22. "Required for temporal grounding." — Task #23.
// Enables responses like "I'll reply in the morning" vs "I'll reply after the meeting."
//
// Uses HourOfDay, WorkingDay from ContextSnapshot.
// DayOfWeek is Tier 3 Medium (Task #22) and not part of the 5 essential elements,
// so it is NOT included here per the final research findings.
func inferCurrentTime(ctx *models.ContextSnapshot) string {
	if ctx.HourOfDay == nil {
		if ctx.TS != nil {
			return *ctx.TS
		}
		return ""
	}

	hour := *ctx.HourOfDay
	var timeLabel string
	switch {
	case hour >= 5 && hour < 12:
		timeLabel = fmt.Sprintf("morning (%d:00)", hour)
	case hour >= 12 && hour < 17:
		timeLabel = fmt.Sprintf("afternoon (%d:00)", hour)
	case hour >= 17 && hour < 21:
		timeLabel = fmt.Sprintf("evening (%d:00)", hour)
	default:
		timeLabel = fmt.Sprintf("night (%d:00)", hour)
	}

	if ctx.WorkingDay != nil && *ctx.WorkingDay == 0 {
		return fmt.Sprintf("%s on a non-working day", timeLabel)
	}

	return timeLabel
}

// inferExpectedResponseTime estimates when the user will be available to respond.
// "Always required. Use activity end time or default (~30 min) if unknown." — Task #23.
// Task #20 Combo 6 showed Activity + Response Time alone achieves Good quality.
//
// Uses EventTimeLeft (milliseconds) and CalendarEvent from ContextSnapshot.
func inferExpectedResponseTime(ctx *models.ContextSnapshot) string {
	// Use time remaining in current event, converting ms to minutes
	if ctx.EventTimeLeft != nil && *ctx.EventTimeLeft > 0 {
		minutes := int(math.Round(float64(*ctx.EventTimeLeft) / 60000))
		if minutes <= 0 {
			return "shortly"
		}
		if minutes < 60 {
			return fmt.Sprintf("approximately %d minutes", minutes)
		}
		hours := minutes / 60
		remaining := minutes % 60
		if remaining == 0 {
			return fmt.Sprintf("approximately %d hour(s)", hours)
		}
		return fmt.Sprintf("approximately %d hour(s) and %d minutes", hours, remaining)
	}

	// Active calendar event but no EventTimeLeft — use ~30 min default
	// per Task #20 baseline prompt convention
	if ctx.CalendarEvent != nil && *ctx.CalendarEvent == 1 {
		return "after the current event (~30 minutes)"
	}

	return ""
}

// inferSenderRole determines who sent the message and their relationship to the user.
// Sender Role is Tier 2 High — Task #22.
// "Tone became generic without it." — Task #19 T2 (Aniket Patil)
// "Sender role drives dramatic tone shift between manager and friend." — Task #20 Combo 8
//
// Uses NotifApp (which messaging app) and Key (contact identifier) from ContextSnapshot.
// Note: The dataset does not contain explicit relationship labels (manager/friend).
// We can only infer the messaging channel and whether the sender is a known contact.
func inferSenderRole(ctx *models.ContextSnapshot) string {
	appName := ""
	if ctx.NotifApp != nil && *ctx.NotifApp != "" {
		appName = resolveAppName(*ctx.NotifApp)
	}

	isKnownContact := ctx.Key != nil && *ctx.Key != ""

	if appName != "" && isKnownContact {
		return fmt.Sprintf("known contact via %s", appName)
	}
	if appName != "" {
		return fmt.Sprintf("contact via %s", appName)
	}
	if isKnownContact {
		return "known contact"
	}

	return ""
}


// inferUrgency estimates the urgency of the incoming message from passive signals.
// Message Urgency is Tier 2 High — Task #22.
// "Without urgency, response timing became unclear." — Task #19 T3 (Aniket Patil)
//
// No explicit urgency field exists in ContextSnapshot — it is inferred from:
//
//	NotifCenterValue      — number of pending notifications
//	TimeSinceLastMessageSession — how long since last messaging activity
//	BackgroundConvo       — active background conversation signal
//	DoNotDisturb          — whether DND was bypassed
func inferUrgency(ctx *models.ContextSnapshot) string {
	score := 0

	// Multiple pending notifications suggest urgency
	if ctx.NotifCenterValue != nil {
		if *ctx.NotifCenterValue >= 5 {
			score += 2
		} else if *ctx.NotifCenterValue >= 2 {
			score += 1
		}
	}

	// Long silence since last message session
	if ctx.TimeSinceLastMessageSession != nil {
		minutesSince := *ctx.TimeSinceLastMessageSession / 60000
		if minutesSince > 60 {
			score += 1
		}
	}

	// Active background conversation — real-time interaction signal
	if ctx.BackgroundConvo != nil &&
		*ctx.BackgroundConvo != "" &&
		*ctx.BackgroundConvo != "noise" {
		score += 1
	}

	// DND off when message arrives — sender bypassed quiet settings
	if ctx.DoNotDisturb != nil && *ctx.DoNotDisturb == "none" {
		score += 1
	}

	switch {
	case score >= 4:
		return "high"
	case score >= 2:
		return "medium"
	default:
		return "low"
	}
}
