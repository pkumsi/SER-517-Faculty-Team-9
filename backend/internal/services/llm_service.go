package services

import (
	"errors"
	"fmt"
	"log"

	"github.com/pkumsi/SER-517-Faculty-Team-9/backend/internal/models"
)

// GenerateAutoResponse builds a prompt and calls the LLM. If exposeLLMErrors is true
// (development), LLM failures return a wrapped error so the handler can surface
// provider messages (e.g. 401 Invalid API Key) for debugging.
func GenerateAutoResponse(req *models.LLMResponseRequest, model string, apiKey string, baseURL string, exposeLLMErrors bool) (*models.LLMResponseResult, error) {
	if req == nil {
		return nil, errors.New("request is nil")
	}
	if req.Context == nil {
		return nil, errors.New("context snapshot is required")
	}

	prompt := buildPrompt(req.Context, req.Rules)
	response, err := callLLM(prompt, model, apiKey, baseURL)
	if err != nil {
		log.Printf("LLM call failed: %v", err)
		return nil, llmFailureReturn(err, exposeLLMErrors)
	}

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

func llmFailureReturn(cause error, expose bool) error {
	if expose {
		return fmt.Errorf("LLM provider error: %v", cause)
	}
	return errors.New("auto-response generation failed. The AI service is currently unavailable or took too long to respond. Please try again in a few seconds")
}
