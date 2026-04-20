package memory

import (
	"net/http"

	"github.com/gin-gonic/gin"
)

// Handler exposes HTTP endpoints for the in-memory record store.
type Handler struct {
	store *Store
}

// NewHandler wires the HTTP layer to a Store.
func NewHandler(s *Store) *Handler {
	return &Handler{store: s}
}

// RegisterRoutes attaches the memory endpoints under the given router group.
//
//	POST   {group}/memory           -> Create
//	GET    {group}/memory/:uuid     -> List
//	DELETE {group}/memory/:uuid/:id -> Delete
func (h *Handler) RegisterRoutes(rg *gin.RouterGroup) {
	rg.POST("/memory", h.Create)
	rg.GET("/memory/:uuid", h.List)
	rg.DELETE("/memory/:uuid/:id", h.Delete)
}

// Create handles POST /memory.
func (h *Handler) Create(c *gin.Context) {
	var req CreateRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "invalid request body: " + err.Error()})
		return
	}
	rec := h.store.Create(req.UUID, req.Payload)
	c.JSON(http.StatusCreated, rec)
}

// List handles GET /memory/:uuid.
func (h *Handler) List(c *gin.Context) {
	uuid := c.Param("uuid")
	records := h.store.ListByUUID(uuid)
	// Always return a JSON array (never null) even when empty.
	c.JSON(http.StatusOK, records)
}

// Delete handles DELETE /memory/:uuid/:id.
func (h *Handler) Delete(c *gin.Context) {
	uuid := c.Param("uuid")
	id := c.Param("id")
	if ok := h.store.Delete(uuid, id); !ok {
		c.JSON(http.StatusNotFound, gin.H{"error": "record not found"})
		return
	}
	c.Status(http.StatusNoContent)
}
