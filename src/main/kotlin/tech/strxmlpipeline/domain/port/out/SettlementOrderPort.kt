package tech.strxmlpipeline.domain.port.out

import tech.strxmlpipeline.domain.enum.OrderStatus
import tech.strxmlpipeline.domain.model.OrderType
import tech.strxmlpipeline.domain.model.SettlementOrder
import tech.strxmlpipeline.domain.model.SettlementWindow
import tech.strxmlpipeline.domain.valueobject.Ispb
import java.time.LocalDate
import java.util.UUID

interface SettlementOrderPort {
    fun save(order: SettlementOrder, window: SettlementWindow): SettlementOrder
    fun findById(id: UUID): SettlementOrder?
    fun findByStatus(status: OrderStatus): List<SettlementOrder>
    fun findPendingForWindow(window: SettlementWindow, date: LocalDate, ispb: Ispb): List<SettlementOrder>
    fun updateStatus(order: SettlementOrder): SettlementOrder
    fun updateStatusBatch(orders: List<SettlementOrder>): List<SettlementOrder>
    fun findByBatchId(batchId: UUID): List<SettlementOrder>
    fun bulkInsert(orders: List<SettlementOrder>, batchId: UUID)
}