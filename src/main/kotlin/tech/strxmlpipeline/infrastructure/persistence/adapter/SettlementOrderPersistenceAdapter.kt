package tech.strxmlpipeline.infrastructure.persistence.adapter

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import tech.strxmlpipeline.domain.enum.OrderStatus
import tech.strxmlpipeline.domain.model.SettlementOrder
import tech.strxmlpipeline.domain.model.SettlementWindow
import tech.strxmlpipeline.domain.port.out.SettlementOrderPort
import tech.strxmlpipeline.infrastructure.persistence.mapper.toSettlementOrderDomain
import tech.strxmlpipeline.infrastructure.persistence.mapper.toSettlementOrderEntity
import tech.strxmlpipeline.infrastructure.persistence.repository.SettlementOrderJpaRepository

import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID

@Component
@Transactional
class SettlementOrderPersistenceAdapter(
    private val orderJpaRepository: SettlementOrderJpaRepository,
    private val copyBulkAdapter: SettlementOrderCopyBulkAdapter
) : SettlementOrderPort {

    override fun save(order: SettlementOrder, window: SettlementWindow): SettlementOrder {
        val entity = order.toSettlementOrderEntity(window = window.partitioningKey)
        return orderJpaRepository.save(entity).toSettlementOrderDomain()
    }

    override fun findById(id: UUID): SettlementOrder? {
        return orderJpaRepository.findById(id)
            .map { it.toSettlementOrderDomain() }
            .orElse(null)
    }

    override fun findByStatus(status: OrderStatus): List<SettlementOrder> {
        return orderJpaRepository.findByStatus(status)
            .map { it.toSettlementOrderDomain() }
    }

    override fun findPendingForWindow(window: SettlementWindow, date: LocalDate): List<SettlementOrder> {
        return orderJpaRepository.findPendingOrdersForWindow(
            window = window.partitioningKey,
            date = date
        ).map { it.toSettlementOrderDomain() }
    }

    override fun updateStatus(order: SettlementOrder): SettlementOrder {
        val existing = orderJpaRepository.findById(order.id)
            .orElseThrow { NoSuchElementException("Order not found: ${order.id}") }

        existing.status = order.status
        existing.updatedAt = OffsetDateTime.now()

        return orderJpaRepository.save(existing).toSettlementOrderDomain()
    }

    override fun findByBatchId(batchId: UUID): List<SettlementOrder> {
        return orderJpaRepository.findByBatchId(batchId).map { it.toSettlementOrderDomain() }
    }

    override fun updateStatusBatch(orders: List<SettlementOrder>): List<SettlementOrder> {
        if (orders.isEmpty()) return emptyList()

        val ids = orders.map { it.id }
        val targetStatus = orders.first().status

        val batchId = orders.first().batchId
            ?: throw IllegalStateException("Cannot update batch status in database because batchId is missing in domain model")

        orderJpaRepository.updateStatusAndBatchIdForIds(
            ids = ids,
            status = targetStatus,
            batchId = batchId,
            now = OffsetDateTime.now()
        )

        return orders
    }

    override fun bulkInsert(orders: List<SettlementOrder>, batchId: UUID) {
        copyBulkAdapter.bulkInsertUsingCopy(orders, batchId)
    }
}