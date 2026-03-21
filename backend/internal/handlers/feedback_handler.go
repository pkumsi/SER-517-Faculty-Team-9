package handlers

import (
	"net/http"

	"github.com/gin-gonic/gin"
	"github.com/pkumsi/SER-517-Faculty-Team-9/backend/internal/models"
)

// FeedbackHandler handles feedback submission (like/dislike) for responses.
type FeedbackHandler struct{}

func NewFeedbackHandler() *FeedbackHandler {
	return &FeedbackHandler{}
}

// FeedbackRequest represents the expected JSON body for feedback.
type FeedbackRequest struct {
	RequestID string `json:"request_id"`
	UUID      string `json:"uuid"`
	Like      bool   `json:"like"` // true = like, false = dislike
}

// POST /api/v1/feedback
func (h *FeedbackHandler) SubmitFeedback(c *gin.Context) {
	var req FeedbackRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "invalid request body: " + err.Error()})
		return
	}

	// Here you would persist feedback to a database or log it
	// For now, just return success
	feedback := &models.ResponseFeedback{}
	if req.Like {
		feedback.Appropriate = ptrString("yes")
	} else {
		feedback.Appropriate = ptrString("no")
	}

	c.JSON(http.StatusOK, gin.H{"status": "feedback received", "feedback": feedback})
}

func ptrString(s string) *string {
	return &s
}
