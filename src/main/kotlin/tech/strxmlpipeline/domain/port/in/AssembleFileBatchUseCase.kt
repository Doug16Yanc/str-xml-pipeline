package tech.strxmlpipeline.domain.port.`in`

interface AssembleFileBatchUseCase {
    /**
     * Entry point triggered by the scheduler.
     * Fetches PENDING orders for [windowKey], validates cutoff, advances them to BATCHED,
     * assembles the [FileBatch], persists it, and publishes to Kafka.
     */
    fun assemble(windowKey: String)
}
