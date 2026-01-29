# Complaint System

A production-ready event-driven complaint management system using **Kafka** for asynchronous messaging and **PostgreSQL** for persistence. This project demonstrates best practices for building scalable microservices with proper error handling, dead-letter queue (DLQ) processing, and health monitoring.

---

## Table of Contents
1. [Architecture](#architecture)
2. [Components](#components)
3. [Prerequisites](#prerequisites)
4. [Quick Start](#quick-start)
5. [API Documentation](#api-documentation)
6. [Database Schema](#database-schema)
7. [Error Handling & DLQ](#error-handling--dlq)
8. [Health Monitoring](#health-monitoring)
9. [Configuration](#configuration)
10. [Troubleshooting](#troubleshooting)
11. [Development](#development)

---

## Architecture

This is an **event-driven microservices architecture** with the following flow:

```
Client → Producer Service → Kafka Topic → Consumer Service → PostgreSQL DB
                                   ↓
                            Dead Letter Queue (on failure)
```

### Key Features:
- **Asynchronous Processing**: Decoupled producer and consumer via Kafka
- **Error Handling**: Comprehensive validation and error recovery
- **Dead Letter Queue (DLQ)**: Failed messages are sent to `complaints.created.DLQ` for manual review
- **Idempotent Processing**: Each complaint has a unique ID preventing duplicates
- **Health Checks**: Built-in health indicators for Kafka and database
- **Logging**: Comprehensive logging for debugging and monitoring
- **Data Validation**: Input validation on both producer and consumer sides

---

## Components

### Producer Service (Go)
**Purpose**: Accepts complaint submissions via HTTP and publishes them to Kafka

- **Port**: `8080`
- **Technology Stack**: Go 1.24.6, Kafka-go, UUID generation
- **Key Files**:
  - [cmd/main.go](complaint-producer-service/cmd/main.go) - Application entry point
  - [internal/http/handler.go](complaint-producer-service/internal/http/handler.go) - HTTP request handler
  - [internal/kafka/producer.go](complaint-producer-service/internal/kafka/producer.go) - Kafka producer logic
  - [internal/models/complaint.go](complaint-producer-service/internal/models/complaint.go) - Data model

**Responsibilities**:
- Validate incoming requests
- Generate unique complaint IDs (UUID)
- Set complaint status to "OPEN"
- Timestamp creation date (ISO 8601 format)
- Publish events to Kafka topic `complaints.created`
- Return 202 (Accepted) response

---

### Consumer Service (Spring Boot)
**Purpose**: Consumes complaint events from Kafka and persists them to PostgreSQL

- **Port**: `8081`
- **Technology Stack**: Spring Boot 3.5.6, Java 21, Spring Kafka, PostgreSQL, JPA/Hibernate
- **Key Files**:
  - [ComplaintConsumerServiceApplication.java](complaint-consumer-service/src/main/java/com/complaint/complaint_consumer_service/ComplaintConsumerServiceApplication.java) - Application entry point
  - [kafka/ComplaintKafkaListener.java](complaint-consumer-service/src/main/java/com/complaint/complaint_consumer_service/kafka/ComplaintKafkaListener.java) - Message listener with error handling
  - [config/KafkaConsumerConfig.java](complaint-consumer-service/src/main/java/com/complaint/complaint_consumer_service/config/KafkaConsumerConfig.java) - Kafka and DLQ configuration
  - [domain/model/Complaint.java](complaint-consumer-service/src/main/java/com/complaint/complaint_consumer_service/domain/model/Complaint.java) - JPA entity
  - [domain/service/ComplaintService.java](complaint-consumer-service/src/main/java/com/complaint/complaint_consumer_service/domain/service/ComplaintService.java) - Business logic
  - [DTO/ComplaintEventDTO.java](complaint-consumer-service/src/main/java/com/complaint/complaint_consumer_service/DTO/ComplaintEventDTO.java) - Event data transfer object

**Responsibilities**:
- Listen to `complaints.created` Kafka topic
- Validate complaint data (user field is required)
- Save complaints to PostgreSQL database
- Handle processing errors with retry logic
- Send failed messages to DLQ for manual review
- Log all operations for debugging

---

## Prerequisites

Ensure you have the following installed:

| Component | Version | Purpose |
|-----------|---------|---------|
| Docker | Latest | Run Kafka and PostgreSQL containers |
| Docker Compose | 3.8+ | Orchestrate multi-container setup |
| Go | 1.24.6+ | Run the producer service |
| Java | 21+ | Run the consumer service |
| Maven | 3.9+ (or use ./mvnw) | Build and run consumer |
| curl | Any | Test API endpoints |

**System Requirements**:
- Minimum 2GB RAM (Docker containers)
- At least 5GB free disk space
- Ports available: `8080` (Producer), `8081` (Consumer), `9092` (Kafka), `5432` (PostgreSQL), `2181` (Zookeeper)

---

## Quick Start

### Step 1: Start Infrastructure (Kafka + PostgreSQL)

```bash
docker-compose up -d
```

This command starts:
- **Zookeeper** (port 2181): Kafka cluster coordinator
- **Kafka** (port 9092): Message broker
- **PostgreSQL** (port 5432): Database
  - User: `complaint_user`
  - Password: `complaint_pass`
  - Database: `complaint_db`

**Verify services are running:**
```bash
docker-compose ps
```

**View logs:**
```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f kafka
docker-compose logs -f postgres
```

---

### Step 2: Run Producer Service

```bash
cd complaint-producer-service
go run ./cmd
```

**Expected output:**
```
Producer service running on :8080 ...
```

The producer is now ready to accept complaint submissions. It will:
- Listen on `http://localhost:8080`
- Publish all complaints to Kafka topic `complaints.created`

---

### Step 3: Run Consumer Service

In a new terminal:

```bash
cd complaint-consumer-service
./mvnw spring-boot:run
```

**Expected output:**
```
Started ComplaintConsumerServiceApplication in X.XXX seconds
```

The consumer is now:
- Listening on Kafka topic `complaints.created`
- Persisting complaints to PostgreSQL
- Running on `http://localhost:8081`

---

## API Documentation

### Producer Service Endpoints

#### Create Complaint
**Endpoint**: `POST /complaints`

**Request Headers**:
```
Content-Type: application/json
```

**Request Body**:
```json
{
  "user": "alice",
  "category": "service",
  "message": "delay in response"
}
```

**Query Parameters**:
- All fields are required except `id` and `status` (auto-generated)

**Response (202 Accepted)**:
```json
{
  "message": "Complaint submitted successfully"
}
```

**Example cURL**:
```bash
curl -X POST http://localhost:8080/complaints \
  -H "Content-Type: application/json" \
  -d '{
    "user":"alice",
    "category":"service",
    "message":"delay in response"
  }'
```

**Possible Responses**:
| Status | Meaning |
|--------|---------|
| 202 | Complaint accepted and sent to Kafka |
| 400 | Invalid request (malformed JSON) |
| 500 | Failed to publish to Kafka |

---

### Consumer Service Endpoints

#### Health Check
**Endpoint**: `GET /actuator/health`

**Response (200 OK)**:
```json
{
  "status": "UP",
  "components": {
    "kafkaHealthIndicator": {
      "status": "UP"
    },
    "db": {
      "status": "UP"
    }
  }
}
```

This endpoint checks:
- ✅ Kafka broker connectivity
- ✅ PostgreSQL database connectivity

---

## Database Schema

### Complaints Table

```sql
CREATE TABLE complaint (
    id VARCHAR(255) PRIMARY KEY,
    username VARCHAR(255) NOT NULL,
    category VARCHAR(255),
    message TEXT,
    status VARCHAR(50),
    created VARCHAR(255)
);
```

### Table Details

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | VARCHAR(255) | PRIMARY KEY | UUID generated by producer |
| `username` | VARCHAR(255) | NOT NULL | User who submitted complaint |
| `category` | VARCHAR(255) | - | Complaint type (e.g., "service") |
| `message` | TEXT | - | Complaint details |
| `status` | VARCHAR(50) | - | Current status (e.g., "OPEN", "RESOLVED") |
| `created` | VARCHAR(255) | - | ISO 8601 timestamp |

**Note**: Tables are auto-created by Hibernate (`ddl-auto: update` in configuration)

---

## ⚠️ Error Handling & DLQ

### Error Handling Flow

The consumer implements a robust error handling strategy:

```
Kafka Message
    ↓
[Validation: Is user field present?]
    ├─ YES → Save to Database → ✅ Success
    └─ NO → Retry Logic
           ├─ Retry 3 times with backoff
           └─ Send to Dead Letter Queue (DLQ)
```

### Dead Letter Queue (DLQ)

**Topic**: `complaints.created.DLQ`

Failed messages are automatically forwarded to the DLQ with:
- Original payload
- Exception message
- Original topic name
- Error details

**Consumer DLQ Handler**:
- Listens to `complaints.created.DLQ`
- Logs detailed error information
- Awaits manual intervention for processing

**Example DLQ Log**:
```
DLQ message received from complaints.created. 
Error=User is required. 
Payload={...}
```

### Error Scenarios

| Scenario | Handling |
|----------|----------|
| Missing user field | Validation error → Retry 3x → DLQ |
| Network timeout | Automatic retry with backoff |
| Database unavailable | Retry with exponential backoff |
| Kafka down | Connection retry |
| Malformed JSON | Immediate error → No retry → DLQ |

---

## Health Monitoring

### Consumer Health Endpoint

**Endpoint**: `GET /actuator/health`

The consumer provides health indicators for:

#### 1. Kafka Health
- Checks broker connectivity
- Verifies topic availability
- Implementation: [KafkaHealthIndicator.java](complaint-consumer-service/src/main/java/com/complaint/complaint_consumer_service/kafka/KafkaHealthIndicator.java)

#### 2. Database Health
- Checks PostgreSQL connection
- Verifies table accessibility
- Built-in Spring Boot indicator

**Health Response Example**:
```json
{
  "status": "UP",
  "components": {
    "kafkaHealthIndicator": {
      "status": "UP",
      "details": {
        "message": "Kafka broker is available"
      }
    },
    "db": {
      "status": "UP",
      "details": {
        "database": "PostgreSQL",
        "validationQuery": "isValid()"
      }
    }
  }
}
```

---

## Configuration

### Environment-Specific Configurations

The consumer service supports multiple profiles:

```bash
# Development
./mvnw spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"

# Production
./mvnw spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=prod"
```

### Configuration Files

#### Default Configuration
**File**: [application.yml](complaint-consumer-service/src/main/resources/application.yml)
- Kafka bootstrap server: `host.docker.internal:9092`
- PostgreSQL: `jdbc:postgresql://host.docker.internal:5432/complaint_db`
- Consumer group: `complaint-consumers`
- Server port: `8081`

#### Development Configuration
**File**: [application-dev.yml](complaint-consumer-service/src/main/resources/application-dev.yml)
- Local development settings
- Debug logging enabled

#### Production Configuration
**File**: [application-prod.yml](complaint-consumer-service/src/main/resources/application-prod.yml)
- Production-grade settings
- Error handling and monitoring

### Key Configuration Properties

```yaml
spring:
  kafka:
    bootstrap-servers: host.docker.internal:9092
    consumer:
      group-id: complaint-consumers
      auto-offset-reset: earliest  # Start from beginning if no offset
      key-deserializer: StringDeserializer
      value-deserializer: ErrorHandlingDeserializer
  
  datasource:
    url: jdbc:postgresql://host.docker.internal:5432/complaint_db
    username: complaint_user
    password: complaint_pass
  
  jpa:
    hibernate:
      ddl-auto: update  # Auto-create tables
    show-sql: true
    format_sql: true

management:
  endpoints:
    web:
      exposure:
        include: health  # Expose health endpoint

server:
  port: 8081
```

---

## Troubleshooting

### Producer Issues

#### Producer fails to start
```
Error: connection refused on host.docker.internal:9092
```
**Solution**: Ensure Kafka container is running
```bash
docker-compose ps kafka
docker-compose logs kafka
```

#### Kafka connection timeout
**Solution**: Use `localhost:9092` if running locally without Docker
```go
producer := kafka.NewProducer("localhost:9092", "complaints.created")
```

### Consumer Issues

#### Consumer not receiving messages
**Possible causes**:
1. Kafka topic doesn't exist
2. Consumer group offset issue
3. Kafka bootstrap server misconfigured

**Troubleshooting**:
```bash
# Check Kafka topics
docker exec complaint_system_kafka_1 \
  kafka-topics --list --bootstrap-server localhost:9092

# Reset consumer group offset
docker exec complaint_system_kafka_1 \
  kafka-consumer-groups --bootstrap-server localhost:9092 \
  --group complaint-consumers --reset-offsets \
  --to-earliest --all-topics --execute
```

#### Database connection error
```
Error: Unable to connect to database at jdbc:postgresql://host.docker.internal:5432/complaint_db
```
**Solution**: 
- Verify PostgreSQL is running: `docker-compose logs postgres`
- Check credentials in `application.yml`
- Ensure port 5432 is accessible

#### DLQ messages not being processed
**Solution**: Check DLQ consumer logs
```bash
grep "DLQ message received" application.log
```

### Docker Issues

#### Port already in use
```bash
# Kill process on port 8080
lsof -i :8080 | grep LISTEN | awk '{print $2}' | xargs kill -9

# Kill process on port 5432
lsof -i :5432 | grep LISTEN | awk '{print $2}' | xargs kill -9
```

#### Container memory issues
```bash
# Increase Docker memory limit and restart
docker-compose down
docker-compose up -d
```

---

## Development

### Project Structure

```
complaint-system/
├── complaint-producer-service/          # Go service
│   ├── cmd/main.go                     # Entry point
│   ├── internal/
│   │   ├── http/handler.go            # HTTP handlers
│   │   ├── kafka/producer.go          # Kafka publishing
│   │   └── models/complaint.go        # Data model
│   ├── go.mod                         # Go dependencies
│   └── go.sum
│
├── complaint-consumer-service/          # Spring Boot service
│   ├── src/main/java/com/complaint/
│   │   └── complaint_consumer_service/
│   │       ├── ComplaintConsumerServiceApplication.java
│   │       ├── config/KafkaConsumerConfig.java
│   │       ├── domain/
│   │       │   ├── model/Complaint.java
│   │       │   ├── repository/ComplaintRepository.java
│   │       │   └── service/ComplaintService.java
│   │       ├── kafka/
│   │       │   ├── ComplaintKafkaListener.java
│   │       │   └── KafkaHealthIndicator.java
│   │       └── DTO/ComplaintEventDTO.java
│   ├── src/main/resources/
│   │   ├── application.yml
│   │   ├── application-dev.yml
│   │   └── application-prod.yml
│   ├── pom.xml
│   └── mvnw
│
├── docker-compose.yml                  # Infrastructure
└── Readme.md                           # This file
```

### Running Tests

#### Consumer Tests
```bash
cd complaint-consumer-service
./mvnw test
```

### Building for Production

#### Producer
```bash
cd complaint-producer-service
go build -o complaint-producer ./cmd
./complaint-producer
```

#### Consumer
```bash
cd complaint-consumer-service
./mvnw clean package
java -jar target/complaint-consumer-service-0.0.1-SNAPSHOT.jar
```

### Useful Commands

```bash
# View all logs
docker-compose logs -f

# Restart services
docker-compose restart

# Stop all services
docker-compose down

# Remove all data (clean slate)
docker-compose down -v

# Connect to PostgreSQL
docker exec -it complaint_system_postgres_1 \
  psql -U complaint_user -d complaint_db

# View Kafka topics
docker exec complaint_system_kafka_1 \
  kafka-topics --list --bootstrap-server localhost:9092

# Consume from Kafka topic directly
docker exec -it complaint_system_kafka_1 \
  kafka-console-consumer --bootstrap-server localhost:9092 \
  --topic complaints.created --from-beginning
```

---

## Notes

- **Host DNS Resolution**: Services use `host.docker.internal` to allow Docker containers to communicate with services running on the host machine
- **Message Format**: All messages use JSON serialization for cross-platform compatibility
- **Idempotency**: Each complaint has a UUID, ensuring no duplicate processing
- **Data Retention**: Messages are retained in Kafka topic based on configured retention policy
- **Timezone**: All timestamps are in UTC (ISO 8601 format)

---

## Additional Resources

- [Kafka Documentation](https://kafka.apache.org/documentation/)
- [Spring Kafka Guide](https://spring.io/projects/spring-kafka)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)
- [Go Kafka Client](https://pkg.go.dev/github.com/segmentio/kafka-go)

---

## License & Contributing

This is a demo project for learning event-driven microservices architecture. Feel free to fork and extend!

---

**Last Updated**: 2025
**Version**: 1.1.0