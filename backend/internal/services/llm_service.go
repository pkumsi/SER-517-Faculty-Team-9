package services

import (
	"context"
	"errors"
	"log"

	"github.com/pkumsi/SER-517-Faculty-Team-9/backend/internal/cache"
	"github.com/pkumsi/SER-517-Faculty-Team-9/backend/internal/models"
)

func GenerateAutoResponse(req *models.LLMResponseRequest, model string, apiKey string, c *cache.RedisCache) (*models.LLMResponseResult, error) {
	// Step 1 — Validate request
	// Activity is the single most critical element — Task #23 Context Selection Guidelines:
	// "Activity — Always required. If unavailable, do not generate a response."
	if req == nil {
		return nil, errors.New("request is nil")
	}
	if req.Context == nil {
		return nil, errors.New("context snapshot is required")
	}

	// Step 2 — Infer the 5 essential elements from raw sensor data
	// Raw sensor values never leave this step — Task #23, Task #25 Section 6
	elements := InferFromSnapshot(req.Context)

	// Step 3 — Validate that activity was successfully inferred
	// Without activity the response would be vague and unjustified — Task #19 T1
	if elements.Activity == "" {
		return nil, errors.New("unable to infer user activity from context — cannot generate response")
	}

	var response string
	if c != nil {
		cacheKey := cache.BuildKey(
			elements.Activity,
			elements.CurrentTime,
			elements.SenderRole,
			elements.Urgency,
			elements.ExpectedResponseTime,
			string(elements.Variant),
		)
		if cached, ok := c.Get(context.Background(), cacheKey); ok {
			log.Printf("Cache hit for key %s", cacheKey)
			response = cached
		} else {
			prompt := buildPrompt(elements)
			var err error
			response, err = callLLM(prompt, model, apiKey)
			if err != nil {
				return nil, errors.New("auto-response generation failed. The AI service is currently unavailable or took too long to respond. Please try again in a few seconds")
			}
			if setErr := c.Set(context.Background(), cacheKey, response); setErr != nil {
				log.Printf("Cache set failed: %v", setErr)
			}
		}
	} else {
		prompt := buildPrompt(elements)
		var err error
		response, err = callLLM(prompt, model, apiKey)
		if err != nil {
			return nil, errors.New("auto-response generation failed. The AI service is currently unavailable or took too long to respond. Please try again in a few seconds")
		}
	}

	// Step 6 — Assemble and return LLMResponseResult
	arEnabled := true
	sentAR := true

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
