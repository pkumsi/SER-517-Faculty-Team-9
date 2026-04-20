package services

import (
	"testing"

	"github.com/pkumsi/SER-517-Faculty-Team-9/backend/internal/models"
)

// Helper functions to create pointers for test data
func ptrStr(s string) *string { return &s }
func ptrInt(i int64) *int64   { return &i }

// TestInferActivity tests the inferActivity function with various sensor scenarios
func TestInferActivity(t *testing.T) {
	tests := []struct {
		name     string
		snapshot *models.ContextSnapshot
		expected string
	}{
		{
			name: "Calendar event with event name",
			snapshot: &models.ContextSnapshot{
				CalendarEvent: ptrInt(1),
				EventName:     ptrStr("Standup Meeting"),
			},
			expected: "in a scheduled event: Standup Meeting",
		},
		{
			name: "Calendar event without event name",
			snapshot: &models.ContextSnapshot{
				CalendarEvent: ptrInt(1),
			},
			expected: "in a scheduled calendar event",
		},
		{
			name: "Predicted availability unavailable (0)",
			snapshot: &models.ContextSnapshot{
				PredictedAvailability: ptrStr("0"),
			},
			expected: "currently unavailable",
		},
		{
			name: "Predicted availability unavailable (unavailable)",
			snapshot: &models.ContextSnapshot{
				PredictedAvailability: ptrStr("unavailable"),
			},
			expected: "currently unavailable",
		},
		{
			name: "Predicted availability available (1)",
			snapshot: &models.ContextSnapshot{
				PredictedAvailability: ptrStr("1"),
			},
			expected: "currently available but may have stepped away",
		},
		{
			name: "Predicted availability available (available)",
			snapshot: &models.ContextSnapshot{
				PredictedAvailability: ptrStr("available"),
			},
			expected: "currently available but may have stepped away",
		},
		{
			name: "On a phone call",
			snapshot: &models.ContextSnapshot{
				OnCall: ptrInt(1),
			},
			expected: "currently on a phone call",
		},
		{
			name: "In vehicle",
			snapshot: &models.ContextSnapshot{
				UserActType: ptrStr("in_vehicle"),
			},
			expected: "currently driving or traveling",
		},
		{
			name: "On bicycle",
			snapshot: &models.ContextSnapshot{
				UserActType: ptrStr("on_bicycle"),
			},
			expected: "currently cycling",
		},
		{
			name: "Running",
			snapshot: &models.ContextSnapshot{
				UserActType: ptrStr("running"),
			},
			expected: "currently running",
		},
		{
			name: "Walking",
			snapshot: &models.ContextSnapshot{
				UserActType: ptrStr("walking"),
			},
			expected: "currently walking",
		},
		{
			name: "Still with phone locked",
			snapshot: &models.ContextSnapshot{
				UserActType: ptrStr("still"),
				ScreenValue: ptrStr("Locked"),
			},
			expected: "away from their phone",
		},
		{
			name: "Still with phone unlocked",
			snapshot: &models.ContextSnapshot{
				UserActType: ptrStr("still"),
				ScreenValue: ptrStr("Unlocked"),
			},
			expected: "currently stationary and may not be checking messages",
		},
		{
			name: "Background conversation (speech)",
			snapshot: &models.ContextSnapshot{
				BackgroundConvo: ptrStr("speech"),
			},
			expected: "currently in a conversation",
		},
		{
			name: "Background convo is noise (ignored)",
			snapshot: &models.ContextSnapshot{
				BackgroundConvo: ptrStr("noise"),
			},
			expected: "",
		},
		{
			name:     "All fields nil",
			snapshot: &models.ContextSnapshot{},
			expected: "",
		},
	}

	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			result := inferActivity(tt.snapshot)
			if result != tt.expected {
				t.Errorf("inferActivity() = %q, want %q", result, tt.expected)
			}
		})
	}
}

