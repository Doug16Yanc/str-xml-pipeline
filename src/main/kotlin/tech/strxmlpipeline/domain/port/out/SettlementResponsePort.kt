package tech.strxmlpipeline.domain.port.out

import tech.strxmlpipeline.domain.model.SettlementResponse
import java.util.UUID

interface SettlementResponsePort {
    fun save(response: SettlementResponse): SettlementResponse
    fun findByBatchId(batchId: UUID): SettlementResponse?
}