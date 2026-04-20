package memory

import (
	"encoding/json"
	"time"
)

// Record is a single freeform memory document owned by a user (identified by UUID).
// Payload is stored as-is so callers can round-trip arbitrary JSON without the store
// imposing any schema.
type Record struct {
	ID        string          `json:"id"`
	UUID      string          `json:"uuid"`
	Payload   json.RawMessage `json:"payload"`
	CreatedAt time.Time       `json:"created_at"`
}

// CreateRequest is the JSON body accepted by POST /api/v1/memory.
type CreateRequest struct {
	UUID    string          `json:"uuid"    binding:"required"`
	Payload json.RawMessage `json:"payload" binding:"required"`
}
