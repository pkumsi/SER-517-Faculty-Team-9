package handlers

import (
	"net/http"
	"strings"

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

	result, err := services.GenerateAutoResponse(
		&req,
		h.cfg.LLM.Model,
		h.cfg.LLM.APIKey,
		h.cfg.LLM.BaseURL,
		h.cache,
		h.cfg.IsDevelopment(),
	)
	if err != nil {
		body := gin.H{"error": err.Error()}
		if h.cfg.IsDevelopment() {
			body["llm_base_url"] = h.cfg.LLM.BaseURL
			body["llm_model"] = h.cfg.LLM.Model
			body["api_key_set"] = h.cfg.LLM.APIKey != ""
			body["api_key_prefix"] = apiKeyPrefix(h.cfg.LLM.APIKey)
		}
		c.JSON(http.StatusInternalServerError, body)
		return
	}

	c.JSON(http.StatusOK, result)
}

func apiKeyPrefix(key string) string {
	key = strings.TrimSpace(key)
	if key == "" {
		return "(empty)"
	}
	n := 7
	if len(key) < n {
		n = len(key)
	}
	return key[:n] + "…"
}
