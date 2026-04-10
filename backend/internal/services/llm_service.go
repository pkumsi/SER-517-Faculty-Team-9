package services

import (
	"encoding/json"
	"errors"
	"fmt"
	"log"
	"strings"

	"github.com/pkumsi/SER-517-Faculty-Team-9/backend/internal/models"
)

type llmJSONResponse struct {
	Response    string `json:"response"`
	Explanation string `json:"explanation"`
}

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
	raw, err := callLLM(prompt, model, apiKey, baseURL)
	if err != nil {
		log.Printf("LLM call failed: %v", err)
		return nil, llmFailureReturn(err, exposeLLMErrors)
	}

	parsed := parseLLMResponse(raw)

	arEnabled := true
	sentAR := true
	explanation := parsed.Explanation

	return &models.LLMResponseResult{
		RequestID:             req.RequestID,
		UUID:                  req.Context.UUID,
		Key:                   req.Context.Key,
		TSRaw:                 req.Context.TSRaw,
		AREnabled:             &arEnabled,
		SentAR:                &sentAR,
		PredictedAvailability: req.Context.PredictedAvailability,
		Responses:             &[]string{parsed.Response},
		Explainability:        &models.Explainability{ShapRaw: &explanation},
	}, nil
}

// parseLLMResponse extracts the response and explanation from the LLM's JSON output.
// Falls back to using the raw string as the response if JSON parsing fails.
func parseLLMResponse(raw string) llmJSONResponse {
	raw = strings.TrimSpace(raw)
	// Strip markdown code fences if the model wrapped the JSON
	if strings.HasPrefix(raw, "```") {
		if idx := strings.Index(raw, "\n"); idx != -1 {
			raw = raw[idx+1:]
		}
		raw = strings.TrimSuffix(strings.TrimSpace(raw), "```")
		raw = strings.TrimSpace(raw)
	}
	var result llmJSONResponse
	if err := json.Unmarshal([]byte(raw), &result); err != nil {
		log.Printf("LLM response was not valid JSON, using raw text: %v", err)
		result.Response = raw
	}
	return result
}

func llmFailureReturn(cause error, expose bool) error {
	if expose {
		return fmt.Errorf("LLM provider error: %v", cause)
	}
	return errors.New("auto-response generation failed. The AI service is currently unavailable or took too long to respond. Please try again in a few seconds")
}
