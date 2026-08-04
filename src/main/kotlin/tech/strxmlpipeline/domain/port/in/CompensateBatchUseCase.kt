package tech.strxmlpipeline.domain.port.`in`

import java.util.UUID

interface CompensateBatchUseCase {
    /**
     * Reverts a TRANSMISSION_REJECTED batch's orders back to PENDING and
     * closes the batch as COMPENSATED. Idempotent — safe to call repeatedly
     * for the same batchId (Kafka at-least-once redelivery).
     */
    fun compensate(batchId: UUID)
}