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
