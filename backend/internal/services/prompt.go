package services

import "fmt"

// buildPrompt constructs the full prompt from the 5 inferred elements.
// It selects the correct template variant (Minimal / Standard / Rich) based
// on the Variant field set by the inference layer in inference.go.
//
// Implements the Prompt Template Structure from Task #25 (Prerana Kumsi) Section 4:
//   - System Instruction Layer (constant, always included)
//   - Structured Context Block (dynamic, built from InferredElements)
//
// The three variants correspond directly to Task #20 (Aniket Patil) findings:
//   Minimal  → Combo 6: Activity + Response Time (Good, best privacy ratio)
//   Rich     → Combo 2: Activity + Time + Sender + Urgency (Good, social tone)
//   Standard → Combo 3: All 5 elements (Excellent, recommended default)
func buildPrompt(elements *InferredElements) string {
	systemInstruction := buildSystemInstruction()
	contextBlock := buildContextBlock(elements)
	return fmt.Sprintf("%s\n\n%s", systemInstruction, contextBlock)
}

// buildSystemInstruction returns the constant system-level instruction for the LLM.
// This is the System Instruction Layer from Task #25 Section 4.1.
//
// These rules directly address the research questions from Sprint 1:
//   - "Do NOT fabricate details" → prevents hallucination (Task #22 Section 5)
//   - "Speak in third person" → consistent voice per Task #19 Section 1
//   - "Adjust tone based on sender" → implements Task #20 Combo 8 finding
//   - "Do not mention raw sensor data" → enforces Task #23 exclusion rules
//   - "1 to 3 sentences" → conciseness per Task #25 Section 3
func buildSystemInstruction() string {
	return `You are an auto-response generation system for a mobile messaging application.
Generate a polite, context-aware auto-response message on behalf of the user.

Rules:
- Speak about the user in third person. Do NOT imitate the user directly.
- Do NOT fabricate details. Use only the context supplied below.
- Adjust tone based on sender relationship: formal for managers or colleagues, casual for friends.
- Always set a realistic expectation for when the user will respond.
- Keep the response concise: 1 to 3 sentences maximum.
- Do not mention raw sensor data, device state, noise levels, or battery information.`
}

// buildContextBlock assembles the structured context block passed to the LLM.
// Selects the appropriate variant based on InferredElements.Variant.
func buildContextBlock(elements *InferredElements) string {
	switch elements.Variant {
	case VariantStandard:
		return buildStandardContext(elements)
	case VariantRich:
		return buildRichContext(elements)
	default:
		return buildMinimalContext(elements)
	}
}

// buildMinimalContext implements the Minimal variant — Task #25 Section 5.1.
// Corresponds to Combo 6 (Task #20): Activity + Expected Response Time.
// Quality: Good. "Clean, informative response with minimal data exposure.
// Answers both 'why unavailable' and 'when will they respond'." — Task #20 Combo 6
//
// Falls back to Activity + Time (Combo 1) if response time is unavailable.
// Quality: Acceptable. "Functional but vague, no tone adaptation." — Task #20 Combo 1
func buildMinimalContext(elements *InferredElements) string {
	context := ""

	if elements.Activity != "" {
		context += fmt.Sprintf("Current Activity: %s\n", elements.Activity)
	}

	if elements.ExpectedResponseTime != "" {
		context += fmt.Sprintf("Expected Response Time: %s\n", elements.ExpectedResponseTime)
	} else if elements.CurrentTime != "" {
		// Combo 1 fallback — Activity + Time
		context += fmt.Sprintf("Current Time: %s\n", elements.CurrentTime)
	}

	context += "\nGenerate a concise auto-response in third person."
	return context
}

// buildRichContext implements the Rich variant — Task #25 Section 5.3 fallback.
// Corresponds to Combo 2 (Task #20): Activity + Time + Sender Role + Urgency.
// Quality: Good. "Social context adds professional tone." — Task #20 Combo 2
//
// This is used when the full 5-element Standard set is not available —
// specifically when Expected Response Time cannot be inferred.
func buildRichContext(elements *InferredElements) string {
	context := ""

	if elements.Activity != "" {
		context += fmt.Sprintf("Current Activity: %s\n", elements.Activity)
	}

	if elements.CurrentTime != "" {
		context += fmt.Sprintf("Current Time: %s\n", elements.CurrentTime)
	}

	if elements.SenderRole != "" {
		context += fmt.Sprintf("Sender: %s\n", elements.SenderRole)
	}

	if elements.Urgency != "" {
		context += fmt.Sprintf("Message Urgency: %s\n", elements.Urgency)
	}

	context += "\nGenerate a concise auto-response in third person. Adjust tone based on sender relationship."
	return context
}

// buildStandardContext implements the Standard variant — Task #25 Section 5.2.
// Corresponds to Combo 3 (Task #20): all 5 optimal elements.
// Quality: Excellent. "Near-identical quality to the full baseline." — Task #20 Combo 3
//
// This is the recommended default configuration per Task #25.
// "Specific time estimate sets clear expectations.
//  Professional tone matches sender relationship." — Task #20 Combo 3
func buildStandardContext(elements *InferredElements) string {
	context := ""

	if elements.Activity != "" {
		context += fmt.Sprintf("Current Activity: %s\n", elements.Activity)
	}

	if elements.CurrentTime != "" {
		context += fmt.Sprintf("Current Time: %s\n", elements.CurrentTime)
	}

	if elements.SenderRole != "" {
		context += fmt.Sprintf("Sender: %s\n", elements.SenderRole)
	}

	if elements.Urgency != "" {
		context += fmt.Sprintf("Message Urgency: %s\n", elements.Urgency)
	}

	if elements.ExpectedResponseTime != "" {
		context += fmt.Sprintf("Expected Response Time: %s\n", elements.ExpectedResponseTime)
	}

	context += "\nGenerate a concise auto-response in third person. Adjust tone based on sender relationship and set a clear response time expectation."
	return context
}
