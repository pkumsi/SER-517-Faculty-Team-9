package services

import (
	"errors"
	
	"github.com/pkumsi/SER-517-Faculty-Team-9/backend/internal/models"
)

// GenerateAutoResponse is the single public entry point for the LLM response pipeline.
// It orchestrates the full flow established in Sprint 1 research and Task #25 (Prerana Kumsi):
//
//	Sensor Data → Inference → Prompt Assembly → LLM → Auto-Response
//
// Returns a populated LLMResponseResult or an error if the pipeline fails.
func GenerateAutoResponse(req *models.LLMResponseRequest) (*models.LLMResponseResult, error) {
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

	// Step 4 — Build the prompt from inferred elements
	// Selects Minimal / Standard / Rich variant based on available elements — Task #25
	prompt := buildPrompt(elements)

	// Step 5 — Call the LLM with the constructed prompt
	response, err := callLLM(prompt)
	if err != nil {
		return nil, err
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
