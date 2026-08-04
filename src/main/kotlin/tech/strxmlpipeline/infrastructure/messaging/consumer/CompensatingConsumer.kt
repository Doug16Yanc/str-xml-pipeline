package tech.strxmlpipeline.infrastructure.messaging.consumer

import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.DltHandler
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.annotation.RetryableTopic
import org.springframework.kafka.retrytopic.TopicSuffixingStrategy
import org.springframework.kafka.support.Acknowledgment
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.handler.annotation.Header
import org.springframework.retry.annotation.Backoff
import org.springframework.stereotype.Component
import tech.strxmlpipeline.domain.port.`in`.CompensateBatchUseCase
import tech.strxmlpipeline.infrastructure.messaging.message.BatchCompensationMessage
import java.util.UUID

/**
 * Third Mile — choreographed compensation. Reacts to the compensation
 * trigger published by ProcessSettlementReturnServiceImpl once a batch enters
 * TRANSMISSION_REJECTED. No orchestrator: this consumer only knows how to
 * compensate the one batchId in the message it received.
 */
@Component
class CompensatingConsumer(
    private val compensateUseCase: CompensateBatchUseCase,
    private val objectMapper: ObjectMapper,
) {

    private val log = LoggerFactory.getLogger(javaClass)

    @RetryableTopic(
        attempts = "4",
        backoff = Backoff(
            delay = 1_000L,
            multiplier = 2.0,
            maxDelay = 10_000L,
        ),
        topicSuffixingStrategy = TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE,
        exclude = [IllegalArgumentException::class],
        dltTopicSuffix = ".DLT",
    )
    @KafkaListener(
        topics           = ["\${kafka.topics.batch-compensation.name:str.batch.compensation}"],
        groupId          = "\${kafka.consumer.group-id:str-xml-pipeline}",
        containerFactory = "kafkaListenerContainerFactory",
    )
    fun consume(
        record: ConsumerRecord<String, String>,
        ack: Acknowledgment,
    ) {
        val message = objectMapper.readValue(record.value(), BatchCompensationMessage::class.java)
        val batchId = UUID.fromString(message.batchId)

        log.info(
            "Received compensation trigger — batchId [{}] reason [{}] window [{}] partition [{}] offset [{}]",
            batchId, message.rejectionReason, message.windowKey, record.partition(), record.offset(),
        )

        // compensateUseCase.compensate() is idempotent by design (status + optimistic
        // lock guards) — safe to call again on redelivery without extra checks here.
        compensateUseCase.compensate(batchId)

        ack.acknowledge()

        log.info("Compensation handling finished — batchId [{}]", batchId)
    }

    @DltHandler
    fun onDlt(
        record: ConsumerRecord<String, String>,
        @Header(KafkaHeaders.EXCEPTION_MESSAGE) exceptionMessage: String?,
    ) {
        log.error(
            "Batch compensation reached DLT — payload [{}] — reason [{}]",
            record.value(),
            exceptionMessage,
        )
    }
}