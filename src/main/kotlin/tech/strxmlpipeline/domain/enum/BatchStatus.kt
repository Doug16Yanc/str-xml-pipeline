package tech.strxmlpipeline.domain.enum

enum class BatchStatus {
    PENDING,
    EMITTED,
    CONFIRMED,
    REJECTED;

    fun canTransitionTo(destination: BatchStatus): Boolean = when (this) {
        PENDING -> destination == EMITTED
        EMITTED -> destination == CONFIRMED || destination == REJECTED
        CONFIRMED -> false
        REJECTED -> false
    }
}