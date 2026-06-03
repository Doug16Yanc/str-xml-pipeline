package tech.strxmlpipeline.infrastructure.persistence.adapter

import jakarta.transaction.Transactional
import org.springframework.stereotype.Component
import tech.strxmlpipeline.domain.enum.OrderStatus
import tech.strxmlpipeline.domain.model.SettlementOrder
import tech.strxmlpipeline.domain.model.SettlementWindow
import tech.strxmlpipeline.domain.port.out.SettlementOrderPort
import tech.strxmlpipeline.infrastructure.persistence.mapper.toDomain
import tech.strxmlpipeline.infrastructure.persistence.mapper.toEntity
import tech.strxmlpipeline.infrastructure.persistence.repository.SettlementOrderJpaRepository
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID

@Component
@Transactional
class SettlementOrderPersistenceAdapter(
    private val orderJpaRepository: SettlementOrderJpaRepository
) : SettlementOrderPort {

    override fun save(order: SettlementOrder): SettlementOrder {
        return orderJpaRepository.save(order.toEntity()).toDomain()
    }

    override fun findPendingForWindow(window: SettlementWindow, date: LocalDate): List<SettlementOrder> {
        return orderJpaRepository.findPendingOrdersForWindow(date).map { it.toDomain() }
    }

    override fun updateStatus(order: SettlementOrder): SettlementOrder {
        val existing = orderJpaRepository.findById(order.id)
            .orElseThrow { NoSuchElementException("Order not found: ${order.id}") }

        existing.status = OrderStatus.valueOf(order.status.name)

        return orderJpaRepository.save(existing).toDomain()
    }

    override fun findByBatchId(batchId: UUID): List<SettlementOrder> {
        return orderJpaRepository.findByBatchId(batchId).map { it.toDomain() }
    }
}