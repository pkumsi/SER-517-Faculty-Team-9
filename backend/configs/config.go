package configs

import (
	"fmt"
	"log"
	"os"
	"strconv"
	"time"

	"github.com/joho/godotenv"
)

// struct for all configuration for the application
type Config struct {
	Server  ServerConfig
	Logging LoggingConfig
	API     APIConfig
	// OpenAI   OpenAIConfig
	LLM     LLMConfig
	Metrics MetricsConfig
}

// struct for server-related configuration
type ServerConfig struct {
	Port        string
	Host        string
	Environment string
}

// struct for logging configuration
type LoggingConfig struct {
	Level  string
	Format string // "json" or "text"
}

// struct API-related configuration
type APIConfig struct {
	Version string
	Timeout time.Duration
}

// struct for OpenAI integration settings - can be changed to differnent AI provider in the future if needed
//
//	type OpenAIConfig struct {
//		APIKey    string
//		Model     string
//		MaxTokens int
//		OpenRouterAPIKey string
//	}
type LLMConfig struct {
	OpenRouterAPIKey string
	Model            string
	MaxTokens        int
}

// struct for monitoring configuration
type MetricsConfig struct {
	Enabled bool
	Port    string
}

// LoadConfig loads configuration from environment variables
func LoadConfig() (*Config, error) {
	// Load .env file if it exists (for local development)
	// In production, environment variables should be set by the system
	if err := godotenv.Load("/Users/pshar169/Documents/SER-517-Faculty-Team-9/backend/configs/.env"); err != nil {
		log.Println("No .env file found, using system environment variables")
	}

	config := &Config{
		Server: ServerConfig{
			Port:        getEnv("PORT", "8080"),
			Host:        getEnv("HOST", "localhost"),
			Environment: getEnv("ENVIRONMENT", "development"),
		},
		Logging: LoggingConfig{
			Level:  getEnv("LOG_LEVEL", "info"),
			Format: getEnv("LOG_FORMAT", "json"),
		},
		API: APIConfig{
			Version: getEnv("API_VERSION", "v1"),
			Timeout: getEnvAsDuration("API_TIMEOUT", 30*time.Second),
		},
		// OpenAI: OpenAIConfig{
		// 	APIKey:    getEnv("OPENAI_API_KEY", ""),
		// 	Model:     getEnv("OPENAI_MODEL", "gpt-4"),
		// 	MaxTokens: getEnvAsInt("OPENAI_MAX_TOKENS", 1000),
		// 	OpenRouterAPIKey: getEnv("OPENROUTER_API_KEY", ""),
		// },
		LLM: LLMConfig{
			OpenRouterAPIKey: getEnv("OPENROUTER_API_KEY", ""),
			Model:            getEnv("LLM_MODEL", "google/gemma-3-4b-it:free"),
			MaxTokens:        getEnvAsInt("LLM_MAX_TOKENS", 1000),
		},
		Metrics: MetricsConfig{
			Enabled: getEnvAsBool("ENABLE_METRICS", true),
			Port:    getEnv("METRICS_PORT", "9090"),
		},
	}

	// Validate critical configuration
	if err := config.Validate(); err != nil {
		return nil, err
	}

	return config, nil
}

// Validate checks if required configuration values are set
func (c *Config) Validate() error {
	// right it only validates open router api key because that what we are using we can later add validation for other llm providers if we add support for them in the future
	if c.LLM.OpenRouterAPIKey == "" {
		return fmt.Errorf("OPENROUTER_API_KEY is required but not set")
	}

	// Validate log level
	validLogLevels := map[string]bool{
		"debug": true,
		"info":  true,
		"warn":  true,
		"error": true,
	}
	if !validLogLevels[c.Logging.Level] {
		return fmt.Errorf("invalid LOG_LEVEL: %s (must be debug, info, warn, or error)", c.Logging.Level)
	}

	// Validate environment
	validEnvs := map[string]bool{
		"development": true,
		"staging":     true,
		"production":  true,
	}
	if !validEnvs[c.Server.Environment] {
		return fmt.Errorf("invalid ENVIRONMENT: %s (must be development, staging, or production)", c.Server.Environment)
	}

	return nil
}

// returns true if running in development mode
func (c *Config) IsDevelopment() bool {
	return c.Server.Environment == "development"
}

// returns true if running in production mode
func (c *Config) IsProduction() bool {
	return c.Server.Environment == "production"
}

// returns the full server address
func (c *Config) GetServerAddress() string {
	return c.Server.Host + ":" + c.Server.Port
}

// Helper functions to get environment variables with defaults

func getEnv(key, defaultValue string) string {
	value := os.Getenv(key)
	if value == "" {
		return defaultValue
	}
	return value
}

func getEnvAsInt(key string, defaultValue int) int {
	valueStr := os.Getenv(key)
	if valueStr == "" {
		return defaultValue
	}
	value, err := strconv.Atoi(valueStr)
	if err != nil {
		log.Printf("Warning: Invalid integer value for %s, using default: %d", key, defaultValue)
		return defaultValue
	}
	return value
}

func getEnvAsBool(key string, defaultValue bool) bool {
	valueStr := os.Getenv(key)
	if valueStr == "" {
		return defaultValue
	}
	value, err := strconv.ParseBool(valueStr)
	if err != nil {
		log.Printf("Warning: Invalid boolean value for %s, using default: %t", key, defaultValue)
		return defaultValue
	}
	return value
}

func getEnvAsDuration(key string, defaultValue time.Duration) time.Duration {
	valueStr := os.Getenv(key)
	if valueStr == "" {
		return defaultValue
	}
	value, err := time.ParseDuration(valueStr)
	if err != nil {
		log.Printf("Warning: Invalid duration value for %s, using default: %s", key, defaultValue)
		return defaultValue
	}
	return value
}
