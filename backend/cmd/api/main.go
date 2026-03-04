package main

import (
	"net/http"

	"github.com/gin-gonic/gin"
	"github.com/pkumsi/SER-517-Faculty-Team-9/backend/internal/handlers"
)

func main() {
	r := gin.Default()

	r.GET("/health", func(c *gin.Context) {
		c.JSON(http.StatusOK, gin.H{"status": "ok"})
	})

	r.GET("/", func(c *gin.Context) {
		c.JSON(http.StatusOK, gin.H{"message": "API running"})
	})

	// LLM response generation endpoint
	// POST /api/v1/response
	// Accepts LLMResponseRequest, returns LLMResponseResult
	v1 := r.Group("/api/v1")
	{
		v1.POST("/response", handlers.GenerateAutoResponse)
	}

	r.Run()
}
