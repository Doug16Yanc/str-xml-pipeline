package tech.strxmlpipeline.domain.port.`in`

import java.util.UUID

interface FileBatchEmissionUseCase {
    /**
     * Entry point triggered by the Kafka consumer.
     * Receives the [batchId] from the message and orchestrates the XML generates and upload.
     */
    fun emit(batchId: UUID)
}