package main

import (
	"fmt"
	"log"
	"net/http"

	"github.com/gin-gonic/gin"
	"github.com/pkumsi/SER-517-Faculty-Team-9/backend/configs"
)

func main() {
	// Load configuration
	cfg, err := configs.LoadConfig()
	if err != nil {
		log.Fatalf("Failed to load configuration: %v", err)
	}

	// Debug info
	fmt.Printf("\n Starting API Server\n")
	fmt.Printf("Environment: %s\n", cfg.Server.Environment)
	fmt.Printf("Server: http://%s\n", cfg.GetServerAddress())
	fmt.Printf("Log Level: %s\n", cfg.Logging.Level)
	fmt.Printf("OpenAI Model: %s\n", cfg.OpenAI.Model)
	fmt.Printf("Metrics Enabled: %v\n\n", cfg.Metrics.Enabled)

	r := gin.Default()

	r.GET("/", func(c *gin.Context) {
		c.JSON(http.StatusOK, gin.H{"message": "API running"})
	})

	r.GET("/health", func(c *gin.Context) {
		c.JSON(http.StatusOK, gin.H{"status": "ok"})
	})

	r.Run()

	serverAddr := ":" + cfg.Server.Port

	fmt.Printf("Listening on %s\n", serverAddr)
	fmt.Printf("Endpoints:\n")
	fmt.Printf("  GET  http://localhost%s/\n", serverAddr)
	fmt.Printf("  GET  http://localhost%s/health\n", serverAddr)

	// Start Gin server
	if err := r.Run(serverAddr); err != nil {
		log.Fatalf("Server failed to start: %v", err)
	}
}
