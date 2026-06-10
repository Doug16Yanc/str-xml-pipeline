package tech.strxmlpipeline.infrastructure.messaging.consumer

import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.DltHandler
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.retrytopic.TopicSuffixingStrategy
import org.springframework.kafka.support.Acknowledgment
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.handler.annotation.Header
import org.springframework.kafka.retrytopic.RetryTopicNamesProviderFactory
import org.springframework.kafka.annotation.RetryableTopic
import org.springframework.retry.annotation.Backoff
import org.springframework.stereotype.Component
import tech.strxmlpipeline.domain.port.`in`.FileBatchEmissionUseCase
import tech.strxmlpipeline.infrastructure.messaging.message.BatchEmissionMessage
import java.util.UUID

@Component
class FileBatchEmissionConsumer(
    private val emissionUseCase: FileBatchEmissionUseCase,
    private val objectMapper: ObjectMapper,
) {

    private val log = LoggerFactory.getLogger(javaClass)

    @RetryableTopic(
        attempts = "4",
        backoff = Backoff(
            delay = 1000L,
            multiplier = 2.0,
            maxDelay = 10000L,
        ),
        topicSuffixingStrategy = TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE,
        exclude = [IllegalArgumentException::class, IllegalStateException::class],
        dltTopicSuffix = ".DLT",
    )
    @KafkaListener(
        topics           = ["\${kafka.topics.batch-emission.name:str.batch.emission}"],
        groupId          = "\${kafka.consumer.group-id:str-xml-pipeline}",
        containerFactory = "kafkaListenerContainerFactory",
    )
    fun consume(
        record: ConsumerRecord<String, String>,
        ack: Acknowledgment,
    ) {
        val message = objectMapper.readValue(record.value(), BatchEmissionMessage::class.java)
        val batchId = UUID.fromString(message.batchId)

        log.info(
            "Received emission event — batchId [{}] window [{}] partition [{}] offset [{}]",
            batchId, message.windowKey, record.partition(), record.offset(),
        )

        emissionUseCase.emit(batchId)

        ack.acknowledge()

        log.info("Emission completed — batchId [{}]", batchId)
    }

    @DltHandler
    fun onDlt(
        record: ConsumerRecord<String, String>,
        @Header(KafkaHeaders.EXCEPTION_MESSAGE) exceptionMessage: String?,
    ) {
        log.error(
            "FileBatch reached DLT — payload [{}] — reason [{}]",
            record.value(),
            exceptionMessage,
        )
    }
}