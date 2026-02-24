package models

type LLMResponseResult struct {
	RequestID *string `json:"request_id,omitempty"`
	UUID      *string `json:"uuid,omitempty"`
	Key       *string `json:"key,omitempty"`
	TSRaw     *int64  `json:"ts_raw,omitempty"`

	AREnabled *bool `json:"ar_enabled,omitempty"`
	SentAR    *bool `json:"sent_ar,omitempty"`

	PredictedAvailability *string  `json:"predicted_availability,omitempty"`
	Responses             *[]string `json:"responses,omitempty"`

	Feedback       *ResponseFeedback `json:"feedback,omitempty"`
	Explainability *Explainability    `json:"explainability,omitempty"`
}

type ResponseFeedback struct {
	AttendTime             *int64  `json:"Attend_time,omitempty"`
	Appropriate            *string `json:"Appropriate,omitempty"`
	Useful                 *string `json:"Useful,omitempty"`
	Granularity            *string `json:"Granularity,omitempty"`
	ExplanationCorrectness *string `json:"Explanation_Correctness,omitempty"`
	Comfortable            *string `json:"Comfortable,omitempty"`
	Reason                 *string `json:"Reason,omitempty"`
}

type Explainability struct {
	ShapRaw *string             `json:"Shap_raw,omitempty"`
	ShapMap *map[string]float64 `json:"Shap_map,omitempty"`
}