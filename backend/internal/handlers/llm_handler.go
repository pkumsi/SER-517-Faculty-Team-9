package handlers

import (
	"net/http"

	"github.com/gin-gonic/gin"
	"github.com/pkumsi/SER-517-Faculty-Team-9/backend/configs"
	"github.com/pkumsi/SER-517-Faculty-Team-9/backend/internal/cache"
	"github.com/pkumsi/SER-517-Faculty-Team-9/backend/internal/models"
	"github.com/pkumsi/SER-517-Faculty-Team-9/backend/internal/services"
)

type LLMHandler struct {
	cfg   *configs.Config
	cache *cache.RedisCache
}

func NewLLMHandler(cfg *configs.Config, c *cache.RedisCache) *LLMHandler {
	return &LLMHandler{cfg: cfg, cache: c}
}

func (h *LLMHandler) GenerateAutoResponse(c *gin.Context) {
	var req models.LLMResponseRequest

	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{
			"error": "invalid request body: " + err.Error(),
		})
		return
	}

	if req.Context == nil {
		c.JSON(http.StatusBadRequest, gin.H{
			"error": "context snapshot is required",
		})
		return
	}

	result, err := services.GenerateAutoResponse(&req, h.cfg.LLM.Model, h.cfg.LLM.OpenRouterAPIKey, h.cache)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{
			"error": err.Error(),
		})
		return
	}

	c.JSON(http.StatusOK, result)
}
