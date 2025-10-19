package main

import (
	"net/http"
	"github.com/elshewemy/complaint-producer-service/internal/http"
    "github.com/elshewemy/complaint-producer-service/internal/kafka"
)

func main() {
	producer := kafka.NewProducer("localhost:9092", "complaints.created")
	handler := &http.Handler{Producer: producer}

	http.HandleFunc("/complaints", handler.CreateComplaint)

	println("Producer service running on :8080 ...")
	http.ListenAndServe(":8080", nil)
}

