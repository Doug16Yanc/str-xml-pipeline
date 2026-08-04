package tech.strxmlpipeline.domain.enum

enum class OrderStatus {
    PENDING,
    BATCHED,
    EMITTED,
    ACCEPTED,
    REJECTED,
    REJECTED_CUTOFF;

    fun canTransitionTo(next: OrderStatus): Boolean = when (this) {
        PENDING -> next == BATCHED || next == REJECTED_CUTOFF
        BATCHED -> next == EMITTED || next == REJECTED_CUTOFF
        EMITTED -> next == ACCEPTED || next == REJECTED
        ACCEPTED -> false
        // Compensation saga: a rejected order is reverted back to PENDING
        // (batchId cleared) so it re-enters assembly for the next SettlementWindow cycle.
        REJECTED -> next == PENDING
        REJECTED_CUTOFF -> false
    }
}