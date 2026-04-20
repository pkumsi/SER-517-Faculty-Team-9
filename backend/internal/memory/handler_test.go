package memory

import (
	"bytes"
	"encoding/json"
	"net/http"
	"net/http/httptest"
	"testing"

	"github.com/gin-gonic/gin"
)

func newTestRouter() (*gin.Engine, *Store) {
	gin.SetMode(gin.TestMode)
	r := gin.New()
	store := New(0)
	NewHandler(store).RegisterRoutes(r.Group("/api/v1"))
	return r, store
}

func doRequest(t *testing.T, r *gin.Engine, method, path string, body any) *httptest.ResponseRecorder {
	t.Helper()
	var reader *bytes.Reader
	if body != nil {
		raw, err := json.Marshal(body)
		if err != nil {
			t.Fatalf("marshal body: %v", err)
		}
		reader = bytes.NewReader(raw)
	} else {
		reader = bytes.NewReader(nil)
	}
	req := httptest.NewRequest(method, path, reader)
	req.Header.Set("content-type", "application/json")
	w := httptest.NewRecorder()
	r.ServeHTTP(w, req)
	return w
}

func TestHandler_Create_HappyPath(t *testing.T) {
	r, store := newTestRouter()

	body := map[string]any{
		"uuid":    "u1",
		"payload": map[string]any{"note": "hello"},
	}
	w := doRequest(t, r, http.MethodPost, "/api/v1/memory", body)

	if w.Code != http.StatusCreated {
		t.Fatalf("status = %d, want 201. body=%s", w.Code, w.Body.String())
	}
	var got Record
	if err := json.Unmarshal(w.Body.Bytes(), &got); err != nil {
		t.Fatalf("decode response: %v", err)
	}
	if got.ID == "" || got.UUID != "u1" {
		t.Errorf("unexpected record: %+v", got)
	}
	if len(store.ListByUUID("u1")) != 1 {
		t.Error("store should contain 1 record after POST")
	}
}

func TestHandler_Create_BadRequest(t *testing.T) {
	r, _ := newTestRouter()

	cases := []struct {
		name string
		body any
	}{
		{"missing uuid", map[string]any{"payload": map[string]any{"x": 1}}},
		{"missing payload", map[string]any{"uuid": "u1"}},
		{"empty body", nil},
	}
	for _, tc := range cases {
		t.Run(tc.name, func(t *testing.T) {
			w := doRequest(t, r, http.MethodPost, "/api/v1/memory", tc.body)
			if w.Code != http.StatusBadRequest {
				t.Errorf("status = %d, want 400. body=%s", w.Code, w.Body.String())
			}
		})
	}
}

func TestHandler_List_UnknownUUIDReturnsEmptyArray(t *testing.T) {
	r, _ := newTestRouter()

	w := doRequest(t, r, http.MethodGet, "/api/v1/memory/nobody", nil)
	if w.Code != http.StatusOK {
		t.Fatalf("status = %d, want 200", w.Code)
	}
	// Must be a JSON array, not "null".
	if got := w.Body.String(); got != "[]" && got != "[]\n" {
		t.Errorf("body = %q, want empty JSON array", got)
	}
}

func TestHandler_List_ReturnsStoredRecords(t *testing.T) {
	r, store := newTestRouter()
	store.Create("u1", json.RawMessage(`{"n":1}`))
	store.Create("u1", json.RawMessage(`{"n":2}`))

	w := doRequest(t, r, http.MethodGet, "/api/v1/memory/u1", nil)
	if w.Code != http.StatusOK {
		t.Fatalf("status = %d, want 200", w.Code)
	}
	var got []Record
	if err := json.Unmarshal(w.Body.Bytes(), &got); err != nil {
		t.Fatalf("decode: %v", err)
	}
	if len(got) != 2 {
		t.Fatalf("len = %d, want 2", len(got))
	}
}

func TestHandler_Delete_HitAndMiss(t *testing.T) {
	r, store := newTestRouter()
	rec := store.Create("u1", json.RawMessage(`{"n":1}`))

	// Miss: unknown id.
	w := doRequest(t, r, http.MethodDelete, "/api/v1/memory/u1/does-not-exist", nil)
	if w.Code != http.StatusNotFound {
		t.Errorf("miss status = %d, want 404", w.Code)
	}

	// Hit.
	w = doRequest(t, r, http.MethodDelete, "/api/v1/memory/u1/"+rec.ID, nil)
	if w.Code != http.StatusNoContent {
		t.Errorf("hit status = %d, want 204", w.Code)
	}
	if n := len(store.ListByUUID("u1")); n != 0 {
		t.Errorf("store size after delete = %d, want 0", n)
	}
}
