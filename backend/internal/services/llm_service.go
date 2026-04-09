package services

import (
	"errors"
	"log"

	"github.com/pkumsi/SER-517-Faculty-Team-9/backend/internal/models"
)

func GenerateAutoResponse(req *models.LLMResponseRequest, model string, apiKey string) (*models.LLMResponseResult, error) {
	if req == nil {
		return nil, errors.New("request is nil")
	}
	if req.Context == nil {
		return nil, errors.New("context snapshot is required")
	}

	prompt := buildPrompt(req.Context, req.Rules)
	response, err := callLLM(prompt, model, apiKey)
	if err != nil {
		log.Printf("LLM call failed: %v", err)
		return nil, errors.New("auto-response generation failed. The AI service is currently unavailable or took too long to respond. Please try again in a few seconds")
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
