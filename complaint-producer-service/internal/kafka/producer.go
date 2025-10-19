package kafka

import (
	"context"
	"encoding/json"
	"log"
	"time"

	"github.com/segmentio/kafka-go"
	"complaint-producer/internal/models"
)

type Producer struct {
	writer *kafka.Writer
}

func NewProducer(broker string, topic string) *Producer {
	writer := &kafka.Writer{
		Addr:     kafka.TCP(broker),
		Topic:    topic,
		Balancer: &kafka.LeastBytes{},
	}
	return &Producer{writer: writer}
}

func (p *Producer) PublishComplaintEvent(c models.Complaint) error {
	event := map[string]interface{}{
		"eventType":    "complaint.created",
		"eventVersion": 1,
		"timestamp":    time.Now().UTC().Format(time.RFC3339),
		"payload":      c,
	}

	message, _ := json.Marshal(event)
	err := p.writer.WriteMessages(context.Background(), kafka.Message{
		Value: message,
	})

	if err != nil {
		log.Printf("Failed to publish message: %v", err)
	}
	return err
}
