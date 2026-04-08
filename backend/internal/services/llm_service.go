package services

import (
	"context"
	"encoding/json"
	"errors"
	"fmt"
	"log"

	"github.com/pkumsi/SER-517-Faculty-Team-9/backend/internal/cache"
	"github.com/pkumsi/SER-517-Faculty-Team-9/backend/internal/models"
)

// GenerateAutoResponse builds a prompt and calls the LLM. If exposeLLMErrors is true
// (development), LLM failures return a wrapped error so the handler can surface
// provider messages (e.g. 401 Invalid API Key) for debugging.
func GenerateAutoResponse(req *models.LLMResponseRequest, model string, apiKey string, baseURL string, c *cache.RedisCache, exposeLLMErrors bool) (*models.LLMResponseResult, error) {
	if req == nil {
		return nil, errors.New("request is nil")
	}
	if req.Context == nil {
		return nil, errors.New("context snapshot is required")
	}

	var response string
	if c != nil {
		contextJSON, _ := json.Marshal(req.Context)
		cacheKey := cache.BuildKeyFromRaw(string(contextJSON))
		if cached, ok := c.Get(context.Background(), cacheKey); ok {
			log.Printf("Cache hit for key %s", cacheKey)
			response = cached
		} else {
			prompt := buildPrompt(req.Context, req.Rules)
			var err error
			response, err = callLLM(prompt, model, apiKey, baseURL)
			if err != nil {
				log.Printf("LLM call failed: %v", err)
				return nil, llmFailureReturn(err, exposeLLMErrors)
			}
			if setErr := c.Set(context.Background(), cacheKey, response); setErr != nil {
				log.Printf("Cache set failed: %v", setErr)
			}
		}
	} else {
		prompt := buildPrompt(req.Context, req.Rules)
		var err error
		response, err = callLLM(prompt, model, apiKey, baseURL)
		if err != nil {
			log.Printf("LLM call failed: %v", err)
			return nil, llmFailureReturn(err, exposeLLMErrors)
		}
	}

	// Step 6 — Assemble and return LLMResponseResult
	arEnabled := true
	sentAR := true

	// Increment message statistics if cache is available
	if c != nil {
		if err := c.IncrementMessageCount(context.Background()); err != nil {
			log.Printf("Failed to increment message count: %v", err)
		}
	}

	return &models.LLMResponseResult{
		RequestID:             req.RequestID,
		UUID:                  req.Context.UUID,
		Key:                   req.Context.Key,
		TSRaw:                 req.Context.TSRaw,
		AREnabled:             &arEnabled,
		SentAR:                &sentAR,
		PredictedAvailability: req.Context.PredictedAvailability,
		Responses:             &[]string{response},
	}, nil
}

func llmFailureReturn(cause error, expose bool) error {
	if expose {
		return fmt.Errorf("LLM provider error: %v", cause)
	}
	return errors.New("auto-response generation failed. The AI service is currently unavailable or took too long to respond. Please try again in a few seconds")
}