// TestInferCurrentTime tests the inferCurrentTime function for various time scenarios
func TestInferCurrentTime(t *testing.T) {
	tests := []struct {
		name     string
		snapshot *models.ContextSnapshot
		expected string
	}{
		{
			name: "No HourOfDay, fallback to TS",
			snapshot: &models.ContextSnapshot{
				TS: ptrStr("2024-01-01 08:30"),
			},
			expected: "2024-01-01 08:30",
		},
		{
			name:     "No HourOfDay and no TS",
			snapshot: &models.ContextSnapshot{},
			expected: "",
		},
		{
			name: "Morning (hour 5 boundary)",
			snapshot: &models.ContextSnapshot{
				HourOfDay: ptrInt(5),
			},
			expected: "morning (5:00)",
		},
		{
			name: "Morning (hour 11)",
			snapshot: &models.ContextSnapshot{
				HourOfDay: ptrInt(11),
			},
			expected: "morning (11:00)",
		},
		{
			name: "Afternoon (hour 12 boundary)",
			snapshot: &models.ContextSnapshot{
				HourOfDay: ptrInt(12),
			},
			expected: "afternoon (12:00)",
		},
		{
			name: "Afternoon (hour 16)",
			snapshot: &models.ContextSnapshot{
				HourOfDay: ptrInt(16),
			},
			expected: "afternoon (16:00)",
		},
		{
			name: "Evening (hour 17 boundary)",
			snapshot: &models.ContextSnapshot{
				HourOfDay: ptrInt(17),
			},
			expected: "evening (17:00)",
		},
		{
			name: "Evening (hour 20)",
			snapshot: &models.ContextSnapshot{
				HourOfDay: ptrInt(20),
			},
			expected: "evening (20:00)",
		},
		{
			name: "Night (hour 0)",
			snapshot: &models.ContextSnapshot{
				HourOfDay: ptrInt(0),
			},
			expected: "night (0:00)",
		},
		{
			name: "Night (hour 23)",
			snapshot: &models.ContextSnapshot{
				HourOfDay: ptrInt(23),
			},
			expected: "night (23:00)",
		},
		{
			name: "Morning on non-working day",
			snapshot: &models.ContextSnapshot{
				HourOfDay:  ptrInt(10),
				WorkingDay: ptrInt(0),
			},
			expected: "morning (10:00) on a non-working day",
		},
		{
			name: "Morning on working day",
			snapshot: &models.ContextSnapshot{
				HourOfDay:  ptrInt(10),
				WorkingDay: ptrInt(1),
			},
			expected: "morning (10:00)",
		},
	}

	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			result := inferCurrentTime(tt.snapshot)
			if result != tt.expected {
				t.Errorf("inferCurrentTime() = %q, want %q", result, tt.expected)
			}
		})
	}
}
func TestPtrHelpers(t *testing.T) {
    s := "hello"
    i := int64(42)
    if ptrStr(s) == nil || *ptrStr(s) != s {
        t.Errorf("ptrStr failed")
    }
    if ptrInt(i) == nil || *ptrInt(i) != i {
        t.Errorf("ptrInt failed")
    }
}

// TestInferExpectedResponseTime tests expected response time inference from event timing
func TestInferExpectedResponseTime(t *testing.T) {
	tests := []struct {
		name     string
		snapshot *models.ContextSnapshot
		expected string
	}{
		{
			name: "30 minutes remaining in event",
			snapshot: &models.ContextSnapshot{
				EventTimeLeft: ptrInt(1800000), // 30 minutes in milliseconds
			},
			expected: "approximately 30 minutes",
		},
		{
			name: "Very short time remaining",
			snapshot: &models.ContextSnapshot{
				EventTimeLeft: ptrInt(100), // rounds to 0
			},
			expected: "shortly",
		},
		{
			name: "Exactly 1 hour remaining",
			snapshot: &models.ContextSnapshot{
				EventTimeLeft: ptrInt(3600000), // 60 minutes
			},
			expected: "approximately 1 hour(s)",
		},
		{
			name: "1 hour 30 minutes remaining",
			snapshot: &models.ContextSnapshot{
				EventTimeLeft: ptrInt(5400000), // 90 minutes
			},
			expected: "approximately 1 hour(s) and 30 minutes",
		},
		{
			name: "2 hours 15 minutes remaining",
			snapshot: &models.ContextSnapshot{
				EventTimeLeft: ptrInt(8100000), // 135 minutes
			},
			expected: "approximately 2 hour(s) and 15 minutes",
		},
		{
			name: "No EventTimeLeft but calendar event active",
			snapshot: &models.ContextSnapshot{
				CalendarEvent: ptrInt(1),
			},
			expected: "after the current event (~30 minutes)",
		},
		{
			name:     "No EventTimeLeft and no calendar event",
			snapshot: &models.ContextSnapshot{},
			expected: "",
		},
	}

	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			result := inferExpectedResponseTime(tt.snapshot)
			if result != tt.expected {
				t.Errorf("inferExpectedResponseTime() = %q, want %q", result, tt.expected)
			}
		})
	}
}

