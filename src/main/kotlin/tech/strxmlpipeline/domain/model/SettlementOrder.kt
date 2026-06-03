package tech.strxmlpipeline.domain.model

import tech.strxmlpipeline.domain.enum.OrderStatus
import java.math.BigDecimal
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID

data class SettlementOrder(
    val id: UUID = UUID.randomUUID(),
    val type: OrderType,
    val amount: BigDecimal,
    val settlementDate: LocalDate,
    val originator: Participant,
    val destination: Participant,
    val endToEndId: String,
    val status: OrderStatus = OrderStatus.PENDING,
    val createdAt: OffsetDateTime = OffsetDateTime.now(),
    val updatedAt: OffsetDateTime = OffsetDateTime.now(),
) {
    init {
        require(amount > BigDecimal.ZERO) { "Order amount must be positive" }
        require(originator.ispb != destination.ispb) { "Originator and destination cannot be the same participant" }
        require(endToEndId.isNotBlank()) { "EndToEndId cannot be empty" }
    }

    fun accept(): SettlementOrder = transition(OrderStatus.ACCEPTED)
    fun reject(): SettlementOrder = transition(OrderStatus.REJECTED)
    fun emit(): SettlementOrder = transition(OrderStatus.EMITTED)

    private fun transition(newStatus: OrderStatus): SettlementOrder {
        check(status.canTransitionTo(newStatus)) {
            "Invalid order transition: $status → $newStatus for order $id"
        }
        return copy(status = newStatus, updatedAt = OffsetDateTime.now())
    }
}
