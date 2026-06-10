package tech.strxmlpipeline.infrastructure.messaging.model

import java.time.OffsetDateTime
import java.util.UUID

data class RawSettlementReturn(
    val id: UUID = UUID.randomUUID(),
    val rawPayload: String,
    val batchId: UUID? = null,
    val partition: Int,
    val offset: Long,
    val receivedAt: OffsetDateTime = OffsetDateTime.now()
)