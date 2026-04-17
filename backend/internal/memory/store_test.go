package memory

import (
	"encoding/json"
	"fmt"
	"sync"
	"testing"
)

func TestStore_Create_ReturnsRecordWithIDAndFields(t *testing.T) {
	s := New(0)
	payload := json.RawMessage(`{"note":"hello"}`)

	rec := s.Create("u1", payload)

	if rec.ID == "" {
		t.Fatal("expected non-empty ID")
	}
	if rec.UUID != "u1" {
		t.Errorf("UUID = %q, want %q", rec.UUID, "u1")
	}
	if string(rec.Payload) != string(payload) {
		t.Errorf("Payload = %s, want %s", rec.Payload, payload)
	}
	if rec.CreatedAt.IsZero() {
		t.Error("CreatedAt should not be zero")
	}
}

func TestStore_Create_GeneratesUniqueIDs(t *testing.T) {
	s := New(0)
	seen := map[string]bool{}
	for i := 0; i < 100; i++ {
		rec := s.Create("u1", json.RawMessage(`{}`))
		if seen[rec.ID] {
			t.Fatalf("duplicate ID generated: %s", rec.ID)
		}
		seen[rec.ID] = true
	}
}

func TestStore_ListByUUID_UnknownReturnsEmptySlice(t *testing.T) {
	s := New(0)
	got := s.ListByUUID("nobody")
	if got == nil {
		t.Fatal("ListByUUID should never return nil")
	}
	if len(got) != 0 {
		t.Errorf("len = %d, want 0", len(got))
	}
}

func TestStore_ListByUUID_ReturnsCopy(t *testing.T) {
	s := New(0)
	s.Create("u1", json.RawMessage(`{"x":1}`))

	first := s.ListByUUID("u1")
	if len(first) != 1 {
		t.Fatalf("len = %d, want 1", len(first))
	}
	// Mutate the returned slice — should not affect the store.
	first[0].Payload = json.RawMessage(`{"tampered":true}`)
	first = append(first, Record{ID: "fake"})

	second := s.ListByUUID("u1")
	if len(second) != 1 {
		t.Fatalf("store length changed via returned slice: got %d", len(second))
	}
	if string(second[0].Payload) != `{"x":1}` {
		t.Errorf("payload mutated via returned slice: %s", second[0].Payload)
	}
}

func TestStore_Create_EvictsOldestWhenOverCapacity(t *testing.T) {
	s := New(2)
	s.Create("u1", json.RawMessage(`{"n":1}`))
	s.Create("u1", json.RawMessage(`{"n":2}`))
	s.Create("u1", json.RawMessage(`{"n":3}`))

	got := s.ListByUUID("u1")
	if len(got) != 2 {
		t.Fatalf("len = %d, want 2 (capacity)", len(got))
	}
	if string(got[0].Payload) != `{"n":2}` {
		t.Errorf("oldest was not evicted: got[0] = %s", got[0].Payload)
	}
	if string(got[1].Payload) != `{"n":3}` {
		t.Errorf("newest missing: got[1] = %s", got[1].Payload)
	}
}

func TestStore_PerUUIDIsolation(t *testing.T) {
	s := New(0)
	s.Create("alice", json.RawMessage(`{"who":"alice"}`))
	s.Create("bob", json.RawMessage(`{"who":"bob"}`))

	a := s.ListByUUID("alice")
	b := s.ListByUUID("bob")
	if len(a) != 1 || len(b) != 1 {
		t.Fatalf("isolation broken: alice=%d bob=%d", len(a), len(b))
	}
	if string(a[0].Payload) != `{"who":"alice"}` || string(b[0].Payload) != `{"who":"bob"}` {
		t.Error("records bled across UUIDs")
	}
}

func TestStore_Delete(t *testing.T) {
	s := New(0)
	r1 := s.Create("u1", json.RawMessage(`{"n":1}`))
	r2 := s.Create("u1", json.RawMessage(`{"n":2}`))

	if ok := s.Delete("u1", "does-not-exist"); ok {
		t.Error("Delete should return false for unknown id")
	}
	if ok := s.Delete("other-uuid", r1.ID); ok {
		t.Error("Delete should return false for unknown uuid")
	}
	if ok := s.Delete("u1", r1.ID); !ok {
		t.Error("Delete should return true for matching record")
	}

	got := s.ListByUUID("u1")
	if len(got) != 1 || got[0].ID != r2.ID {
		t.Fatalf("after delete, list = %+v, want only %s", got, r2.ID)
	}

	// Remove the last one: the UUID bucket should be cleaned up.
	if ok := s.Delete("u1", r2.ID); !ok {
		t.Error("Delete should return true for the last record")
	}
	if got := s.ListByUUID("u1"); len(got) != 0 {
		t.Errorf("list after full delete = %+v, want empty", got)
	}
}

func TestStore_ConcurrentAccess(t *testing.T) {
	s := New(0)
	const workers = 16
	const perWorker = 50

	var wg sync.WaitGroup
	for w := 0; w < workers; w++ {
		wg.Add(1)
		go func(w int) {
			defer wg.Done()
			uuid := fmt.Sprintf("u-%d", w%4) // 4 distinct buckets
			for i := 0; i < perWorker; i++ {
				s.Create(uuid, json.RawMessage(fmt.Sprintf(`{"w":%d,"i":%d}`, w, i)))
				_ = s.ListByUUID(uuid)
			}
		}(w)
	}
	wg.Wait()

	// With 4 buckets and 16*50 = 800 writes evenly distributed, each bucket
	// should hold 200 records.
	total := 0
	for i := 0; i < 4; i++ {
		total += len(s.ListByUUID(fmt.Sprintf("u-%d", i)))
	}
	if total != workers*perWorker {
		t.Errorf("total records = %d, want %d", total, workers*perWorker)
	}
}
