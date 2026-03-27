package handlers

import (
	"net/http"
	"net/http/httptest"
	"testing"

	"github.com/gin-gonic/gin"
	"github.com/pkumsi/SER-517-Faculty-Team-9/backend/internal/cache"
)

func TestStatisticsHandler_GetMessageStatistics(t *testing.T) {
	// Setup
	gin.SetMode(gin.TestMode)
	
	// Test with nil cache (simulating Redis not configured)
	handler := NewStatisticsHandler(nil)
	
	// Test cases
	tests := []struct {
		name           string
		queryParams    string
		expectedStatus int
	}{
		{
			name:           "nil cache - service unavailable",
			queryParams:    "",
			expectedStatus: http.StatusServiceUnavailable,
		},
		{
			name:           "nil cache with days param",
			queryParams:    "?days=3",
			expectedStatus: http.StatusServiceUnavailable,
		},
	}
	
	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			// Create request
			req, _ := http.NewRequest("GET", "/api/v1/statistics/messages"+tt.queryParams, nil)
			w := httptest.NewRecorder()
			
			// Create Gin context
			c, _ := gin.CreateTestContext(w)
			c.Request = req
			
			// Call handler
			handler.GetMessageStatistics(c)
			
			// Check status
			if w.Code != tt.expectedStatus {
				t.Errorf("Expected status %d, got %d", tt.expectedStatus, w.Code)
			}
		})
	}
}

func TestStatisticsHandler_GetMessageStatistics_ParameterValidation(t *testing.T) {
	// Setup
	gin.SetMode(gin.TestMode)
	
	// Mock cache that doesn't do anything (we won't call Redis methods)
	mockCache := &cache.RedisCache{} // This will be nil in practice, but we test parameter validation first
	
	handler := NewStatisticsHandler(mockCache)
	
	// Test parameter validation (these should fail before Redis calls)
	tests := []struct {
		name           string
		queryParams    string
		expectedStatus int
	}{
		{
			name:           "invalid days parameter",
			queryParams:    "?days=invalid",
			expectedStatus: http.StatusBadRequest,
		},
		{
			name:           "negative days",
			queryParams:    "?days=-1",
			expectedStatus: http.StatusBadRequest,
		},
		{
			name:           "zero days",
			queryParams:    "?days=0",
			expectedStatus: http.StatusBadRequest,
		},
		{
			name:           "too many days",
			queryParams:    "?days=400",
			expectedStatus: http.StatusBadRequest,
		},
	}
	
	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			// Create request
			req, _ := http.NewRequest("GET", "/api/v1/statistics/messages"+tt.queryParams, nil)
			w := httptest.NewRecorder()
			
			// Create Gin context
			c, _ := gin.CreateTestContext(w)
			c.Request = req
			
			// Call handler
			handler.GetMessageStatistics(c)
			
			// Check status
			if w.Code != tt.expectedStatus {
				t.Errorf("Expected status %d, got %d", tt.expectedStatus, w.Code)
			}
		})
	}
}