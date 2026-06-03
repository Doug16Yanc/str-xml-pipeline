package tech.strxmlpipeline.domain.port.out

import tech.strxmlpipeline.domain.model.SettlementOrder
import tech.strxmlpipeline.domain.model.SettlementWindow
import java.time.LocalDate
import java.util.UUID

interface SettlementOrderPort {
    fun save(order: SettlementOrder): SettlementOrder
    fun findPendingForWindow(window: SettlementWindow, date: LocalDate): List<SettlementOrder>
    fun updateStatus(order: SettlementOrder): SettlementOrder
    fun findByBatchId(batchId: UUID): List<SettlementOrder>
}