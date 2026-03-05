package handlers

import (
	"net/http"

	"github.com/gin-gonic/gin"
	"github.com/pkumsi/SER-517-Faculty-Team-9/backend/configs"
	"github.com/pkumsi/SER-517-Faculty-Team-9/backend/internal/models"
	"github.com/pkumsi/SER-517-Faculty-Team-9/backend/internal/services"
)

// LLMHandler holds the application config so that LLM settings
// flow from .env → configs.LLMConfig → handler → service → llm client.
type LLMHandler struct {
	cfg *configs.Config
}

// NewLLMHandler creates a new LLMHandler with the application config.
// Called once in main.go during startup.
func NewLLMHandler(cfg *configs.Config) *LLMHandler {
	return &LLMHandler{cfg: cfg}
}

// GenerateAutoResponse handles POST /api/v1/response
// It validates the incoming request, calls the LLM service pipeline,
// and returns the generated auto-response as JSON.
//
// Request body: models.LLMResponseRequest (JSON)
// Response body: models.LLMResponseResult (JSON)
func (h *LLMHandler) GenerateAutoResponse(c *gin.Context) {
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
	// Model and API key come from config, which reads from .env
	result, err := services.GenerateAutoResponse(&req, h.cfg.LLM.Model, h.cfg.LLM.OpenRouterAPIKey)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{
			"error": err.Error(),
		})
		return
	}

	c.JSON(http.StatusOK, result)
}
