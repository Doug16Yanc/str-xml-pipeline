package tech.strxmlpipeline.domain.model

import tech.strxmlpipeline.domain.enum.OrderStatus
import java.math.BigDecimal
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID

data class SettlementOrder(
    var id: UUID = UUID.randomUUID(),
    val type: OrderType,
    val amount: BigDecimal,
    val settlementDate: LocalDate,
    val originator: Participant,
    val destination: Participant,
    val endToEndId: String,
    val status: OrderStatus = OrderStatus.PENDING,
    val batchId: UUID? = null,
    val createdAt: OffsetDateTime = OffsetDateTime.now(),
    val updatedAt: OffsetDateTime = OffsetDateTime.now(),
) {
    init {
        require(amount > BigDecimal.ZERO) {
            "Order amount must be positive"
        }
        require(originator.ispb != destination.ispb) {
            "Internal settlement between same ISPB [${originator.ispb}] must not transit through STR — resolve via internal ledger"
        }
        require(endToEndId.isNotBlank()) {
            "EndToEndId must not be blank"
        }
    }
    fun batch(): SettlementOrder = transition(OrderStatus.BATCHED)
    fun emit(): SettlementOrder = transition(OrderStatus.EMITTED)
    fun confirm(): SettlementOrder = transition(OrderStatus.CONFIRMED)
    fun reject(): SettlementOrder = transition(OrderStatus.REJECTED)
    fun rejectCutoff(): SettlementOrder = transition(OrderStatus.REJECTED_CUTOFF)

    fun associateWithBatch(batchId: UUID): SettlementOrder {
        return copy(batchId = batchId, updatedAt = OffsetDateTime.now())
    }

    private fun transition(next: OrderStatus): SettlementOrder {
        check(status.canTransitionTo(next)) {
            "Invalid order transition: $status → $next [id=$id]"
        }
        return copy(status = next, updatedAt = OffsetDateTime.now())
    }
}
