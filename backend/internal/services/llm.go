package services

import (
	"context"
	"errors"
	"os"

	openai "github.com/sashabaranov/go-openai"
)

// callLLM sends the constructed prompt to OpenRouter and returns the generated response.
// Uses google/gemma-3-4b-it:free — lightweight free model via OpenRouter.
// API key is loaded from OPENROUTER_API_KEY environment variable — never hardcoded.
//
// OpenRouter is OpenAI-compatible, so we reuse the OpenAI SDK by simply
// overriding the base URL to point to OpenRouter's API endpoint.
func callLLM(prompt string) (string, error) {
	apiKey := os.Getenv("OPENROUTER_API_KEY")
	if apiKey == "" {
		return "", errors.New("OPENROUTER_API_KEY environment variable is not set")
	}

	config := openai.DefaultConfig(apiKey)
	config.BaseURL = "https://openrouter.ai/api/v1"

	client := openai.NewClientWithConfig(config)

	resp, err := client.CreateChatCompletion(
		context.Background(),
		openai.ChatCompletionRequest{
			Model: "google/gemma-3-4b-it:free",
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
