package handlers

import (
	"net/http"

	"github.com/gin-gonic/gin"
	"github.com/pkumsi/SER-517-Faculty-Team-9/backend/internal/models"
	"github.com/pkumsi/SER-517-Faculty-Team-9/backend/internal/services"
)

// GenerateAutoResponse handles POST /api/v1/response
// It validates the incoming request, calls the LLM service pipeline,
// and returns the generated auto-response as JSON.
//
// Request body: models.models_input (JSON)
// Response body: models.models_output (JSON)
func GenerateAutoResponse(c *gin.Context) {
	var req models.LLMResponseRequest

	// Parse and validate JSON request body
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{
			"error": "invalid request body: " + err.Error(),
		})
		return
	}

	// Context snapshot is required — activity cannot be inferred without it
	if req.Context == nil {
		c.JSON(http.StatusBadRequest, gin.H{
			"error": "context snapshot is required",
		})
		return
	}

	// Call the LLM service pipeline:
	// inference → prompt → LLM → result
	result, err := services.GenerateAutoResponse(&req)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{
			"error": err.Error(),
		})
		return
	}

	c.JSON(http.StatusOK, result)
}
