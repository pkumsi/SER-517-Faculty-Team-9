package main

import (
	"log"
	"net/http"

	"github.com/Depado/ginprom"
	"github.com/gin-gonic/gin"
	"github.com/pkumsi/SER-517-Faculty-Team-9/backend/configs"
	"github.com/pkumsi/SER-517-Faculty-Team-9/backend/internal/handlers"
	"github.com/pkumsi/SER-517-Faculty-Team-9/backend/internal/logger"
)

func main() {
	logFile, err := logger.Init("api.log")
	if err != nil {
		log.Fatalf("Failed to open log file: %v", err)
	}
	defer logFile.Close()

	// Load configuration
	cfg, err := configs.LoadConfig()
	if err != nil {
		log.Fatalf("Failed to load configuration: %v", err)
	}

	// Debug info
	log.Println("Starting API Server")
	log.Printf("Environment: %s", cfg.Server.Environment)
	log.Printf("Server: http://%s", cfg.GetServerAddress())
	log.Printf("Log Level: %s", cfg.Logging.Level)
	log.Printf("LLM Model: %s", cfg.LLM.Model)
	log.Printf("Metrics Enabled: %v", cfg.Metrics.Enabled)

	r := gin.Default()

	// Add Prometheus middleware
	p := ginprom.New(
		ginprom.Engine(r),
		ginprom.Path("/metrics"), // Expose metrics at /metrics
	)
	r.Use(p.Instrument())

	r.GET("/", func(c *gin.Context) {
		log.Println("Root endpoint hit")
		c.JSON(http.StatusOK, gin.H{"message": "API running"})
	})

	r.GET("/health", func(c *gin.Context) {
		log.Println("Health endpoint hit")
		c.JSON(http.StatusOK, gin.H{"status": "ok"})
	})

	// Instantiate handler with config so LLM settings flow from
	// .env → configs.LLMConfig → handler → service → llm client
	llmHandler := handlers.NewLLMHandler(cfg)
	feedbackHandler := handlers.NewFeedbackHandler()

	// LLM response generation endpoint
	// POST /api/v1/response
	// Accepts LLMResponseRequest, returns LLMResponseResult
	v1 := r.Group("/api/v1")
	{
		v1.POST("/response", llmHandler.GenerateAutoResponse)
		v1.POST("/feedback", feedbackHandler.SubmitFeedback)
	}

	serverAddr := ":" + cfg.Server.Port

	log.Printf("Listening on %s", serverAddr)
	log.Printf("Endpoints:")
	log.Printf("  GET  http://localhost%s/", serverAddr)
	log.Printf("  GET  http://localhost%s/health", serverAddr)
	log.Printf("  POST http://localhost%s/api/v1/response", serverAddr)
	log.Printf("  POST http://localhost%s/api/v1/feedback", serverAddr)
	log.Printf("OpenRouter API Key loaded: %v", cfg.LLM.OpenRouterAPIKey != "")
	log.Printf("LLM Model: %s", cfg.LLM.Model)

	// Start Gin server
	if err := r.Run(serverAddr); err != nil {
		log.Fatalf("Server failed to start: %v", err)
	}
}
