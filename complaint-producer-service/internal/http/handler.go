package http

import (
	"encoding/json"
	"net/http"
	"time"
	"github.com/google/uuid"
	"github.com/elshewemy/complaint-producer-service/internal/kafka"
	"github.com/elshewemy/complaint-producer-service/internal/models"
)

type Handler struct {
	Producer *kafka.Producer
}

func (h *Handler) CreateComplaint(w http.ResponseWriter, r *http.Request) {
	var c models.Complaint
	if err := json.NewDecoder(r.Body).Decode(&c); err != nil {
		http.Error(w, err.Error(), http.StatusBadRequest)
		return
	}

	c.ID = uuid.NewString()
	c.Status = "OPEN"
	c.CreatedAt = time.Now().UTC().Format(time.RFC3339)

	err := h.Producer.PublishComplaintEvent(c)
	if err != nil {
		http.Error(w, "Failed to send to Kafka", http.StatusInternalServerError)
		return
	}

	w.WriteHeader(http.StatusAccepted)
	json.NewEncoder(w).Encode(map[string]string{
		"message": "Complaint submitted successfully",
	})
}
