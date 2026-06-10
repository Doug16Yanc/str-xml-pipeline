package tech.strxmlpipeline.domain.port.out

import tech.strxmlpipeline.domain.model.SettlementReturn
import java.util.UUID

interface SettlementReturnPort {
    fun save(response: SettlementReturn): SettlementReturn
    fun findById(id: UUID): SettlementReturn?
    fun findByBatchId(batchId: UUID): SettlementReturn?
}