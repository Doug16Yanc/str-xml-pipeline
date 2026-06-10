package tech.strxmlpipeline.infrastructure.messaging.producer


import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.SendResult
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Component
import tech.strxmlpipeline.domain.model.FileBatch
import tech.strxmlpipeline.domain.port.out.FileBatchPublisherPort
import tech.strxmlpipeline.infrastructure.messaging.message.BatchEmissionMessage

@Component
class FileBatchEmissionProducer(
    private val kafkaTemplate: KafkaTemplate<String, String>,
    private val objectMapper: ObjectMapper,
    @Value("\${kafka.topics.batch-emission.name:str.batch.emission}") private val topic: String,
) : FileBatchPublisherPort {

    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * Publishes a [FileBatch] to the emission topic.
     * Partition key = [SettlementWindow.partitionKey] (e.g. "STR-D1-07h30") —
     * guarantees all messages for the same window land on the same partition,
     * preserving temporal ordering without distributed locks.
     */
    override fun publish(batch: FileBatch) {
        val payload  = objectMapper.writeValueAsString(BatchEmissionMessage.from(batch))
        val key      = batch.window.partitioningKey

        kafkaTemplate
            .send(topic, key, payload)
            .whenComplete { result: SendResult<String, String>?, ex: Throwable? ->
                if (ex != null) {
                    log.error(
                        "Failed to publish FileBatch [{}] to topic [{}]: {}",
                        batch.id, topic, ex.message, ex,
                    )
                } else {
                    log.info(
                        "FileBatch [{}] published — topic [{}] partition [{}] offset [{}]",
                        batch.id,
                        topic,
                        result?.recordMetadata?.partition(),
                        result?.recordMetadata?.offset(),
                    )
                }
            }
    }
}
