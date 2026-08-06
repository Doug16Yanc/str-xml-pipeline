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
    val totalOrdersOverride: Int? = null,
    val status: BatchStatus = BatchStatus.PENDING,
    val participant: Participant,
    val createdAt: OffsetDateTime = OffsetDateTime.now(),
    val updatedAt: OffsetDateTime = OffsetDateTime.now(),
    /**
     * Optimistic-lock version. Doubles as the saga's coordination/audit trail
     * (Third Mile) — no orchestrator needed, each state transition bumps
     * this and `updatedAt`, and choreography consumers react to `status` alone.
     */
    val version: Long = 0,
) {
    companion object {
        /**
         * Primeira Milha — assembles a brand-new batch from orders that were
         * just moved to BATCHED. This is the ONLY point in the batch's
         * lifecycle where "all orders are BATCHED" is actually true, so the
         * invariant is enforced here instead of in `init`, which also runs
         * every time this class is reconstituted from persistence with its
         * orders eagerly loaded (see FileBatchPersistenceAdapter.findByIdWithOrders)
         * — at that point the orders have long since moved past BATCHED
         * (EMITTED, REJECTED, etc.), and re-checking a creation-time rule
         * against reconstituted state breaks the Terceira Milha flow.
         */
        fun assemble(
            window: SettlementWindow,
            referenceDate: LocalDate,
            orders: List<SettlementOrder>,
            participant: Participant,
        ): FileBatch {
            require(orders.isNotEmpty()) { "Cannot assemble a FileBatch with no orders" }
            require(orders.all { it.status == OrderStatus.BATCHED }) {
                "All orders must be in BATCHED status when assembled into a FileBatch"
            }
            return FileBatch(
                window        = window,
                referenceDate = referenceDate,
                orders        = orders,
                status        = BatchStatus.PENDING,
                participant   = participant,
            )
        }

        fun fromPersistence(
            id: UUID,
            window: SettlementWindow,
            referenceDate: LocalDate,
            totalOrders: Int,
            status: BatchStatus,
            participant: Participant,
            createdAt: OffsetDateTime,
            updatedAt: OffsetDateTime,
            version: Long = 0,
        ): FileBatch = FileBatch(
            id            = id,
            window        = window,
            referenceDate = referenceDate,
            orders        = emptyList(),
            totalOrdersOverride = totalOrders,
            status        = status,
            participant   = participant,
            createdAt     = createdAt,
            updatedAt     = updatedAt,
            version       = version,
        )
    }

    val totalOrders: Int get() = totalOrdersOverride ?: orders.size
    val totalAmount: BigDecimal get() = orders.sumOf { it.amount }

    fun emit(): FileBatch = transition(BatchStatus.EMITTED)

    /** XML successfully handed off to the BACEN/STR API — Camada de Transmissão. */
    fun transmit(): FileBatch = transition(BatchStatus.TRANSMITTED)

    /** BACEN/STR accepted the batch. Terminal. */
    fun accept(): FileBatch = transition(BatchStatus.ACCEPTED)

    /** BACEN/STR rejected the batch — this is what kicks off the compensation saga. */
    fun rejectTransmission(): FileBatch = transition(BatchStatus.TRANSMISSION_REJECTED)

    /** Compensating consumer has started reverting this batch's orders. */
    fun startCompensation(): FileBatch = transition(BatchStatus.COMPENSATING)

    /** Compensation finished — orders released back to PENDING for the next window. Terminal. */
    fun completeCompensation(): FileBatch = transition(BatchStatus.COMPENSATED)

    private fun transition(newStatus: BatchStatus): FileBatch {
        check(status.canTransitionTo(newStatus)) {
            "Invalid batch transition: $status → $newStatus for batch $id"
        }
        return copy(status = newStatus, updatedAt = OffsetDateTime.now())
    }
}