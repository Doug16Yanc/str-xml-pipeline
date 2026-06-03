package tech.strxmlpipeline.domain.port.out

import tech.strxmlpipeline.domain.model.FileBatch

interface KafkaFileBatchPort {
    /** Publishes the message to the outbound emission topic using the SettlementWindow as partition key. */
    fun publish(batch: FileBatch)
}