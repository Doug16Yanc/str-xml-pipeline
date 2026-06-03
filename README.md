# STR-XML-PIPELINE: High-Throughput Interbank Settlement Architecture

An enterprise-grade, event-driven interbank settlement pipeline engineered for high-concurrency, low-latency transaction grouping, and automated structural XML generation compliant with Central Bank (**BACEN / STR**) standards.

Built using a strict **Hexagonal Architecture (Ports & Adapters)** pattern.

The core processing engine leverages **Java 25** and **Project Loom Virtual Threads** to handle massive I/O-bound workloads asynchronously without blocking database connections or causing thread starvation.

---

# 🏗️ Architecture Overview

The system is designed around decoupled architectural boundaries where the business domain remains completely isolated from framework-specific behaviors, infrastructure clients, and database protocols.

<img width="1672" height="941" alt="Image" src="https://github.com/user-attachments/assets/ed3753f4-8738-43b3-af04-227fee461c80" />

## Architectural Stack Highlights

### Core Engine
- Kotlin 2.2
- Java 25
- Spring Boot 4

### Concurrency Model
- Active utilization of **Virtual Threads**
- Lightweight, non-blocking stream consumption
- Parallel processing with minimal resource overhead

### Inbound (Driving) Layer
- Reactive Kafka Consumers
- Micro-batch processing
- Localized partitioning strategy
- OAuth2 Resource Server secured REST API

### Domain & Security
- Pure POJO/Kotlin domain entities
- Structural invariant validation through `init` blocks
- JAXB 4 XML serialization
- Argon2id hashing via Bouncy Castle

### Outbound (Driven) Infrastructure

#### PostgreSQL 16
- HikariCP connection pooling
- `reWriteBatchedInserts=true`
- Native PostgreSQL `COPY` support
- Bulk transaction optimization

#### Redis 7
- Distributed idempotency filter
- Duplicate event prevention
- Low-latency lookups

#### Amazon S3
- Immutable XML artifact storage
- Canonical persistence layer
- Long-term archival

---

# ⚡ Core Design Patterns & Optimizations

## 1. Chronological Partitioning via `SettlementWindow`

To eliminate distributed row-lock contention during settlement aggregation, the system uses a custom value object:

```text
SYSTEM-CYCLE-HHhMM
```

represented by:

```java
SettlementWindow
```

This object acts as the Kafka Partition Key.

### Benefits

- Transactions belonging to the same settlement cycle are routed to the same Kafka partition.
- Ordering is guaranteed by Kafka.
- Virtual Threads can process records concurrently without race conditions.
- No distributed locking is required.

---

## 2. High-Throughput JDBC Batching

The persistence layer relies on optimized JDBC batch execution.

With:

```properties
reWriteBatchedInserts=true
```

the PostgreSQL driver automatically transforms sequential inserts into bulk operations.

### Example

Instead of:

```sql
INSERT INTO settlement_order (...) VALUES (...);
INSERT INTO settlement_order (...) VALUES (...);
INSERT INTO settlement_order (...) VALUES (...);
```

the driver generates:

```sql
INSERT INTO settlement_order (...)
VALUES
(...),
(...),
(...);
```

### Benefits

- Reduced network round-trips
- Lower database CPU utilization
- Throughput close to native bulk loading mechanisms

---

## 3. Strict Idempotency Anchor

Every generated XML file receives a unique:

```text
checksumSha256
```

Before persisting the file into Amazon S3:

1. The checksum is validated against Redis.
2. Duplicate payloads are rejected.
3. Only unique files are stored.

This guarantees safe **at-least-once delivery semantics** without generating duplicate XML artifacts.

---

# 🗂️ Domain-Driven Directory Layout

The codebase enforces unidirectional dependencies where infrastructure adapts to domain contracts.

```text
br.com.xmlemission
├── domain
│   ├── model
│   │   ├── FileBatch
│   │   ├── SettlementOrder
│   │   ├── Ispb
│   │   └── S3Key
│   │
│   └── port
│       ├── in
│       │   ├── FileBatchEmissionUseCase
│       │   └── ProcessSettlementResponseUseCase
│       │
│       └── out
│           ├── FileBatchPort
│           ├── XmlFilePort
│           └── KafkaFileBatchPort
│
└── infra
    ├── adapter
    │   ├── PostgresRepository
    │   └── S3ClientAdapter
    │
    ├── config
    │   ├── VirtualThreadsConfig
    │   ├── SecurityConfig
    │   └── KafkaConfig
    │
    └── entity
        ├── ORM Entities
        └── DTOs
```

---

# 🛠️ Local Development Environment

The complete infrastructure can be emulated locally through Docker Compose.

## Prerequisites

- Docker
- Docker Compose V2
- Java 25 (Eclipse Temurin recommended)
- Maven 3.9+

---

## 1. Environment Setup

Create a `.env` file in the project root.

```env
SPRING_PROFILES_ACTIVE=prod

SERVER_PORT=

DB_PORT=
DB_NAME=
DB_USER=
DB_PASSWORD=
DB_POOL_MAX_SIZE=

REDIS_PORT=
REDIS_PASSWORD=

KAFKA_PORT=
KAFKA_UI_PORT=

AWS_REGION=
AWS_S3_PORT=
AWS_ACCESS_KEY_ID=
AWS_SECRET_ACCESS_KEY=
AWS_S3_BUCKET_NAME=

JWT_SECRET_KEY=
```

---

## 2. Launching the Cluster

Start the complete ecosystem:

- PostgreSQL
- Redis
- Kafka (KRaft)
- Floci AWS Emulator
- Kafka UI
- STR-XML-PIPELINE

```bash
docker compose --env-file .env up --build -d
```

---

## 3. Local Service Endpoints

### Application API

```text
http://localhost:8080/v1
```

### Kafka UI

```text
http://localhost:8081
```

### Local S3 Endpoint

```text
http://localhost:4566
```

---

# 🐳 Production JVM Tuning (AWS Fargate Optimized)

The runtime container is packaged as a lightweight multi-stage image and configured specifically for high-throughput Virtual Thread workloads.

```dockerfile
ENV JAVA_OPTS="-server \
               -XX:+UseContainerSupport \
               -XX:MaxRAMPercentage=75.0 \
               -XX:MinRAMPercentage=50.0 \
               -XX:+UseG1GC \
               -XX:+UseStringDeduplication \
               -XX:+ExitOnOutOfMemoryError \
               -Dfile.encoding=UTF-8"
```

## JVM Configuration Rationale

### `-XX:+UseG1GC`

Optimized for workloads that frequently allocate and deallocate temporary XML buffers generated by JAXB.

### `-XX:MaxRAMPercentage=75.0`

Allows the JVM Heap to consume up to 75% of the container memory while preserving 25% for:

- Native allocations
- Socket buffers
- Network I/O
- TLS operations

### `-XX:+ExitOnOutOfMemoryError`

Forces immediate container termination when memory exhaustion occurs, allowing ECS/Fargate auto-healing policies to recreate healthy instances automatically.

---

# 🚀 Key Characteristics

- Event-Driven Architecture
- Hexagonal Architecture (Ports & Adapters)
- Virtual Threads (Project Loom)
- Kafka-Based Settlement Processing
- PostgreSQL Batch Optimization
- Redis Idempotency Layer
- JAXB XML Generation
- Amazon S3 Immutable Storage
- OAuth2 Resource Server Security
- AWS Fargate Ready
- High Throughput & Low Latency
- STR / BACEN Compliance-Oriented Design
