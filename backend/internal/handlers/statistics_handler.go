package handlers

import (
	"net/http"
	"strconv"

	"github.com/gin-gonic/gin"
	"github.com/pkumsi/SER-517-Faculty-Team-9/backend/internal/cache"
)

type StatisticsHandler struct {
	cache *cache.RedisCache
}

func NewStatisticsHandler(c *cache.RedisCache) *StatisticsHandler {
	return &StatisticsHandler{cache: c}
}

// GetMessageStatistics returns daily message statistics
// GET /api/v1/statistics/messages?days=7
func (h *StatisticsHandler) GetMessageStatistics(c *gin.Context) {
	if h.cache == nil {
		c.JSON(http.StatusServiceUnavailable, gin.H{
			"error": "statistics service unavailable - Redis cache not configured",
		})
		return
	}

	// Parse days parameter, default to 7 days
	daysStr := c.DefaultQuery("days", "7")
	days, err := strconv.Atoi(daysStr)
	if err != nil || days <= 0 || days > 365 {
		c.JSON(http.StatusBadRequest, gin.H{
			"error": "invalid days parameter - must be a number between 1 and 365",
		})
		return
	}

	stats, err := h.cache.GetDailyMessageCounts(c.Request.Context(), days)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{
			"error": "failed to retrieve statistics: " + err.Error(),
		})
		return
	}

	c.JSON(http.StatusOK, gin.H{
		"statistics": stats,
		"days":       days,
	})
}