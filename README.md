# STR-XML-PIPELINE

Enterprise-grade, event-driven interbank settlement pipeline engineered for high-throughput transaction grouping and automated XML generation compliant with **BACEN / STR** standards.

Built on **Hexagonal Architecture (Ports & Adapters)** with a stateless, cookie-based security model designed for institutional B2B access.

---

## Architecture Overview

The system enforces strict unidirectional dependencies: infrastructure adapts to domain contracts, never the inverse. The business domain is completely isolated from framework behavior, database protocols, and external clients.

### First Mile

![First mile](https://github.com/user-attachments/assets/247d77f7-a9ca-4333-b84c-6be72dcfdd18)

### Second Mile 

![Second mile](https://github.com/user-attachments/assets/1c76e9c3-6aae-4114-a29f-cc54dc11bc4e)

---

## Stack

### Core Engine
- Kotlin 2.1 on Java 21
- Spring Boot 3.5.14
- Project Loom Virtual Threads: non-blocking I/O at scale without thread starvation

### Inbound Layer
- Kafka consumers with micro-batch processing and localized partitioning
- REST API secured as OAuth2 Resource Server with HTTP-only cookie transport

### Domain
- Pure Kotlin domain entities with structural invariants enforced in `init` blocks
- JAXB 4 for deterministic XML serialization
- Value objects for `Ispb`, `S3Key`, `SettlementWindow`, and `OperatorName`
- Sealed class state machines for `OrderStatus` and `BatchStatus`

### Security
- JWT issued and transported exclusively via **HTTP-only `Secure` cookies**, no `Authorization` header, no token exposure to JavaScript
- **Argon2id** password hashing via Bouncy Castle — memory-hard, resistant to GPU-based attacks
- **Token blacklist in Redis** — logout invalidates the token server-side before expiry
- Three institutional roles: `SETTLEMENT_OPERATOR`, `BACEN_AUDITOR`, `ADMIN`
- Operator identity bound to ISPB via `OperatorName` convention (`{ispb}_{role_abbrev}_{seq}`), ownership enforced at the service layer via `AuthUtil`
- `@PreAuthorize` at controller level + ISPB ownership check at service level

### Infrastructure

| Component | Role |
|---|---|
| PostgreSQL 16 | Primary persistence — HikariCP, batch inserts, native `COPY` |
| Redis 7 | Token blacklist + distributed idempotency filter |
| Apache Kafka | Settlement event bus — windowed partitioning |
| Amazon S3 | Immutable XML artifact storage and long-term archival |

---

## Core Design Decisions

### 1. Chronological Partitioning via `SettlementWindow`

`SettlementWindow` is a value object that encodes the settlement cycle as a canonical partition key:

```
{SYSTEM}-{CYCLE}-{HH}h{MM}    →    STR-D1-07h30
```

All orders belonging to the same window are routed to the same Kafka partition. Temporal ordering is guaranteed by Kafka — no distributed locks required, Virtual Threads process concurrently without contention.

### 2. Two-Layer Idempotency

**Redis layer** — rejects duplicate HTTP requests in milliseconds using a payload hash. Prevents double-submission from API clients before any domain logic runs.

**Domain layer** — every generated XML file carries a `checksumSha256`. Before upload, the checksum is verified against both Redis and the database. If the consumer crashes and redelivers, the pipeline detects the duplicate and short-circuits without creating a ghost record in S3.

### 3. Bulk Persistence via PostgreSQL `COPY`

High-volume order ingestion uses the PostgreSQL `COPY` protocol via `CopyManager`, bypassing the ORM entirely. Status transitions on thousands of orders use `@Modifying` bulk JPQL updates rather than per-entity `save()` calls.

```properties
reWriteBatchedInserts=true
```

Further transforms sequential inserts into multi-row `VALUES` statements at the driver level.

### 4. Raw Payload Audit Trail

Every XML return received from BACEN/STR is persisted in `raw_settlement_return` before any parsing occurs, in a dedicated `REQUIRES_NEW` transaction. The audit record survives processing failures and rollbacks. `batch_id` is populated after successful parsing — a `NULL` value signals a parse failure requiring manual inspection.

### 5. `SettlementWindow` Cutoff Enforcement

The domain validates the current instant against the window cutoff before any I/O. Orders that miss their window are marked `REJECTED_CUTOFF` — distinguishable in audit from BACEN rejections. The `Clock` bean is injected, making cutoff validation fully deterministic in tests via `Clock.fixed(...)`.

---

## Security Model

### Authentication Flow

```
POST /v1/auth/login
  → credentials validated (Argon2id)
  → JWT issued
  → set-cookie: jwt=<token>; HttpOnly; Secure; SameSite=Strict
```

```
POST /v1/auth/logout
  → token extracted from cookie
  → token added to Redis blacklist (TTL = remaining JWT expiry)
  → cookie cleared
```

### Role Matrix

| Endpoint | SETTLEMENT_OPERATOR | BACEN_AUDITOR | ADMIN |
|---|:---:|:---:|:---:|
| `POST /v1/orders` | ✓ | — | — |
| `GET /v1/orders/**` | ✓ | ✓ | ✓ |
| `GET /v1/batches/**` | ✓ | ✓ | ✓ |
| `GET /v1/returns/**` | ✓ | ✓ | ✓ |
| `GET /v1/files/**` | ✓ | ✓ | ✓ |
| `GET /v1/files/{id}/download` | — | ✓ | ✓ |
| `GET /v1/files/checksum/**` | — | ✓ | ✓ |
| `POST /v1/participants` | — | — | ✓ |
| `PUT /v1/participants/**` | — | — | ✓ |

`SETTLEMENT_OPERATOR` is additionally restricted by ISPB ownership — an operator can only submit orders where `originator.ispb` matches the ISPB embedded in their `OperatorName`.

### Operator Naming Convention

```
{ispb}_{role_abbrev}_{seq}    →    60746948_op_01
```

The ISPB prefix makes institutional ownership auditable at a glance and enables queries like `WHERE name LIKE '60746948_%'` without joins.

---

## Domain Directory Layout

```
tech.strxmlpipeline
├── domain
│   ├── model
│   │   ├── FileBatch, BatchStatus
│   │   ├── SettlementOrder, OrderStatus, OrderType
│   │   ├── SettlementReturn, ReturnResult, RejectionReason
│   │   ├── XmlFile
│   │   ├── Participant
│   │   ├── Ispb, S3Key, SettlementWindow, OperatorName
│   │   └── User, Role
│   ├── port
│   │   ├── in  — FileBatchEmissionUseCase, AssembleFileBatchUseCase, ProcessSettlementReturnUseCase
│   │   └── out — FileBatchPort, SettlementOrderPort, XmlFilePort, ParticipantPort,
│   │              SettlementReturnPort, FileBatchPublisherPort, XmlGeneratorPort,
│   │              XmlFileStoragePort
│   └── exception
│       └── domain-scoped typed exceptions
│
└── infrastructure
|   ├── persistence
|       ├── adapter      — JPA persistence adapters, S3 storage, COPY bulk adapter
|       ├── config       — KafkaConfig, AwsS3Config, AppConfig (Clock, Jackson)
|       ├── entity       — JPA entities, Flyway migrations V1–V2
|       ├── messaging    — FileBatchEmissionProducer, FileBatchEmissionConsumer,
|       │                  SettlementReturnConsumer, StrReturnXmlParser
|       ├── repository   — Spring Data repositories with bulk JPQL operations
|       ├── security     — JWT filter, AuthUtil, token blacklist
|       └── service      — JaxbXmlGeneratorService, S3StorageService,
|                          RawReturnPersistenceService
└── web
    ├── controller
    ├── dto
        ├── auth
        ├── request
        ├── response
```

---

## Local Development

### Prerequisites

- Docker and Docker Compose V2
- Java 25 (Eclipse Temurin recommended)
- Gradle 8+

### Environment Setup

```env
# .env
SPRING_PROFILES_ACTIVE=

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

JWT_SECRET=
```

### Start the Cluster

```bash
docker compose --env-file .env up --build -d
```

| Service | Endpoint |
|---|---|
| Application API | `http://localhost:8080/v1` |
| Kafka UI | `http://localhost:8081` |
| LocalStack S3 | `http://localhost:4566` |

---

## Production JVM Configuration (AWS Fargate)

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

`G1GC` is selected for workloads that frequently allocate and discard temporary JAXB XML buffers. `MaxRAMPercentage=75.0` reserves 25% of container memory for native allocations, socket buffers, and TLS operations. `ExitOnOutOfMemoryError` enables ECS/Fargate auto-healing by forcing immediate container termination on memory exhaustion rather than degraded operation.

---

## Key Characteristics

Event-Driven · Hexagonal Architecture · Virtual Threads (Project Loom) · Stateless JWT via HTTP-only Cookies · Argon2id · Redis Token Blacklist · Kafka Windowed Partitioning · PostgreSQL COPY · JAXB Deterministic XML · S3 Immutable Artifacts · STR/BACEN Compliance-Oriented · AWS Fargate Ready
