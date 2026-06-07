package tech.strxmlpipeline.domain.port.out

import tech.strxmlpipeline.domain.model.FileBatch

interface FileBatchPublisherPort {
    /**
     * Publishes a message to the emission topic using [SettlementWindow.partitionKey]
     * as the Kafka partition key, guaranteeing temporal ordering within the same window.
     */
    fun publish(batch: FileBatch)
}
