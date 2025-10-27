# Complaint System

Simple producer/consumer demo using Kafka + Postgres.

Components
- Producer (Go): exposes POST /complaints and publishes events to Kafka.
  - Entry: complaint-producer-service/cmd/main.go
  - Handler: complaint-producer-service/internal/http/handler.go
  - Kafka producer: complaint-producer-service/internal/kafka/producer.go
  - Model: complaint-producer-service/internal/models/complaint.go
- Consumer (Spring Boot): listens to `complaints.created` and persists complaints to Postgres.
  - App: complaint-consumer-service/src/main/java/.../ComplaintConsumerServiceApplication.java
  - Listener: complaint-consumer-service/src/main/java/.../kafka/ComplaintKafkaListener.java
  - DTO: complaint-consumer-service/src/main/java/.../DTO/ComplaintEventDTO.java
  - Domain model: complaint-consumer-service/src/main/java/.../domain/model/Complaint.java
  - Kafka config: complaint-consumer-service/src/main/java/.../config/KafkaConsumerConfig.java

Prerequisites
- Docker & docker-compose
- Go (to run producer) or use `go run`
- Java 21 and Maven (or use the included `mvnw`) for consumer

Quick start (dev)
1. Start infrastructure:
   $ docker-compose up -d
   This starts Kafka (on host.docker.internal:9092) and Postgres (5432).

2. Run producer:
   $ cd complaint-producer-service
   $ go run ./cmd

   Producer runs on :8080 and publishes to topic `complaints.created`.

3. Run consumer:
   $ cd complaint-consumer-service
   $ ./mvnw spring-boot:run

   Consumer runs on :8081 and listens to `complaints.created`. DB config is in
   complaint-consumer-service/src/main/resources/application.yml.

Test
- Submit a complaint:
  $ curl -X POST http://localhost:8080/complaints \
    -H "Content-Type: application/json" \
    -d '{"user":"alice","category":"service","message":"delay in response"}'

Notes
- Topic name: `complaints.created`
- Kafka bootstrap server and Postgres connection are configured to `host.docker.internal` to allow services running on host to connect to containers.
- See docker-compose.yml for container versions and ports.

Useful files
- [docker-compose.yml](docker-compose.yml)
- Producer: [cmd/main.go](complaint-producer-service/cmd/main.go), [handler.go](complaint-producer-service/internal/http/handler.go)
- Consumer: [ComplaintKafkaListener.java](complaint-consumer-service/src/main/java/com/complaint/complaint_consumer_service/kafka/ComplaintKafkaListener.java)