// TestInferSenderRole tests sender role inference from notification app and contact info
func TestInferSenderRole(t *testing.T) {
	tests := []struct {
		name     string
		snapshot *models.ContextSnapshot
		expected string
	}{
		{
			name: "Known contact via WhatsApp",
			snapshot: &models.ContextSnapshot{
				NotifApp: ptrStr("com.whatsapp"),
				Key:      ptrStr("user123"),
			},
			expected: "known contact via WhatsApp",
		},
		{
			name: "Known contact via Telegram",
			snapshot: &models.ContextSnapshot{
				NotifApp: ptrStr("org.telegram.messenger"),
				Key:      ptrStr("user456"),
			},
			expected: "known contact via Telegram",
		},
		{
			name: "Unknown contact via Slack",
			snapshot: &models.ContextSnapshot{
				NotifApp: ptrStr("com.slack"),
			},
			expected: "contact via Slack",
		},
		{
			name: "Unknown app, known contact",
			snapshot: &models.ContextSnapshot{
				NotifApp: ptrStr("com.unknown.app"),
				Key:      ptrStr("user123"),
			},
			expected: "known contact via com.unknown.app",
		},
		{
			name: "No app, known contact",
			snapshot: &models.ContextSnapshot{
				Key: ptrStr("user123"),
			},
			expected: "known contact",
		},
		{
			name:     "Empty app and key",
			snapshot: &models.ContextSnapshot{},
			expected: "",
		},
	}

	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			result := inferSenderRole(tt.snapshot)
			if result != tt.expected {
				t.Errorf("inferSenderRole() = %q, want %q", result, tt.expected)
			}
		})
	}
}

// TestResolveAppName tests Android package name to app name mapping
func TestResolveAppName(t *testing.T) {
	tests := []struct {
		packageName string
		expected    string
	}{
		{"com.whatsapp", "WhatsApp"},
		{"com.facebook.messenger", "Facebook Messenger"},
		{"org.telegram.messenger", "Telegram"},
		{"com.google.android.gm", "Gmail"},
		{"com.microsoft.teams", "Microsoft Teams"},
		{"com.slack", "Slack"},
		{"com.instagram.android", "Instagram"},
		{"com.twitter.android", "Twitter"},
		{"com.snapchat.android", "Snapchat"},
		{"com.linkedin.android", "LinkedIn"},
		{"com.google.android.apps.messaging", "Google Messages"},
		{"com.unknown.package", "com.unknown.package"}, // fallback
		{"", ""}, // empty fallback
	}

	for _, tt := range tests {
		t.Run(tt.packageName, func(t *testing.T) {
			result := resolveAppName(tt.packageName)
			if result != tt.expected {
				t.Errorf("resolveAppName(%q) = %q, want %q", tt.packageName, result, tt.expected)
			}
		})
	}
}

