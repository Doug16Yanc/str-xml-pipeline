package tech.strxmlpipeline.infrastructure.messaging.producer

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.SendResult
import org.springframework.stereotype.Component
import tech.strxmlpipeline.domain.enum.RejectionReason
import tech.strxmlpipeline.domain.model.FileBatch
import tech.strxmlpipeline.domain.port.out.BatchCompensationPublisherPort
import tech.strxmlpipeline.infrastructure.messaging.message.BatchCompensationMessage

@Component
class BatchCompensationProducer(
    private val kafkaTemplate: KafkaTemplate<String, String>,
    private val objectMapper: ObjectMapper,
    @Value("\${kafka.topics.batch-compensation.name:str.batch.compensation}") private val topic: String,
) : BatchCompensationPublisherPort {

    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * Partition key = window partitioningKey, same convention as
     * [tech.strxmlpipeline.infrastructure.messaging.producer.FileBatchEmissionProducer] —
     * keeps compensation events for the same window ordered on one partition.
     */
    override fun publishCompensation(batch: FileBatch, rejectionReason: RejectionReason?) {
        val payload = objectMapper.writeValueAsString(BatchCompensationMessage.from(batch, rejectionReason))
        val key = batch.window.partitioningKey

        kafkaTemplate
            .send(topic, key, payload)
            .whenComplete { result: SendResult<String, String>?, ex: Throwable? ->
                if (ex != null) {
                    log.error(
                        "Failed to publish compensation trigger for batch [{}] to topic [{}]: {}",
                        batch.id, topic, ex.message, ex,
                    )
                } else {
                    log.info(
                        "Compensation trigger published — batch [{}] topic [{}] partition [{}] offset [{}]",
                        batch.id,
                        topic,
                        result?.recordMetadata?.partition(),
                        result?.recordMetadata?.offset(),
                    )
                }
            }
    }
}