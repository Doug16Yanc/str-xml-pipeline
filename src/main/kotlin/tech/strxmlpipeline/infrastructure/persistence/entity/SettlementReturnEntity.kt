package tech.strxmlpipeline.infrastructure.persistence.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import tech.strxmlpipeline.domain.enum.RejectionReason
import tech.strxmlpipeline.domain.enum.ReturnResult
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(
    name = "settlement_return",
    indexes = [
        Index(name = "idx_return_batch_id", columnList = "batch_id"),
        Index(name = "idx_return_file_id",  columnList = "file_id"),
    ]
)
class SettlementReturnEntity(

    @Id
    @Column(name = "id", updatable = false)
    val id: UUID = UUID.randomUUID(),

    @Column(name = "batch_id", nullable = false, updatable = false)
    val batchId: UUID,

    @Column(name = "file_id", nullable = false, updatable = false)
    val fileId: UUID,

    @Column(name = "result", length = 10, nullable = false, updatable = false)
    @Enumerated(EnumType.STRING)
    val result: ReturnResult,

    @Column(name = "message_code", length = 20, nullable = false, updatable = false)
    val messageCode: String,

    @Column(name = "description", nullable = false, updatable = false)
    val description: String,

    @Column(name = "rejection_reason", length = 30, updatable = false)
    @Enumerated(EnumType.STRING)
    val rejectionReason: RejectionReason? = null,

    @Column(name = "received_at", nullable = false, updatable = false)
    val receivedAt: OffsetDateTime,
)