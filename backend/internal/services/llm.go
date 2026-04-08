package services

import (
	"context"
	"errors"
	"net/http"
	"time"

	openai "github.com/sashabaranov/go-openai"
)

// callLLM sends the prompt to an OpenAI-compatible API (default: Groq).
// baseURL, model, and apiKey come from configs.LLMConfig via the handler.
func callLLM(prompt string, model string, apiKey string, baseURL string) (string, error) {
	if apiKey == "" {
		return "", errors.New("GROQ_API_KEY is not set")
	}
	if model == "" {
		return "", errors.New("LLM model is not set")
	}
	if baseURL == "" {
		return "", errors.New("LLM base URL is not set")
	}

	config := openai.DefaultConfig(apiKey)
	config.BaseURL = baseURL
	config.HTTPClient = &http.Client{
		Timeout: 90 * time.Second,
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
		return "", errors.New("no response returned from LLM provider")
	}

	return resp.Choices[0].Message.Content, nil
}
