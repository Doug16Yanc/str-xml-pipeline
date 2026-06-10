package tech.strxmlpipeline.domain.model

import tech.strxmlpipeline.domain.enum.BatchStatus
import tech.strxmlpipeline.domain.enum.OrderStatus
import java.math.BigDecimal
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID

data class FileBatch(
    val id: UUID = UUID.randomUUID(),
    val window: SettlementWindow,
    val referenceDate: LocalDate,
    val orders: List<SettlementOrder>,
    val status: BatchStatus = BatchStatus.PENDING,
    val createdAt: OffsetDateTime = OffsetDateTime.now(),
    val updatedAt: OffsetDateTime = OffsetDateTime.now(),
) {
    companion object {
        fun fromPersistence(
            id: UUID,
            window: SettlementWindow,
            referenceDate: LocalDate,
            status: BatchStatus,
            createdAt: OffsetDateTime,
            updatedAt: OffsetDateTime,
        ): FileBatch = FileBatch(
            id            = id,
            window        = window,
            referenceDate = referenceDate,
            orders        = emptyList(),
            status        = status,
            createdAt     = createdAt,
            updatedAt     = updatedAt,
        )
    }

    init {
        if (orders.isNotEmpty()) {
            require(orders.all { it.status == OrderStatus.BATCHED }) {
                "All orders must be in BATCHED status when assembled into a FileBatch"
            }
        }
        require(orders.all { it.status == OrderStatus.BATCHED }) {
            "All orders must be in BATCHED status when assembled into a FileBatch"
        }
    }

    val totalOrders: Int get() = orders.size

    val totalAmount: BigDecimal get() = orders.sumOf { it.amount }

    fun emit(): FileBatch = transition(BatchStatus.EMITTED)
    fun confirm(): FileBatch = transition(BatchStatus.CONFIRMED)
    fun reject(): FileBatch = transition(BatchStatus.REJECTED)

    private fun transition(newStatus: BatchStatus): FileBatch {
        check(status.canTransitionTo(newStatus)) {
            "Invalid batch transition: $status → $newStatus for batch $id"
        }
        return copy(status = newStatus, updatedAt = OffsetDateTime.now())
    }
}