package tech.strxmlpipeline.domain.enum

enum class OrderStatus {
    PENDING,
    EMITTED,
    ACCEPTED,
    REJECTED;

    fun canTransitionTo(destination: OrderStatus): Boolean = when (this) {
        PENDING -> destination == EMITTED
        EMITTED -> destination == ACCEPTED || destination == REJECTED
        ACCEPTED -> false
        REJECTED -> false
    }
}