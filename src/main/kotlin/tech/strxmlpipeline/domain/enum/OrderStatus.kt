package tech.strxmlpipeline.domain.enum

enum class OrderStatus {
    PENDING,
    BATCHED,
    EMITTED,
    CONFIRMED,
    REJECTED,
    REJECTED_CUTOFF;

    fun canTransitionTo(next: OrderStatus): Boolean = when (this) {
        PENDING -> next == BATCHED || next == REJECTED_CUTOFF
        BATCHED -> next == EMITTED || next == REJECTED_CUTOFF
        EMITTED -> next == CONFIRMED || next == REJECTED
        CONFIRMED -> false
        REJECTED -> false
        REJECTED_CUTOFF -> false
    }
}