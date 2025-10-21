package main

import (
	"log"
	"net/http"
	internalhttp "github.com/elshewemy/complaint-producer-service/internal/http"
    "github.com/elshewemy/complaint-producer-service/internal/kafka"
)

func main() {
	producer := kafka.NewProducer("host.docker.internal:9092", "complaints.created")
	handler := &internalhttp.Handler{Producer: producer}

	http.HandleFunc("/complaints", handler.CreateComplaint)

	println("Producer service running on :8080 ...")
	// Start server
	if err := http.ListenAndServe(":8080", nil); err != nil {
		log.Fatal(err)
	}
}

