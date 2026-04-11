package services

import (
    "testing"
)

func TestSelectVariant(t *testing.T) {
    tests := []struct {
        name     string
        elements InferredElements
        expected PromptVariant
    }{
        {
            name: "All elements present (Standard)",
            elements: InferredElements{
                Activity:             "meeting",
                CurrentTime:          "morning",
                ExpectedResponseTime: "30 min",
                SenderRole:           "manager",
                Urgency:              "high",
            },
            expected: VariantStandard,
        },
        {
            name: "Four elements present (Rich)",
            elements: InferredElements{
                Activity:    "meeting",
                CurrentTime: "morning",
                SenderRole:  "manager",
                Urgency:     "high",
            },
            expected: VariantRich,
        },
        {
            name: "Only activity and response time (Minimal)",
            elements: InferredElements{
                Activity:             "meeting",
                ExpectedResponseTime: "30 min",
            },
            expected: VariantMinimal,
        },
    }

    for _, tt := range tests {
        t.Run(tt.name, func(t *testing.T) {
            got := selectVariant(&tt.elements)
            if got != tt.expected {
                t.Errorf("selectVariant() = %v, want %v", got, tt.expected)
            }
        })
    }
}