// TestInferUrgency tests urgency scoring from various signals
func TestInferUrgency(t *testing.T) {
	tests := []struct {
		name     string
		snapshot *models.ContextSnapshot
		expected string
	}{
		{
			name: "All signals low/absent",
			snapshot: &models.ContextSnapshot{
				NotifCenterValue: ptrInt(1),
			},
			expected: "low",
		},
		{
			name: "2-4 notifications (score=1)",
			snapshot: &models.ContextSnapshot{
				NotifCenterValue: ptrInt(2),
			},
			expected: "low",
		},
		{
			name: "5+ notifications (score=2)",
			snapshot: &models.ContextSnapshot{
				NotifCenterValue: ptrInt(5),
			},
			expected: "medium",
		},
		{
			name: "5+ notifications + long silence (score=3)",
			snapshot: &models.ContextSnapshot{
				NotifCenterValue:            ptrInt(5),
				TimeSinceLastMessageSession: ptrInt(3700000), // > 1 hour
			},
			expected: "medium",
		},
		{
			name: "5+ notifications + conversation (score=3)",
			snapshot: &models.ContextSnapshot{
				NotifCenterValue: ptrInt(5),
				BackgroundConvo:  ptrStr("speech"),
			},
			expected: "medium",
		},
		{
			name: "5+ notifications + long silence + conversation (score=4)",
			snapshot: &models.ContextSnapshot{
				NotifCenterValue:            ptrInt(5),
				TimeSinceLastMessageSession: ptrInt(3700000),
				BackgroundConvo:             ptrStr("speech"),
			},
			expected: "high",
		},
		{
			name: "5+ notifications + DND=none (score=3)",
			snapshot: &models.ContextSnapshot{
				NotifCenterValue: ptrInt(5),
				DoNotDisturb:     ptrStr("none"),
			},
			expected: "medium",
		},
		{
			name: "5+ notifications + conversation + DND=none (score=4)",
			snapshot: &models.ContextSnapshot{
				NotifCenterValue: ptrInt(5),
				BackgroundConvo:  ptrStr("speech"),
				DoNotDisturb:     ptrStr("none"),
			},
			expected: "high",
		},
		{
			name: "Background convo=noise is ignored",
			snapshot: &models.ContextSnapshot{
				BackgroundConvo: ptrStr("noise"),
			},
			expected: "low",
		},
	}

	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			result := inferUrgency(tt.snapshot)
			if result != tt.expected {
				t.Errorf("inferUrgency() = %q, want %q", result, tt.expected)
			}
		})
	}
}

// TestSelectVariant tests prompt variant selection based on available elements
func TestSelectVariant(t *testing.T) {
	tests := []struct {
		name     string
		elements *InferredElements
		expected PromptVariant
	}{
		{
			name: "All 5 elements present → Standard",
			elements: &InferredElements{
				Activity:             "in a meeting",
				CurrentTime:          "morning (10:00)",
				SenderRole:           "known contact via Slack",
				Urgency:              "high",
				ExpectedResponseTime: "1 hour",
			},
			expected: VariantStandard,
		},
		{
			name: "4 elements (missing ResponseTime) → Rich",
			elements: &InferredElements{
				Activity:    "in a meeting",
				CurrentTime: "morning (10:00)",
				SenderRole:  "known contact via Slack",
				Urgency:     "high",
			},
			expected: VariantRich,
		},
		{
			name: "4 elements (missing Urgency) → Minimal",
			elements: &InferredElements{
				Activity:             "in a meeting",
				CurrentTime:          "morning (10:00)",
				SenderRole:           "known contact via Slack",
				ExpectedResponseTime: "1 hour",
			},
			expected: VariantMinimal,
		},
		{
			name: "Only Activity → Minimal",
			elements: &InferredElements{
				Activity: "in a meeting",
			},
			expected: VariantMinimal,
		},
		{
			name: "Activity + ResponseTime (Combo 6) → Minimal",
			elements: &InferredElements{
				Activity:             "in a meeting",
				ExpectedResponseTime: "1 hour",
			},
			expected: VariantMinimal,
		},
		{
			name: "Activity + Time (Combo 1) → Minimal",
			elements: &InferredElements{
				Activity:    "in a meeting",
				CurrentTime: "morning (10:00)",
			},
			expected: VariantMinimal,
		},
	}

	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			result := selectVariant(tt.elements)
			if result != tt.expected {
				t.Errorf("selectVariant() = %q, want %q", result, tt.expected)
			}
		})
	}
}

