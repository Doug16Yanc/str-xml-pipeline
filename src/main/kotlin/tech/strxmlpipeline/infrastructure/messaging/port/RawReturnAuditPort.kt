package tech.strxmlpipeline.infrastructure.messaging.port

import tech.strxmlpipeline.infrastructure.messaging.model.RawSettlementReturn
import java.util.UUID

interface RawReturnAuditPort {
    fun persist(rawPayload: String, partition: Int, offset: Long): RawSettlementReturn
    fun linkToBatch(id: UUID, batchId: UUID)
}