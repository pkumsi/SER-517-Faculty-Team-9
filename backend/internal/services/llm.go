package services

import (
	"context"
	"errors"
	"net/http"
	"time"

	openai "github.com/sashabaranov/go-openai"
)

// callLLM sends the constructed prompt to OpenRouter and returns the generated response.
// Both model and apiKey are passed in from the service layer — nothing is hardcoded or
// read from env here. All config comes from configs.LLMConfig via the handler.
//
// OpenRouter is OpenAI-compatible, so we reuse the OpenAI SDK by simply
// overriding the base URL to point to OpenRouter's API endpoint.
//
// A 30 second timeout is set on the HTTP client to prevent indefinite hanging
// if OpenRouter is slow or unavailable.
func callLLM(prompt string, model string, apiKey string) (string, error) {
	if apiKey == "" {
		return "", errors.New("OPENROUTER_API_KEY is not set")
	}
	if model == "" {
		return "", errors.New("LLM model is not set")
	}

	config := openai.DefaultConfig(apiKey)
	config.BaseURL = "https://openrouter.ai/api/v1"
	config.HTTPClient = &http.Client{
		Timeout: 30 * time.Second,
	}

	client := openai.NewClientWithConfig(config)

	resp, err := client.CreateChatCompletion(
		context.Background(),
		openai.ChatCompletionRequest{
			Model: model,
			Messages: []openai.ChatCompletionMessage{
				{
					Role:    openai.ChatMessageRoleUser,
					Content: prompt,
				},
			},
		},
	)
	if err != nil {
		return "", err
	}

	if len(resp.Choices) == 0 {
		return "", errors.New("no response returned from OpenRouter")
	}

	return resp.Choices[0].Message.Content, nil
}