// TestInferFromSnapshot tests the full inference pipeline
func TestInferFromSnapshot(t *testing.T) {
	tests := []struct {
		name             string
		snapshot         *models.ContextSnapshot
		expectActivity   bool // should Activity be non-empty
		expectVariant    PromptVariant
		expectSenderRole bool
		expectUrgency    string
	}{
		{
			name: "Full context with calendar event and known contact",
			snapshot: &models.ContextSnapshot{
				CalendarEvent:               ptrInt(1),
				EventName:                   ptrStr("Team Sync"),
				EventTimeLeft:               ptrInt(1800000),
				HourOfDay:                   ptrInt(14),
				WorkingDay:                  ptrInt(1),
				NotifApp:                    ptrStr("com.slack"),
				Key:                         ptrStr("user123"),
				NotifCenterValue:            ptrInt(5),
				TimeSinceLastMessageSession: ptrInt(3700000),
			},
			expectActivity:   true,
			expectVariant:    VariantStandard,
			expectSenderRole: true,
		},
		{
			name: "On-call scenario",
			snapshot: &models.ContextSnapshot{
				OnCall:    ptrInt(1),
				HourOfDay: ptrInt(22),
			},
			expectActivity:   true,
			expectVariant:    VariantMinimal,
			expectSenderRole: false,
		},
		{
			name:           "Empty snapshot",
			snapshot:       &models.ContextSnapshot{},
			expectActivity: false,
		},
		{
			name: "Calendar event with minimal other context",
			snapshot: &models.ContextSnapshot{
				CalendarEvent: ptrInt(1),
				EventName:     ptrStr("1:1 Meeting"),
				EventTimeLeft: ptrInt(1200000), // 20 min
			},
			expectActivity:   true,
			expectVariant:    VariantMinimal,
			expectSenderRole: false,
		},
	}

	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			result := InferFromSnapshot(tt.snapshot)

			// Verify Activity inference
			if tt.expectActivity && result.Activity == "" {
				t.Errorf("InferFromSnapshot: expected non-empty Activity, got empty")
			}
			if !tt.expectActivity && result.Activity != "" {
				t.Errorf("InferFromSnapshot: expected empty Activity, got %q", result.Activity)
			}

			// Verify variant selection (if Activity is present)
			if tt.expectActivity && result.Variant != tt.expectVariant {
				t.Errorf("InferFromSnapshot: Variant = %q, want %q", result.Variant, tt.expectVariant)
			}

			// Verify SenderRole if expected
			if tt.expectSenderRole && result.SenderRole == "" {
				t.Errorf("InferFromSnapshot: expected non-empty SenderRole, got empty")
			}
			if !tt.expectSenderRole && result.SenderRole != "" {
				t.Errorf("InferFromSnapshot: expected empty SenderRole, got %q", result.SenderRole)
			}
		})
	}
}

// TestInferenceEdgeCases tests edge cases and boundary conditions
func TestInferenceEdgeCases(t *testing.T) {
	tests := []struct {
		name     string
		snapshot *models.ContextSnapshot
		check    func(*InferredElements) bool
		desc     string
	}{
		{
			name: "Priority: Calendar event beats predicted availability",
			snapshot: &models.ContextSnapshot{
				CalendarEvent:         ptrInt(1),
				EventName:             ptrStr("Meeting"),
				PredictedAvailability: ptrStr("0"), // Would normally mean unavailable
			},
			check: func(e *InferredElements) bool {
				return e.Activity == "in a scheduled event: Meeting"
			},
			desc: "Calendar event should take priority",
		},
		{
			name: "Priority: Predicted availability beats motion sensor",
			snapshot: &models.ContextSnapshot{
				PredictedAvailability: ptrStr("unavailable"),
				UserActType:           ptrStr("walking"),
			},
			check: func(e *InferredElements) bool {
				return e.Activity == "currently unavailable"
			},
			desc: "Predicted availability should beat motion sensor",
		},
		{
			name: "Priority: On-call beats motion sensor",
			snapshot: &models.ContextSnapshot{
				OnCall:      ptrInt(1),
				UserActType: ptrStr("running"),
			},
			check: func(e *InferredElements) bool {
				return e.Activity == "currently on a phone call"
			},
			desc: "On-call should take priority over motion",
		},
	}

	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			result := InferFromSnapshot(tt.snapshot)
			if !tt.check(result) {
				t.Errorf("InferFromSnapshot edge case failed: %s", tt.desc)
			}
		})
	}
}
