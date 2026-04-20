package memory

import (
	"crypto/rand"
	"encoding/hex"
	"encoding/json"
	"sync"
	"time"
)

// Store is a thread-safe, in-memory collection of freeform Records bucketed by UUID.
// It is intentionally not persisted: all data is lost on process restart.
type Store struct {
	mu       sync.RWMutex
	perUser  map[string][]Record
	capacity int // max records per UUID; <= 0 means unlimited
}

// New creates an empty Store. perUserCapacity <= 0 disables the cap.
func New(perUserCapacity int) *Store {
	return &Store{
		perUser:  make(map[string][]Record),
		capacity: perUserCapacity,
	}
}

// Create appends a new Record for the given UUID and returns it. When the
// per-user capacity is exceeded, the oldest record for that UUID is evicted.
func (s *Store) Create(uuid string, payload json.RawMessage) Record {
	rec := Record{
		ID:        newID(),
		UUID:      uuid,
		Payload:   cloneRaw(payload),
		CreatedAt: time.Now().UTC(),
	}

	s.mu.Lock()
	defer s.mu.Unlock()

	bucket := s.perUser[uuid]
	bucket = append(bucket, rec)
	if s.capacity > 0 && len(bucket) > s.capacity {
		// Drop the oldest so the newest always wins.
		bucket = bucket[len(bucket)-s.capacity:]
	}
	s.perUser[uuid] = bucket
	return rec
}

// ListByUUID returns a copy of the records for the given UUID, oldest first.
// A nil slice is never returned; an empty slice is returned for unknown UUIDs.
func (s *Store) ListByUUID(uuid string) []Record {
	s.mu.RLock()
	defer s.mu.RUnlock()

	src := s.perUser[uuid]
	out := make([]Record, len(src))
	for i, r := range src {
		out[i] = Record{
			ID:        r.ID,
			UUID:      r.UUID,
			Payload:   cloneRaw(r.Payload),
			CreatedAt: r.CreatedAt,
		}
	}
	return out
}

// Delete removes the record with the given id under uuid. Returns true if a
// record was removed, false if no match was found.
func (s *Store) Delete(uuid, id string) bool {
	s.mu.Lock()
	defer s.mu.Unlock()

	bucket, ok := s.perUser[uuid]
	if !ok {
		return false
	}
	for i, r := range bucket {
		if r.ID == id {
			s.perUser[uuid] = append(bucket[:i], bucket[i+1:]...)
			if len(s.perUser[uuid]) == 0 {
				delete(s.perUser, uuid)
			}
			return true
		}
	}
	return false
}

// newID returns a 32-character hex identifier backed by 16 random bytes.
// crypto/rand on modern platforms does not return errors in practice, but we
// fall back to a time-based ID if the OS CSPRNG ever fails so Create never panics.
func newID() string {
	var b [16]byte
	if _, err := rand.Read(b[:]); err != nil {
		return hex.EncodeToString([]byte(time.Now().UTC().Format(time.RFC3339Nano)))
	}
	return hex.EncodeToString(b[:])
}

// cloneRaw returns an independent copy of a json.RawMessage so external
// mutations to the caller's buffer cannot corrupt the store (and vice versa).
func cloneRaw(raw json.RawMessage) json.RawMessage {
	if raw == nil {
		return nil
	}
	out := make(json.RawMessage, len(raw))
	copy(out, raw)
	return out
}
