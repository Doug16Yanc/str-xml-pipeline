package tech.strxmlpipeline.infrastructure.persistence.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import tech.strxmlpipeline.domain.enum.ResponseResult
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(
    name = "settlement_response",
    indexes = [
        Index(name = "idx_response_batch_id", columnList = "batch_id")
    ]
)
class SettlementResponseEntity(
    @Id
    @Column(name = "id", updatable = false)
    val id: UUID,

    @Column(name = "batch_id", nullable = false, updatable = false)
    val batchId: UUID,

    @Column(name = "response_code", length = 10, nullable = false, updatable = false)
    val responseCode: String,

    @Column(name = "description", nullable = false, updatable = false)
    val description: String,

    @Column(name = "status", length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
    val status: ResponseResult,

    @Column(name = "received_at", nullable = false, updatable = false)
    val receivedAt: OffsetDateTime = OffsetDateTime.now()
)