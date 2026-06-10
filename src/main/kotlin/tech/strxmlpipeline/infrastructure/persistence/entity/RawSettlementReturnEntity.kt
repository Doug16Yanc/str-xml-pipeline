package tech.strxmlpipeline.infrastructure.persistence.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.OffsetDateTime
import java.util.UUID

/**
 * Stores the raw XML payload received from BACEN/STR before any parsing.
 * Committed in REQUIRES_NEW so the audit trail survives even if processing
 * rolls back. [batchId] is null until parsing succeeds and links the record.
 */
@Entity
@Table(name = "raw_settlement_return")
class RawSettlementReturnEntity(

    @Id
    @Column(name = "id", updatable = false)
    val id: UUID,

    @Column(name = "raw_payload", nullable = false, columnDefinition = "TEXT")
    val rawPayload: String,

    @Column(name = "batch_id")
    var batchId: UUID? = null,

    @Column(name = "kafka_partition", nullable = false, updatable = false)
    val partition: Int,

    @Column(name = "kafka_offset", nullable = false, updatable = false)
    val offset: Long,

    @Column(name = "received_at", nullable = false, updatable = false)
    val receivedAt: OffsetDateTime,
)
